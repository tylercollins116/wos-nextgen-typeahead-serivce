package com.thomsonreuters.models.services.suggesterOperation.models.company;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.models.services.suggesterOperation.models.Entry;

public class TRCompanyEntryIterator implements InputIterator {
	private Iterator<Entry> TRIterator;
	private Entry currentSuggest;
	private int count = 0;

	private static final Logger log = LoggerFactory
			.getLogger(InputIterator.class);

	private String type = "";

	public TRCompanyEntryIterator(Iterator<Entry> organizationEntry) {
		this.TRIterator = organizationEntry;
	}

	public TRCompanyEntryIterator(Iterator<Entry> organizationEntry, String type) {
		this.TRIterator = organizationEntry;
		if (type != null && type.trim().length() > 0) {
			this.type = type;
		}
	}

	public boolean hasContexts() {
		return false;
	}

	public boolean hasPayloads() {
		return true;
	}

	public BytesRef next() {
		if (TRIterator.hasNext()) {
			if (++count % 100000 == 0) {
				log.info(count + "\t"+type+ " documents  indexed");
			}
			currentSuggest = TRIterator.next();

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