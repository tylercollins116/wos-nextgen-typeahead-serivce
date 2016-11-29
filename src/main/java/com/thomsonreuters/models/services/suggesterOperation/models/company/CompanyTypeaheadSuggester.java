package com.thomsonreuters.models.services.suggesterOperation.models.company;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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

		if (condition < 1 || condition > 3) {
			condition = 2;
		}

		if (query == null) {
			return "{}";
		} else {
			query = query.toLowerCase();
		}

		List<LookupResult> results = suggester.lookup(query, null, num,
				condition, true, false);

		List<Company> companyList = new ArrayList<Company>();
		for (LookupResult r : results) {
			processToModel(r, suggester, companyList);
		}

		List<Company> ultimateParentList = new ArrayList<Company>();
		for (Company company : companyList) {
			maintainNode(company, ultimateParentList);
		}

		final String query_ = query;
		Collections.sort(ultimateParentList, new Comparator<Company>() {

			@Override
			public int compare(Company o1, Company o2) {
				return ((Integer) o2.getCount(0, query_))
						.compareTo((Integer) o1.getCount(0, query_));
			}
		});

		List<JSONObject> finalOnj = new ArrayList<JSONObject>();

		for (Company company : ultimateParentList) {
			JSONObject json = null;

			if ((json = company.createJson(query)) != null) {
				finalOnj.add(json);

				if (finalOnj.size() == num) {
					break;
				}
			}
		}

		JSONObject suggestion = new JSONObject();

		suggestion.put("suggestion", finalOnj);

		// ObjectMapper mapper = new ObjectMapper();
		// Object json = mapper.readValue(suggestion.toString(), Object.class);
		// String indented =
		// mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
		// System.out.println(indented);

		return (suggestion.toString());
	}

	private void maintainNode(Company company, List<Company> finalList)
			throws Exception {
		if (!company.hasParent) {
			/**
			 * this will test whether the top level company is alread added or
			 * not.. if added just ignore it
			 **/
			for (Company company_1 : finalList) {
				if (company_1.getName().equalsIgnoreCase(company.getName())) {
					return;
				}
			}
			finalList.add(company);
			return;
		}

		Company ultimateParent = getUltimateParent(company);

		Company companyToExplore = null;
	 
			for (Company company_1 : finalList) {
				if (ultimateParent.getName().equalsIgnoreCase(
						company_1.getName())) {
					companyToExplore = company_1;
					break;
				}
			}

			if (companyToExplore != null) {
				addChildOnCorrespondingPosition(ultimateParent,
						companyToExplore);
			} else {
				finalList.add(ultimateParent);
			}
		  

	}

	private void addChildOnCorrespondingPosition(Company ultimateparent,
			Company parentCompany) {
		if (ultimateparent == null || ultimateparent.getChildren().size() == 0) {
			/**
			 * ultimateparent.getChildren().size()==0 means its a last node
			 * which is already included so no more need to add
			 **/
			return;
		}

		Company child = new ArrayList<Company>(ultimateparent.getChildren()
				.values()).get(0);

		Company parent = null;
		if ((parentCompany.getChildren().size() > 0)
				&& (parent = parentCompany.getChildren().get(child.getName())) != null) {
			addChildOnCorrespondingPosition(child, parent);
		} else {
			parentCompany.add(child);
			return;
		}

	}

	public Company getUltimateParent(Company company) throws Exception {
		Company parent = getParent(company.getPatent().getName());
		if (parent != null) {
			parent.add(company);

			if (parent.hasParent) {
				return getUltimateParent(parent);
			} else {
				return parent;
			}
		}
		return company;
	}

	private void processToModel(LookupResult r, TRInfixSuggester suggester,
			List<Company> companyList) throws JSONException {

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
					try {
						company.setCount(Integer.parseInt(value));
					} catch (Exception e) {
						company.setCount(0);
					}

				}

			} else if (key.equalsIgnoreCase("keyword")) {
				if (value != null && value.length() > 0) {
					company.setName(value);
				}
			} else if (key.equalsIgnoreCase("name")) {
				if (value != null && value.length() > 0) {
					company.setVariation(value);
				}
			} else if (key.equalsIgnoreCase("id")) {
				if (value != null && value.length() > 0) {
					company.setId(value);
				}
			}

		}
		companyList.add(company);

	}

	class Company {
		private boolean hasParent = false;
		private String name;
		private HashMap<String, Company> children = new HashMap<String, Company>();
		private Company patent = null;
		private int count;
		private String id="-";
		private String variation;
		private List<Company> sortedCompany = null; 

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Company getPatent() {
			return patent;
		}

		public String getVariation() {
			return variation;
		}

		public void setVariation(String variation) {
			this.variation = variation;
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
			for (Company company2 : this.children.values()) {
				if (company.getName().equalsIgnoreCase(company2.getName())) {
					remove = company2;
					Collection<Company> children = company2.getChildren()
							.values();
					for (Company child : children) {
						company.add(child);

					}

				}
			}

			if (remove != null) {
				this.children.remove(remove);
			}
			this.children.put(company.getName(), company);

		}

		public String getName() {
			if (this.name.indexOf(Entry.DELIMETER) > -1) {
				String pname = name.split(Entry.DELIMETER)[0];
				return pname;
			}
			return name;
		}

		public HashMap<String, Company> getChildren() {
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

			if (this.sortedCompany == null) {

				this.sortedCompany = new ArrayList<CompanyTypeaheadSuggester.Company>(
						this.children.values());
			}

			term = term.toLowerCase();

			if (canInclude(this.getName(), term)) {
				jsonobj.put("name", this.getName());
			} else if (canInclude(this.getVariation(), term)) {
				jsonobj.put("name", this.getVariation());
			} else {
				jsonobj.put("name", this.getName());
			}

			jsonobj.put("count", this.getCount());
			
			jsonobj.put("clusterId", this.getId());

			List<JSONObject> object = new ArrayList<JSONObject>();
			for (Company company_1 : this.sortedCompany) {
				JSONObject json = null;
				if ((json = company_1.createJson(term)) != null) {
					object.add(json);
				}
			}
			jsonobj.put("children", object);
			return jsonobj;
		}

		public int getCount(int count, String subterm) {

			if (canInclude(this.name, subterm)
					|| canInclude(this.variation, subterm)) {
				if (this.count > count) {
					count = this.count;
				}
			}

			if (this.children != null && this.children.size() <= 0) {
				return count;
			}

			this.sortedCompany = new ArrayList<CompanyTypeaheadSuggester.Company>(
					this.children.values());
			Collections.sort(this.sortedCompany, new Comparator<Company>() {

				@Override
				public int compare(Company o1, Company o2) {
					return ((Integer) o2.getCount(0, subterm))
							.compareTo((Integer) o1.getCount(0, subterm));
				}
			});

			for (Company company : this.sortedCompany) {
				count = company.getCount(count, subterm);
			}

			return count;
		}
	}

	public boolean canInclude(String term, String subterm) {

		StringBuilder sb = new StringBuilder();
		char[] chars = subterm.toCharArray();
		for (char c : chars) {
			if (c == ' ') {
				sb.append(c);
				continue;
			}
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				sb.append(c);
				continue;
			}
		}
		subterm = sb.toString();
		return (term != null && (term.toLowerCase().startsWith(subterm)
				|| (convertSpaceIntoUnderScore(term).indexOf("_" + subterm) > -1) || term
					.equalsIgnoreCase(subterm)));
	}

	private String convertSpaceIntoUnderScore(String text) {
		StringBuilder sb = new StringBuilder();
		char[] chars = text.toLowerCase().toCharArray();
		for (char c : chars) {
			if (c == ' ') {
				sb.append('_');
				continue;
			}
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				sb.append(c);
				continue;
			}
		}
		return sb.toString();
	}

	private Company getParent(String text) throws Exception {
		List<Company> companyList = new ArrayList<CompanyTypeaheadSuggester.Company>(
				1);
		List<LookupResult> results = suggester.lookForParent(text);

		if (results.size() > 0) {
			processToModel(results.get(0), suggester, companyList);
			if (companyList.size() > 0) {
				return companyList.get(0);
			}
		}
		return null;
	}

	private TRInfixSuggester createCompanyTypeaheadSuggester(InputStream is) {
		TRInfixSuggester suggester = null;
		try {

			TRCompanyPrepareDictionary dictionary = new TRCompanyPrepareDictionary(
					is, new CompanyEntry());
			
			Path p1 = Paths.get("D:\\TRTEST");

			 
			Directory indexDirectory = 
				      FSDirectory.open(p1);
			
			//suggester = new TRInfixSuggester(indexDirectory, indexAnalyzer);
			
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
