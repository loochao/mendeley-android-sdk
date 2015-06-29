package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.File;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.NetworkUtils;

import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.API_URL;
import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

public class PostFileNetworkProcedure extends NetworkProcedure<File> {
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
    protected File run() throws MendeleyException {
        String link = "<"+ NetworkUtils.API_URL+"documents/"+documentId+">; rel=\"document\"";
        String contentDisposition = "attachment; filename*=UTF-8\'\'"+fileName;

        try {

            final int bufferSize = 65536;
            final byte[] buffer = new byte[bufferSize];

            con = getConnection(filesUrl, "POST", authenticationManager);
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
                return JsonParser.parseFile(readInputStream(is));
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
}
