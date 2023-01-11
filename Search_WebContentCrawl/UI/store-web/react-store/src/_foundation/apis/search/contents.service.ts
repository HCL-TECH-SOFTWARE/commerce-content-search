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
