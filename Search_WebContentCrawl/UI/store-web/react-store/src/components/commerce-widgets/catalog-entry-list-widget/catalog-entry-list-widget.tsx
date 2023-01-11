/**
 *==================================================
 * Licensed Materials - Property of HCL Technologies
 *
 * HCL Commerce
 *
 * (C) Copyright HCL Technologies Limited 2020
 *
 *==================================================
 */
//Standard libraries
import React from "react";
import { useTranslation } from "react-i18next";
import { LazyComponentProps, trackWindowScroll } from "react-lazy-load-image-component";
//Foundation libraries
import { useProductGridLayout } from "./hooks/use-product-grid-layout";
//Custom libraries
import { CatalogEntryListWidget as CatalogEntryList, ITabs } from "@hcl-commerce-store-sdk/react-component";
import { SEARCH } from "../../../constants/routes";
import { useProductCardWidget } from "../../widgets/product-card/ProductCardWidget";
import FormattedPriceDisplay from "../../widgets/formatted-price-display";
import { useCompare } from "../../../_foundation/hooks/use-compare";
import { useLocation } from "react-router";
import { useEffect } from "react";
import { CompareCollectorWidget } from "../../widgets/compare/collector";

interface ProductGridProps extends LazyComponentProps {
  cid: string;
  page?: any;
  searchTerm?: string;
  searchType?:string;
}
/**
 * Product Grid component
 * displays catalog entry listing, pagination and selected facets
 * @summary Displays Product Card List in Grid Layout.
 * `@prop {any} props` have following properties:
 * `@property {String} cid(required)` Unique identifier used for layouts.`
 * `@property {String} page(optional)`  page object has category info such as category identifier.`
 * `@property {String} searchTerm(optional)` The input string for searching.`
 */
const CatalogEntryListWidget: React.FC<ProductGridProps> = (props: any) => {
  const cid = props.cid;
  const categoryId: string = props.page?.tokenValue ?? "";
  const searchTerm: string = props.searchTerm ?? "";
  const searchTypeValue: string = props.searchType;
  const location: any = useLocation();
  const { compare: currentCompare } = location?.state ?? {};
  const { compare } = useCompare();
  const  searchResultTabs:ITabs[] = [];

  const {
    isValidUrl,
    onPageChange,
    clearPriceFacet,
    onSortOptionChange,
    onClearAll,
    onFacetRemove,
    priceSelected,
    selectedFacets,
    productListTotal,
    selectedSortOption,
    pageLimit,
    selectedPageOffset,
    defaultCurrencyID,
    selectedMaxPrice,
    productList,
    selectedMinPrice,
    sortOptions,
    suggestedKeywords,
    selectFacetRemove,
    paramsBase,
    content,
    setContent,
    searchType,
    setSearchType
  } = useProductGridLayout({ categoryId, searchTerm, searchTypeValue, CatalogEntryListWidget });

  const ProductCards = useProductCardWidget({
    productListTotal,
    productList,
    categoryId,
    compare,
    selectedFacets,
    priceSelected,
    paramsBase,
    searchTerm,
    content,
    setContent,
    searchType,
    setSearchType,
    searchResultTabs
  });

  const formattedPriceDisplay = (
    <FormattedPriceDisplay min={selectedMinPrice} max={selectedMaxPrice} currency={defaultCurrencyID} />
  );
  const { t } = useTranslation();

  // Translation File values
  const translation = {
    ProductGridLabelsnoMatches: t("ProductGrid.Labels.noMatches", {
      searchTerm: searchTerm,
    }),
    ProductGridLabelssearchAgain: t("ProductGrid.Labels.searchAgain", {
      searchTerm: searchTerm,
    }),
    ProductGridLabelssuggestion: t("ProductGrid.Labels.suggestion"),
    ProductGridLabelsproductFound: t("ProductGrid.Labels.productFound", {
      count: productListTotal,
    }),
    ProductGridLabelsproductSearchFound: t("ProductGrid.Labels.productSearchFound", {
      count: productListTotal,
      searchTerm: searchTerm,
    }),
    ProductGridLabelsfilteredBy: t("ProductGrid.Labels.filteredBy"),
    ProductGridActionsclearAll: t("ProductGrid.Actions.clearAll"),
    optiontranslationKey: sortOptions?.map((option: any) => t(`${option.translationKey}`)),
  };

  useEffect(() => {
    compare?.copyFrom(currentCompare);
  }, [currentCompare]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div
      style={{
        position: "relative",
        display: "flex",
        flexDirection: "column",
        height: "100%",
      }}>
      <span style={{ flex: "1" }}>
        <CatalogEntryList
          {...{
            cid,
            categoryId,
            content,
            searchResultTabs,
            searchTerm,
            isValidUrl,
            onPageChange,
            clearPriceFacet,
            onSortOptionChange,
            onClearAll,
            onFacetRemove,
            priceSelected,
            selectedFacets,
            productListTotal,
            selectedSortOption,
            pageLimit,
            productList,
            sortOptions,
            selectedPageOffset,
            suggestedKeywords,
            SEARCH,
            formattedPriceDisplay,
            ProductCards,
            translation,
            selectFacetRemove,
            compare,
          }}
        />
      </span>
      {productList.length && compare?.data.len ? <CompareCollectorWidget {...{ compare }} /> : null}
    </div>
  );
};

export default trackWindowScroll(CatalogEntryListWidget);
