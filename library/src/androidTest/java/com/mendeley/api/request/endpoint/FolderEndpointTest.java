package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.request.Request;

import java.lang.reflect.InvocationTargetException;

public class FolderEndpointTest extends AndroidTestCase {
	private FolderEndpoint provider;
    private String foldersUrl;
    private final String folderId = "test-folder_id";
	
	@Override
	protected void setUp() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        provider = new FolderEndpoint();
		
		String apiUrl = Request.MENDELEY_API_BASE_URL;
		
		foldersUrl = apiUrl+"folders";
    }
	
	@SmallTest
	public void test_getGetFoldersUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String groupId = "test-group_id";
				
		String paramsString = "?group_id=" + groupId;; 
		Uri expectedUrl = Uri.parse(foldersUrl+paramsString);

		FolderEndpoint.FolderRequestParameters params = new FolderEndpoint.FolderRequestParameters();
		params.groupId = groupId;
		
		Uri url = FolderEndpoint.getGetFoldersUrl(params);
		
		assertEquals("Get folders url with parameters is wrong", expectedUrl, url);
		
		expectedUrl = Uri.parse(foldersUrl);
		params = new FolderEndpoint.FolderRequestParameters();
		url = FolderEndpoint.getGetFoldersUrl(params);
		
		assertEquals("Get folders url without parameters is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Uri expectedUrl = Uri.parse(foldersUrl+"/"+folderId);
		Uri url = FolderEndpoint.getGetFolderUrl(folderId);

		assertEquals("Get folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getGetFolderDocumentIdsUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		String expectedUrl = foldersUrl+"/"+folderId+"/documents";		
		String url = FolderEndpoint.getGetFolderDocumentIdsUrl(folderId);
		
		assertEquals("Get folder document ids url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getPostDocumentToFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Uri expectedUrl = Uri.parse(foldersUrl+"/"+folderId+"/documents");
		Uri url = FolderEndpoint.getPostDocumentToFolderUrl(folderId);

		assertEquals("Post document to folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getDeleteFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Uri expectedUrl = Uri.parse(foldersUrl+"/"+folderId);
		Uri url = FolderEndpoint.getDeleteFolderUrl(folderId);

		assertEquals("Delete folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getDeleteDocumentFromFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String documentId = "test-document_id";
		Uri expectedUrl = Uri.parse(foldersUrl+"/"+folderId+"/documents/"+documentId);
		Uri url = FolderEndpoint.getDeleteDocumentFromFolderUrl(folderId, documentId);

		assertEquals("Delete document from folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getPatchFolderUrlUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Uri expectedUrl = Uri.parse(foldersUrl + "/"+folderId);
		Uri url = FolderEndpoint.getPatchFolderUrl(folderId);

		assertEquals("Patch folder url is wrong", expectedUrl, url);
	}
}
