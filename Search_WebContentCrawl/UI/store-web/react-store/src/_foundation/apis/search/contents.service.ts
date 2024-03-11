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

//Foundation libraries
import { getSite } from "../../hooks/useSite";
import Axios, { AxiosPromise, AxiosRequestConfig } from "axios";

const contentsService = { 
  webContentsBySearchTerm(body:any):AxiosPromise<any> { 
    const storeID = getSite()?.storeID;
    const { searchTerm ,searchType} = body;
    const requestOptions: AxiosRequestConfig = Object.assign({
      url: "/search/resources/store/" + storeID + "/sitecontent/webContentsBySearchTerm/" + searchTerm + "?searchType=" +searchType,
      method: "get",
      data: body,
      headers : {
        storeID:storeID
      }
    });  
    return Axios(requestOptions);
  }
};

export default contentsService;
