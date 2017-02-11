package in.yagnyam.myid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import in.yagnyam.myid.model.ProfileEntry;
import in.yagnyam.myid.utils.JsonUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "data: " + getIntent().getData());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        launchFragment(chooseFragment());
    }

    private boolean isLoginMode() {
        Intent intent = getIntent();
        return Intent.ACTION_VIEW.equals(intent.getAction());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private Fragment chooseFragment() {
        if (!AppConstants.hasName(this)) {
            return WelcomeFragment.newInstance();
        } else {
            return ProfilesFragment.newInstance(isLoginMode());
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
        Log.d(TAG, "uri: " + getIntent().getData());
        Intent intent = getIntent();
        Uri uri = intent.getData();

        String targetUrl;
        if (uri == null) {
            Toast.makeText(this, "No URL mentioned.", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else if (!StringUtils.isEmpty(uri.getQueryParameter("ret"))) {
            targetUrl = URLDecoder.decode(uri.getQueryParameter("ret"));
        } else if (!StringUtils.isEmpty(uri.getQueryParameter("authenticate"))) {
            targetUrl = URLDecoder.decode(uri.getQueryParameter("authenticate"));
        } else {
            Toast.makeText(this, "Invalid URL: " + intent.getData(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Toast.makeText(this, targetUrl, Toast.LENGTH_LONG).show();
        String audience = uri.getQueryParameter("audience") != null ? URLDecoder.decode(uri.getQueryParameter("audience")) : null;
        Log.d(TAG, "target: " + targetUrl + ", audience: " + audience);
        try {
            String jwt = TokenIssuer.issueToken(audience, profileEntry.getClaims(), profileEntry.getPath(), UserKeyStore.getKeyPair().getPrivate());
            if (!StringUtils.isEmpty(uri.getQueryParameter("ret"))) {
                startActivity(getTargetIntent(targetUrl, jwt));
                finish();
            } else {
                new LoginTask(targetUrl, jwt).execute();
            }
            //resultIntent.putExtra(ACTION_RETURN_JWT, jwt);
            // setResult(RESULT_OK, resultIntent);
        } catch (Throwable t) {
            Log.e(TAG, "failed to provide authentication Token", t);
            Intent errorIntent = new Intent(REQUEST_MIJD_JWT);
            errorIntent.putExtra(ACTION_RETURN_ERROR, "Error issuing authorization: " + t.getMessage());
            setResult(RESULT_CANCELED, errorIntent);
            Toast.makeText(this, "Invalid URL: " + intent.getData(), Toast.LENGTH_LONG).show();
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

    public class LoginTask extends AsyncTask<String, Void, Void> {

        private final String urlString;
        private final String data;

        public LoginTask(String url, String data) {
            this.urlString = url;
            this.data = data;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(HttpMethods.POST);
                urlConnection.setDoOutput(false);
                OutputStream outputPost = new BufferedOutputStream(urlConnection.getOutputStream());
                outputPost.write(data.getBytes());
                outputPost.flush();
                outputPost.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error sending Token", e);
            }
            finish();
            return null;
        }

    }



}