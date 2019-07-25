package com.thomsonreuters.models.services.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.thomsonreuters.models.services.util.ItemIterator;
import com.thomsonreuters.models.services.util.Item;

public class JsonReader {
    public ItemIterator ReadJSON(String fn) throws Exception {
        // Init parser
        JSONParser parser = new JSONParser();

        // Init JSONArray
        JSONArray arr = new JSONArray();

        // Read file line by line
        BufferedReader br = new BufferedReader(new FileReader(fn));
        String line;
        while ((line = br.readLine()) != null) {
            // Parse into JSON object
            JSONObject obj = (JSONObject) parser.parse(line);

            // Add JSON object to JSON array
            arr.add(obj);
        }

        final List<Item> entities = new ArrayList<>();

        for (Object obj: arr) {
            // Get suggestibleText
            String suggestibleText = (String) ((JSONObject) obj).get("suggestibleText");

            // Get weight
            int weight = ((Long) ((JSONObject) obj).get("weight")).intValue();

            // Add name to entities
            entities.add(new Item(suggestibleText, "", weight));
        }

        return(new ItemIterator(entities.iterator()));
    }
}
