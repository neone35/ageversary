package com.github.neone35.ageversary.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.neone35.ageversary.MainFragment;
import com.github.neone35.ageversary.R;
import com.github.neone35.ageversary.pojo.User;
import com.github.neone35.ageversary.friends.FriendFragment.OnListFragmentInteractionListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendRVAdapter extends RecyclerView.Adapter<FriendRVAdapter.ViewHolder> {

    private final List<User> mUsers;
    private final OnListFragmentInteractionListener mListener;

    FriendRVAdapter(List<User> items, OnListFragmentInteractionListener listener) {
        mUsers = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mUsers.get(position);

        User user = holder.mItem = mUsers.get(position);

        holder.tvUsername.setText(user.getUsername());
        holder.tvBirthdate.setText(user.getBirthDate());
        MainFragment.loadPicture(user.getPhotoUrl(), holder.ivProfile);

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(R.id.tv_username)
        TextView tvUsername;
        @BindView(R.id.tv_birthdate)
        TextView tvBirthdate;
        @BindView(R.id.iv_profile)
        ImageView ivProfile;
        User mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }
}
