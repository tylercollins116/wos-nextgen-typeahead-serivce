package com.thomsonreuters.models.services.suggesterOperation.models;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

public class EntryIterator implements InputIterator {
	private Iterator<Entry> organizationIterator;
	private Entry currentSuggest;

	public EntryIterator(Iterator<Entry> organizationEntry) {
		this.organizationIterator = organizationEntry;
	}

	public boolean hasContexts() {
		return false;
	}

	public boolean hasPayloads() {
		return true;
	}

	public BytesRef next() {
		if (organizationIterator.hasNext()) {
			currentSuggest = organizationIterator.next();
		 
			try {
				return new BytesRef(currentSuggest.getTerm().getBytes("UTF8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Couldn't convert to UTF-8", e);
			}
		} else {
			return null;
		}
	}

	public BytesRef payload() {
		try {
			return new BytesRef(currentSuggest.getJson().getBytes("UTF8"));
		} catch (IOException e) {
			throw new RuntimeException("Well that's unfortunate.");
		}
	}

	public long weight() {
		return currentSuggest.getWeight();
	}

	@Override
	public Set<BytesRef> contexts() {
		// TODO Auto-generated method stub
		return null;
	}

}