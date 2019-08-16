package com.thomsonreuters.models.services.util;

public class TextNormalizer {
    public String NormalizeText(String text) {
        // Initial assign
        String normalizedText = text;

        // Lower case
        normalizedText = normalizedText.toLowerCase();

        // Return normalized text
        return(normalizedText);
    }
}
