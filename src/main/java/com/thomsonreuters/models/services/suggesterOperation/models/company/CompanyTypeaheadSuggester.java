package com.thomsonreuters.models.services.suggesterOperation.models.company;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.thomsonreuters.models.services.suggesterOperation.models.CompanyEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;

public class CompanyTypeaheadSuggester extends Lookup {

	public static final CharArraySet stopSet = new CharArraySet(
			CharArraySet.EMPTY_SET, false);
	private Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);

	private Analyzer queryAnalyzer = new StandardAnalyzer(
			CharArraySet.EMPTY_SET);

	private TRInfixSuggester suggester = null;

	public CompanyTypeaheadSuggester(InputStream is) {

		suggester = createCompanyTypeaheadSuggester(is);
	}

	/**
	 * 
	 * @param query
	 * @param num
	 * @param condition
	 *            values (1,2,3) 1: will return only prefix match 2: return
	 *            terms matched anywhere 3: first searched for prefix match
	 *            terms, if prefix max terms are not sufficient then other terms
	 *            are searched and merged into result
	 * @return
	 * @throws Exception
	 */

	public String lookup(String query, int num, int condition) throws Exception {

		/**
		 * 
		 * public List<LookupResult> lookup(CharSequence key, BooleanQuery
		 * contextQuery, int num, int condition, boolean allTermsRequired,
		 * boolean doHighlight) throws IOException {
		 */

		if (query == null) {
			return "{}";
		} else {
			query = query.toLowerCase();
		}

		List<LookupResult> results = suggester.lookup(query, null, num,
				condition, true, false);

		Set<Company> companyList = new HashSet<Company>();
		for (LookupResult r : results) {
			processToModel(r, suggester, companyList);
		}

		for (Company company : companyList) {
			add(company, companyList);
		}

		JSONObject suggestion = new JSONObject();

		List<JSONObject> finalOnj = new ArrayList<JSONObject>();

		for (Company company : companyList) {
			if (company.isRemoved) {
				continue;
			}

			if (company.hasParent) {
				if (company.getName().toLowerCase().startsWith("inventec")) {
					System.out.println("java");
				}
				Company parent = company.getPatent();
				parent.add(company);
				finalOnj.add(parent.createJson(query));
			} else {
				finalOnj.add(company.createJson(query));
			}
		}

		suggestion.put("suggestion", finalOnj);

		return (suggestion.toString());
	}

	private void add(Company company, Set<Company> orginalList) {
		if (!company.hasParent) {
			return;
		}

		for (Company company_1 : orginalList) {
			if (company.getName().equalsIgnoreCase(company_1.getName())) {
				continue;
			}

			if (company.getPatent().getName()
					.equalsIgnoreCase(company_1.getName())) {

				company_1.add(company);
				company.setRemoved(true);
				return;
			}

			add(company, company_1.getChildren());
		}

	}

	public JSONObject createJsonOfChildren(Company company,
			JSONObject suggestion) throws JSONException {

		suggestion.put("name", company.getName());

		return suggestion;
	}

	private void processToModel(LookupResult r, TRInfixSuggester suggester,
			Set<Company> companyList) throws JSONException {

		String json = new String(suggester.getReturn(
				new String(r.payload.bytes), Process.json));

		Map<String, String> suggestions = TRCompanyPrepareDictionary
				.processJson(json);

		Set<String> keys = suggestions.keySet();

		Company company = new Company();

		for (String key : keys) {

			String value = suggestions.get(key);

			/***
			 * if (key.equalsIgnoreCase("children") ||
			 * key.equalsIgnoreCase("parents")) { if
			 * (value.indexOf(TRSecEntry.DELIMETER) > 0) { suggestion.put(key,
			 * Arrays.asList(value .split(TRSecEntry.DELIMETER))); } else { if
			 * (value != null && value.length() > 0) { suggestion.put(key,
			 * Arrays .asList(new String[] { value })); } else {
			 * 
			 * suggestion.put(key, Arrays.asList(new String[] {}));
			 * 
			 * } } } else { if (key.equalsIgnoreCase("keyword")) { key = "id"; }
			 * if (key.equalsIgnoreCase("count")) { continue; }
			 * suggestion.put(key, value);
			 **/

			if (key.equalsIgnoreCase("children")) {
				if (value.indexOf(Entry.DELIMETER) > 0) {

					String[] childrens = value.split(Entry.DELIMETER);

					for (String children : childrens) {
						Company company_1 = new Company();
						company_1.setName(children);
						company.add(company_1);
					}
				} else {
					if (value != null && value.length() > 0) {
						Company company_1 = new Company();
						company_1.setName(value);
						company.add(company_1);
					}
				}
			} else if (key.equalsIgnoreCase("parents")) {
				if (value != null && value.length() > 0) {
					Company company_1 = new Company();
					company_1.setName(value);
					company.setPatent(company_1);
					company.setHasParent(true);
				}

			} else if (key.equalsIgnoreCase("count")) {
				if (value != null && value.length() > 0) {
					company.setCount(0);
				}

			} else if (key.equalsIgnoreCase("keyword")) {
				if (value != null && value.length() > 0) {
					company.setName(value);
				}
			} else if (key.equalsIgnoreCase("name")) {
				if (value != null && value.length() > 0) {
					company.setRealName(value);
				}
			}

		}
		companyList.add(company);

	}

	class Company {
		private boolean hasParent = false;
		private String name;
		private Set<Company> children = new HashSet<Company>();
		private Company patent = null;
		private int count;
		private boolean isRemoved = false;
		private String realName = null;

		public String getRealName() {
			return realName;
		}

		public void setRealName(String realName) {
			this.realName = realName;
		}

		public boolean isRemoved() {
			return isRemoved;
		}

		public void setRemoved(boolean isRemoved) {
			this.isRemoved = isRemoved;
		}

		public Company getPatent() {
			return patent;
		}

		public void setPatent(Company patent) {
			this.patent = patent;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isHasParent() {
			return hasParent;
		}

		public void setHasParent(boolean hasParent) {
			this.hasParent = hasParent;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public void add(Company company) {

			// remove old one and add new one

			Company remove = null;
			for (Company company2 : this.children) {
				if (company.getName().equalsIgnoreCase(company2.getName())) {
					remove = company2;
					Set<Company> children = company2.getChildren();
					for (Company child : children) {
						company.add(child);

					}

				}
			}

			if (remove != null) {
				this.children.remove(remove);
			}
			this.children.add(company);

		}

		public String getName() {
			return name;
		}

		public Set<Company> getChildren() {
			return children;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {

			return this.name;
		}

		private JSONObject createJson(String term) throws Exception {
			JSONObject jsonobj = new JSONObject();
			Set<Company> company = this.getChildren();
			term = term.toLowerCase();

			if (canInclude(this.getName(), term)) {
				jsonobj.put("name", this.getName());
			} else if (canInclude(this.getRealName(), term)) {
				jsonobj.put("name", this.getRealName());
			} else {
				jsonobj.put("name", this.getName());
			}
			jsonobj.put("count", "0");

			List<JSONObject> object = new ArrayList<JSONObject>();
			for (Company company_1 : company) {
				object.add(company_1.createJson(term));
			}
			jsonobj.put("children", object);
			return jsonobj;
		}
	}

	public boolean canInclude(String term, String subterm) {
		return (term != null && (term.toLowerCase().startsWith(subterm) || convertSpaceIntoUnderScore(
				term).indexOf("_" + subterm) > -1));
	}

	private String convertSpaceIntoUnderScore(String text) {
		StringBuilder sb = new StringBuilder();
		char[] chars = text.toLowerCase().toCharArray();
		for (char c : chars) {
			if (c == ' ') {
				sb.append('_');
				continue;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private TRInfixSuggester createCompanyTypeaheadSuggester(InputStream is) {
		TRInfixSuggester suggester = null;
		try {

			TRCompanyPrepareDictionary dictionary = new TRCompanyPrepareDictionary(
					is, new CompanyEntry());

			suggester = new TRInfixSuggester(new RAMDirectory(), indexAnalyzer);

			suggester.build(new TRCompanyEntryIterator(dictionary));

			dictionary.close();
			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	/*********************** This is nothing only to make it Lookup subclass ************/

	@Override
	public long ramBytesUsed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCount() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void build(InputIterator inputIterator) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<LookupResult> lookup(CharSequence key, Set<BytesRef> contexts,
			boolean onlyMorePopular, int num) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean store(DataOutput output) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean load(DataInput input) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
