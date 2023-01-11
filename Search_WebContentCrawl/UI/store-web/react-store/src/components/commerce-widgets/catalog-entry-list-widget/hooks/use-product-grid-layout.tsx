/*
 *---------------------------------------------------
 * Licensed Materials - Property of HCL Technologies
 *
 * HCL Commerce
 *
 * (C) Copyright HCL Technologies Limited 2021
 *
 *---------------------------------------------------
 */
//Standard libraries
import { useEffect, ChangeEvent, MouseEvent, useRef, useMemo, useCallback, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import Axios, { Canceler } from "axios";
//Foundation libraries
import { useSite } from "../../../../_foundation/hooks/useSite";
import { useBreadcrumbTrail } from "../../breadcrumb-trail-widget/hooks/use-breadcrumb-trail";
//Custom libraries
import getDisplayName from "react-display-name";
import { PRODUCT_LIST_FIELDS, PAGINATION_CONFIGS } from "../../../../configs/catalog";
import { SORT_OPTIONS } from "../../../../constants/catalog";
//Redux
import { keywordSelector } from "../../../../redux/selectors/search";
import { selectedPageOffsetSelector } from "../../../../redux/selectors/catalog";
import {
  productListSelector,
  productListTotalSelector,
  priceModeSelector,
  selectedFacetsSelector,
  selectedFacetLimitsSelector,
  selectedFacetPricesSelector,
  selectedSortOptionSelector,
  selectFacetRemoveSelector,
} from "../../../../redux/selectors/catalog";
import * as catalogActions from "../../../../redux/actions/catalog";
import { currentContractIdSelector } from "../../../../redux/selectors/contract";
//GA360
import AsyncCall from "../../../../_foundation/gtm/async.service";
import { SEARCH_PLP_PROFILE, SEARCH_FIND_PROFILE, STRING_TRUE } from "../../../../constants/common";
import { selectedSellersSelector } from "../../../../redux/selectors/sellers";
import { isEmpty } from "lodash-es";

export const useProductGridLayout = (props: any) => {
  const categoryId = props.categoryId;
  const searchTerm = props.searchTerm;
  const dispatch = useDispatch();
  const cancels: Canceler[] = [];
  const { mySite } = useSite();
  const _productList = useSelector(productListSelector);
  const [content,setContent] = useState<any>(null);
  const [searchType,setSearchType] = useState<any>(props.searchTypeValue);
  const productListTotal = useSelector(productListTotalSelector);
  const priceMode = useSelector(priceModeSelector);
  const selectedFacetLimits = useSelector(selectedFacetLimitsSelector);
  const { breadcrumbs } = useBreadcrumbTrail();
  const selectedFacets = useSelector(selectedFacetsSelector);
  const selectedFacetPrices = useSelector(selectedFacetPricesSelector);
  const selectFacetRemove: boolean = useSelector(selectFacetRemoveSelector);
  const selectedMinPrice = selectedFacetPrices.min;
  const selectedMaxPrice = selectedFacetPrices.max;
  const selectedSortOption = useSelector(selectedSortOptionSelector);
  const defaultCurrencyID: string = mySite ? mySite.defaultCurrencyID : "";
  const pageDefaultOffset: number = PAGINATION_CONFIGS.pageDefaultOffset;
  const pageLimit: number = PAGINATION_CONFIGS.pageLimit;
  const contract = useSelector(currentContractIdSelector);
  const selectedPageOffset = useSelector(selectedPageOffsetSelector);
  const CancelToken = Axios.CancelToken;
  const priceSelected = selectedMinPrice > -1 && selectedMaxPrice > -1;
  const widgetName = getDisplayName("ProductGridLayout");
  const suggestedKeywords = useSelector(keywordSelector);
  const paramsBase: any = {
    currency: defaultCurrencyID,
    contractId: contract ? contract : "",
    _fields: PRODUCT_LIST_FIELDS,
    limit: pageLimit,
    offset: 0,
    widget: widgetName,
    cancelToken: new CancelToken(function executor(c) {
      cancels.push(c);
    }),
  };
  const selectedSellers = useSelector(selectedSellersSelector);

  const getSKUs = useCallback(
    (products) => {
      // first collect items that require SKUs to be fetched (if no price info available)
      const ofInterest = products.filter(
        ({ type, buyable, hasSingleSKU, items, groupingProperties }) =>
          !groupingProperties &&
          !hasSingleSKU &&
          !items &&
          buyable === STRING_TRUE &&
          (type === "product" || type === "variant")
      );

      if (ofInterest.length) {
        const { contractId, widget, cancelToken } = paramsBase;
        const parameters: any = {
          storeId: mySite.storeID,
          catalogId: mySite.catalogID,
          id: ofInterest.map(({ id }) => id),
          contractId,
          widget,
          cancelToken,
        };
        dispatch(catalogActions.getProductListDetailsAction({ parameters }));
      }
    },
    [mySite, contract, searchTerm] // eslint-disable-line react-hooks/exhaustive-deps
  );

  if (categoryId !== "") {
    paramsBase["categoryId"] = categoryId;
  }

  if (selectedSortOption !== "0") {
    paramsBase["orderBy"] = selectedSortOption;
  }

  if (selectedSortOption === "0" && paramsBase["orderBy"]) {
    delete paramsBase["orderBy"];
  }

  if (searchTerm !== "") {
    paramsBase["searchTerm"] = searchTerm;
  }
  if (searchType !== "") {
    paramsBase["searchType"] = searchType;
  }

  const productList = useMemo(() => {
    let rc = _productList;
    if (breadcrumbs?.length) {
      rc = _productList.map((p: any) => ({
        ...p,
        link: {
          pathname: p.seo?.href,
          state: {
            categoryId,
            breadCrumbTrailEntryView: [...breadcrumbs, { label: p.name, value: p.id }],
          },
        },
      }));
    }
    return rc;
  }, [breadcrumbs, _productList, categoryId]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (mySite && contract !== null && (!isEmpty(categoryId) || !isEmpty(searchTerm))) {
      const parameters = { ...paramsBase };
      const query = !isEmpty(selectedFacets) ? { facet: Object.keys(selectedFacets) } : {};
      const states = !isEmpty(selectedFacets) ? { selectedFacets } : undefined;
      Object.assign(query, { profileName: isEmpty(parameters.searchTerm) ? SEARCH_PLP_PROFILE : SEARCH_FIND_PROFILE });
      Object.assign(parameters, { query });

      dispatch(catalogActions.getProductListAction({ parameters, selectedSellers, states }));
    }
    return () => {
      dispatch(catalogActions.resetProductListAction());
      cancels.forEach((cancel) => cancel());
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [categoryId, mySite, contract, selectedSellers]);

  //GA360
  const mounted = useRef(false);
  useEffect(() => {
    if (mySite.enableGA) {
      if (!mounted.current) {
        mounted.current = true;
      } else {
        if (categoryId === "" && productListTotal >= 0) {
          AsyncCall.sendSearchPageViewEvent(
            { productListTotal, searchTerm },
            { enableUA: mySite.enableUA, enableGA4: mySite.enableGA4 }
          );
          if (productListTotal > 0) {
            AsyncCall.sendProductImpressionEvent(
              {
                productList: _productList,
                listerFlag: false,
                breadcrumbs,
              },
              { enableUA: mySite.enableUA, enableGA4: mySite.enableGA4 }
            );
          }
        } else if (categoryId !== "" && productListTotal >= 0) {
          AsyncCall.sendListerPageViewEvent(
            { productListTotal, breadcrumb: breadcrumbs },
            { enableUA: mySite.enableUA, enableGA4: mySite.enableGA4 }
          );
          if (productListTotal > 0) {
            AsyncCall.sendProductImpressionEvent(
              { productList: _productList, listerFlag: true, breadcrumbs },
              { enableUA: mySite.enableUA, enableGA4: mySite.enableGA4 }
            );
          }
        }
      }
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [_productList]);

  useEffect(() => getSKUs(_productList), [_productList]); // eslint-disable-line react-hooks/exhaustive-deps

  /**
   * Removes selected facet and dispatches request to get product list
   * @param selection The selected facet entry's value
   */
  const onFacetRemove = (selection: string) => {
    let newSelectedFacets = { ...selectedFacets };
    if (newSelectedFacets[selection] !== undefined) {
      const { [selection]: tempLabel, ...newRemovedSelectedFacets }: { [key: string]: string } = newSelectedFacets;
      newSelectedFacets = newRemovedSelectedFacets;
    }

    const newStates = {
      selectedFacets: newSelectedFacets,
    };
    dispatchGetProductListAction(
      newSelectedFacets,
      selectedFacetLimits,
      selectedMinPrice,
      selectedMaxPrice,
      pageDefaultOffset,
      selectedSortOption,
      newStates
    );
  };

  /**
   * Removes all selected facet and dispatches request to get product list
   * @param event The event handler
   */
  const onClearAll = (event: MouseEvent<HTMLAnchorElement>) => {
    event?.preventDefault();
    const newSelectedFacets = {};
    const newStates = {
      selectedFacets: newSelectedFacets,
      minPrice: -1,
      maxPrice: -1,
    };
    dispatchGetProductListAction(
      newSelectedFacets,
      selectedFacetLimits,
      -1,
      -1,
      pageDefaultOffset,
      selectedSortOption,
      newStates
    );
  };

  /**
   * Gets the list of default product sort options plus price sort options if price mode allows
   * @returns List of permitted sort options
   */
  const getSortOptions = () => {
    let sortOptionsArray = SORT_OPTIONS.defaultSortOptions;
    if (priceMode === "1") {
      sortOptionsArray = sortOptionsArray.concat(SORT_OPTIONS.priceSortOptions);
    }

    return sortOptionsArray;
  };
  const sortOptions = getSortOptions();

  /**
   * Sets the sort option to order the product list and dispatches request to get product list
   * @param event The event handler
   */
  const onSortOptionChange = (event: ChangeEvent<{ name?: string | undefined; value: unknown }>) => {
    const selection = event.target.value as string;

    const newStates = {
      selectedSortOption: selection,
    };
    dispatchGetProductListAction(
      selectedFacets,
      selectedFacetLimits,
      selectedMinPrice,
      selectedMaxPrice,
      pageDefaultOffset,
      selection,
      newStates
    );
  };
  /**
   * Reset the selected price range and dispatches request to get product list
   */
  const clearPriceFacet = () => {
    const newStates = {
      minPrice: -1,
      maxPrice: -1,
    };
    dispatchGetProductListAction(
      selectedFacets,
      selectedFacetLimits,
      -1,
      -1,
      pageDefaultOffset,
      selectedSortOption,
      newStates
    );
  };
  /**
   * Sets the page offset and dispatches request to get product list
   * @param value The page number
   */
  const onPageChange = (value: number) => {
    value = value * pageLimit - pageLimit;
    if (value >= 0) {
      const newStates = {
        selectedPageOffset: value,
      };
      dispatchGetProductListAction(
        selectedFacets,
        selectedFacetLimits,
        selectedMinPrice,
        selectedMaxPrice,
        value,
        selectedSortOption,
        newStates
      );
    }
  };

  /**
   * Wrapper to dispatch request to get product list
   * @param selectedFacets The updated selected facets map
   * @param selectedFacetLimits The updated selected facet limits array
   * @param selectedMinPrice The selected min price or -1, if not set
   * @param selectedMaxPrice The selected max price or -1, if not set
   * @param selectedPageOffset The selected offset for pagination
   * @param selectedSortOption The selected sort option
   * @param states The states that have changed and should be persisted back to redux
   */
  const dispatchGetProductListAction = (
    selectedFacets: any,
    selectedFacetLimits: string[],
    selectedMinPrice: number,
    selectedMaxPrice: number,
    selectedPageOffset: number,
    selectedSortOption: string,
    states: any
  ) => {
    const { widget, cancelToken, ...query } = paramsBase;
    Object.assign(query, { profileName: isEmpty(query.searchTerm) ? SEARCH_PLP_PROFILE : SEARCH_FIND_PROFILE });

    const selectedFacetsArray = Object.keys(selectedFacets);
    if (selectedFacetsArray.length > 0) {
      Object.assign(query, { facet: selectedFacetsArray });
    }
    if (selectedFacetLimits.length > 0) {
      Object.assign(query, { facetLimit: selectedFacetLimits });
    }
    if (selectedMinPrice > -1 && selectedMaxPrice > -1) {
      Object.assign(query, { minPrice: selectedMinPrice, maxPrice: selectedMaxPrice });
    }
    if (selectedPageOffset >= 0) {
      Object.assign(query, { offset: selectedPageOffset });
    }
    if (selectedSortOption !== "0") {
      Object.assign(query, { orderBy: selectedSortOption });
    }
    if (selectedSortOption === "0" && query.orderBy) {
      delete query.orderBy;
    }
    const parameters = { widget, cancelToken, query };
    const newStates = { ...states };
    if (newStates["selectedPageOffset"] === undefined) {
      newStates["selectedPageOffset"] = pageDefaultOffset;
    }
    dispatch(
      catalogActions.getProductListAction({
        parameters,
        states: newStates,
        selectedSellers,
      })
    );
    window.scrollTo(0, 0);
  };

  const isValidUrl = (productList: any): boolean => {
    return productList?.length && productList[0].seo?.href;
  };

  return {
    onSortOptionChange,
    clearPriceFacet,
    onPageChange,
    isValidUrl,
    onClearAll,
    onFacetRemove,
    cancels,
    content,
    searchType,
    setSearchType,
    setContent,
    paramsBase,
    sortOptions,
    selectedSortOption,
    defaultCurrencyID,
    categoryId,
    searchTerm,
    selectedMinPrice,
    selectedMaxPrice,
    pageLimit,
    selectedFacets,
    priceSelected,
    productList,
    selectedPageOffset,
    productListTotal,
    suggestedKeywords,
    selectFacetRemove,
  };
};
