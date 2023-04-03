/**
 * 
 */
package com.hcl.commerce.search.rest;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.commerce.search.exception.SearchApplicationException;
import com.hcl.commerce.search.expression.SearchCriteria;
import com.hcl.commerce.search.expression.SearchExpressionConstants;
import com.hcl.commerce.search.internal.config.ValueMappingService;
import com.hcl.commerce.search.internal.util.StoreHelper;
import com.hcl.commerce.search.rest.swagger.model.CategorySuggestion;
import com.hcl.commerce.search.rest.swagger.model.CommonSuggestions;
import com.hcl.commerce.search.rest.swagger.model.KeywordSuggestion;
import com.hcl.commerce.search.rest.swagger.model.ProductSuggestion;
import com.hcl.commerce.search.rest.swagger.model.StaticContentDetail;
import com.hcl.commerce.search.rest.util.GenericUtils;
import com.hcl.commerce.search.rest.util.SpecialCharacterHelper;


import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This class provides RESTful services to get <code>sitecontent</code> resource
 * details.
 */
@RestController
@RequestMapping("/store/{storeId}/sitecontent")
@Tag(name = "Site-Content-Resource", description = "This class provides RESTful services to get resource details for site content.")
public class SiteContentResource extends AbstractSearchResource {

	private static final String CLASSNAME = SiteContentResource.class.getName();
	private static final Logger LOGGER = LoggerFactory.getLogger(CLASSNAME);

	/** The string constant for "content". */
	private static final String RESOURCE_NAME = "sitecontent";
	
	/**
	 * The string constant for "suggestions".
	 */
	private static final String SUGGESTIONS = "suggestions";
	
	private static final String SUGGEST_CATEGORY = "Category";
	private static final String SUGGEST_BRAND = "Brand";
	private static final String SUGGEST_ARTICLES = "Articles";
	private static final String SUGGEST_KEYWORD = "Keyword";
	private static final String SUGGEST_PRODUCT = "Product";
	private static final String SUGGEST_SELLER = "Seller";
	
	/** parameter name */
	public static final String PARAMETER_SEARCH_TYPE = "searchType";
	
	/** parameter name */
	public static final String PARAMETER_SEARCH_TYPE_DESCRIPTION = "The type of search contents to search for.";


	/** The string constant of resource path, "store/{storeId}/sitecontent/". */
	private static final String RESOURCE_PATH = STORE_STOREID_PATH + RESOURCE_NAME + "/";

	private static final String PARAMETER_LIMIT = "limit";

	private static final String PARAMETER_LIMIT_DESCRIPTION = "Limit.";

	public static final String PARAMETER_COUNT = "count";

	private static final String PARAMETER_COUNT_DESCRIPTION = "The number of suggested keywords to be returned. The default value is 4.";

	public static final String PARAMETER_TERMS_SORT = "termsSort";

	private static final String PARAMETER_TERMS_SORT_DESCRIPTION = "The sorting to be used in the returned result, \"count\" or \"index\". By default, it is \"count\".";

	private static final String PARAMETER_SUGGESTION_TYPE_DESCRIPTION = "The suggestion type. Accepted values are '"
			+ SUGGEST_CATEGORY
			+ "', '"
			+ SUGGEST_BRAND
			+ "', '"
			+ SUGGEST_SELLER
			+ "', '"
			+ SUGGEST_ARTICLES
			+ "', '"
			+ SUGGEST_KEYWORD
			+ "', and '"
			+ SUGGEST_PRODUCT + "'.";
	
	/**
	 * The string constant for "categorySuggestions".
	 */
	private static final String CATEGORY_SUGGESTIONS = "categorySuggestions";
	
	/**
	 * The string constant for "brandSuggestions".
	 */
	private static final String BRAND_SUGGESTIONS = "brandSuggestions";	/**
	 * The string constant for "brandSuggestions".
	 */
	private static final String SELLER_SUGGESTIONS = "sellerSuggestions";
	
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
	
		
	public static final String TERM = "term";
	
	/**
	 * Response format, e.g., json, xml.
	 */
	public static final String RESPONSE_FORMAT = "responseFormat";
	/**
	 * The response format value for JSON. Value is "json".
	 */
	public static final String FORMAT_JSON = "json";
	
	/**
	 * The default list of supported site content types.
	 */
	protected static List iSuggestionTypes = Arrays.asList(new String[] {
			SUGGEST_CATEGORY, SUGGEST_BRAND, SUGGEST_SELLER, SUGGEST_ARTICLES });
	
	/**
	 * The string constant for "keywordSuggestionsByTerm/{term}".
	 */
	private static final String KEYWORDS_SUGGESTIONS_BY_TERM = "keywordSuggestionsByTerm/{"
			+ TERM + "}";
	
	/**
	 * The string constant for "productSuggestions".
	 */
	private static final String PRODUCT_SUGGESTIONS_BY_SEARCH_TERM = "productSuggestionsBySearchTerm/{"
			+ PARAMETER_SEARCH_TERM + "}";
	
	

	/**
	 * Returns the category suggestions by specified a term.
	 * 
	 * @return The rest service response object containing an array of
	 *         <code>sitecontent</code> resource details.
	 */
	@RequestMapping(value = CATEGORY_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides suggestions with type-ahead for search result page.")
	@Parameters({
			@Parameter(name =  PARAMETER_LIMIT, required =  false, schema = @Schema(description =  PARAMETER_LIMIT_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_COUNT, required =  false, schema = @Schema(description =  PARAMETER_COUNT_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CONTRACT_ID, required =  false, schema = @Schema(description =  PARAMETER_CONTRACT_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_TERMS_SORT, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CATALOG_ID, required =  false, schema = @Schema(description =  PARAMETER_CATALOG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = CategorySuggestion.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION)})
	public ResponseEntity findCategorySuggestions(
			@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}

		ResponseEntity result = null;
		SearchCriteria searchCriteria = null;

		searchCriteria = prepareSearchCriteria(iStoreId, "", RESOURCE_NAME,
				RESOURCE_PATH + CATEGORY_SUGGESTIONS);
		
		if (null!=request.getParameter("term") && !request.getParameter("term").isEmpty()) {
			searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, request.getParameter("term"));
		}

		searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_SORT, "1");

		result = performSearch(searchCriteria);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Result : " + result);
			LOGGER.trace("EXIT");
		}
		return result;
	}

	/**
	 * Returns the keyword suggestions by specified a term.
	 * 
	 * @param term
	 *            the search term, this is mandatory parameter and cannot be
	 *            null or empty.
	 * @return The rest service response object containing an array of
	 *         <code>sitecontent</code> resource details.
	 */
	@RequestMapping(value = KEYWORDS_SUGGESTIONS_BY_TERM, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides keyword suggestions with type-ahead for search result page based on a term.")
	@Parameters({
			@Parameter(name =  PARAMETER_LIMIT, required =  false, schema = @Schema(description =  PARAMETER_LIMIT_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CONTRACT_ID, required =  false, schema = @Schema(description =  PARAMETER_CONTRACT_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_TERMS_SORT, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CATALOG_ID, required =  false, schema = @Schema(description =  PARAMETER_CATALOG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY), 
			@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = KeywordSuggestion.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findKeywordSuggestionsByTerm(@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@ApiParam(value = PARAMETER_TERM_DESCRIPTION, required = true) @PathVariable(TERM) String term) throws Exception {
		// store/{storeId}/sitecontent/keywordSuggestionsByTerm/{term}
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("term : "+term);
			LOGGER.trace("URL: "+getAbsoluteURL());
		}
		
		ResponseEntity result = null;
		SearchCriteria searchCriteria = null;
		
		ValueMappingService valueMappingService = ValueMappingService.getInstance();
		
		searchCriteria = prepareSearchCriteria(iStoreId, term,
				RESOURCE_NAME, RESOURCE_PATH + KEYWORDS_SUGGESTIONS_BY_TERM);
		
		term = SpecialCharacterHelper.ignoreQuoteInTerm(searchCriteria.
				getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM));
		
		if(term == null)
		{
			term = SpecialCharacterHelper.ignoreQuoteInTerm(searchCriteria.
					getControlParameterValue(TERM));
			searchCriteria.addControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, 
					searchCriteria.getControlParameterValue(TERM));
		}
		
		searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_ORIGINAL_SEARCH_TERM, term);
		term = SpecialCharacterHelper.getHandledString(term, true).toString();
		//Reassign search term after removing all quotes (if any)
		searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, term);
		
		Boolean searchBasedKeywordSuggestions = valueMappingService
				.getExtendedConfigurationPropertyValue(SearchExpressionConstants.SEARCH_BASED_KEYWORD_SUGGESTIONS_CONFIG_PARAMETER) != null ? Boolean
				.valueOf(valueMappingService
						.getExtendedConfigurationPropertyValue(SearchExpressionConstants.SEARCH_BASED_KEYWORD_SUGGESTIONS_CONFIG_PARAMETER))
				: false;
		
		if (term == null || term.length() == 0) {
			throw new SearchApplicationException(HttpStatus.BAD_REQUEST.value(), "CWXFR0228E", "term");
		} else {
			if (!searchBasedKeywordSuggestions) {
				// TODO - Search by using Term
			} else {
				result = performSearch(searchCriteria);
			}
		}
				
		LOGGER.trace("EXIT");
		return result;
	}
	
	@RequestMapping(value = SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides suggestions with type-ahead for search result page.")
	@Parameters({
			@Parameter(name =  PARAM_SUGGEST_TYPE, required =  false, schema = @Schema(description =  PARAMETER_SUGGESTION_TYPE_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  TERM, required =  false, schema = @Schema(description =  PARAMETER_TERM_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LIMIT, required =  false, schema = @Schema(description =  PARAMETER_LIMIT_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_COUNT, required =  false, schema = @Schema(description =  PARAMETER_COUNT_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CONTRACT_ID, required =  false, schema = @Schema(description =  PARAMETER_CONTRACT_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_TERMS_SORT, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CATALOG_ID, required =  false, schema = @Schema(description =  PARAMETER_CATALOG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = CommonSuggestions.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findSuggestions(
			@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId)
			throws Exception {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}
		ResponseEntity result = null;
		String[] arrSuggestTypes = request
				.getParameterValues(PARAM_SUGGEST_TYPE);
		List<String> suggestTypes = null;
		if (arrSuggestTypes == null || arrSuggestTypes.length == 0) {
			suggestTypes = new ArrayList<String>(iSuggestionTypes);
		} else {
			suggestTypes = Arrays.asList(arrSuggestTypes);
		}
		String responseFormat = request
				.getParameter(RESPONSE_FORMAT);
		if (responseFormat == null || responseFormat.length() == 0) {
			responseFormat = FORMAT_JSON;
		}

		
		for (int i = 0; i < suggestTypes.size(); i++) {
			String suggestType = suggestTypes.get(i);
			ResponseEntity subResult = null;
			if (suggestType.equalsIgnoreCase(SUGGEST_CATEGORY)) {
				subResult = findCategorySuggestions(iStoreId);
			} else if (suggestType.equalsIgnoreCase(SUGGEST_BRAND)) {
				subResult = findBrandSuggestions(iStoreId);
			} else if (suggestType.equalsIgnoreCase(SUGGEST_SELLER)) {
					subResult = findSellerSuggestions(iStoreId);				
			} else if (suggestType.equalsIgnoreCase(SUGGEST_ARTICLES)) {
				//subResult = findWebContentSuggestions();
			} else if (suggestType.equalsIgnoreCase(SUGGEST_KEYWORD)) {
				subResult = findKeywordSuggestionsByTerm(iStoreId, null);
			} else if (suggestType.equalsIgnoreCase(SUGGEST_PRODUCT)) {
				subResult = findProductSuggestionsBySearchTerm(iStoreId, null);
			} else {
				//subResult = findCustomSuggestions(request
						//.getParameter(TERM), suggestType);
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(CLASSNAME +" - findSuggestions - : - "+ INVALID_PARAMETER_VALUE +" - "+PARAM_SUGGEST_TYPE);
				}
				throw new SearchApplicationException(HttpStatus.BAD_REQUEST.value(), INVALID_PARAMETER_VALUE, PARAM_SUGGEST_TYPE,suggestType);
			}

			if (subResult != null) {
				result = mergeResponses(result, subResult, responseFormat);
			}
		}
		
		if(LOGGER.isTraceEnabled() && result != null) {
			LOGGER.trace(result.toString());
			LOGGER.trace("EXIT");
		}
		return result;
	}
	
	@RequestMapping(value = PRODUCT_SUGGESTIONS_BY_SEARCH_TERM, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides suggestions with type-ahead for search result page.")
	@Parameters({
		@Parameter(name =  PARAMETER_PAGE_SIZE, required =  false, schema = @Schema(description =  PARAMETER_PAGE_SIZE_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_PAGE_NUMBER, required =  false, schema = @Schema(description =  PARAMETER_PAGE_NUMBER_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_SEARCH_TYPE, required =  false, schema = @Schema(description =  PARAMETER_SEARCH_TYPE_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
		@Parameter(name =  TERM, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_CONTRACT_ID, required =  false, schema = @Schema(description =  PARAMETER_CONTRACT_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_TERMS_SORT, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_CATALOG_ID, required =  false, schema = @Schema(description =  PARAMETER_CATALOG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
		@Parameter(name =  PARAMETER_CHECK_ENTITLEMENT, required =  false, schema = @Schema(description =  PARAMETER_ENTITLEMENT_CHECK_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY), 
		@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = ProductSuggestion.class))),
		@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
		@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
		@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findProductSuggestionsBySearchTerm(@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@ApiParam(value = PARAMETER_SEARCH_TERM_DESCRIPTION, required = true) @PathVariable(PARAMETER_SEARCH_TERM) String searchTerm) throws Exception {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}
		ResponseEntity result = null;
		
		SearchCriteria searchCriteria = null;
		
		searchCriteria = prepareSearchCriteria(iStoreId, searchTerm,
				RESOURCE_NAME, RESOURCE_PATH
						+ PRODUCT_SUGGESTIONS_BY_SEARCH_TERM);
		//throwRestExceptionIfErrorsAreDetected();
		searchTerm = searchCriteria
				.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM);
		
		if(searchTerm == null)
		{
			searchTerm = searchCriteria
			.getControlParameterValue(TERM);
			searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, searchCriteria.getControlParameterValue(TERM));
		}
		
		if (searchTerm != null && searchTerm.equals("*")) {
            searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, "####");
        }
		
		if (searchTerm == null || searchTerm.length() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("term (keyword) is not valid: \'" + searchTerm);
			}
			throw new SearchApplicationException(HttpStatus.BAD_REQUEST.value(), "CWXFR0228E", "term");
		} else {
			result = performSearch(searchCriteria);
		}
		
		LOGGER.trace("EXIT");
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
			String resourceUri) throws Exception {

		final String METHODNAME = "prepareSearchCriteria(String,String,String,String)";
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("storeId : " + storeId + ",  searchTerm : " + searchTerm + ",  resourceName : " + resourceName
					+ ",  resourceUri : " + resourceUri);
		}
		SearchCriteria searchCriteria = initSearchCriteria(storeId, "", resourceName, resourceUri);
		checkUnboundConditions(searchCriteria);

		String searchTermQueryParameter = searchCriteria
				.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM);
		if (searchTermQueryParameter != null && searchTermQueryParameter.trim().length() > 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Using the search Term query parameter: " + searchTermQueryParameter
						+ " instade of the search term path parameter :" + searchTerm);
			}
			searchTerm = searchTermQueryParameter.trim();
		} else if (searchTerm != null) {
			searchTerm = GenericUtils.decodeString(searchTerm, request.getCharacterEncoding());
		}
		searchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, searchTerm);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("searchCriteria : " + searchCriteria);
			LOGGER.trace("EXIT");
		}
		return searchCriteria;
	}

	/**
	 * This method merges two REST response. It is done by taking the 2nd
	 * response and append to the end of the first response. A new response
	 * object will be created instead of modifying the given two response
	 * object.
	 * <p>
	 * 
	 * @param response1
	 *            The response object to be merged into.
	 * @param response2
	 *            The other response object to be extracted out of.
	 * @param responseFormat
	 *            The response format shortcut. Response format is used to
	 *            resolve the media type for the REST response. If response
	 *            format is not provided, then <code>Accept</code> header is
	 *            used to resolve the media type for the REST response.
	 * 
	 * @return The new merged REST response object.
	 */
	private ResponseEntity mergeResponses(ResponseEntity response1, ResponseEntity response2,
			String responseFormat) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("responseFormat: {},\n response1: {},\n response2: {}",responseFormat,response1,response2);
		}
		
		if (response1 == null) {
			// Just return the other response
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("response1 is null. Returning response2.");
			}
			LOGGER.trace("EXIT");
			return response2;
		} else if (response2 == null) {
			// Just return the other response
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("response2 is null. Returning response1.");
			}
			LOGGER.trace("EXIT");
			return response1;
		}
		
		Map<String, Object> responseData1 = (Map<String, Object>) response1
				.getBody();
		if (responseData1 != null) {
			// Try again to look up suggestview for solrJ responses.
			if (!responseData1
					.containsKey(SearchExpressionConstants.SUGGESTION_VIEW)) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("responseData1 does not contain suggestview. Return response2.");
				}
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("EXIT");
				}
				return response2;
			}
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("responseData1 is null. Return response2");
			}
			LOGGER.trace("EXIT");
			return response2;
		}
		
		Map<String, Object> responseData2 = (Map<String, Object>) response2
				.getBody();
		if (responseData2 != null) {
			// Try again to look up suggestview for solrJ responses.
			if (!responseData2
					.containsKey(SearchExpressionConstants.SUGGESTION_VIEW)) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("responseData2 does not contain suggestview. Return response1.");
				}
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("EXIT");
				}
				return response1;
			}
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("responseData2 is null. Return response1.");
			}
			LOGGER.trace("EXIT");
			return response1;
		}
		
		// Retrieve CatalogGroupView result set
		List<Map<String, Object>> suggestionView1 = (List<Map<String, Object>>) responseData1
				.get(SearchExpressionConstants.SUGGESTION_VIEW);
		List<Map<String, Object>> suggestionView2 = (List<Map<String, Object>>) responseData2
				.get(SearchExpressionConstants.SUGGESTION_VIEW);
		if (suggestionView1 == null || suggestionView1.size() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("suggestionView1 is null or empty. Return response2.");
			}
			LOGGER.trace("EXIT");
			return response2;
		}
		if (suggestionView2 == null || suggestionView2.size() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("suggestionView2 is null or empty. Return response1.");
			}
			LOGGER.trace("EXIT");
			return response1;
		}
		suggestionView1.addAll(suggestionView2);
		
		// Append the 2nd list to the end of the 1st one
		Map<String, Object> responseDataCombined = responseData1;

		int statusCode1 = response1.getStatusCodeValue();
		int statusCode2 = response2.getStatusCodeValue();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("The status code for the first response: "
							+ statusCode1);
			LOGGER.trace("The status code for the second response: "
							+ statusCode2);
		}

		int statusCodeCombined = HttpStatus.BAD_REQUEST.value();
		if (statusCode1 == HttpStatus.NOT_FOUND.value()
				&& statusCode2 == HttpStatus.NOT_FOUND.value()) {
			statusCodeCombined = HttpStatus.NOT_FOUND.value();
		} else if (statusCode1 == HttpStatus.OK.value()
				|| statusCode2 == HttpStatus.OK.value()) {
			statusCodeCombined = HttpStatus.OK.value();
		}

		MediaType outputFormat = MediaType.APPLICATION_JSON;;
		//ResponseBuilder builder = null;
		responseDataCombined.put(SearchExpressionConstants.RESOURCE_HANDLER, this);
		//builder = Response.status(statusCodeCombined).type(outputFormat)
			//	.entity(responseDataCombined);
		//builder = addCacheHeaders(builder);

		//addCookieUpdateHeader();
		//Response result = builder.build();
		ResponseEntity result = ResponseEntity.status(statusCodeCombined).contentType(outputFormat).body(responseDataCombined);
		
		LOGGER.trace("EXIT");
		return result;
	}
	
	/**
	 * Returns the category suggestions by specified a term.
	 * 
	 * @return The rest service response object containing an array of
	 *         <code>sitecontent</code> resource details.
	 */
	@RequestMapping(value = BRAND_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides suggestions with type-ahead for search result page.")
	@Parameters({
			@Parameter(name =  PARAMETER_LIMIT, required =  false, schema = @Schema(description =  PARAMETER_LIMIT_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CONTRACT_ID, required =  false, schema = @Schema(description =  PARAMETER_CONTRACT_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_TERMS_SORT, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CATALOG_ID, required =  false, schema = @Schema(description =  PARAMETER_CATALOG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = CategorySuggestion.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findBrandSuggestions(@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}

		ResponseEntity result = null;
		SearchCriteria searchCriteria = null;

		searchCriteria = prepareSearchCriteria(iStoreId, "", RESOURCE_NAME,
				RESOURCE_PATH + BRAND_SUGGESTIONS);
		
		searchCriteria.setControlParameterValue(
				SearchExpressionConstants.CTRL_PARAM_SEARCH_FACET_FIELD,
				SearchExpressionConstants.BRAND_INDEX_FIELD_NAME);

		result = performSearch(searchCriteria);
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Result : " + result);
			LOGGER.trace("EXIT");
		}
		return result;
	}
	
	/**
	 * Returns the seller suggestions by specified a term.
	 * 
	 * @return The rest service response object containing an array of
	 *         <code>sitecontent</code> resource details.
	 */
	@RequestMapping(value = SELLER_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides suggestions with type-ahead for search result page.")
	@Parameters({
			@Parameter(name =  PARAMETER_LIMIT, required =  false, schema = @Schema(description =  PARAMETER_LIMIT_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CONTRACT_ID, required =  false, schema = @Schema(description =  PARAMETER_CONTRACT_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_TERMS_SORT, required =  false, schema = @Schema(description =  PARAMETER_TERMS_SORT_DESCRIPTION, type =  DATATYPE_BOOLEAN), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_CATALOG_ID, required =  false, schema = @Schema(description =  PARAMETER_CATALOG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = CategorySuggestion.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findSellerSuggestions(@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}

		ResponseEntity result = null;
		SearchCriteria searchCriteria = null;
		String strLanguageId = request.getParameter(PARAMETER_LANG_ID);
        String marketplaceStore = StoreHelper.getStoreConfigurationPropertyValueByName(iStoreId, SearchExpressionConstants.STORECONFIG_HCL_MARKETPLACE_ENABLED,strLanguageId);

		searchCriteria = prepareSearchCriteria(iStoreId, "", RESOURCE_NAME,
				RESOURCE_PATH + SELLER_SUGGESTIONS);
		
		searchCriteria.setControlParameterValue(
				SearchExpressionConstants.CTRL_PARAM_SEARCH_FACET_FIELD,
				SearchExpressionConstants.SELLER_INDEX_FIELD_NAME);		
		// show seller Seller suggestions only when marketplace enables for store i.e. hcl.marketplace.enabled =true
		if("true".equalsIgnoreCase(marketplaceStore)) {
			result = performSearch(searchCriteria);

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
	@RequestMapping(value = WEBCONTENTS_SUGGESTIONS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Provides webContents for search result page.")
	@Parameters({
			@Parameter(name =  PARAMETER_SEARCH_TYPE, required =  false, schema = @Schema(description =  PARAMETER_SEARCH_TYPE_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = CategorySuggestion.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findWebContentsBySearchTerm(@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@ApiParam(value = PARAMETER_SEARCH_TERM_DESCRIPTION, required = true) @PathVariable(PARAMETER_SEARCH_TERM) String searchTerm)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}
		String strLanguageId = request.getParameter(PARAMETER_LANG_ID);
		String marketplaceStore = StoreHelper.getStoreConfigurationPropertyValueByName(iStoreId, SearchExpressionConstants.STORECONFIG_HCL_MARKETPLACE_ENABLED,strLanguageId);
		ResponseEntity result = null;
		ResponseEntity webContentResult = null;
		ResponseEntity unstructuredContentResult = null;
		String searchType = request.getParameter(PARAMETER_SEARCH_TYPE);
		
		if( searchType.equals("webcontent")) {
			result = getWebContentSearchResults(iStoreId, searchTerm);
		}
		else if( searchType.equals("attachments")) {
			result = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
		else {
			System.out.println("3333333333333");
			webContentResult = getWebContentSearchResults(iStoreId, searchTerm);
			unstructuredContentResult = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
				
		JSONArray webContentJsonArray = null;
		if (null != webContentResult && null != unstructuredContentResult) {
			String webContentJSONObjectString = new ObjectMapper().writeValueAsString(webContentResult.getBody());
			JSONObject webContentJsonObject = new JSONObject(webContentJSONObjectString);

			String attachmentContentJSONObjectString = new ObjectMapper()
					.writeValueAsString(unstructuredContentResult.getBody());
			JSONObject attachmentContentJsonObject = new JSONObject(attachmentContentJSONObjectString);

			for (String key : webContentJsonObject.keySet()) {
				if (key.equals("webContentView")) {
					webContentJsonArray = webContentJsonObject.getJSONArray(key);
				}
			}
			for (String key : attachmentContentJsonObject.keySet()) {
				if (key.equals("webContentView")) {
					JSONArray jsonArray = attachmentContentJsonObject.getJSONArray(key);
					System.out.println(jsonArray);
					for (int i = 0; i < webContentJsonArray.length(); i++) {
						jsonArray.put(webContentJsonArray.get(i));
					}
					webContentJsonObject.put("webContentView",jsonArray);
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
	@Operation(summary = "Provides webContents for search result page.")
	@Parameters({
			@Parameter(name =  PARAMETER_SEARCH_TYPE, required =  false, schema = @Schema(description =  PARAMETER_SEARCH_TYPE_DESCRIPTION, type =  DATATYPE_INTEGER), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_LANG_ID, required =  false, schema = @Schema(description =  PARAMETER_LANG_ID_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY),
			@Parameter(name =  PARAMETER_PROFILE_NAME, required =  false, schema = @Schema(description =  PARAMETER_PROFILE_NAME_DESCRIPTION, type =  DATATYPE_STRING), in =  ParameterIn.QUERY)})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION, content = @Content(schema = @Schema(implementation = CategorySuggestion.class))),
			@ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION),
			@ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION),
			@ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION) })
	public ResponseEntity findUnstructuredContentsBySearchTerm(@ApiParam(value = "The store ID.", required = true) @PathVariable("storeId") String iStoreId,
			@ApiParam(value = PARAMETER_SEARCH_TERM_DESCRIPTION, required = true) @PathVariable(PARAMETER_SEARCH_TERM) String searchTerm)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("URL: "+getAbsoluteURL());
		}
		String strLanguageId = request.getParameter(PARAMETER_LANG_ID);
		 String marketplaceStore = StoreHelper.getStoreConfigurationPropertyValueByName(iStoreId, SearchExpressionConstants.STORECONFIG_HCL_MARKETPLACE_ENABLED,strLanguageId);
		ResponseEntity result = null;
		ResponseEntity webContentResult = null;
		ResponseEntity unstructuredContentResult = null;
		String searchType = request.getParameter(PARAMETER_SEARCH_TYPE);		
		if(searchType.equals("webcontent")) {
			result = getWebContentSearchResults(iStoreId, searchTerm);
		}
		else if(searchType.equals("attachments")) {
			result = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
		else {
			webContentResult = getWebContentSearchResults(iStoreId, searchTerm);
			unstructuredContentResult = getUnsturcturedContentSearchResults(iStoreId, searchTerm);
		}
				
		JSONArray webContentJsonArray = null;
		if (null != webContentResult && null != unstructuredContentResult) {
			String webContentJSONObjectString = new ObjectMapper().writeValueAsString(webContentResult.getBody());
			JSONObject webContentJsonObject = new JSONObject(webContentJSONObjectString);

			String attachmentContentJSONObjectString = new ObjectMapper()
					.writeValueAsString(unstructuredContentResult.getBody());
			JSONObject attachmentContentJsonObject = new JSONObject(attachmentContentJSONObjectString);

			for (String key : webContentJsonObject.keySet()) {
				if (key.equals("webContentView")) {
					webContentJsonArray = webContentJsonObject.getJSONArray(key);
				}
			}
			for (String key : attachmentContentJsonObject.keySet()) {
				if (key.equals("webContentView")) {
					JSONArray jsonArray = attachmentContentJsonObject.getJSONArray(key);
					for (int i = 0; i < webContentJsonArray.length(); i++) {
						jsonArray.put(webContentJsonArray.get(i));
					}
					webContentJsonObject.put("webContentView",jsonArray);
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
	
	
	
	private ResponseEntity getWebContentSearchResults(String iStoreId, String searchTerm) throws Exception {
		ResponseEntity result = null;
		SearchCriteria webContentsearchCriteria = null;
		String webContentSearchTerm = searchTerm;
		webContentsearchCriteria = prepareSearchCriteria(iStoreId, searchTerm, RESOURCE_NAME,
					RESOURCE_PATH + WEBCONTENTS_SUGGESTIONS);
		webContentSearchTerm = webContentsearchCriteria.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM);
		if(webContentSearchTerm == null)
		{
			webContentSearchTerm = webContentsearchCriteria.getControlParameterValue(TERM);
			webContentsearchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, webContentsearchCriteria.getControlParameterValue(TERM));
		}
		if (webContentSearchTerm != null && webContentSearchTerm.equals("*")) {
			webContentsearchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, "####");
        }
		if (webContentSearchTerm == null || webContentSearchTerm.length() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("term (keyword) is not valid: \'" + webContentSearchTerm);
			}
			throw new SearchApplicationException(HttpStatus.BAD_REQUEST.value(), "CWXFR0228E", "term");
		} else {
			result = performSearch(webContentsearchCriteria);			
		}
		return result;
	}
	
	private ResponseEntity getUnsturcturedContentSearchResults(String iStoreId, String searchTerm) throws Exception {
		ResponseEntity result = null;
		SearchCriteria attachmentContentSearchCriteria = null;
		String attachmentContentSearchTerm = searchTerm;
		       
        attachmentContentSearchCriteria = prepareSearchCriteria(iStoreId, searchTerm, RESOURCE_NAME,
				RESOURCE_PATH + UNSTRUCTUREDCONTENTS_SUGGESTIONS);
        
        attachmentContentSearchTerm = attachmentContentSearchCriteria.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM);
				
		if(attachmentContentSearchTerm == null)
		{
			attachmentContentSearchTerm = attachmentContentSearchCriteria.getControlParameterValue(TERM);
			attachmentContentSearchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, attachmentContentSearchCriteria.getControlParameterValue(TERM));
		}		
		if (attachmentContentSearchTerm != null && attachmentContentSearchTerm.equals("*")) {
			attachmentContentSearchCriteria.setControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_TERM, "####");
        }
		if (attachmentContentSearchTerm == null || attachmentContentSearchTerm.length() == 0) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("term (keyword) is not valid: \'" + attachmentContentSearchTerm);
			}
			throw new SearchApplicationException(HttpStatus.BAD_REQUEST.value(), "CWXFR0228E", "term");
		} else {
			result = performSearch(attachmentContentSearchCriteria);			
		}
		return result;
	}
	
	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

}
