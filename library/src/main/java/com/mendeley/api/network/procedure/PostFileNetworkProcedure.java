package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.API_URL;
import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

public abstract class PostFileNetworkProcedure<ResultType> extends NetworkProcedure<ResultType> {
    private final String contentType;
    private final String documentId;
    private final String fileName;
    private final InputStream inputStream;

    private static String filesUrl = API_URL + "files";


    public PostFileNetworkProcedure(String contentType, String documentId, String fileName,
                                    InputStream inputStream, AuthenticationManager authenticationManager) {
        super(authenticationManager);
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
    protected ResultType run() throws MendeleyException {
        String link = "<https://api.mendeley.com/documents/"+documentId+">; rel=\"document\"";
        String contentDisposition = "attachment; filename*=UTF-8\'\'"+fileName;

        try {

            int bytesAvailable;
            final int MAX_BUF_SIZE = 65536;
            int bufferSize;
            final byte[] buffer = new byte[MAX_BUF_SIZE];
            int bytesRead;

            con = getConnection(filesUrl, "POST", authenticationManager);
            con.addRequestProperty("Content-Disposition", contentDisposition);
            con.addRequestProperty("Content-type", contentType);
            con.addRequestProperty("Link", link);

            os = new DataOutputStream(con.getOutputStream());

            bytesAvailable = inputStream.available();
            bufferSize = Math.min(bytesAvailable, MAX_BUF_SIZE);
            bytesRead = inputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                os.write(buffer, 0, bufferSize);
                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, MAX_BUF_SIZE);
                bytesRead = inputStream.read(buffer, 0, bufferSize);
            }

            os.close();
            inputStream.close();
            con.connect();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            } else {
                is = con.getInputStream();
                String jsonString = readInputStream(is);
                is.close();

                return processJsonString(jsonString);
            }
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + filesUrl);
        } catch (IOException e) {
            throw new MendeleyException(e.getMessage());
        } catch (JSONException e) {
            throw new JsonParsingException(e.getMessage());
        } finally {
            closeConnection();
        }
    }

    protected abstract ResultType processJsonString(String jsonString) throws JSONException;
}
