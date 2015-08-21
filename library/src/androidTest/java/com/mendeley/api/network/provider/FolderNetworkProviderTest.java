package com.mendeley.api.network.provider;

import android.os.AsyncTask;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.NetworkUtils;
import com.mendeley.api.params.FolderRequestParameters;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;

public class FolderNetworkProviderTest extends AndroidTestCase {
	private FolderNetworkProvider provider;
    private String foldersUrl;
    private final String folderId = "test-folder_id";
	
	@Override
	protected void setUp() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Environment environment = new Environment() {
            @Override
            public Executor getExecutor() {
                return AsyncTask.SERIAL_EXECUTOR;
            }
        };
        final AccessTokenProvider accessTokenProvider = new AccessTokenProvider() {
            @Override
            public String getAccessToken() {
                return null;
            }
        };
        provider = new FolderNetworkProvider(environment, accessTokenProvider);
		
		String apiUrl = NetworkUtils.API_URL;
		
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
