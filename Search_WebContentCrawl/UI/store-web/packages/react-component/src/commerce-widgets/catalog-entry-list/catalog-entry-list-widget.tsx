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
import React, { ChangeEvent, Fragment } from "react";
import { Navigate } from "react-router-dom";
import { LazyComponentProps, trackWindowScroll } from "react-lazy-load-image-component";
//UI
import {
  StyledGrid,
  StyledChip,
  StyledSelect,
  StyledPagination,
  StyledButton,
  StyledTypography,
  StyledFormControl,
  StyledLink,
} from "../../elements";

/**
 * Catalog entry list widget
 *  @summary Displays Product Card List in Grid Layout.
 * `@prop {any} props` have following properties:
 * `@property {String} cid(required)` Unique identifier used for layouts.
 * `@property {String} categoryId(required)` Category unique identifier.
 * `@property {String} searchTerm` The input string for search.
 * `@property {any} translation(required)` The translation JSON object for i18n.
 * `@property {any} ProductCards(required)` The wrapper component to display product list.
 * `@property {any} formattedPriceDisplay(required)` The wrapper element use to display price on product grid layout.
 * `@property {any} sortOptions(required)` The options to sort the products like Name, Brand etc.
 * `@property {any} onSortOptionChange `The function will sort the products like Name, Brand etc.
 * `@property {any} SEARCH(required)` The constant variable - "bySearchTerm".
 * `@property {any} pageLimit(required)`  The number of products to dislay in a single page for pagination.
 * `@property {any} selectedFacets(required)` The labels use to display selected facet name.
 * `@property {any} clearPriceFacet(required)` The handler to clear the price facets.
 * `@property {any} onPageChange(required)` The handler to go to next page - pagination.
 * `@property {any} isValidUrl(required)` The function will return true if URL is valid.
 */
interface CatalogEntryListWidgetProps extends LazyComponentProps {
  cid: string;
  categoryId?: string;
  searchTerm?: string;
  isValidUrl: any;
  onPageChange: any;
  clearPriceFacet: any;
  onSortOptionChange: any;
  onClearAll: any;
  onFacetRemove: any;
  priceSelected: any;
  selectedFacets: any;
  productListTotal: any;
  selectedSortOption: any;
  pageLimit: any;
  productList: any;
  sortOptions: any;
  selectedPageOffset: any;
  suggestedKeywords: any;
  SEARCH: any;
  formattedPriceDisplay: any;
  ProductCards?: any;
  translation?: any;
  selectFacetRemove: boolean;
  content:any;
  searchResultTabs:any;
}
/**
 * Product Grid component
 * displays catalog entry listing, pagination and selected facets
 * @param props
 */
const CatalogEntryListWidget: React.FC<CatalogEntryListWidgetProps> = (props: any) => {
  const {
    priceSelected,
    selectedFacets,
    selectedSortOption,
    sortOptions,
    selectedPageOffset,
    suggestedKeywords,
    SEARCH,
    formattedPriceDisplay,
    ProductCards,
    translation,
    onSortOptionChange,
    productListTotal,
    pageLimit,
    cid,
    onFacetRemove,
    onClearAll,
    categoryId,
    content,
    searchResultTabs,
    searchTerm,
    isValidUrl,
    onPageChange,
    clearPriceFacet,
    productList,
    selectFacetRemove,
  } = props;
  const pageCountTotal: number = Math.ceil(productListTotal / pageLimit);
  const selectedPage = Math.floor(selectedPageOffset / pageLimit) + 1;

  return (
    <div className="product-listing-container productListingWidget top-margin-3">
      {productListTotal === 0 && searchTerm !== "" && !selectFacetRemove ? (
        <div id={`productGrid_div_18_${cid}`} className="suggested-keywords">
          {/* <h4 id={`productGrid_p_19_${cid}`}>{translation.ProductGridLabelsnoMatches}</h4>
          <p id={`productGrid_p_21_${cid}`}>{translation.ProductGridLabelssearchAgain}</p> */}
          {suggestedKeywords?.length > 0 && (
            <>
              {translation.ProductGridLabelssuggestion}
              <br />
              {suggestedKeywords?.map((keyword: string, index: number) => (
                <StyledLink
                  key={keyword}
                  to={SEARCH + "?searchTerm=" + keyword}
                  className="suggestion-link"
                  id={`productGrid_a_22_${index}_${cid}`}
                  testId={`suggestedKeywords_${keyword}`}>
                  {keyword} <br />
                </StyledLink>
              ))}
            </>
          )}
        </div>
      ) : null}
      {/* Search result is 1, then go to PDP directly */}
      {productListTotal === 1 &&
        isValidUrl(productList) &&
        searchTerm !== "" &&
        Object.keys(selectedFacets)?.length === 0 &&
        !priceSelected &&
        !selectFacetRemove && <Navigate replace to={productList[0].seo.href} />}

      {productListTotal > 1 ? (
        <StyledGrid container className="bottom-margin-1" justifyContent="space-between" alignItems="center">
          <StyledGrid item>
            <StyledTypography variant="subtitle2">
              {categoryId !== ""
                ? translation.ProductGridLabelsproductFound
                : translation.ProductGridLabelsproductSearchFound}
            </StyledTypography>
          </StyledGrid>
          <StyledGrid container item style={{ width: "auto", alignItems: "center" }}>
            <StyledGrid item>
              <StyledFormControl variant="outlined">
                <StyledSelect
                  data-testid="list-sort-option"
                  id={`productGrid_select_6_${cid}`}
                  value={selectedSortOption}
                  native
                  onChange={(event: any) => onSortOptionChange(event)}
                  fullWidth>
                  {sortOptions?.map((option: any, index: number) => (
                    <option value={option.value} key={option.value} id={`productGrid_option_7_${index}_${cid}`}>
                      {translation.optiontranslationKey[index]}
                    </option>
                  ))}
                </StyledSelect>
              </StyledFormControl>
            </StyledGrid>
          </StyledGrid>
        </StyledGrid>
      ) : null}

      {/* Facet selection listing */}
      {Object.keys(selectedFacets).length > 0 || priceSelected ? (
        <StyledGrid item container direction="row" alignItems="center" className="bottom-margin-3">
          <StyledTypography variant="body2">{translation.ProductGridLabelsfilteredBy}</StyledTypography>
          {Object.keys(selectedFacets).map((key: string, index: number) => (
            <Fragment key={key}>
              <StyledChip
                size="medium"
                className="left-margin-1"
                label={selectedFacets[key]}
                onClick={() => onFacetRemove(key)}
                onDelete={() => onFacetRemove(key)}
                data-testid={`catalog-entry-list-${key}-selected-facet-chip`}
              />
            </Fragment>
          ))}
          {priceSelected && (
            <StyledChip
              size="medium"
              className="left-margin-1"
              label={formattedPriceDisplay}
              onClick={() => clearPriceFacet()}
              onDelete={() => clearPriceFacet()}
              data-testid="catalog-entry-list-formatted-price-chip"
            />
          )}
          {(Object.keys(selectedFacets).length > 1 || (priceSelected && Object.keys(selectedFacets).length > 0)) && (
            <StyledButton testId="clear-all-facets" variant="text" className="left-margin-1">
              <StyledLink onClick={(event) => onClearAll(event)} to="" className="clear-all" testId="clear-all-facets">
                {translation.ProductGridActionsclearAll}
              </StyledLink>
            </StyledButton>
          )}
        </StyledGrid>
      ) : null}

      {/* Product listing and pagination */}
      {ProductCards}
      {productListTotal > pageLimit && (
        <StyledPagination
          count={pageCountTotal}
          shape="rounded"
          page={selectedPage}
          onChange={(event: ChangeEvent<unknown>, value: number) => onPageChange(value)}
        />
      )}
    </div>
  );
};
export default trackWindowScroll(CatalogEntryListWidget);
