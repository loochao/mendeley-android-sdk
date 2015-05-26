package com.mendeley.api.request;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.model.Document;
import com.mendeley.api.testUtils.InMemoryAuthTokenManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static com.mendeley.api.request.NetworkUtils.getConnection;
import static com.mendeley.api.request.NetworkUtils.readInputStream;

public class NetworkConnectionTest extends AndroidTestCase {

	static String testDocumentId = "test_id";
	
	HttpsURLConnection getConnection;
	HttpsURLConnection postConnection;
	HttpsURLConnection deleteConnection;
	NetworkUtils.HttpPatch httpPatch;
    AuthTokenManager accessTokenManager;
	
	@Override
	protected void setUp()
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		createConnections() ;
    }
	

	public static Document getTestDocument() {
		return new Document.Builder()
                .setTitle("test_document_title")
                .setType("book")
                .setId("test_document_id").build();
	}

	private void createConnections()
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		
		String testUrl = "https://httpbin.org/";
		String getTestUrl = testUrl+"get";
		String postTestUrl =  testUrl+"post";
		String deleteTestUrl = testUrl+"delete";
		String patchTestUrl = testUrl+"patch";
		String date = "";

        accessTokenManager = new InMemoryAuthTokenManager();

        getConnection = getHttpsURLConnection(getTestUrl, "GET", false);
		postConnection = getHttpsURLConnection(postTestUrl, "POST", true);
		deleteConnection = getHttpsURLConnection(deleteTestUrl, "DELETE", false);
		httpPatch = getHttpPatch(patchTestUrl, date);

	}
	
	private HttpsURLConnection getHttpsURLConnection(String testUrl, String endPoint, boolean doOutput)
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		HttpsURLConnection con = getConnection(testUrl, endPoint, accessTokenManager);
		con.setDoOutput(doOutput);
		con.addRequestProperty("Content-type", "application/vnd.mendeley-document.1+json");
		
		return con;
	}
	
	private NetworkUtils.HttpPatch getHttpPatch(String url, String date) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return NetworkUtils.getHttpPatch(url, date, null, accessTokenManager);
	}
	
	public void testPreconditions() {
		assertNotNull("HttpsURLConnection object getConnection  is null", getConnection);
		assertNotNull("HttpsURLConnection object postConnection  is null", postConnection);
		assertNotNull("HttpsURLConnection object deleteConnection  is null", deleteConnection);
		assertNotNull("HttpPatch object httpPacth is null", httpPatch);
    }	
	
	
	@SmallTest
	public void test_getResponse() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {
        String testProprty = "Test-Proprty";
        String testValue = "testValue";

        getConnection.addRequestProperty(testProprty, testValue);
        getConnection.connect();

        InputStream is = getConnection.getInputStream();

        String responseString = readInputStream(is);

        JSONObject reponseJson = new JSONObject(responseString);
        JSONObject headersJson = reponseJson.getJSONObject("headers");

        String responseValue = headersJson.getString(testProprty);

        assertEquals("Request propery with wrong value", testValue, responseValue);
        assertEquals("Response code != 200", 200, getConnection.getResponseCode());
	}

	@SmallTest
	public void test_postResponse() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {
		
		JsonParser parser = new JsonParser();
		Document testDocument = getTestDocument();
		
		String testDocumentString = parser.jsonFromDocument(testDocument);

		postConnection.connect();		
		OutputStream os = postConnection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(
		        new OutputStreamWriter(os, "UTF-8"));
		writer.write(testDocumentString);
		writer.flush();
		writer.close();
		os.close();

		InputStream is = postConnection.getInputStream();
		String responseString = readInputStream(is);
		is.close();
		
		JSONObject reponseJson = new JSONObject(responseString);
		JSONObject jsonObject = reponseJson.getJSONObject("json");
		
		Document responseDocument = parser.parseDocument(jsonObject.toString());
		
		assertEquals("Response code != 200", 200, postConnection.getResponseCode());
		assertEquals("Posted and returned documents are not equal", testDocument, responseDocument);
	}
	
	@SmallTest
	public void test_deleteResponse() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {
		
		deleteConnection.connect();
		
		assertEquals("Response code != 200", 200, deleteConnection.getResponseCode());
	}
	
	@SmallTest
	public void test_patchResponse() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {

    	JsonParser parser = new JsonParser();
		Document testDocument = getTestDocument();
		
		String testDocumentString = parser.jsonFromDocument(testDocument);

    	httpPatch.setEntity(new StringEntity(testDocumentString));

    	HttpClient httpclient = new DefaultHttpClient();
    	HttpResponse response = httpclient.execute(httpPatch);

    	int responseCode = response.getStatusLine().getStatusCode();	
    	
    	ArrayList<Object> values = new ArrayList<Object>();		
		values.add(deleteConnection);
    	InputStream is = response.getEntity().getContent();
		
		String responseString = readInputStream(is);

		JSONObject reponseJson = new JSONObject(responseString);
		JSONObject jsonObject = reponseJson.getJSONObject("json");
		
		Document responseDocument = parser.parseDocument(jsonObject.toString());
		
		assertEquals("Response code != 200", 200, responseCode);
		assertEquals("Posted and returned documents are not equal", testDocument, responseDocument);
	}
	
	
}
