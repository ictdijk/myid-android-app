package in.yagnyam.myid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IdentityFragment extends Fragment {

    private static final String TAG = "IdentityFragment";

    private TextInputEditText nameEditText;
    private TextInputEditText idEditText;
    private TextInputEditText dobEditText;
    private OnFragmentInteractionListener mListener;

    public IdentityFragment() {
    }

    public static IdentityFragment newInstance() {
        IdentityFragment fragment = new IdentityFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_identity, container, false);
        nameEditText = (TextInputEditText) rootView.findViewById(R.id.nameEditText);
        nameEditText.setText(AppConstants.getName(getContext()));
        idEditText = (TextInputEditText) rootView.findViewById(R.id.idEditText);
        idEditText.setText(AppConstants.getId(getContext()));
        dobEditText = (TextInputEditText) rootView.findViewById(R.id.dobEditText);
        dobEditText.setText(AppConstants.getDobString(getContext()));
        Button registerButton = (Button) rootView.findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
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
        if (TextUtils.isEmpty(idEditText.getText()) || idEditText.getText().toString().trim().isEmpty()) {
            valid = false;
            focusView = focusView != null ? focusView : idEditText;
            idEditText.setError(getString(R.string.id_mandatory));
        }
        if (TextUtils.isEmpty(dobEditText.getText()) || dobEditText.getText().toString().trim().isEmpty()) {
            valid = false;
            focusView = focusView != null ? focusView : dobEditText;
            dobEditText.setError(getString(R.string.dob_mandatory));
        } else if (!AppConstants.isValidDate(dobEditText.getText().toString().trim())){
            valid = false;
            focusView = focusView != null ? focusView : dobEditText;
            dobEditText.setError(getString(R.string.invalid_dob));
        }
        if (focusView != null) {
            focusView.requestFocus();
        }
        return valid;
    }


    private void signUp() {
        try {
            AppConstants.setName(getContext(), nameEditText.getText().toString().trim());
            AppConstants.setId(getContext(), idEditText.getText().toString().trim());
            AppConstants.setDob(getContext(), dobEditText.getText().toString().trim());
            if (mListener != null) {
                mListener.onIdentityFinish();
            }
        } catch (ParseException e) {
            Log.e(TAG, "Invalid Date", e);
            dobEditText.setError(getString(R.string.invalid_dob));
            dobEditText.requestFocus();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            Log.e(TAG, context.toString() + " must implement OnFragmentInteractionListener");
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onIdentityFinish();
    }

}
