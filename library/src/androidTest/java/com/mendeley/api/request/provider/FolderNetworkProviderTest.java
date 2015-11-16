package com.mendeley.api.request.provider;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.request.params.FolderRequestParameters;
import com.mendeley.api.request.Request;

import java.lang.reflect.InvocationTargetException;

public class FolderNetworkProviderTest extends AndroidTestCase {
	private FolderNetworkProvider provider;
    private String foldersUrl;
    private final String folderId = "test-folder_id";
	
	@Override
	protected void setUp() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        provider = new FolderNetworkProvider();
		
		String apiUrl = Request.API_URL;
		
		foldersUrl = apiUrl+"folders";
    }
	
	@SmallTest
	public void test_getGetFoldersUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String groupId = "test-group_id";
				
		String paramsString = "?group_id=" + groupId;; 
		String expectedUrl = foldersUrl+paramsString;

		FolderRequestParameters params = new FolderRequestParameters();
		params.groupId = groupId;
		
		String url = FolderNetworkProvider.getGetFoldersUrl(params);
		
		assertEquals("Get folders url with parameters is wrong", expectedUrl, url);
		
		expectedUrl = foldersUrl;
		params = new FolderRequestParameters();
		url = FolderNetworkProvider.getGetFoldersUrl(params);
		
		assertEquals("Get folders url without parameters is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String expectedUrl = foldersUrl+"/"+folderId;
		String url = FolderNetworkProvider.getGetFolderUrl(folderId);

		assertEquals("Get folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getGetFolderDocumentIdsUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		String expectedUrl = foldersUrl+"/"+folderId+"/documents";		
		String url = FolderNetworkProvider.getGetFolderDocumentIdsUrl(folderId);
		
		assertEquals("Get folder document ids url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getPostDocumentToFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String expectedUrl = foldersUrl+"/"+folderId+"/documents";
		String url = FolderNetworkProvider.getPostDocumentToFolderUrl(folderId);

		assertEquals("Post document to folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getDeleteFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String expectedUrl = foldersUrl+"/"+folderId;
		String url = FolderNetworkProvider.getDeleteFolderUrl(folderId);

		assertEquals("Delete folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getDeleteDocumentFromFolderUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String documentId = "test-document_id";
		String expectedUrl = foldersUrl+"/"+folderId+"/documents/"+documentId;
		String url = FolderNetworkProvider.getDeleteDocumentFromFolderUrl(folderId, documentId);

		assertEquals("Delete document from folder url is wrong", expectedUrl, url);
	}

	@SmallTest
	public void test_getPatchFolderUrlUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String expectedUrl = foldersUrl + "/"+folderId;
		String url = FolderNetworkProvider.getPatchFolderUrl(folderId);

		assertEquals("Patch folder url is wrong", expectedUrl, url);
	}
}
