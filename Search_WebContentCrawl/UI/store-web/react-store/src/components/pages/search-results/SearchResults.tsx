/**
 *==================================================
 * Licensed Materials - Property of HCL Technologies
 *
 * HCL Commerce
 *
 * (C) Copyright HCL Technologies Limited 2020, 2021
 *
 *==================================================
 */
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
      searchType = "webcontent";
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
