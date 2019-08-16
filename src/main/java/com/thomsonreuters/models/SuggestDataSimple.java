package com.thomsonreuters.models;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestDataSimple {
    @JsonProperty("keywords")
    private List<String> keywords;

    public SuggestDataSimple(List<String> keywords) {
        this.keywords = new ArrayList<>(keywords);
    }
}
