package com.hcl.commerce.search.internal.expression.postprocessor;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.commerce.search.expression.SearchCriteria;
import com.hcl.commerce.search.expression.SearchExpressionConstants;
import com.hcl.commerce.search.expression.SearchResponse;
import com.hcl.commerce.search.internal.config.SearchConfigurationRegistry;
import com.hcl.commerce.search.internal.config.ValueMappingService;
import com.hcl.commerce.search.internal.expression.processor.SearchQueryPostprocessor;

public class SearchWebContentPostprocessor extends AbstractSearchQueryPostprocessor
		implements SearchQueryPostprocessor {

	private static final String CLASSNAME = SearchWebContentPostprocessor.class.getName();
	private static final Logger LOGGER = LoggerFactory.getLogger(CLASSNAME);
	private static final String URLMAPPER = "UnstructuredContentResponseFieldMapping";
	
	private static final String EXTERNAL_VALUE = "externalValue";
	private static final String INTERNAL_VALUE = "internalValue";

	/**
	 * Resets the implementation so it can be serially re-used.
	 * @return false if this object cannot be serially re-used.
	 */
	@Override
	public boolean reset() {
		return super.reset();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void invoke(SearchCriteria searchCriteria, Object... queryResponseObjects) throws Exception {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ENTRY");
			LOGGER.trace("SearchCriteria : " + searchCriteria);
			LOGGER.trace("HitResults : " + ((SearchResponse)queryResponseObjects[0]).getMatches());
		}
		super.invoke(searchCriteria, queryResponseObjects);
		Object results = iSearchResponseObject.getResponse().remove(SearchResponse.HITLIST);
		List<Map<String, Object>> contents = new LinkedList<Map<String, Object>>();
		
		if(results instanceof SearchHits) {
			SearchHit[] hits = ((SearchHits) results).getHits();
			ValueMappingService mapper = ValueMappingService.getInstance();
			SearchConfigurationRegistry registry = SearchConfigurationRegistry.getInstance();
			int hitLength = hits.length;
			for (int i = 0; i < hits.length; i++) {
				Map<String, Object> contentMap = new HashMap<>();
				SearchHit doc = hits[i];				
				for (String entryName: doc.getFields().keySet()) {
					String fieldname = null;
					fieldname = registry.getExternalFieldName(mapper, URLMAPPER, entryName);
					if(entryName.equals("attachment.content"))
						contentMap.put(fieldname, doc.getFields().get(entryName).getValue().toString().split("\n")[0]);
					else if(entryName.equals("fileABSPath")) {
						String fileABSPath = doc.getFields().get(entryName).getValue().toString().replace("__", "/");
						if(fileABSPath.contains("http")) {
							contentMap.put(fieldname, fileABSPath.substring(fileABSPath.indexOf("http"), fileABSPath.length()));
						}
						else {
							contentMap.put(fieldname, fileABSPath.substring(fileABSPath.lastIndexOf("/")+1, fileABSPath.length()));
						}
					}
					else
						contentMap.put(fieldname, doc.getFields().get(entryName).getValue());
					
					
				}
				contents.add(contentMap);
			}
		}
		
		String resourceName = searchCriteria
				.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE);
		
		Map matchesDocList = iSearchResponseObject.getMatches().get(SearchResponse.HITLIST);
		if (matchesDocList != null) {
			iSearchResponseObject.getResponse().put(SearchExpressionConstants.KEY_NAME_RECORD_SET_COMPLETE,matchesDocList.get(SearchExpressionConstants.KEY_NAME_RECORD_SET_COMPLETE));
			iSearchResponseObject.getResponse().put(SearchExpressionConstants.KEY_NAME_RECORD_SET_COUNT,matchesDocList.get(SearchExpressionConstants.KEY_NAME_RECORD_SET_COUNT));
			iSearchResponseObject.getResponse().put(SearchExpressionConstants.KEY_NAME_RECORD_SET_START_NUM,matchesDocList.get(SearchExpressionConstants.KEY_NAME_RECORD_SET_START_NUM));
			iSearchResponseObject.getResponse().put(SearchExpressionConstants.KEY_NAME_RECORD_SET_TOTAL,matchesDocList.get(SearchExpressionConstants.KEY_NAME_RECORD_SET_TOTAL));
			iSearchResponseObject.getResponse().put(SearchExpressionConstants.KEY_NAME_RESOURCE_ID,searchCriteria.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE_URL));
			iSearchResponseObject.getResponse().put(SearchExpressionConstants.KEY_NAME_RESOURCE_NAME,searchCriteria.getControlParameterValue(SearchExpressionConstants.CTRL_PARAM_SEARCH_INTERNAL_SERVICE_RESOURCE));
		}
		iSearchResponseObject.getResponse().put("webContentView",contents);
		LOGGER.trace("EXIT");
	}


	
}
