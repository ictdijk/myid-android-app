package in.yagnyam.myid;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ProfilesFragment extends BaseFragment {

    public static final int REQUEST_CODE_SCAN_ACCOUNT = 0x0000c0de; // Only use bottom 16 bits
    public static final int REQUEST_CODE_PAY = 0x0000c0df; // Only use bottom 16 bits
    private static final String TAG = "ProfilesFragment";

    private static final String LOGIN_MODE = "loginMode";

    private boolean loginMode = false;
    private ProfilesAdapter profilesAdapter;
    private Listener listener;

    public static ProfilesFragment newInstance(boolean loginMode) {
        Log.d(TAG, "ProfilesFragment.newInstance()");
        ProfilesFragment fragment = new ProfilesFragment();
        Bundle args = new Bundle();
        args.putBoolean(LOGIN_MODE, loginMode);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loginMode = getArguments().getBoolean(LOGIN_MODE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tap_and_send:
                tapAndSend();
                return false;
            case R.id.action_scan_and_pay:
                showQR();
                return false;
            case R.id.new_profile:
                if (listener != null) {
                    listener.createProfile();
                }
            default:
                return true;
        }
    }

    public void tapAndSend() {
        Log.i(TAG, "tapAndSend()");
    }


    public void showQR() {
        Log.i(TAG, "showQR()");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_profiles, container, false);
        Context context = rootView.getContext();
        Button newProfileButton = (Button) rootView.findViewById(R.id.newProfileButton);
        newProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.createProfile();
                }
            }
        });

        RecyclerViewEmptySupport recyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setEmptyView(rootView.findViewById(R.id.createProfileFrame));
        profilesAdapter = ProfilesAdapter.getInstance(getContext(), loginMode);
        recyclerView.setAdapter(profilesAdapter);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (profilesAdapter != null) {
            profilesAdapter.refresh();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        } else {
            throw new RuntimeException(context + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface Listener {
        void createProfile();
    }

}
