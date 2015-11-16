package com.mendeley.api.request;

import com.mendeley.api.exceptions.MendeleyException;

import java.util.HashMap;
import java.util.Map;

public class IdentifierTypesRequestTest extends RequestTest {

    private final Map<String,String> expectedIdentifierTypes;

    public IdentifierTypesRequestTest() {
        super();
        expectedIdentifierTypes = new HashMap<String, String>();

        expectedIdentifierTypes.put("arxiv", "arXiv ID");
        expectedIdentifierTypes.put("doi", "DOI");
        expectedIdentifierTypes.put("isbn", "ISBN");
        expectedIdentifierTypes.put("issn", "ISSN");
        expectedIdentifierTypes.put("pmid", "PubMed Unique Identifier (PMID)");
        expectedIdentifierTypes.put("scopus", "Scopus identifier (EID)");

    }

    public void test_getIdentifierTypes_receivesCorrectIdentifierTypes() throws MendeleyException {

        Map<String, String> actualIdTypes = getSdk().getIdentifierTypes().run().resource;

        // we test that the API returns at least the identifiers that existed when writing this test
        for (String key : expectedIdentifierTypes.keySet()) {
            assertTrue(actualIdTypes.containsKey(key));
            assertEquals(expectedIdentifierTypes.get(key), actualIdTypes.get(key));
        }
    }

}
