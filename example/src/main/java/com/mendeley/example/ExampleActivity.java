package com.mendeley.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mendeley.sdk.Mendeley;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.RequestsFactory;
import com.mendeley.sdk.example.R;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.model.Document;
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
    private RequestsFactory requestFactory;


    enum SignInStatus { SIGNED_OUT, SIGNING_IN, SIGNED_IN }
    private SignInStatus signInStatus = SignInStatus.SIGNED_OUT;
	
    private Button getDocumentsButton;
	private TextView outputView;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getDocumentsButton = (Button) findViewById(R.id.getDocumentsButton);
        getDocumentsButton.setOnClickListener(this);
        disableControls();
        outputView = (TextView) findViewById(R.id.output);

        init();
    }

    private void init() {
        InputStream is = null;
        try {
            is = getAssets().open(CONFIG_FILE);
            InputStream bis = new BufferedInputStream(is);
            Reader reader = new InputStreamReader(bis);
            ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

            final String clientId = propertyResourceBundle.getString(KEY_PROJECT_ID);
            final String clientSecret = propertyResourceBundle.getString(KEY_CLIENT_SECRET);
            Mendeley.getInstance().init(this, clientId, clientSecret);
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

    @Override
    protected void onResume() {
        super.onResume();
        checkSigned();
    }

    private void checkSigned() {
        if (Mendeley.getInstance().isSignedIn()) {
            setSignInStatus(SignInStatus.SIGNED_IN);
        } else {
            Mendeley.getInstance().signIn(this);
            setSignInStatus(SignInStatus.SIGNING_IN);
        }
    }

    private void signOut() {
        clearOutput();
        setSignInStatus(SignInStatus.SIGNED_OUT);
        Mendeley.getInstance().signOut();
        checkSigned();
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
                init();
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
        outputView.setText("");
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
        if (Mendeley.getInstance().onActivityResult(requestCode, resultCode, this)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getDocuments() {
        requestFactory = Mendeley.getInstance().getRequestFactory();
        DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.limit = 3;
        Request<List<Document>> documentRequest = requestFactory.newGetDocumentsRequest(params);

        documentRequest.runAsync(new DocumentsRequestCallback());
    }

    private void manageResponse(List<Document> resource, Uri next) {
        appendToOutputView("Page received:");
        for (Document doc : resource) {
            appendToOutputView("* " + doc.title);
        }
        appendToOutputView("");

        if (next != null) {
            Request<List<Document>> documentRequest = requestFactory.newGetDocumentsRequest(next);
            documentRequest.runAsync(new DocumentsRequestCallback());
        }
    }

    private void appendToOutputView(String str) {
        outputView.append(str);
        outputView.append("\n");
    }

    private class DocumentsRequestCallback implements Request.RequestCallback<List<Document>> {

        @Override
        public void onSuccess(List<Document> resource, Uri next, Date serverDate) {
            manageResponse(resource, next);
        }

        @Override
        public void onFailure(MendeleyException mendeleyException) {
            Log.e(ExampleActivity.class.getSimpleName(), "Error in request", mendeleyException);
            appendToOutputView(mendeleyException.toString());
        }

        @Override
        public void onCancelled() {
            outputView.setText("request canceled");
        }
    }


}
