package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.request.Request;
import com.mendeley.api.util.DateUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;

public class DocumentEndpointTest extends AndroidTestCase {
    private String documentsUrl;
    private final String documentId = "test-document_id";
	
	@Override
	protected void setUp() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String apiUrl = Request.MENDELEY_API_BASE_URL;
		documentsUrl = apiUrl+"documents";
    }
	
	@SmallTest
	public void test_getDeleteDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Uri expectedUrl = Uri.parse(documentsUrl + "/" + documentId);
		Uri url = DocumentEndpoint.getDeleteDocumentUrl(documentId);
		
		assertEquals("Documents url is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getTrashDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		Uri expectedUrl = Uri.parse(documentsUrl+"/"+documentId+"/trash");
		Uri url = DocumentEndpoint.getTrashDocumentUrl(documentId);
		
		assertEquals("Post trash url is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String paramsString = "?view=client";
		Uri expectedUrl = Uri.parse(documentsUrl+"/"+documentId+paramsString);

		Uri url = DocumentEndpoint.getGetDocumentUrl(documentId, DocumentEndpoint.DocumentRequestParameters.View.CLIENT);
		
		assertEquals("Get document url with parameters is wrong", expectedUrl, url);
		
		expectedUrl = Uri.parse(documentsUrl+"/"+documentId);
		url = DocumentEndpoint.getGetDocumentUrl(documentId, null);
		
		assertEquals("Get document url without parameters is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetDocumentsUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnsupportedEncodingException, ParseException {

		DocumentEndpoint.DocumentRequestParameters.View view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
		String groupId = "test-group_id";
		Date modifiedSince = DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z");
		Date deletedSince = DateUtils.parseMendeleyApiTimestamp("2014-01-21T11:52:30.000Z");
		int limit = 7;
		String marker = "12";
		boolean reverse = true;
		DocumentEndpoint.DocumentRequestParameters.Order order = DocumentEndpoint.DocumentRequestParameters.Order.DESC;
		DocumentEndpoint.DocumentRequestParameters.Sort sort = DocumentEndpoint.DocumentRequestParameters.Sort.MODIFIED;

		String paramsString = "?view=" + view +
				"&group_id=" + groupId +
				"&modified_since=" + URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(modifiedSince), "ISO-8859-1") +
				"&limit=" + limit +
				"&reverse=" + reverse +
				"&order=" + order +
				"&sort=" + sort +
                "&deleted_since=" + URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(deletedSince), "ISO-8859-1");
                Uri expectedUrl = Uri.parse(documentsUrl+paramsString);

		DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
		params.view = view;
		params.groupId = groupId;
		params.modifiedSince = modifiedSince;
		params.limit = 7;
		params.reverse = true;
		params.order = order;
		params.sort = sort;

		Uri url = DocumentEndpoint.getGetDocumentsUrl(params, deletedSince);

		assertEquals("Get documents url with parameters is wrong", expectedUrl, url);

		expectedUrl = Uri.parse(documentsUrl+"?view=" + view);
		params = new DocumentEndpoint.DocumentRequestParameters();
		params.view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
		url = DocumentEndpoint.getGetDocumentsUrl(params, null);

		assertEquals("Get documents url with parameters is wrong", expectedUrl, url);

	}
	
	@SmallTest
	public void test_getPatchDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		Uri expectedUrl = Uri.parse(documentsUrl+"/"+documentId);
		Uri url = DocumentEndpoint.getPatchDocumentUrl(documentId);
		
		assertEquals("Patch document url is wrong", expectedUrl, url);
	}
}
