package com.thomsonreuters.models.services.ESoperation;

import com.thomsonreuters.models.services.util.ElasticEntityProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by finner on 09/02/17.
 */
public class ESEntryTest {

    private ElasticEntityProperties eep;
    private HashMap<String, String> sortFields;

    private static final int FROM = 0;
    private static final int SIZE = 50;


    @Before
    public void init() {
        eep = new ElasticEntityProperties();
        sortFields = new HashMap<String, String>();
        sortFields.put("recordInfo.recordName.sorting", "asc");
        sortFields.put("recordInfo.scoring", "desc");
        eep.setSortFields(sortFields);
    }


    @Test
    public void sortFieldsAreAddedOnce() {
        ESEntry ese = new ESEntry(eep, "userQuery", FROM, SIZE, "source", false);
        ese.addSortFields();
        Assert.assertEquals("There should only be 2 sort fields", 2, ese.sorts.size());
        Assert.assertEquals("Expecting {\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", "{\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", ese.sorts.get(0).toString());
        Assert.assertEquals("Expecting {\"recordInfo.scoring\": { \"order\": \"desc\" } }", "{\"recordInfo.scoring\": { \"order\": \"desc\" } }", ese.sorts.get(1).toString());

    }


    @Test
    public void existingSortFieldsAreNotDuplicated() {
        ESEntry ese = new ESEntry(eep, "userQuery", FROM, SIZE, "source", false);
        ese.addSortFields();
        ese.addSortFields();
        Assert.assertEquals("There should only be 2 sort fields", 2, ese.sorts.size());
        Assert.assertEquals("Expecting {\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", "{\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", ese.sorts.get(0).toString());
        Assert.assertEquals("Expecting {\"recordInfo.scoring\": { \"order\": \"desc\" } }", "{\"recordInfo.scoring\": { \"order\": \"desc\" } }", ese.sorts.get(1).toString());
    }

    @Test
    public void multipleAddDoesNotDuplicateExistingField() {
        ESEntry ese = new ESEntry(eep, "userQuery", FROM, SIZE, "source", false);
        ese.addSortFields();
        ese.addSortFields();
        ese.addSortFields();
        ese.addSortFields();
        Assert.assertEquals("There should only be 2 sort fields", 2, ese.sorts.size());
        Assert.assertEquals("Expecting {\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", "{\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", ese.sorts.get(0).toString());
        Assert.assertEquals("Expecting {\"recordInfo.scoring\": { \"order\": \"desc\" } }", "{\"recordInfo.scoring\": { \"order\": \"desc\" } }", ese.sorts.get(1).toString());
    }

    @Test
    public void onlyAddNonExitingKeys() {
        ESEntry ese = new ESEntry(eep, "userQuery", FROM, SIZE, "source", false);
        ese.addSortFields();
        ese.addSortFields();
        sortFields.put("recordInfo.dummy", "desc");
        ese.addSortFields();
        Assert.assertEquals("There should only be 3 sort fields", 3, ese.sorts.size());
        Assert.assertEquals("Expecting {\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", "{\"recordInfo.recordName.sorting\": { \"order\": \"asc\" } }", ese.sorts.get(0).toString());
        Assert.assertEquals("Expecting {\"recordInfo.scoring\": { \"order\": \"desc\" } }", "{\"recordInfo.scoring\": { \"order\": \"desc\" } }", ese.sorts.get(1).toString());
        Assert.assertEquals("Expecting {\"recordInfo.dummy\": { \"order\": \"desc\" } }", "{\"recordInfo.dummy\": { \"order\": \"desc\" } }", ese.sorts.get(2).toString());
    }

}
