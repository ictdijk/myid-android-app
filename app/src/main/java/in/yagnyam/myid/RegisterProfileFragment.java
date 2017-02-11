package in.yagnyam.myid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.api.client.http.HttpResponseException;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.yagnyam.digid.registerApi.model.RegistrationRequest;
import in.yagnyam.digid.registerApi.model.RegistrationResponse;
import in.yagnyam.myid.data.DbHelper;
import in.yagnyam.myid.model.ProfileEntry;
import in.yagnyam.myid.utils.PemUtils;

public class RegisterProfileFragment extends BaseFragment {

    private static final String TAG = "RegisterProfileFragment";

    private RegisterTask mRegisterTask = null;

    private TextInputEditText profileNameEditText;
    private TextInputEditText digitdEditText;
    private TextInputEditText passwordEditText;
    private CheckBox nameCheckBox;
    private CheckBox bsnCheckBox;
    private CheckBox dobCheckBox;
    private CheckBox bloodGroupCheckBox;
    private CheckBox refugeeStatusCheckBox;
    private OnFragmentInteractionListener mListener;

    public RegisterProfileFragment() {
    }

    public static RegisterProfileFragment newInstance() {
        RegisterProfileFragment fragment = new RegisterProfileFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_register_profile, container, false);
        profileNameEditText = (TextInputEditText) rootView.findViewById(R.id.profileNameEditText);
        digitdEditText = (TextInputEditText) rootView.findViewById(R.id.digitdEditText);
        passwordEditText = (TextInputEditText) rootView.findViewById(R.id.passwordEditText);
        nameCheckBox = (CheckBox) rootView.findViewById(R.id.nameCheckBox);
        bsnCheckBox = (CheckBox) rootView.findViewById(R.id.bsnCheckBox);
        dobCheckBox = (CheckBox) rootView.findViewById(R.id.dobCheckBox);
        bloodGroupCheckBox = (CheckBox) rootView.findViewById(R.id.bloodGroupCheckBox);
        refugeeStatusCheckBox = (CheckBox) rootView.findViewById(R.id.refugeeStatusCheckBox);

        Button registerButton = (Button) rootView.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    register();
                }
            }
        });
        return rootView;
    }

    private boolean isValid() {
        View focusView = null;
        boolean valid = true;
        if (TextUtils.isEmpty(profileNameEditText.getText()) || profileNameEditText.getText().toString().trim().isEmpty()) {
            valid = false;
            focusView = focusView != null ? focusView : profileNameEditText;
            profileNameEditText.setError(getString(R.string.profile_mandatory));
        }
        if (TextUtils.isEmpty(digitdEditText.getText()) || digitdEditText.getText().toString().trim().isEmpty()) {
            valid = false;
            focusView = focusView != null ? focusView : digitdEditText;
            digitdEditText.setError(getString(R.string.name_mandatory));
        }
        if (TextUtils.isEmpty(passwordEditText.getText()) || passwordEditText.getText().toString().isEmpty()) {
            valid = false;
            focusView = focusView != null ? focusView : passwordEditText;
            passwordEditText.setError(getString(R.string.id_mandatory));
        }
        if (focusView != null) {
            focusView.requestFocus();
        }
        return valid;
    }


    private void register() {
        String profileName = profileNameEditText.getText().toString().trim();
        String digid = digitdEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        try {
            KeyPair keyPair = UserKeyStore.getKeyPair();
            String publicKey = PemUtils.encodePublicKey(keyPair.getPublic());
            // String privateKey = PemUtils.encodePrivateKey(keyPair.getPrivate());
            RegistrationRequest request = new RegistrationRequest();
            request.setVerificationKey(publicKey);
            addClaims(request);
            showProgressDialog(R.string.registering);
            mRegisterTask = new RegisterTask(getContext(), profileName, digid, password, request);
            mRegisterTask.execute((Void) null);
        } catch (Throwable t) {
            Log.e(TAG, "failed to make registration request", t);
            return;
        }
    }

    private void addClaims(RegistrationRequest request, CheckBox checkBox, String claim) {
        if (checkBox.isChecked()) {
            List<String> claims = request.getClaims();
            if (claims == null) {
                claims = new ArrayList<>();
                request.setClaims(claims);
            }
            claims.add(claim);
        }
    }

    private void addClaims(RegistrationRequest request) {
        addClaims(request, bsnCheckBox, "bsn");
        addClaims(request, dobCheckBox, "dob");
        addClaims(request, nameCheckBox, "name");
        addClaims(request, bloodGroupCheckBox, "bloodGroup");
        addClaims(request, refugeeStatusCheckBox, "refugeeStatus");
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
        void onRegistration(ProfileEntry profile);
    }


    public class RegisterTask extends AsyncTask<Void, Void, RegistrationResponse> {

        private final Context context;
        private final String profileName;
        private final String digid;
        private final String password;
        private final RegistrationRequest registrationRequest;

        RegisterTask(Context context, String profileName, String digid, String password, RegistrationRequest request) {
            this.context = context;
            this.profileName = profileName;
            this.digid = digid;
            this.password = password;
            this.registrationRequest = request;
        }

        @Override
        protected RegistrationResponse doInBackground(Void... params) {
            Log.d(TAG, "RegisterTask.doInBackground()");
            try {
                return ApiHome.getRegisterApiHandle(context, digid, password).register(registrationRequest).execute();
            } catch (HttpResponseException e) {
                Log.e(TAG, "error while registering user", e);
                return null;
            } catch (IOException e) {
                Log.e(TAG, "error while registering user", e);
                return null;
            }

        }

        @Override
        protected void onPostExecute(final RegistrationResponse result) {
            Log.d(TAG, "RegisterTask.onPostExecute(" + result + ")");
            mRegisterTask = null;
            hideProgressDialog();
            if (result != null) {
                Toast.makeText(context, getString(R.string.registration_successful), Toast.LENGTH_LONG).show();
                ProfileEntry profileEntry = persistResponse(result);
                if (mListener != null) {
                    mListener.onRegistration(profileEntry);
                }
            } else {
                Toast.makeText(context, getString(R.string.registration_failed), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "RegisterTask.onCancelled()");
            mRegisterTask = null;
            hideProgressDialog();
        }

        private ProfileEntry persistResponse(RegistrationResponse response) {
            ProfileEntry profile = new ProfileEntry();

            profile.setProfileId(response.getPath());
            profile.setProfileName(profileName);
            profile.setDigid(response.getDigid());
            profile.setPath(response.getPath());

            if (bsnCheckBox.isChecked()) {
                profile.setBsn(response.getBsn());
            }
            if (dobCheckBox.isChecked()) {
                profile.setDob(new Date(response.getDob().getValue()));
            }
            if (nameCheckBox.isChecked()) {
                profile.setName(response.getName());
            }
            if (bloodGroupCheckBox.isChecked()) {
                profile.setBloodGroup(response.getBloodGroup());
            }
            if (refugeeStatusCheckBox.isChecked()) {
                profile.setRefugeeStatus(response.getRefugeeStatus());
            }
            if (false) {
                profile.setDeaf(response.getDeaf());
                profile.setDumb(response.getDumb());
            }

            DbHelper.getInstance(context).insertProfile(profile);
            return profile;
        }

        private void handleError(String error) {
            mRegisterTask = null;
            hideProgressDialog();
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }

    }


}
