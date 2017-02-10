package in.yagnyam.myid;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import in.yagnyam.myid.model.ProfileEntry;
import in.yagnyam.myid.utils.JsonUtils;

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        Intent intent = getIntent();
        Uri uri = intent.getData();

        Intent resultIntent = new Intent(REQUEST_MIJD_JWT);
        try {
            String jwt = TokenIssuer.issueToken("", null, "", null);
            resultIntent.putExtra(ACTION_RETURN_JWT, jwt);
            setResult(RESULT_OK, resultIntent);
        } catch (Throwable t) {
            Log.e(TAG, "failed to provide authentication Token", t);
            resultIntent.putExtra(ACTION_RETURN_ERROR, "Error issuing authorization: " + t.getMessage());
            setResult(RESULT_CANCELED, new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
