package in.yagnyam.myid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;

import in.yagnyam.myid.data.DataSet;
import in.yagnyam.myid.data.DataSetObserver;
import in.yagnyam.myid.data.ProfilesDataSet;
import in.yagnyam.myid.model.ProfileEntry;
import in.yagnyam.myid.utils.JsonUtils;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ViewHolder>
        implements DataSetObserver<ProfileEntry>, NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private static final String TAG = "ProfilesAdapter";

    private final Activity context;
    private final boolean loginMode;
    private final DataSet<ProfileEntry> dataSet;
    private final Handler handler;

    private NfcAdapter nfcAdapter;
    private NdefMessage ndefMessage;

    private ProfilesAdapter(Activity context, boolean loginMode) {
        Log.d(TAG, "ProfilesAdapter()");
        this.context = context;
        this.loginMode = loginMode;
        this.dataSet = ProfilesDataSet.getInstance(context);
        this.handler = new Handler(/*Looper.getMainLooper()*/);
        initNFC();
    }

    public static ProfilesAdapter getInstance(Activity context, boolean loginMode) {
        ProfilesAdapter ret = new ProfilesAdapter(context, loginMode);
        ret.dataSet.registerObserver(ret);
        return ret;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_profile_entry, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = dataSet.get(position);
        holder.profileNameTextView.setText(holder.mItem.getProfileName());
        holder.descriptionTextView.setText(holder.mItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    private boolean isCurrentThread() {
        return handler.getLooper().getThread().equals(Thread.currentThread());
    }

    @Override
    public void itemInserted(final int pos) {
        if (isCurrentThread()) {
            notifyItemInserted(pos);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(pos);
                }
            });
        }
    }

    @Override
    public void itemChanged(final int pos) {
        if (isCurrentThread()) {
            notifyItemChanged(pos);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(pos);
                }
            });
        }
    }

    @Override
    public void itemRemoved(final int pos) {
        if (isCurrentThread()) {
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, getItemCount());
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, getItemCount());
                }
            });
        }
    }

    public void refresh() {
        Log.d(TAG, "refresh() => " + dataSet);
        notifyDataSetChanged();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Log.i(TAG, "createNdefMessage: " + ndefMessage);
        return ndefMessage;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.i(TAG, "onNdefPushComplete");
        ndefMessage = null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final Context context;
        final View view;
        final TextView profileNameTextView;
        final TextView descriptionTextView;
        final Button showQrButton;
        final Button tapButton;
        final View menu;
        ProfileEntry mItem;

        ViewHolder(final Context context, View view) {
            super(view);
            this.context = context;
            this.view = view;
            profileNameTextView = (TextView) view.findViewById(R.id.profileNameTextView);
            descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
            showQrButton = (Button) view.findViewById(R.id.showQrButton);
            tapButton = (Button) view.findViewById(R.id.tapButton);
            menu = view.findViewById(R.id.menu);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Profile Chosen: " + mItem);
                    if (loginMode) {
                        if (context instanceof Listener) {
                            ((Listener) context).profileSelected(mItem);
                        }
                    } else if (menu.getVisibility() == View.VISIBLE) {
                        menu.setVisibility(View.GONE);
                    } else {
                        menu.setVisibility(View.VISIBLE);
                    }
                }
            });
            showQrButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showQR(mItem);
                }
            });
            tapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tapAndSend(mItem);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + profileNameTextView.getText() + "'";
        }
    }


    public void tapAndSend(ProfileEntry profileEntry) {
        Log.i(TAG, "tapAndSend()");
        try {
            String token = TokenIssuer.issueToken(null, profileEntry.getClaims(), profileEntry.getPath(), UserKeyStore.getKeyPair().getPrivate());
            createNdefMessage(token);
        } catch (Throwable t) {
            Log.e(TAG, "Error generating token", t);
            Toast.makeText(context, "Error generating Token", Toast.LENGTH_LONG).show();
        }
    }


    public void showQR(ProfileEntry profileEntry) {
        Log.i(TAG, "showQR()");
        try {
            String token = TokenIssuer.issueToken(null, profileEntry.getClaims(), profileEntry.getPath(), UserKeyStore.getKeyPair().getPrivate());
            Uri uri = Uri.parse("https://chart.googleapis.com/chart?cht=qr&chs=500x500&chl=" + URLEncoder.encode(token));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Throwable t) {
            Log.e(TAG, "Error generating token", t);
            Toast.makeText(context, "Error generating Token", Toast.LENGTH_LONG).show();
        }
    }


    public void initNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            Toast.makeText(context, "NFC is not available", Toast.LENGTH_LONG).show();
        } else {
            nfcAdapter.setNdefPushMessageCallback(this, context);
            nfcAdapter.setOnNdefPushCompleteCallback(this, context);
        }
    }

    public void createNdefMessage(String jwt) {
        if (nfcAdapter == null) {
            Toast.makeText(context, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        } else if (!nfcAdapter.isEnabled()) {
            Log.d(TAG, "NFC not enabled");
            Toast.makeText(context, "NFC Not Enabled", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } else if (!nfcAdapter.isNdefPushEnabled()) {
            Log.d(TAG, "NFC push not enabled");
            Toast.makeText(context, "NFC Push not enabled", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        }
        Log.i(TAG, "Setting NDF Message to be pushed over NFC");
        ndefMessage = new NdefMessage(
                new NdefRecord[]{
                        NdefRecord.createMime("application/json", jwt.getBytes()),
                        NdefRecord.createApplicationRecord("in.yagnyam.mijd")
                });
        /*
        nfcAdapter.setNdefPushMessage(ndefMessage, getActivity());
        */
        // TODO: When the minimum supported version is changed, this has to be removed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nfcAdapter.invokeBeam(context);
        }
    }


    public interface Listener {
        void profileSelected(ProfileEntry profileEntry);
    }

}
