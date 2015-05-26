package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.File;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.NetworkUtils;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static com.mendeley.api.request.NetworkUtils.API_URL;
import static com.mendeley.api.request.NetworkUtils.getConnection;
import static com.mendeley.api.request.NetworkUtils.readInputStream;

public class PostFileNetworkRequest extends NetworkRequest<File> {
    private final String contentType;
    private final String documentId;
    private final String fileName;
    private final InputStream inputStream;

    private static String filesUrl = API_URL + "files";

    public PostFileNetworkRequest(String contentType, String documentId, String fileName,
                                  InputStream inputStream, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.contentType = contentType;
        this.documentId = documentId;
        this.fileName = fileName;
        this.inputStream = inputStream;
    }

    @Override
    protected int getExpectedResponse() {
        return 201;
    }

    @Override
    protected RequestResponse<File> doRun() throws MendeleyException {
        String link = "<"+ NetworkUtils.API_URL+"documents/"+documentId+">; rel=\"document\"";
        String contentDisposition = "attachment; filename*=UTF-8\'\'"+fileName;

        try {

            final int bufferSize = 65536;
            final byte[] buffer = new byte[bufferSize];

            con = getConnection(filesUrl, "POST", authTokenManager);
            con.setDoOutput(true);

            con.addRequestProperty("Content-Disposition", contentDisposition);
            con.addRequestProperty("Content-type", contentType);
            con.addRequestProperty("Link", link);
            con.setChunkedStreamingMode(0);

            con.connect();
            os = new DataOutputStream(con.getOutputStream());

            int r;
            while ((r =  inputStream.read(buffer, 0, bufferSize)) > 0) {
                os.write(buffer, 0, r);
            }

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            } else {
                is = con.getInputStream();
                return new RequestResponse<File>(JsonParser.parseFile(readInputStream(is)), serverDate);
            }
        } catch (ParseException pe) {
            throw new JsonParsingException("Could not post file" + filesUrl, pe);
        } catch (IOException e) {
            throw new MendeleyException("Could not post file" + filesUrl , e);
        } catch (JSONException e) {
            throw new JsonParsingException("Could not post file", e);
        } finally {
            closeConnection();
        }
    }
}
