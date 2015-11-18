package com.mendeley.api.request.endpoint;

import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.request.SignedInTest;

import java.util.HashMap;
import java.util.Map;

public class DocumentIdentifiersRequestTest extends SignedInTest {

    private final Map<String,String> expectedIdentifierTypes;

    public DocumentIdentifiersRequestTest() {
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

        Map<String, String> actualIdTypes = getRequestFactory().getDocumentIdentifierTypes().run().resource;

        // we test that the API returns at least the identifiers that existed when writing this test
        for (String key : expectedIdentifierTypes.keySet()) {
            assertTrue(actualIdTypes.containsKey(key));
            assertEquals(expectedIdentifierTypes.get(key), actualIdTypes.get(key));
        }
    }

}
