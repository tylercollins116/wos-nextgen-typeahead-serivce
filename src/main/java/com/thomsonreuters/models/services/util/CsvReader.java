package com.thomsonreuters.models.services.util;

import com.opencsv.*;
import org.apache.lucene.search.suggest.InputIterator;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    public InputIterator ReadCsv(String fn) throws Exception {
        // Create an object of filereader
        // class with CSV file as a parameter.
        FileReader filereader = new FileReader(fn);

        // create csvReader object passing
        // file reader as a parameter
        CSVReader csvReader = new CSVReader(filereader);
        String[] nextRecord;

        // Pull out headers first
        nextRecord = csvReader.readNext();

        // Initialize entities
        final List<Item> entities = new ArrayList<>();

        // Make text normalizer
        TextNormalizer tn = new TextNormalizer();

        // we are going to read data line by line
        while ((nextRecord = csvReader.readNext()) != null) {
            String orgName = tn.NormalizeText(nextRecord[0]);
            int docCount = Integer.parseInt(nextRecord[1]);

            // Get suggestibleText
            String suggestibleText = orgName;

            // Get weight
            int weight = docCount;

            // Add name to entities
            entities.add(new Item(suggestibleText, "", weight));
        }

        // Return item iterator
        return(new ItemIterator(entities.iterator()));
    }
}
