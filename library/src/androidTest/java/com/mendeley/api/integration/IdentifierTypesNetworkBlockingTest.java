package com.mendeley.api.integration;

import android.test.AndroidTestCase;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Folder;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.sort;

public class IdentifierTypesNetworkBlockingTest extends AndroidTestCase {

    private final Map<String,String> expectedIdentifierTypes;


    private static final Comparator<Folder> FOLDER_COMPARATOR = new Comparator<Folder>() {
        @Override
        public int compare(Folder folder1, Folder folder2) {
            return folder1.name.compareTo(folder2.name);
        }
    };

    private BlockingSdk sdk;

    public IdentifierTypesNetworkBlockingTest() {
        super();
        expectedIdentifierTypes = new HashMap<String, String>();

        expectedIdentifierTypes.put("arxiv", "arXiv ID");
        expectedIdentifierTypes.put("doi", "DOI");
        expectedIdentifierTypes.put("isbn", "ISBN");
        expectedIdentifierTypes.put("issn", "ISSN");
        expectedIdentifierTypes.put("pmid", "PubMed Unique Identifier (PMID)");
        expectedIdentifierTypes.put("scopus", "Scopus identifier (EID)");

    }

    @Override
    protected void setUp() throws InterruptedException, SignInException {
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetIdentifierTypes() throws MendeleyException {

        Map<String, String> actualIdTypes = sdk.getIdentifierTypes();

        // we test that the API returns at least the identifiers that existed when writing this test
        for (String key : expectedIdentifierTypes.keySet()) {
            assertTrue(actualIdTypes.containsKey(key));
            assertEquals(expectedIdentifierTypes.get(key), actualIdTypes.get(key));
        }
    }

}
