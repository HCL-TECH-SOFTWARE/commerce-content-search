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
//Standard libraries
import React from "react";
import { useLocation } from "react-router-dom";
//Custom libraries
import { ProductListingPageLayout } from "@hcl-commerce-store-sdk/react-component";
import { SectionContent } from "../../../_foundation/constants/section-content-type";
import FacetNavigationWidget from "../../commerce-widgets/facet-navigation-widget/facet-navigation-widget";
import CatalogEntryListWidget from "../../commerce-widgets/catalog-entry-list-widget/catalog-entry-list-widget";
import { SEARCHTERM } from "../../../constants/common";
//UI
import { StyledContainer } from "@hcl-commerce-store-sdk/react-component";

const SearchResults: React.FC = (props: any) => {
  const { widget, page } = props;
  const location: any = useLocation();

  let searchTerm = "";
  let searchType = "";
  const searchParam = location.search;
  if (searchParam) {
    const params = new URLSearchParams(searchParam);
    const searchTermValue = params.get(SEARCHTERM);
    if (searchTermValue !== null && searchTermValue !== undefined) {
      searchTerm = searchTermValue;
      searchType = "all";
    }
    
    
  }

  const rightContentSection: SectionContent[] = [
    {
      key: `search-results-rightContentSection-product-grid-${searchTerm}`,
      CurrentComponent: () => {
        return <CatalogEntryListWidget cid={`search-results-${searchTerm}`} page={page} searchType={searchType} searchTerm={searchTerm} />;
      },
    },
    {
      key: `search-results-rightContentSection-product-recommendation-${searchTerm}`,
      CurrentComponent: () => {
        //place holder for product-recommendation layout.
        return <></>;
      },
    },
  ];

  const leftNavigationSection: SectionContent[] = [
    {
      key: `search-results-leftNavigationSection-product-filter-${searchTerm}`,
      CurrentComponent: () => {
        return <FacetNavigationWidget widget={widget} page={page} {...{ searchTerm }} />;
      },
    },
  ];

  const slots: { [key: string]: SectionContent[] } = {
    2: leftNavigationSection,
    3: rightContentSection,
  };

  return (
    <StyledContainer className="page">
      <ProductListingPageLayout cid={`search-results-${searchTerm}`} slots={slots} />
    </StyledContainer>
  );
};

export default SearchResults;
