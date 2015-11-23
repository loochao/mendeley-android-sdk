package com.mendeley.api.testUtils;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.BuildConfig;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.RequestsFactory;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.NetworkUtils;
import com.mendeley.api.request.endpoint.GroupsEndpoint;
import com.mendeley.api.util.DateUtils;

import junit.framework.Assert;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class for setting the test account for running the tests
 */
public class TestAccountSetupUtils{

    private final RequestsFactory requestFactory;
    private final AuthTokenManager authTokenManager;

    public TestAccountSetupUtils(AuthTokenManager authTokenManager, RequestsFactory requestFactory) {
        this.requestFactory = requestFactory;
        this.authTokenManager = authTokenManager;
    }

    /**
     * After calling this method the account should be clean (no docs, no groups, nothing...)
     */
    public void cleanAll() {
        cleanDocs();
        cleanFolders();
    }

    private void cleanDocs() {
        try {
            // delete non-trashed docs
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            for (Document doc: requestFactory.getDocuments().run().resource) {
                // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
                requestFactory.deleteDocument(doc.id).run();
            }

            // delete trashed docs
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            for (Document doc: requestFactory.getTrashedDocuments().run().resource) {
                // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
                requestFactory.deleteTrashedDocument(doc.id).run();
            }

            // ensure no documents at all (trashed or deleted)
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            Assert.assertEquals("Expected empty list of non trashed docs in server", 0, requestFactory.getDocuments().run().resource.size());
            Assert.assertEquals("Expected empty list of trashed docs in server", 0, requestFactory.getTrashedDocuments().run().resource.size());

        } catch (Exception e) {
            throw new TestAccountSetupException(e);
        }
    }

    private void cleanFolders() {
        try {
            // delete parent folders as sub folders will be deleted as well
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            for (Folder folder: requestFactory.getFolders().run().resource) {
                // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
                if (folder.parentId == null) {
                    requestFactory.deleteFolder(folder.id).run();
                }
            }

            Assert.assertEquals("Expected empty list of folders in server", 0, requestFactory.getFolders().run().resource.size());

        } catch (Exception e) {
            throw new TestAccountSetupException(e);
        }
    }

    public Document setupDocument(Document doc) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.postDocument(doc).run().resource;
    }

    public void trashDocument(String docId) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
       requestFactory.trashDocument(docId).run();
    }

    public Annotation setupAnnotation(Annotation annotation) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.postAnnotation(annotation).run().resource;
    }

    public File setupFile(String docId, String fileName, InputStream inputStream) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.postFileBinary("application/pdf", docId, inputStream, fileName).run().resource;
    }

    public Folder setupFolder(Folder folder) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.postFolder(folder).run().resource;
    }

    public List<Group> getGroups() throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.getGroups(new GroupsEndpoint.GroupRequestParameters()).run().resource;
    }

    public ReadPosition setupReadingPosition(String fileId, int page, int verticalPosition, Date date) throws Exception{
        HttpsURLConnection con = null;
        OutputStream os = null;
        BufferedWriter writer = null;

        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("file_id", fileId);
            jsonObject.put("page", page);
            jsonObject.put("vertical_position", verticalPosition);
            jsonObject.put("date", DateUtils.formatMendeleyApiTimestamp(date));


            final URL callUrl = new URL(BuildConfig.WEB_API_BASE_URL + "/" + "recently_read");

            con = (HttpsURLConnection) callUrl.openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-recently-read.1+json");

            con.connect();

            os = con.getOutputStream();

            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 && responseCode >= 300) {
                throw new Exception("Invalid response code posting recently read position: " + responseCode + " " + con.getResponseMessage());
            }

            final JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
            return JsonParser.parseReadPosition(reader);
        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (os != null) {
                    os.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public List<ReadPosition> getAllReadingPositions() throws Exception {
        HttpsURLConnection con = null;
        InputStream is = null;

        try {
            final URL callUrl = new URL(BuildConfig.WEB_API_BASE_URL + "/" + "recently_read");

            con = (HttpsURLConnection) callUrl.openConnection();
            con.setRequestMethod("GET");
            con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-recently-read.1+json");

            con.connect();

            is = con.getInputStream();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 && responseCode >= 300) {
                throw new Exception("Invalid response code getting recently read position: " + responseCode + " " + con.getResponseMessage());
            }

            final JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
            return JsonParser.parseReadPositionList(reader);
        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored) {}
        }
    }


    public String setupApplicationFeature(String name) throws Exception{
        HttpsURLConnection con = null;
        OutputStream os = null;
        BufferedWriter writer = null;

        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("availability_level", "EVERYONE");

            final URL callUrl = new URL("https://api-staging.mendeley.com/application_feature_mappings");

            con = (HttpsURLConnection) callUrl.openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-feature-mappings.1+json");

            con.connect();

            os = con.getOutputStream();

            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception("Invalid response code posting application features: " + responseCode + " " + con.getResponseMessage());
            }

            String responseString = NetworkUtils.readInputStream(con.getInputStream());
            JSONObject returnedJsonObject = new JSONObject(responseString);
            return returnedJsonObject.getString("id");

        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (os != null) {
                    os.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public void deleteApplicationFeatures(List<String> applicationFeatureIds) throws Exception{
        for (String applicationFeatureId : applicationFeatureIds) {
            deleteApplicationFeature(applicationFeatureId);
        }
    }

    private void deleteApplicationFeature(String applicationFeatureId) throws Exception {
        HttpsURLConnection con = null;

        try {

            final URL callUrl = new URL(BuildConfig.WEB_API_BASE_URL + "application_feature_mappings/" + applicationFeatureId);

            con = (HttpsURLConnection) callUrl.openConnection();
            con.setRequestMethod("DELETE");
            con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-feature-mappings.1+json");

            con.connect();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 && responseCode >= 300) {
                throw new Exception("Invalid response code deleting application features:  " + responseCode + " " + con.getResponseMessage());
            }

        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
            } catch (Exception ignored) {
            }
        }
    }


    /**
     * Exceptions that may happen when trying to set up the testing scenario
     */
    public static class TestAccountSetupException extends RuntimeException {
        public TestAccountSetupException(Exception cause) {
            super("A problem happened when trying to setup the testing scenario", cause);
        }
    }
}
