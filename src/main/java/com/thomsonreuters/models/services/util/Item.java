package com.thomsonreuters.models.services.util;

import java.util.Collection;

public class Item {
    private final String suggestibleText;
    private final String arbitraryData; // additional arbitrary data you want to store in the index
    /**
     * These are data you can use for additional filtering
     */
    private Collection<String> contexts = null;
    /*
     * order suggestion results. Higher weight are returned first.
     */
    private final int weight;


    public Item(String suggestibleText, String arbitraryData, Collection<String> contexts, int weight) {
        this.suggestibleText = suggestibleText;
        this.arbitraryData = arbitraryData;
        this.contexts = contexts;
        this.weight = weight;
    }

    public Item(String suggestibleText, String arbitraryData, int weight) {
        this.suggestibleText = suggestibleText;
        this.arbitraryData = arbitraryData;
        this.weight = weight;
    }


    String getSuggestibleText() {
        return(suggestibleText);
    }


    String getArbitraryData() {
        return(arbitraryData);
    }


    Collection<String> getContexts() {
        return(contexts);
    }


    int getWeight() {
        return(weight);
    }
}