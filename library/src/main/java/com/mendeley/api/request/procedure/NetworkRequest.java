package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.request.params.Page;
import com.mendeley.api.util.DateUtils;
import com.mendeley.api.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Base class for all synchronous network calls.
 */
public abstract class NetworkRequest<ResultType> extends Request<ResultType> {
    protected Page next;
    protected String location;
    protected Date serverDate;

    protected InputStream is = null;
    protected OutputStream os = null;
    protected HttpsURLConnection con = null;

    public NetworkRequest(AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
    }

    /**
     * Extracts the headers from the given HttpsURLConnection object.
     */
    protected void getResponseHeaders() throws IOException, ParseException {
        Map<String, List<String>> headersMap = con.getHeaderFields();
        if (headersMap == null) {
            // No headers implies an error, which should be handled based on the HTTP status code;
            // no need to throw another error here.
            return;
        }
        for (String key : headersMap.keySet()) {
            if (key != null) {
                if (key.equals("Date")) {
                    final String dateHeader = headersMap.get(key).get(0);
                    serverDate = DateUtils.parseDateInHeader(dateHeader);

                } else if (key.equals("Vary") || key.equals("Content-Type") || key.equals("X-Mendeley-Trace-Id") || key.equals("Connection") || key.equals("Content-Length") || key.equals("Content-Encoding") || key.equals("Mendeley-Count")) {// Unused

                } else if (key.equals("Location")) {
                    location = headersMap.get(key).get(0);

                    List<String> links = headersMap.get(key);
                    String linkString = null;
                    for (String link : links) {
                        try {
                            linkString = link.substring(link.indexOf("<") + 1, link.indexOf(">"));
                        } catch (IndexOutOfBoundsException e) {
                        }
                        if (link.indexOf("next") != -1) {
                            next = new Page(linkString);
                        }
                        // "last" and "prev" links are not used
                    }

                } else if (key.equals("Link")) {
                    List<String> links = headersMap.get(key);
                    String linkString = null;
                    for (String link : links) {
                        try {
                            linkString = link.substring(link.indexOf("<") + 1, link.indexOf(">"));
                        } catch (IndexOutOfBoundsException e) {
                        }
                        if (link.indexOf("next") != -1) {
                            next = new Page(linkString);
                        }
                        // "last" and "prev" links are not used
                    }

                }
            }
        }
    }

    protected void closeConnection() {
        if (con != null) {
            con.disconnect();
        }
        Utils.closeQuietly(is);
        Utils.closeQuietly(os);
    }

}
