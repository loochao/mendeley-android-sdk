package com.mendeley.api.testUtils;

import com.mendeley.api.BuildConfig;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.impl.AsyncMendeleySdk;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.NetworkUtils;
import com.mendeley.api.util.DateUtils;

import junit.framework.Assert;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
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

    private final AsyncMendeleySdk sdk;

    public TestAccountSetupUtils(AsyncMendeleySdk sdk) {
        this.sdk = sdk;
    }

    /**
     * After calling this method the account should be clean (no docs, no groups, nothing...)
     */
    public void cleanAll() {
        cleanDocs();
    }

    private void cleanDocs() {
        try {
            // delete non-trashed docs
            // FIXME: do not delegate into the sdk to this, because we are testing the sdk...
            for (Document doc: sdk.getDocuments().documents) {
                // FIXME: do not delegate into the sdk to this, because we are testing the sdk...
                sdk.deleteDocument(doc.id);
            }

            // delete trashed docs
            // FIXME: do not delegate into the sdk to this, because we are testing the sdk...
            for (Document doc: sdk.getTrashedDocuments().documents) {
                // FIXME: do not delegate into the sdk to this, because we are testing the sdk...
                sdk.deleteTrashedDocument(doc.id);
            }

            // ensure no documents at all (trashed or deleted)
            // FIXME: do not delegate into the sdk to this, because we are testing the sdk...
            Assert.assertEquals("Expected empty list of non trashed docs in server", 0, sdk.getDocuments().documents.size());
            Assert.assertEquals("Expected empty list of trashed docs in server", 0, sdk.getTrashedDocuments().documents.size());

        } catch (Exception e) {
            throw new TestAccountSetupException(e);
        }
    }

    public Document setupDocument(Document doc) throws MendeleyException {
        // FIXME: do not delegate into the sdk to this, because we are testing the sdk this should receive a JSON and post it using HTTP
        return sdk.postDocument(doc);
    }

    public File setupFile(String docId, String fileName, InputStream inputStream) throws MendeleyException {
        // FIXME: do not delegate into the sdk to this, because we are testing the sdk this should receive a JSON and post it using HTTP
        return sdk.postFile("application/pdf", docId, inputStream, fileName);
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
            con.addRequestProperty("Authorization", "Bearer " + sdk.getAccessToken());
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

            String responseString = NetworkUtils.readInputStream(con.getInputStream());

            return JsonParser.parseReadPosition(responseString);
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
            con.addRequestProperty("Authorization", "Bearer " + sdk.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-recently-read.1+json");

            con.connect();

            is = con.getInputStream();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 && responseCode >= 300) {
                throw new Exception("Invalid response code getting recently read position: " + responseCode + " " + con.getResponseMessage());
            }

            String responseString = NetworkUtils.readInputStream(con.getInputStream());

            return JsonParser.parseReadPositionList(responseString);
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


    /**
     * Exceptions that may happen when trying to set up the testing scenario
     */
    public static class TestAccountSetupException extends RuntimeException {
        public TestAccountSetupException(Exception cause) {
            super("A problem happened when trying to setup the testing scenario", cause);
        }
    }
}
