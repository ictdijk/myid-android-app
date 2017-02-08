package in.yagnyam.myid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.http.HttpResponseException;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Date;

import in.yagnyam.digid.registerApi.model.RegistrationRequest;
import in.yagnyam.digid.registerApi.model.RegistrationResponse;
import in.yagnyam.myid.utils.PemUtils;

public class RegisterFragment extends BaseFragment {

    private static final String TAG = "RegisterFragment";

    private RegisterTask mRegisterTask = null;

    private TextInputEditText digitdEditText;
    private TextInputEditText passwordEditText;
    private OnFragmentInteractionListener mListener;

    public RegisterFragment() {
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);
        digitdEditText = (TextInputEditText) rootView.findViewById(R.id.digitdEditText);
        digitdEditText.setText(AppConstants.getDigid(getContext()));
        passwordEditText = (TextInputEditText) rootView.findViewById(R.id.passwordEditText);
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
        String digid = digitdEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        try {
            KeyPair keyPair = UserKeyStore.getKeyPair();
            String publicKey = PemUtils.encodePublicKey(keyPair.getPublic());
            // String privateKey = PemUtils.encodePrivateKey(keyPair.getPrivate());
            RegistrationRequest request = new RegistrationRequest();
            request.setVerificationKey(publicKey);
            mRegisterTask = new RegisterTask(digid, password, request);
            mRegisterTask.execute((Void) null);

        } catch (Throwable t) {
            Log.e(TAG, "failed to make registration request", t);
            return;
        }
        if (mListener != null) {
            mListener.onRegistration();
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
        void onRegistration();
    }


    public class RegisterTask extends AsyncTask<Void, Void, RegistrationResponse> {

        private final String digid;
        private final String password;
        private final RegistrationRequest registrationRequest;

        RegisterTask(String digid, String password, RegistrationRequest request) {
            this.digid = digid;
            this.password = password;
            this.registrationRequest = request;
        }

        @Override
        protected RegistrationResponse doInBackground(Void... params) {
            Log.d(TAG, "RegisterTask.doInBackground()");
            try {
                return ApiHome.getRegisterApiHandle(getContext(), digid, password).register(registrationRequest).execute();
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
                Toast.makeText(getActivity(), getString(R.string.registration_successful), Toast.LENGTH_LONG).show();
                persistResponse(result);
                if (mListener != null) {
                    mListener.onRegistration();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.registration_failed), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "RegisterTask.onCancelled()");
            mRegisterTask = null;
            hideProgressDialog();
        }

        private void persistResponse(RegistrationResponse response) {
            Context context = getContext();
            AppConstants.setBsn(context, response.getBsn());
            AppConstants.setNodePath(context, response.getPath());
            AppConstants.setDob(context, new Date(response.getDob().getValue()));
            AppConstants.setDigid(context, response.getDigid());
        }

        private void handleError(String error) {
            mRegisterTask = null;
            hideProgressDialog();
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        }

    }


}
