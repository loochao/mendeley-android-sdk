package com.mendeley.api.network.provider;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.callbacks.utils.GetImageCallback;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.NetworkUtils;
import com.mendeley.api.network.task.NetworkTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class UtilsNetworkProvider {

    private final Environment environment;
    private final AccessTokenProvider accessTokenProvider;

    public UtilsNetworkProvider(Environment environment, AccessTokenProvider accessTokenProvider) {
        this.environment = environment;
        this.accessTokenProvider = accessTokenProvider;
    }

    public void doGetImage(final String url, GetImageCallback callback) {
        final GetImageTask imageTask = new GetImageTask(callback);
        String[] params = new String[] {url};
        imageTask.executeOnExecutor(environment.getExecutor(), params);
    }

    private class GetImageTask extends NetworkTask {
        private final GetImageCallback callback;

        byte[] fileData;
        private GetImageTask(GetImageCallback callback) {
            this.callback = callback;
        }

        @Override
        protected int getExpectedResponse() {
            return 200;
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected MendeleyException doInBackground(String... params) {

            String url = params[0];

            try {
                fileData = getImage(url);
                return null;

            } catch (MendeleyException e) {
                return new MendeleyException(e.getMessage(), e);
            }
        }

        @Override
        protected void onCancelled (MendeleyException result) {
            fileData = null;
            super.onCancelled(result);
        }

        @Override
        protected void onSuccess() {
            callback.onImageReceived(fileData);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onImageNotReceived(exception);
        }
    }

    public byte[] getImage(String url) throws MendeleyException {
        ByteArrayOutputStream os = null;
        InputStream is = null;
        byte[] fileData;
        HttpURLConnection con = null;

        try {
            con = NetworkUtils.getHttpDownloadConnection(url, "GET");
            con.connect();

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
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

                return fileData;
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
