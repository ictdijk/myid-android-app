package in.yagnyam.myid;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.yagnyam.myid.data.DataSet;
import in.yagnyam.myid.data.DataSetObserver;
import in.yagnyam.myid.data.ProfilesDataSet;
import in.yagnyam.myid.model.ProfileEntry;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ViewHolder> implements DataSetObserver<ProfileEntry> {

    private static final String TAG = "ProfilesAdapter";

    private final boolean loginMode;
    private final DataSet<ProfileEntry> dataSet;
    private final Handler handler;

    private ProfilesAdapter(Context context, boolean loginMode) {
        Log.d(TAG, "ProfilesAdapter()");
        this.loginMode = loginMode;
        this.dataSet = ProfilesDataSet.getInstance(context);
        this.handler = new Handler(/*Looper.getMainLooper()*/);
    }

    public static ProfilesAdapter getInstance(Context context, boolean loginMode) {
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

    class ViewHolder extends RecyclerView.ViewHolder {
        final Context context;
        final View view;
        final TextView profileNameTextView;
        final TextView descriptionTextView;
        final View menu;
        ProfileEntry mItem;

        ViewHolder(final Context context, View view) {
            super(view);
            this.context = context;
            this.view = view;
            profileNameTextView = (TextView) view.findViewById(R.id.profileNameTextView);
            descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
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
        }

        @Override
        public String toString() {
            return super.toString() + " '" + profileNameTextView.getText() + "'";
        }
    }

    public interface Listener {
        void profileSelected(ProfileEntry profileEntry);
    }

}
