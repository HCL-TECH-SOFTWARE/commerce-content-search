package com.hcl.commerce.search.rest;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.commerce.search.expression.SearchCriteria;
import com.hcl.commerce.search.internal.runtime.SearchServiceFacade;
import com.hcl.commerce.search.internal.util.StoreHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class provides RESTful services to get <code>unstructuredcontent</code> resource
 * details.
 */

@RestController
@RequestMapping("/store/{storeId}/unstructuredcontent")
public class UnstructuredContentResource {

	private static final String CLASSNAME = UnstructuredContentResource.class.getName();
	private static final Logger LOGGER = LoggerFactory.getLogger(CLASSNAME);

	/** The string constant for "content". */
	private static final String RESOURCE_NAME = "unstructuredcontent";
	
	/** The string constant of resource path, "store/{storeId}/". */
	protected static final String STORE_STOREID_PATH = "store/{storeId}/";
	
	/** The string constant of resource path, "store/{storeId}/unstructuredcontent/". */
	private static final String RESOURCE_PATH = STORE_STOREID_PATH + RESOURCE_NAME + "/";
	
	private static final String WEBCONTENT = "webcontent";
	private static final String ATTACHMENTS = "attachments";
	private static final String CONTENTPAGES = "contentpages";
	private static final String WEBCONTENTVIEW = "webContentView";
	
	public static final String TERM = "term";
	private static final String STR_Q = "?";
	
	/** parameter search term */
	public static final String PARAMETER_SEARCH_TERM = "searchTerm";
	/** parameter search term description */
	public static final String PARAMETER_SEARCH_TERM_DESCRIPTION = "The term to search for.";
	
	/** parameter search type */
	public static final String PARAMETER_SEARCH_TYPE = "searchType";
	
	/** parameter search type description */
	public static final String PARAMETER_SEARCH_TYPE_DESCRIPTION = "The type of search contents to search for.";
	
	/** langId parameter */
	public static final String PARAMETER_LANG_ID = "langId";
	/** langId description */
	public static final String PARAMETER_LANG_ID_DESCRIPTION = "Language identifier. "
			+ "If not specified, the \"locale\" parameter will be used. "
			+ "If \"locale\" is not specified, then the store default language will be used.";
	
	/** profileName */
	public static final String PARAMETER_PROFILE_NAME = "profileName";
	/** profileName description */
	public static final String PARAMETER_PROFILE_NAME_DESCRIPTION = "Profile name. Profiles determine the subset of data to be returned by a search query.";

	/** 200 return code description */
	public static final String RESPONSE_200_DESCRIPTION = "The request is completed successfully.";
	/** 201 return code description */
	public static final String RESPONSE_201_DESCRIPTION = "The requested resource has been created.";
	/** 202 return code description */
	public static final String RESPONSE_202_DESCRIPTION = "The requested resource has been accepted.";
	/** 204 return code description */
	public static final String RESPONSE_204_DESCRIPTION = "The requested completed successfully. No content is returned in the response.";
	/** 400 return code description */
	public static final String RESPONSE_400_DESCRIPTION = "Bad request. Some of the inputs provided to the request are not valid.";
	/** 401 return code description */
	public static final String RESPONSE_401_DESCRIPTION = "Not authenticated. The user session is not valid.";
	/** 403 return code description */
	public static final String RESPONSE_403_DESCRIPTION = "The user is not authorized to perform the specified request.";
	/** 404 return code description */
	public static final String RESPONSE_404_DESCRIPTION = "The specified resource could not be found.";
	/** 409 return code description */
	public static final String RESPONSE_409_DESCRIPTION = "The specified resource already exist.";
	/** 500 return code description */
	public static final String RESPONSE_500_DESCRIPTION = "Internal server error. Additional details will be contained on the server logs.";
	/** 503 return code description */
	public static final String RESPONSE_503_DESCRIPTION = "Service unavailable.";
	
	/** string data type **/
	public static final String DATATYPE_STRING = "string";
	/** integer data type **/
	public static final String DATATYPE_INTEGER = "integer";
	/** long data type **/
	public static final String DATATYPE_LONG = "long";
	/** long data type **/
	public static final String DATATYPE_FLOAT = "float";
	/** boolean data type **/
	public static final String DATATYPE_BOOLEAN = "boolean";

	
	/**
	 * The string constant for "WebContentSuggestions".
	 */
	private static final String WEBCONTENTS_SUGGESTIONS = "webContentsBySearchTerm/{"
			+ PARAMETER_SEARCH_TERM + "}";
	
	/**
	 * The string constant for "unstructuredContentsBySearchTerm".
	 */
	private static final String UNSTRUCTUREDCONTENTS_SUGGESTIONS = "unstructuredContentsBySearchTerm/{"
			+ PARAMETER_SEARCH_TERM + "}";
	
	/**
	 * The string constant for "unstructuredContentsBySearchTerm".
	 */
	private static final String SITECONTENTS_SUGGESTIONS = "siteContentsBySearchTerm/{"
			+ PARAMETER_SEARCH_TERM + "}";
	private static final String CTRL_PARAM_SEARCH_TERM = "_wcf.search.term";
	public static final String CTRL_PARAM_SEARCH_STORE_ONLINE = "_wcf.search.store.online";
	public static final String CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE_URI = "_wcf.search.internal.service.resource.uri";
	public static final String CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE_URL = "_wcf.search.internal.service.resource.url";
	public static final String CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE = "_wcf.search.internal.service.resource";
	public static final String CTRL_PARAM_SEARCH_PROFILE = "_wcf.search.profile";
	public static final String CTRL_PARAM_SEARCH_LANGUAGE = "_wcf.search.language";
	public static final String PARAM_NAME_LANG_ID = "langId";
	public static final String STORECONFIG_HCL_MARKETPLACE_ENABLED = "hcl.marketplace.enabled";
	
	@Autowired
	protected HttpServletRequest request;
	
	/**
	 * Returns the webContent suggestions by specified a term.
	 * 
	 * @return The rest service response object containing an array of
	 *         <code>unstructuredcontent</code> resource details.
	 */
	@RequestMapping(value = WEBCONTENTS_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity findWebContentsBySearchTerm(@RequestParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@RequestParam(value = PARAMETER_SEARCH_TERM_DESCRIPTION, required = true) @PathVariable(PARAMETER_SEARCH_TERM) String searchTerm)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
		}
		String strLanguageId = request.getParameter(PARAMETER_LANG_ID);
		String marketplaceStore = StoreHelper.getStoreConfigurationPropertyValueByName(iStoreId, STORECONFIG_HCL_MARKETPLACE_ENABLED,strLanguageId);
		ResponseEntity result = null;
		ResponseEntity webContentResult = null;
		ResponseEntity unstructuredContentResult = null;
		ResponseEntity siteContentResult = null;
		String searchType = request.getParameter(PARAMETER_SEARCH_TYPE);
		
		if(null != searchType && !searchType.isEmpty() && searchType.equals(WEBCONTENT)) {
			result = getWebContentSearchResults(iStoreId, searchTerm);
		}
		else if(null != searchType && !searchType.isEmpty() && searchType.equals(ATTACHMENTS)) {
			result = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
		else if(null != searchType && !searchType.isEmpty() && searchType.equals(CONTENTPAGES)) {
			result = getSiteContentSearchResults(iStoreId, searchTerm);
		}
		else {
			webContentResult = getWebContentSearchResults(iStoreId, searchTerm);
			unstructuredContentResult = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
			siteContentResult = getSiteContentSearchResults(iStoreId, searchTerm);
		}
				
		JSONArray webContentJsonArray = null;
		JSONArray siteContentJsonArray = null;
		if (null != webContentResult && null != unstructuredContentResult) {
			String webContentJSONObjectString = new ObjectMapper().writeValueAsString(webContentResult.getBody());
			JSONObject webContentJsonObject = new JSONObject(webContentJSONObjectString);

			String attachmentContentJSONObjectString = new ObjectMapper()
					.writeValueAsString(unstructuredContentResult.getBody());
			JSONObject attachmentContentJsonObject = new JSONObject(attachmentContentJSONObjectString);
			
			for (String key : webContentJsonObject.keySet()) {
				if (key.equals(WEBCONTENTVIEW)) {
					webContentJsonArray = webContentJsonObject.getJSONArray(key);
				}
			}
			
			if(null!=siteContentResult) {
				String siteContentJSONObjectString = new ObjectMapper()
						.writeValueAsString(siteContentResult.getBody());
				JSONObject siteContentJsonObject = new JSONObject(siteContentJSONObjectString);
	
				for (String key : siteContentJsonObject.keySet()) {
					if (key.equals(WEBCONTENTVIEW)) {
						siteContentJsonArray = siteContentJsonObject.getJSONArray(key);
					}
				}
			}
			
			for (String key : attachmentContentJsonObject.keySet()) {
				if (key.equals(WEBCONTENTVIEW)) {
					JSONArray jsonArray = attachmentContentJsonObject.getJSONArray(key);
					System.out.println(jsonArray);
					for (int i = 0; i < webContentJsonArray.length(); i++) {
						jsonArray.put(webContentJsonArray.get(i));
					}
					if(null!=siteContentResult) {
						for (int i = 0; i < siteContentJsonArray.length(); i++) {
							jsonArray.put(siteContentJsonArray.get(i));
						}
					}
					webContentJsonObject.put(WEBCONTENTVIEW,jsonArray);
				}
			}
			Map<String, Object> map = new ObjectMapper().readValue(webContentJsonObject.toString(), HashMap.class);
			result = new ResponseEntity(map, HttpStatus.OK);
			
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Result : " + result);
			LOGGER.trace("EXIT");
		}
		return result;
	}
	
	/**
	 * Returns the webContent suggestions by specified a term.
	 * 
	 * @return The rest service response object containing an array of
	 *         <code>sitecontent</code> resource details.
	 */
	@RequestMapping(value = UNSTRUCTUREDCONTENTS_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	
	public ResponseEntity findUnstructuredContentsBySearchTerm(@RequestParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@RequestParam(value = PARAMETER_SEARCH_TERM_DESCRIPTION, required = true) @PathVariable(PARAMETER_SEARCH_TERM) String searchTerm)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
		}
		String strLanguageId = request.getParameter(PARAMETER_LANG_ID);
		 String marketplaceStore = StoreHelper.getStoreConfigurationPropertyValueByName(iStoreId, STORECONFIG_HCL_MARKETPLACE_ENABLED,strLanguageId);
		ResponseEntity result = null;
		ResponseEntity webContentResult = null;
		ResponseEntity unstructuredContentResult = null;
		String searchType = request.getParameter(PARAMETER_SEARCH_TYPE);	
		
		if(null != searchType && !searchType.isEmpty() && searchType.equals(WEBCONTENT)) {
			result = getWebContentSearchResults(iStoreId, searchTerm);
		}
		else if(null != searchType && !searchType.isEmpty() && searchType.equals(ATTACHMENTS)) {
			result = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
		else {
			webContentResult = getWebContentSearchResults(iStoreId, searchTerm);
			unstructuredContentResult = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
				
		JSONArray webContentJsonArray = null;
		int totalrecords = 0;
		int count =0;
		if (null != webContentResult && null != unstructuredContentResult) {
			String webContentJSONObjectString = new ObjectMapper().writeValueAsString(webContentResult.getBody());
			JSONObject webContentJsonObject = new JSONObject(webContentJSONObjectString);

			String attachmentContentJSONObjectString = new ObjectMapper()
					.writeValueAsString(unstructuredContentResult.getBody());
			JSONObject attachmentContentJsonObject = new JSONObject(attachmentContentJSONObjectString);
			int webContentCount = 0;
			int attachmentContentCount = 0;
			int webContentTotalRecords = 0;
			int attachmentContentTotalRecords = 0;
			for (String key : webContentJsonObject.keySet()) {
				if (key.equals(WEBCONTENTVIEW)) {
					webContentJsonArray = webContentJsonObject.getJSONArray(key);
				}
				if (key.equals("recordSetCount")) {
					webContentCount = webContentJsonObject.getInt("recordSetCount");
				}
				if (key.equals("recordSetTotal")) {
					webContentTotalRecords = webContentJsonObject.getInt("recordSetTotal");
				}
			}
			for (String key : attachmentContentJsonObject.keySet()) {
				if (key.equals(WEBCONTENTVIEW)) {
					JSONArray jsonArray = attachmentContentJsonObject.getJSONArray(key);
					for (int i = 0; i < webContentJsonArray.length(); i++) {
						jsonArray.put(webContentJsonArray.get(i));
					}
					webContentJsonObject.put(WEBCONTENTVIEW,jsonArray);
				}
				if (key.equals("recordSetCount")) {
					attachmentContentCount = attachmentContentJsonObject.getInt("recordSetCount");
				}
				if (key.equals("recordSetTotal")) {
					attachmentContentTotalRecords = attachmentContentJsonObject.getInt("recordSetTotal");
				}
			}
			count = webContentCount + attachmentContentCount;
			totalrecords = webContentTotalRecords + attachmentContentTotalRecords;
			webContentJsonObject.put("recordSetCount", count);
			webContentJsonObject.put("recordSetTotal", totalrecords);
			Map<String, Object> map = new ObjectMapper().readValue(webContentJsonObject.toString(), HashMap.class);
			result = new ResponseEntity(map, HttpStatus.OK);
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Result : " + result);
			LOGGER.trace("EXIT");
		}
		return result;
	}
	
	/**
	 * Returns the webContent suggestions by specified a term.
	 * 
	 * @return The rest service response object containing an array of
	 *         <code>sitecontent</code> resource details.
	 */
	@RequestMapping(value = SITECONTENTS_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity findSiteContentsBySearchTerm(@RequestParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@RequestParam(value = PARAMETER_SEARCH_TERM_DESCRIPTION, required = true) @PathVariable(PARAMETER_SEARCH_TERM) String searchTerm)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
		}
		String strLanguageId = request.getParameter(PARAMETER_LANG_ID);
		 String marketplaceStore = StoreHelper.getStoreConfigurationPropertyValueByName(iStoreId, STORECONFIG_HCL_MARKETPLACE_ENABLED,strLanguageId);
		ResponseEntity result = null;
		String searchType = request.getParameter(PARAMETER_SEARCH_TYPE);	
		
		result = getSiteContentSearchResults(iStoreId, searchTerm);
		
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Result : " + result);
			LOGGER.trace("EXIT");
		}
		return result;
	}
	
	
	
	private ResponseEntity getWebContentSearchResults(String iStoreId, String searchTerm) throws Exception {
		ResponseEntity result = null;
		SearchCriteria webContentsearchCriteria = null;
		String webContentSearchTerm = searchTerm;
		String profileName = "HCL_findWebContentsBySearchTerm";
		webContentsearchCriteria = prepareSearchCriteria(iStoreId, searchTerm, RESOURCE_NAME,
					RESOURCE_PATH + WEBCONTENTS_SUGGESTIONS, profileName);
		webContentSearchTerm = webContentsearchCriteria.getControlParameterValue(CTRL_PARAM_SEARCH_TERM);
		if(webContentSearchTerm == null)
		{
			webContentSearchTerm = webContentsearchCriteria.getControlParameterValue(TERM);
			webContentsearchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, webContentsearchCriteria.getControlParameterValue(TERM));
		}
		if (webContentSearchTerm != null && webContentSearchTerm.equals("*")) {
			webContentsearchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, "####");
        }
		if (webContentSearchTerm == null || webContentSearchTerm.length() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("searchTerm is not valid: \'" + webContentSearchTerm);
			}
			throw new Exception();
		} else {
			result = SearchServiceFacade.getInstance().performSearch(webContentsearchCriteria);
		}
		return result;
	}
	
	private ResponseEntity getUnsturcturedContentSearchResults(String iStoreId, String searchTerm) throws Exception {
		ResponseEntity result = null;
		SearchCriteria attachmentContentSearchCriteria = null;
		String attachmentContentSearchTerm = searchTerm;
		String profileName = "HCL_findUnstructuredContentsBySearchTerm";
        attachmentContentSearchCriteria = prepareSearchCriteria(iStoreId, searchTerm, RESOURCE_NAME,
				RESOURCE_PATH + UNSTRUCTUREDCONTENTS_SUGGESTIONS, profileName);
        
        attachmentContentSearchTerm = attachmentContentSearchCriteria.getControlParameterValue(CTRL_PARAM_SEARCH_TERM);
				
		if(attachmentContentSearchTerm == null)
		{
			attachmentContentSearchTerm = attachmentContentSearchCriteria.getControlParameterValue(TERM);
			attachmentContentSearchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, attachmentContentSearchCriteria.getControlParameterValue(TERM));
		}		
		if (attachmentContentSearchTerm != null && attachmentContentSearchTerm.equals("*")) {
			attachmentContentSearchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, "####");
        }
		if (attachmentContentSearchTerm == null || attachmentContentSearchTerm.length() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("searchTerm is not valid: \'" + attachmentContentSearchTerm);
			}
			throw new Exception();
		} else {
			result = SearchServiceFacade.getInstance().performSearch(attachmentContentSearchCriteria);
		}
		return result;
	}
	
	private ResponseEntity getSiteContentSearchResults(String iStoreId, String searchTerm) throws Exception {
		ResponseEntity result = null;
		SearchCriteria siteContentsearchCriteria = null;
		String siteContentSearchTerm = searchTerm;
		String profileName = "HCL_findSiteContentsBySearchTerm";
		siteContentsearchCriteria = prepareSearchCriteria(iStoreId, searchTerm, RESOURCE_NAME,
					RESOURCE_PATH + SITECONTENTS_SUGGESTIONS, profileName);
		siteContentSearchTerm = siteContentsearchCriteria.getControlParameterValue(CTRL_PARAM_SEARCH_TERM);
		if(siteContentSearchTerm == null)
		{
			siteContentSearchTerm = siteContentsearchCriteria.getControlParameterValue(TERM);
			siteContentsearchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, siteContentsearchCriteria.getControlParameterValue(TERM));
		}
		if (siteContentSearchTerm != null && siteContentSearchTerm.equals("*")) {
			siteContentsearchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, "####");
        }
		if (siteContentSearchTerm == null || siteContentSearchTerm.length() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("searchTerm is not valid: \'" + siteContentSearchTerm);
			}
			throw new Exception();
		} else {
			result = SearchServiceFacade.getInstance().performSearch(siteContentsearchCriteria);
		}
		return result;
	}
	
	
	
	/**
	 * This method retrieves the unstructured content based on the search term.
	 * 
	 * @param storeId
	 *            the store identifier.
	 * @param searchTerm
	 *            the search term, this is mandatory parameter and cannot be null or
	 *            empty.
	 * @param resourceName
	 *            the resource name
	 * @param resourceUri
	 *            the resource URI
	 * @return the search criteria object
	 */
	private SearchCriteria prepareSearchCriteria(String storeId, String searchTerm, String resourceName,
			String resourceUri, String profileName) throws Exception {

		final String METHODNAME = "prepareSearchCriteria(String,String,String,String)";
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("storeId : " + storeId + ",  searchTerm : " + searchTerm + ",  resourceName : " + resourceName
					+ ",  resourceUri : " + resourceUri);
		}
		
		String langId = request.getParameter(PARAM_NAME_LANG_ID);
		
		SearchCriteria searchCriteria = SearchCriteria.getCriteria();
		//Set all the necessary control parameters in the searchCriteria. Below is just a sample about how to set parameters.
		searchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_LANGUAGE,langId);
		searchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_PROFILE,
				profileName);
		searchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE,
				resourceName);
		searchCriteria.setControlParameterValue(
				CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE_URL, getURIWithQueryString());
		searchCriteria.setControlParameterValue(
				CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE_URI, resourceUri);
		searchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_STORE_ONLINE, storeId);
		String searchTermQueryParameter = searchCriteria
				.getControlParameterValue(CTRL_PARAM_SEARCH_TERM);
		if (searchTermQueryParameter != null && searchTermQueryParameter.trim().length() > 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Using the search Term query parameter: " + searchTermQueryParameter
						+ " instade of the search term path parameter :" + searchTerm);
			}
			searchTerm = searchTermQueryParameter.trim();
		} 
		searchCriteria.setControlParameterValue(CTRL_PARAM_SEARCH_TERM, searchTerm);
		

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("searchCriteria : " + searchCriteria);
			LOGGER.trace("EXIT");
		}
		return searchCriteria;
	}
	
	protected String getURIWithQueryString() {
		StringBuffer requestURI = request.getRequestURL();
		if (request.getQueryString() != null) {
			requestURI.append(STR_Q).append(request.getQueryString());
		}
		return requestURI.toString();
	}

}
