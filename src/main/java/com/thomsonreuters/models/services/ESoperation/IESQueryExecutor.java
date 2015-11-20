package com.thomsonreuters.models.services.ESoperation;

import com.thomsonreuters.models.SuggestData;

public interface IESQueryExecutor {
 
	public SuggestData formatResult(IQueryGenerator responseFormatter)
			throws Exception;

}
