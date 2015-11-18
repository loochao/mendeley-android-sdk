package com.mendeley.api.request.endpoint;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.request.Request;
import com.mendeley.api.request.params.FileRequestParameters;
import com.mendeley.api.util.DateUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;

public class FilesEndpointTest extends AndroidTestCase {
	private FilesEndpoint provider;
    private String filesUrl;
    private final String fileId = "test-file_id";
	
	@Override
	protected void setUp() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        provider = new FilesEndpoint();
		
		String apiUrl = Request.MENDELEY_API_BASE_URL;
		
		filesUrl = apiUrl+"files";
    }
	
	@SmallTest
	public void test_getGetFilesUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnsupportedEncodingException, ParseException {
		
		String documentId = "test-document_id";
		String groupId = "test-group_id";
		Date addedSince = DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z");
		Date deletedSince = DateUtils.parseMendeleyApiTimestamp("2014-01-21T11:52:30.000Z");

		String paramsString = "?document_id=" + documentId +
				"&group_id=" + groupId +
				"&added_since=" +  URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(addedSince), "ISO-8859-1") +
				"&deleted_since=" + URLEncoder.encode(DateUtils.formatMendeleyApiTimestamp(deletedSince), "ISO-8859-1");
		
		Uri expectedUrl = Uri.parse(filesUrl+paramsString);

		FileRequestParameters params = new FileRequestParameters();
		params.documentId = documentId;		
		params.groupId = groupId;
		params.addedSince = addedSince;
		params.deletedSince = deletedSince;

		Uri url = FilesEndpoint.getGetFilesUrl(params);
		
		assertEquals("Get files url with parameters is wrong", expectedUrl, url);
		
		expectedUrl = Uri.parse(filesUrl);
		params = new FileRequestParameters();
		url = FilesEndpoint.getGetFilesUrl(params);
		
		assertEquals("Get files url without parameters is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getGetFileUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		String expectedUrl = filesUrl+"/"+fileId;		
		String url = provider.getGetFileUrl(fileId);
		
		assertEquals("Get file url is wrong", expectedUrl, url);
	}
	
	@SmallTest
	public void test_getDeleteFileUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		Uri expectedUrl = Uri.parse(filesUrl+"/"+fileId);
		Uri url = provider.getDeleteFileUrl(fileId);
		
		assertEquals("Delete file url is wrong", expectedUrl, url);
	}
}
