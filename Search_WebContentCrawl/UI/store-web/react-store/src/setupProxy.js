/**
*==================================================
Copyright [2022] [HCL America, Inc.]
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*==================================================
**/
const { createProxyMiddleware } = require("http-proxy-middleware");

// update this point to your Search and Transaction server.
// const SEARCH_HOST = MOCK_HOST;
// CHANGE SEARCH_HOST to point to the Docker Search Query Service for ElasticSearch
// for example: const SEARCH_HOST = "https://10.190.66.159:30901";

const SEARCH_HOST = "https://45.120.107.63:30901"; //BDA ip of Chaitanya M
const TRANSACTION_HOST = "https://45.120.107.63";    // BDA ip of Chaitanya M
const MOCK_HOST = "http://localhost:9002";
const CMC_HOST = "https://10.115.171.46:7443";
const DX_HOST = "https://10.115.171.46";

const useMock = () => {
  return process.env.REACT_APP_MOCK === "true";
};

const appHost = `${process.env.HTTPS === "true" ? "https" : "http"}://localhost:${
  process.env.PORT ? process.env.PORT : 3000
}`;

let searchTerm = "";
const minPrice = "100";
const maxPrice = "500";

const AppName = process.env.REACT_APP_STORENAME;

if (AppName === "Emerald") {
  searchTerm = "bed";
} else {
  searchTerm = "bolt";
}

const mockPathRewrite = (path, req) => {
  let newPath = path.replace(/searchTerm=[a-zA-Z0-9]+/, "searchTerm=" + searchTerm);
  if (newPath.indexOf("minPrice=0&") === -1 || newPath.indexOf("maxPrice=500&") === -1) {
    newPath = newPath.replace(/minPrice=\d+/, "minPrice=" + minPrice);
    newPath = newPath.replace(/maxPrice=\d+/, "maxPrice=" + maxPrice);
  }
  return newPath;
};

const storeAssetPathRewrite = {
  "^/EmeraldSAS": `/${AppName}/EmeraldSAS`,
  "^/EmeraldCAS": `/${AppName}/EmeraldCAS`,
  "^/SapphireSAS": `/${AppName}/SapphireSAS`,
  "^/SapphireCAS": `/${AppName}/SapphireCAS`,
  "^/hclstore": `/${AppName}/`,
  "^/wcsstore": `/${AppName}/`,
  "^/ExtendedSitesCatalogAssetStore": `/${AppName}/ExtendedSitesCatalogAssetStore`,
};

const options = {
  changeOrigin: true,
  secure: false,
};

const searchProxyContext = useMock()
  ? {
      target: MOCK_HOST,
      ...options,
      pathRewrite: mockPathRewrite,
    }
  : {
      target: SEARCH_HOST,
      ...options,
    };

const hclStoreAssetProxyContext = {
  target: appHost,
  ...options,
  pathRewrite: storeAssetPathRewrite,
};

const lobToolsProxyContext = {
  target: CMC_HOST,
  ...options,
};

const transactionProxyContext = useMock()
  ? {
      target: MOCK_HOST,
      ...options,
      pathRewrite: mockPathRewrite,
    }
  : {
      target: TRANSACTION_HOST,
      ...options,
    };
const dxProxyContext = {
  target: DX_HOST,
  ...options,
};

module.exports = function (app) {
  app.use(["/search/resources/api/", "/search/resources/store/"], createProxyMiddleware(searchProxyContext));
  app.use("/wcs/resources/", createProxyMiddleware(transactionProxyContext));
  app.use(
    [
      "/hclstore",
      "/wcsstore",
      "/EmeraldSAS",
      "/EmeraldCAS",
      "/SapphireSAS",
      "/EmeraldPlusSAS",
      "/SapphirePlusSAS",
      "/ExtendedSitesCatalogAssetStore",
    ],
    createProxyMiddleware(hclStoreAssetProxyContext)
  );
  app.use(["/lobtools/", "/tooling/", "/sockjs-node/", "/rest/"], createProxyMiddleware(lobToolsProxyContext));
  app.use("/dx/", createProxyMiddleware(dxProxyContext));
};
