package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestData {

	@JsonProperty("source")
	public String source;

	@JsonProperty("took")
	public String took;

	@JsonProperty("hits")
	public int hits;

	@JsonProperty("suggestions")
	public List<Suggestions> suggestions = new ArrayList<SuggestData.Suggestions>();

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class Suggestions {
		@JsonProperty("keyword")
		public String keyword;

		@JsonProperty("info")
		public List<Info> info = new ArrayList<SuggestData.Info>();

		@JsonProperty("highlight")
		public String highlight;

		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Suggestions) {

				String keyword = null;

				if ((keyword = ((Suggestions) obj).keyword) != null
						&& keyword.equalsIgnoreCase(this.keyword)) {
					return true;
				}
			}
			return false;
		}

		public Integer countToSort() {
			
			if(info.size()==0){
				SuggestData.Info tmpInfo=new SuggestData.Info();
				tmpInfo.key="count";
				tmpInfo.value="0";
				info.add(tmpInfo);
			}

			for (Info info$ : info) {
				if (info$.key != null
						&& info$.key.trim().equalsIgnoreCase("count")) {

					try {
						return Integer.parseInt(info$.value.trim());
					} catch (Exception e) {
						return 0;
					}
				}
			}

			return 0;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class Info {
		@JsonProperty("key")
		public String key;
		@JsonProperty("value")
		public String value;

	}

}