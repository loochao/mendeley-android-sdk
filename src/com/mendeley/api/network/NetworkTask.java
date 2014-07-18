package com.mendeley.api.network;

import android.os.AsyncTask;

import com.mendeley.api.callbacks.RequestHandle;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.params.Page;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public abstract class NetworkTask extends AsyncTask<String, Integer, MendeleyException>
            implements RequestHandle {
    Page next = new Page();
    String location;

    InputStream is = null;
    OutputStream os = null;
    HttpsURLConnection con = null;

    protected abstract int getExpectedResponse();

    /**
     * Extracts the headers from the given HttpsURLConnection object.
     */
    protected void getResponseHeaders() throws IOException {
        Map<String, List<String>> headersMap = con.getHeaderFields();
        for (String key : headersMap.keySet()) {
            if (key != null) {
                switch (key) {
                    case "Date":
                    case "Vary":
                    case "Content-Type":
                    case "X-Mendeley-Trace-Id":
                    case "Connection":
                    case "Content-Length":
                    case "Content-Encoding":
                    case "Mendeley-Count":
                        // Unused
                        break;
                    case "Location":
                        location = headersMap.get(key).get(0);
                    case "Link":
                        List<String> links = headersMap.get(key);
                        String linkString = null;
                        for (String link : links) {
                            try {
                                linkString = link.substring(link.indexOf("<")+1, link.indexOf(">"));
                            } catch (IndexOutOfBoundsException e) {}
                            if (link.indexOf("next") != -1) {
                                next.link = linkString;
                            }
                            // "last" and "prev" links are not used
                        }
                        break;
                }
            }
        }
    }

    protected void closeConnection() {
        if (con != null) {
            con.disconnect();
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void onPostExecute(MendeleyException exception) {
        if (exception == null) {
            onSuccess();
        } else {
            onFailure(exception);
        }
    }

    protected void onProgressUpdate(Integer[] progress) {
        super.onProgressUpdate();
    }

    protected abstract void onSuccess();

    protected abstract void onFailure(MendeleyException exception);

    public void cancel() {
        // If the request is cancelled, we simply cancel the AsyncTask.
        cancel(true);
    }
}
