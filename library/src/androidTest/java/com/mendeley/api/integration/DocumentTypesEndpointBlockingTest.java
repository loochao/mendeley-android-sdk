package com.mendeley.api.integration;

import java.util.Map;
import java.util.Set;

public class DocumentTypesEndpointBlockingTest extends EndpointBlockingTest {

    public void testGetDocumentTypes() throws Exception {
        Map<String, String> types = getSdk().getDocumentTypes();

        Set<String> keys = types.keySet();
        assertTrue("document types must contain journal", keys.contains("journal"));
        assertTrue("document types must contain book", keys.contains("book"));
        //...
    }

}
