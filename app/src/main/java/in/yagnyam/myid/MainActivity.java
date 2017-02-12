package in.yagnyam.myid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.api.client.http.HttpMethods;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import in.yagnyam.digid.registerApi.model.RegistrationResponse;
import in.yagnyam.myid.loginApi.model.LoginEntity;
import in.yagnyam.myid.model.ProfileEntry;
import in.yagnyam.myid.utils.StringUtils;

public class MainActivity extends AppCompatActivity
        implements WelcomeFragment.OnFragmentInteractionListener,
        IdentityFragment.OnFragmentInteractionListener,
        RegisterProfileFragment.OnFragmentInteractionListener,
        ProfilesFragment.Listener, ProfilesAdapter.Listener {

    private static final String TAG = "MainActivity";

    public static final String REQUEST_MIJD_JWT = "in.yagnyam.myid.REQUEST_JWT";
    public static final String ACTION_RETURN_JWT = "in.yagnyam.myid.JWT";
    public static final String ACTION_RETURN_ERROR = "in.yagnyam.myid.ERROR";

    private static final String LOGIN_MODE = "loginMode";
    private static final String LOGIN_URL = "loginUrl";

    private boolean loginMode;
    private String loginUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "data: " + getIntent().getData());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        launchFragment(chooseFragment());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(LOGIN_MODE, loginMode);
        savedInstanceState.putString(LOGIN_URL, loginUrl);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loginMode = savedInstanceState.getBoolean(LOGIN_MODE);
        loginUrl = savedInstanceState.getString(LOGIN_URL);
    }

    @Override
    public boolean loginMode() {
        Intent intent = getIntent();
        return Intent.ACTION_VIEW.equals(intent.getAction()) || loginMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_profile:
                createProfile();
                return true;
            case R.id.scan_qr:
                new IntentIntegrator(this).initiateScan();
                return true;
            default:
                return true;
        }
    }


    private Fragment chooseFragment() {
        if (!AppConstants.hasName(this)) {
            return WelcomeFragment.newInstance();
        } else {
            return ProfilesFragment.newInstance();
        }
    }

    public void launchFragment(Fragment fragment, String backStackName, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction = transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack && transaction.isAddToBackStackAllowed()) {
            transaction = transaction.addToBackStack(backStackName);
        }
        transaction.commit();
    }

    public void launchFragment(Fragment fragment) {
        launchFragment(fragment, null, false);
    }

    @Override
    public void onWelcomeFinish() {
        launchFragment(chooseFragment());
    }

    @Override
    public void onIdentityFinish() {
        launchFragment(chooseFragment());
    }

    @Override
    public void onRegistration(ProfileEntry profileEntry) {
        launchFragment(chooseFragment());
    }

    @Override
    public void createProfile() {
        launchFragment(RegisterProfileFragment.newInstance());
    }

    @Override
    public void profileSelected(ProfileEntry profileEntry) {
        loginMode = false;
        Log.d(TAG, "uri: " + getIntent().getData());
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri == null && StringUtils.isEmpty(loginUrl)) {
            Toast.makeText(this, "No URL mentioned.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String targetUrl = null;
        if (uri != null&& !StringUtils.isEmpty(uri.getQueryParameter("ret"))) {
            targetUrl = URLDecoder.decode(uri.getQueryParameter("ret"));
        } else {
            targetUrl = loginUrl;
        }

        if (StringUtils.isEmpty(targetUrl)) {
            Toast.makeText(this, "1. Invalid URL: " + intent.getData(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Toast.makeText(this, targetUrl, Toast.LENGTH_LONG).show();
        String audience = uri != null && uri.getQueryParameter("audience") != null ? URLDecoder.decode(uri.getQueryParameter("audience")) : null;
        Log.d(TAG, "target: " + targetUrl + ", audience: " + audience);
        try {
            String jwt = TokenIssuer.issueToken(audience, profileEntry.getClaims(), profileEntry.getPath(), UserKeyStore.getKeyPair().getPrivate());
            if (uri != null && !StringUtils.isEmpty(uri.getQueryParameter("ret"))) {
                startActivity(getTargetIntent(targetUrl, jwt));
                finish();
            } else {
                LoginTask task = new LoginTask(targetUrl, jwt);
                task.execute((Void)null);
            }
            //resultIntent.putExtra(ACTION_RETURN_JWT, jwt);
            // setResult(RESULT_OK, resultIntent);
        } catch (Throwable t) {
            Log.e(TAG, "failed to provide authentication Token", t);
            Intent errorIntent = new Intent(REQUEST_MIJD_JWT);
            errorIntent.putExtra(ACTION_RETURN_ERROR, "Error issuing authorization: " + t.getMessage());
            setResult(RESULT_CANCELED, errorIntent);
            Toast.makeText(this, t.toString(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private Intent getTargetIntent(String targetUrl, String authToken) {
        Log.d(TAG, "getTargetIntent(targetUrl: " + targetUrl + ", authToken: " + authToken + ")");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
        Bundle bundle = new Bundle();
        bundle.putString("X-MijD", authToken);
        browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);
        return browserIntent;

    }

    public class LoginTask extends AsyncTask<Void, Void, Void> {

        private final String urlString;
        private final String data;
        private final String session;

        public LoginTask(String url, String data) {
            Log.i(TAG, "Login (" + url + ", " + data + ")");
            this.urlString = url;
            this.data = data;
            String[] tokens = url.split("=");
            session = tokens[tokens.length-1];
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doInBackground (" + urlString + ", " + data + ")");
            try {
                LoginEntity loginEntity = new LoginEntity();
                loginEntity.setSession(session);
                loginEntity.setAuthToken(data);
                ApiHome.getLoginApiHandle(MainActivity.this).login(loginEntity).execute();
                /*
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(HttpMethods.POST);
                urlConnection.setDoOutput(true);
                OutputStream outputPost = new BufferedOutputStream(urlConnection.getOutputStream());
                Log.i(TAG, "doInBackground (" + urlString + ", " + data + ")");
                outputPost.write(data.getBytes());
                outputPost.flush();
                Log.i(TAG, "doInBackground (" + urlString + ", " + data + ")");
                //outputPost.close();
                //urlConnection.disconnect();
                */
                Toast.makeText(MainActivity.this, "Done Authenticating " + urlString, Toast.LENGTH_LONG).show();
            } catch (Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to Authenticate: " + t.toString(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error sending Token", t);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            Toast.makeText(MainActivity.this, "Post Execute", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                loginUrl = result.getContents();
                loginMode = !StringUtils.isEmpty(loginUrl);
                Toast.makeText(this, "Scanned: " + loginUrl, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}