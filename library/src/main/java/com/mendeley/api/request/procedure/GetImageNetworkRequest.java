package com.mendeley.api.request.procedure;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.RequestResponse;
import com.mendeley.api.request.NetworkUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class GetImageNetworkRequest extends NetworkRequest<byte[]> {

    private final String url;

    public GetImageNetworkRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
        super(authTokenManager, clientCredentials);
        this.url = url;
    }

    @Override
    protected int getExpectedResponse() {
        return 200;
    }

    @Override
    protected RequestResponse<byte[]> doRun() throws MendeleyException {

        ByteArrayOutputStream os = null;
        InputStream is = null;
        byte[] fileData;
        HttpURLConnection con = null;

        try {
            con = NetworkUtils.getHttpDownloadConnection(url, "GET");
            con.connect();

            int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            } else {

                is = con.getInputStream();
                os = new ByteArrayOutputStream();

                byte data[] = new byte[256];
                int count;
                while ((count = is.read(data)) != -1) {
                    os.write(data, 0, count);
                }

                fileData = os.toByteArray();

                return new RequestResponse(fileData, serverDate);
            }
        } catch (IOException e) {
            throw new MendeleyException("Error downloading image: " + url, e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
            if (os != null) {
                try {
                    os.close();
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

}