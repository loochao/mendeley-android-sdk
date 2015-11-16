package com.mendeley.api.request;

import java.util.Map;
import java.util.Set;

public class DocumentTypesRequestTest extends RequestTest {

    public void test_getDocumentTypes_receivesCorrectDocumentTypes() throws Exception {
        final Map<String, String> types = getSdk().getDocumentTypes().run().resource;

        Set<String> keys = types.keySet();
        assertTrue("document types must contain journal", keys.contains("journal"));
        assertTrue("document types must contain book", keys.contains("book"));
        //...
    }

}
