package in.yagnyam.myid;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.security.KeyPair;

public class WelcomeFragment extends Fragment {

    private TextInputEditText nameEditText;
    private OnFragmentInteractionListener mListener;

    public WelcomeFragment() {
    }

    public static WelcomeFragment newInstance() {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        nameEditText = (TextInputEditText) rootView.findViewById(R.id.nameEditText);
        Button signUpButton = (Button) rootView.findViewById(R.id.button_signup);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    signUp();
                }
            }
        });
        return rootView;
    }

    private boolean isValid() {
        View focusView = null;
        boolean valid = true;
        if (TextUtils.isEmpty(nameEditText.getText()) || nameEditText.getText().toString().trim().isEmpty()) {
            valid = false;
            focusView = focusView != null ? focusView : nameEditText;
            nameEditText.setError(getString(R.string.name_mandatory));
        }
        if (focusView != null) {
            focusView.requestFocus();
        }
        return valid;
    }


    private void signUp() {
        String name = nameEditText.getText().toString().trim();
        KeyPair keyPair = UserKeyStore.createNewKeyPair(getContext(), name);
        AppConstants.setName(getContext(), name);
        if (mListener != null) {
            mListener.onWelcomeFinish();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onWelcomeFinish();
    }
}
