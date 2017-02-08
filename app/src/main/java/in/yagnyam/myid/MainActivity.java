package in.yagnyam.myid;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;

public class MainActivity extends AppCompatActivity
        implements WelcomeFragment.OnFragmentInteractionListener,
        IdentityFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        launchFragment(chooseFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private Fragment chooseFragment() {
        if (!AppConstants.hasName(this)) {
            return WelcomeFragment.newInstance();
        } else {
            return RegisterFragment.newInstance();
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
    public void onRegistration() {
        launchFragment(chooseFragment());
    }
}
