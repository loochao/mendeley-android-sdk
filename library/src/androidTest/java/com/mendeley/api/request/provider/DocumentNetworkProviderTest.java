package com.mendeley.api.request.provider;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.request.params.Order;
import com.mendeley.api.request.params.Sort;
import com.mendeley.api.request.params.View;
import com.mendeley.api.request.Request;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;

public class DocumentNetworkProviderTest  extends AndroidTestCase {
    private String documentsUrl;
    private final String documentId = "test-document_id";
	
	@Override
	protected void setUp() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String apiUrl = Request.API_URL;
		documentsUrl = apiUrl+"documents";
    }
	
	@SmallTest
	public void test_getDeleteDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String expectedUrl = documentsUrl+"/"+documentId;		
		String url = DocumentNetworkProvider.getDeleteDocumentUrl(documentId);
		
		assertEquals("Documents url is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getTrashDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		String expectedUrl = documentsUrl+"/"+documentId+"/trash";		
		String url = DocumentNetworkProvider.getTrashDocumentUrl(documentId);
		
		assertEquals("Post trash url is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String paramsString = "?view=client";
		String expectedUrl = documentsUrl+"/"+documentId+paramsString;

		String url = DocumentNetworkProvider.getGetDocumentUrl(documentId, View.CLIENT);
		
		assertEquals("Get document url with parameters is wrong", expectedUrl, url);
		
		expectedUrl = documentsUrl+"/"+documentId;
		url = DocumentNetworkProvider.getGetDocumentUrl(documentId, null);
		
		assertEquals("Get document url without parameters is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetDocumentsUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnsupportedEncodingException {
		View view = View.ALL;
		String groupId = "test-group_id";
		String modifiedSince = "2014-02-28T11:52:30.000Z";
		String deletedSince = "2014-01-21T11:52:30.000Z";
		int limit = 7;
		String marker = "12";
		boolean reverse = true;
		Order order = Order.DESC;
		Sort sort = Sort.MODIFIED;
		
		String paramsString = "?view=" + view +
				"&group_id=" + groupId + 
				"&modified_since=" + URLEncoder.encode(modifiedSince, "ISO-8859-1") + 
				"&limit=" + limit +
				"&reverse=" + reverse +
				"&order=" + order + 
				"&sort=" + sort +
                "&deleted_since=" + URLEncoder.encode(deletedSince, "ISO-8859-1");
                String expectedUrl = documentsUrl+paramsString;

		DocumentRequestParameters params = new DocumentRequestParameters();
		params.view = view;		
		params.groupId = groupId;
		params.modifiedSince = modifiedSince;
		params.limit = 7;
		params.reverse = true;
		params.order = order;
		params.sort = sort;
		
		String url = DocumentNetworkProvider.getGetDocumentsUrl(params, deletedSince);
		
		assertEquals("Get documents url with parameters is wrong", expectedUrl, url);
		
		expectedUrl = documentsUrl+"?view=" + view;
		params = new DocumentRequestParameters();
		params.view = View.ALL;
		url = DocumentNetworkProvider.getGetDocumentsUrl(params, null);
		
		assertEquals("Get documents url with parameters is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getPatchDocumentUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		String expectedUrl = documentsUrl+"/"+documentId;		
		String url = DocumentNetworkProvider.getPatchDocumentUrl(documentId);
		
		assertEquals("Patch document url is wrong", expectedUrl, url);
	}
}
