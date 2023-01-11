/*
 *---------------------------------------------------
 * Licensed Materials - Property of HCL Technologies
 *
 * HCL Commerce
 *
 * (C) Copyright HCL Technologies Limited 2020
 *
 *---------------------------------------------------
 */
//Standard libraries
import { useTranslation } from "react-i18next";
//UI
import { StyledGrid } from "@hcl-commerce-store-sdk/react-component";
//Custom libraries
import { ProductCardLayout } from "../../../components/widgets/product-card";
// import { ITabs } from "@hcl-commerce-store-sdk/react-component";
import { StyledTabs } from "@hcl-commerce-store-sdk/react-component";
import { useEffect } from "react";
import contentsService from "../../../_foundation/apis/search/contents.service";

/**
 * Product Card component
 * displays catentry image, name, price, etc
 * @param props
 */

export  function useProductCardWidget(props: any) {
  const { 
     productListTotal, 
     productList,
     categoryId,
     paramsBase,
     selectedFacets, 
     priceSelected,
     searchTerm, 
     content,
     setContent,
     searchType,
     setSearchType,
     searchResultTabs,
     ...rest
     } = props;
  const { t } = useTranslation();


  const parameters: any = {
    ...paramsBase
    };

 
  useEffect(()=>{
      contentsService
      .webContentsBySearchTerm(parameters)
      .then((res) => {
        setContent(res.data.webContentView);
        })
        .catch((e) => {
          console.log(e);
      })
      //eslint-disable-next-line
  },[searchTerm,searchType])

  const productsView = (
    <StyledGrid container spacing={2} alignItems="stretch" direction="row">
      {productList?.map((product:any) => (
          <StyledGrid item xs={6} sm={4} lg={3} key={product.id}>
              <ProductCardLayout product={product} categoryId={categoryId} {...rest} />
        </StyledGrid>
      ))}
    </StyledGrid>
  )
  searchResultTabs.push({
    title: t("ProductGrid.Labels.productsView"),
    tabContent: productsView
  })

  const contentsView = (
    <StyledGrid container spacing={2} alignItems="stretch" direction="row">
      <ul style={{listStyleType:"none"}}>
          {content?.map((c:any)=>(
            <div style={{ border:"1px solid grey",padding:"10px"}} key={c.name}>
              <li>{c.name}</li>
              <li><a href={c.url}>{c.url}</a></li>
            </div>
          ))}
      </ul>
    </StyledGrid>
  )
  searchResultTabs.push({
    title: t("ProductGrid.Labels.contentsView"),
    tabContent: contentsView
  })

  return (
    <>
        {searchTerm !=="" ?
            <StyledTabs name="result" childrenList={searchResultTabs}></StyledTabs>
            : <>{productsView}</>  
        } 
      
          {productListTotal === 0 && (Object.keys(selectedFacets)?.length > 0 || priceSelected) ? (
            <StyledGrid item xs={12}>
              {t("ProductGrid.Labels.noProductsFoundForFilter")}
            </StyledGrid>
          ) : null}
   </>       
  ) 
}
