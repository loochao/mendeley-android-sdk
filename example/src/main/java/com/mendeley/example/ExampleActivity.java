package com.mendeley.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Mendeley;
import com.mendeley.sdk.RequestsFactory;
import com.mendeley.sdk.example.R;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.request.Request;
import com.mendeley.sdk.request.endpoint.DocumentEndpoint;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ExampleActivity extends Activity implements View.OnClickListener, Mendeley.SignInCallback {

    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_PROJECT_ID = "example_app_project_id";
    private static final String KEY_CLIENT_SECRET = "example_app_client_secret";
    private static final String KEY_CLIENT_REDIRECT_URI = "example_app_client_redirect_url";
    private RequestsFactory requestFactory;


    enum SignInStatus { SIGNED_OUT, SIGNING_IN, SIGNED_IN }
    private SignInStatus signInStatus = SignInStatus.SIGNED_OUT;
	
    private Button getDocumentsButton;
	private TextView outputView;
	
	private StringBuilder outputText = new StringBuilder();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getDocumentsButton = (Button) findViewById(R.id.getDocumentsButton);
        getDocumentsButton.setOnClickListener(this);
        disableControls();
        outputView = (TextView) findViewById(R.id.output);

        signIn();
    }

    private void signIn() {
        InputStream is = null;
        try {
            is = getAssets().open(CONFIG_FILE);
            InputStream bis = new BufferedInputStream(is);
            Reader reader = new InputStreamReader(bis);
            ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

            final String clientId = propertyResourceBundle.getString(KEY_PROJECT_ID);
            final String clientSecret = propertyResourceBundle.getString(KEY_CLIENT_SECRET);
            final String clientRedirectUri = propertyResourceBundle.getString(KEY_CLIENT_REDIRECT_URI);
            ClientCredentials clientCredentials = new ClientCredentials(clientId, clientSecret, clientRedirectUri);

            Mendeley.sdkInitialise(this, clientCredentials);

            if (Mendeley.getInstance().isSignedIn()) {
                setSignInStatus(SignInStatus.SIGNED_IN);
            } else {
                Mendeley.getInstance().signIn(this, false);
                setSignInStatus(SignInStatus.SIGNING_IN);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not read property files with client configuration. Should be located in assets/" + CONFIG_FILE, ioe);
        } catch (MissingResourceException mr) {
            throw new IllegalStateException("Could not read property value from client configuration file. Check everything is configured in assets/"+CONFIG_FILE, mr);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void signOut() {
        clearOutput();
        setSignInStatus(SignInStatus.SIGNED_OUT);
        Mendeley.getInstance().signOut();
    }

    private void setSignInStatus(SignInStatus status) {
        signInStatus = status;
        invalidateOptionsMenu();
        if (status == SignInStatus.SIGNED_IN) {
            enableControls();
        } else {
            disableControls();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem signInItem = menu.findItem(R.id.action_sign_in);
        MenuItem signOutItem = menu.findItem(R.id.action_sign_out);
        if (signInStatus == SignInStatus.SIGNED_IN) {
            signInItem.setEnabled(false);
            signOutItem.setEnabled(true);
        } else if (signInStatus == SignInStatus.SIGNED_OUT) {
            signInItem.setEnabled(true);
            signOutItem.setEnabled(false);
        } else {
            signInItem.setEnabled(false);
            signOutItem.setEnabled(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_in:
                signIn();
                break;
            case R.id.action_sign_out:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void disableControls() {
        getDocumentsButton.setEnabled(false);
    }

    private void enableControls() {
        getDocumentsButton.setEnabled(true);
    }

    private void clearOutput() {
        outputText.setLength(0);
        outputView.setText(outputText.toString());
    }

    @Override
	public void onClick(View view) {
        if (view == getDocumentsButton) {
            getDocuments();
        }
	}

    @Override
    public void onSignedIn() {
        setSignInStatus(SignInStatus.SIGNED_IN);
    }

    @Override
    public void onSignInFailure() {
        setSignInStatus(SignInStatus.SIGNED_OUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Mendeley.getInstance().onActivityResult(requestCode, resultCode, data, this)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getDocuments() {
        requestFactory = Mendeley.getInstance().getRequestFactory();
        DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.limit = 3;
        Request<List<Document>> documentRequest = requestFactory.getDocuments(params);

        documentRequest.runAsync(new DocumentsRequestCallback());
    }

    private void manageResponse(List<Document> resource, Uri next, Date serverDate) {
        outputText.append("Page received:\n");
        for (Document doc : resource) {
            outputText.append("* " + doc.title + "\n");
        }
        outputText.append("\n");
        outputView.setText(outputText.toString());

        if (next != null) {
            Request<List<Document>> documentRequest = requestFactory.getDocuments(next);
            documentRequest.runAsync(new DocumentsRequestCallback());
        }
    }

    private class DocumentsRequestCallback implements Request.RequestCallback<List<Document>> {

        @Override
        public void onSuccess(List<Document> resource, Uri next, Date serverDate) {
            manageResponse(resource, next, serverDate);
        }

        @Override
        public void onFailure(MendeleyException mendeleyException) {
            outputText.append(mendeleyException.toString() + "\n");
            outputView.setText(outputText.toString());
        }
    }
}
