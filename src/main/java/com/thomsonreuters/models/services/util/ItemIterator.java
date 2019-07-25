package com.thomsonreuters.models.services.util;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

public class ItemIterator implements InputIterator {

    private final Iterator<Item> entityIterator;
    private Item currentItem;

    public ItemIterator(final Iterator<Item> entityIterator) {
        this.entityIterator = entityIterator;
    }

    @Override
    public boolean hasContexts() {
        return false;
    }

    @Override
    public boolean hasPayloads() {
        return true;
    }

    @Override
    public BytesRef next() {
        if (entityIterator.hasNext()) {
            currentItem = entityIterator.next();
            try {
                return new BytesRef(currentItem.getSuggestibleText().getBytes("UTF8"));
            } catch (final UnsupportedEncodingException e) {
                throw new Error("Couldn't convert to UTF-8");
            }
        } else { // returning null is fine for lucene...
            return null;
        }
    }

    @Override
    public BytesRef payload() { // returns null if no payload from Item
        try {
            return new BytesRef(currentItem.getArbitraryData().getBytes("UTF8"));
        } catch (final UnsupportedEncodingException e) {
            throw new Error("Could not convert to UTF-8");
        }
    }

    @Override
    public Set<BytesRef> contexts() { // returns null if no context from Item
//        try {
//            final Set<BytesRef> contexts = new HashSet<>();
//            for (final String context : currentItem.getContexts()) {
//                contexts.add(new BytesRef(context.getBytes("UTF8")));
//            }
//            return contexts;
//        } catch (final UnsupportedEncodingException e) {
//            throw new Error("Couldn't convert to UTF-8");
//        }
        return(null);
    }

    @Override
    public long weight() {
        return currentItem.getWeight();
    }
}