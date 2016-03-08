package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuggestData {

	@JsonProperty("source")
	public String source;

	@JsonProperty("took")
	public String took;

	@JsonProperty("suggestions")
	public List<Suggestions> suggestions = new ArrayList<SuggestData.Suggestions>();

	public class Suggestions {
		@JsonProperty("keyword")
		public String keyword;

		@JsonProperty("info")
		public List<Info> info = new ArrayList<SuggestData.Info>();

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

		public Integer getCount() {

			for (Info info$ : info) {
				if (info$.key != null
						&& info$.key.trim().equalsIgnoreCase("count")) {

					try {
						return Integer.parseInt(info$.value.trim());
					} catch (Exception e) {
						info$.value = 0 + "";
						return 0;
					}
				}
			}

			return 0;
		}

	}

	public class Info {
		@JsonProperty("key")
		public String key;
		@JsonProperty("value")
		public String value;

	}

}