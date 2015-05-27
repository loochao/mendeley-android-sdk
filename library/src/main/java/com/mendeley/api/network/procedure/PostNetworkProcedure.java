package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.JsonParsingException;
import com.mendeley.api.exceptions.MendeleyException;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import static com.mendeley.api.network.NetworkUtils.getConnection;
import static com.mendeley.api.network.NetworkUtils.readInputStream;

public abstract class PostNetworkProcedure<ResultType> extends NetworkProcedure<ResultType> {
    private final String url;
    private final String contentType;
    private final String json;

    public PostNetworkProcedure(String url, String contentType, String json,
                                AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.url = url;
        this.contentType = contentType;
        this.json = json;
    }

    @Override
    protected int getExpectedResponse() {
        return 201;
    }

    @Override
    protected ResultType run() throws MendeleyException {
        try {
            con = getConnection(url, "POST", authenticationManager);
            con.addRequestProperty("Content-type", contentType);
            con.setFixedLengthStreamingMode(json.getBytes().length);
            con.connect();

            os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(json);
            writer.flush();
            writer.close();
            os.close();

            getResponseHeaders();

            final int responseCode = con.getResponseCode();
            if (responseCode != getExpectedResponse()) {
                throw HttpResponseException.create(con);
            } else {
                is = con.getInputStream();
                String responseString = readInputStream(is);
                return processJsonString(responseString);
            }
        } catch (ParseException pe) {
            throw new MendeleyException("Could not parse web API headers for " + url);
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
