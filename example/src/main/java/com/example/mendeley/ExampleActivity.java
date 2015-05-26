package com.example.mendeley;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mendeley.api.ClientCredentials;
import com.mendeley.api.MendeleySdk;
import com.mendeley.api.MendeleySdkFactory;
import com.mendeley.api.MendeleySignInInterface;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.impl.DefaultMendeleySdk;
import com.mendeley.api.model.Document;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.Page;

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

public class ExampleActivity extends Activity implements View.OnClickListener, GetDocumentsCallback, MendeleySignInInterface
{
    private static final String CONFIG_FILE = "config.properties";

    private static final String KEY_PROJECT_ID = "example_app_project_id";
    private static final String KEY_CLIENT_SECRET = "example_app_client_secret";
    private static final String KEY_CLIENT_REDIRECT_URI = "example_app_client_redirect_url";


    enum SignInStatus { SIGNED_OUT, SIGNING_IN, SIGNED_IN };
    private SignInStatus signInStatus = SignInStatus.SIGNED_OUT;
	
    private MendeleySdk sdk;

    private Button getDocumentsButton;
	private TextView outputView;
	
	private StringBuilder outputText = new StringBuilder();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getDocumentsButton = (Button) findViewById(R.id.getDocumentsButton);
        getDocumentsButton.setOnClickListener(this);
        disableControls();
        outputView = (TextView) findViewById(R.id.output);

        sdk = MendeleySdkFactory.getInstance();
        signIn();
    }

    private void signIn() {
        if (sdk.isSignedIn()) {
            setSignInStatus(SignInStatus.SIGNED_IN);
        } else {
            try {
                InputStream is = getAssets().open(CONFIG_FILE);
                InputStream bis = new BufferedInputStream(is);
                Reader reader = new InputStreamReader(bis);
                ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

                final String clientId = propertyResourceBundle.getString(KEY_PROJECT_ID);
                final String clientSecret = propertyResourceBundle.getString(KEY_CLIENT_SECRET);
                final String clientRedirectUri = propertyResourceBundle.getString(KEY_CLIENT_REDIRECT_URI);

                setSignInStatus(SignInStatus.SIGNING_IN);
                ClientCredentials clientCredentials = new ClientCredentials(clientId, clientSecret, clientRedirectUri);
                DefaultMendeleySdk.sdkInitialise(ExampleActivity.this.getApplicationContext(), clientCredentials);

                sdk.signIn(this, this, true);
            } catch (IOException ioe) {
                throw new IllegalStateException("Could not read property files with client configuration. Should be located in assets/"+CONFIG_FILE, ioe);
            } catch (MissingResourceException mr) {
                throw new IllegalStateException("Could not read property value from client configuration file. Check everything is configured in assets/"+CONFIG_FILE, mr);
            }
        }
    }

    private void signOut() {
        clearOutput();
        setSignInStatus(SignInStatus.SIGNED_OUT);
        sdk.signOut();
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
	public void onDocumentsReceived(List<Document> docs, Page next, Date serverDate) {
		outputText.append("Page received:\n");
		for (Document doc : docs) {
			outputText.append("* " + doc.title + "\n");
		}
		outputText.append("\n");
		outputView.setText(outputText.toString());
		
    	sdk.getDocuments(next, this);
	}

    @Override
	public void onDocumentsNotReceived(MendeleyException mendeleyException) {
		outputText.append(mendeleyException.toString() + "\n");
		outputView.setText(outputText.toString());
	}

    private void getDocuments() {
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = 3;
        outputText.setLength(0);
        sdk.getDocuments(params, this);
    }
}
