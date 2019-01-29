package com.github.neone35.ageversary.friends;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ToastUtils;
import com.github.neone35.ageversary.MainActivity;
import com.github.neone35.ageversary.R;
import com.github.neone35.ageversary.pojo.User;

import org.parceler.Parcels;

import java.util.ArrayList;

public class FriendFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_USER_LIST = "user-list";
    private int mColumnCount = 1;
    private ArrayList<User> mUserList;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendFragment() {
    }

    public static FriendFragment newInstance(int columnCount, ArrayList<User> userList) {
        FriendFragment fragment = new FriendFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        ArrayList<Parcelable> userParcelList = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            userParcelList.add(Parcels.wrap(userList.get(i)));
        }
        args.putParcelableArrayList(ARG_USER_LIST, userParcelList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mUserList = new ArrayList<>();
            ArrayList<Parcelable> userParcelList = getArguments().getParcelableArrayList(ARG_USER_LIST);
            if (userParcelList != null) {
                for (int i = 0; i < userParcelList.size(); i++) {
                    mUserList.add(Parcels.unwrap(userParcelList.get(i)));
                }
            }
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        // Set the adapter
        if (MainActivity.mIsLoggedIn) {
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view;
                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                FriendRVAdapter friendAdapter = new FriendRVAdapter(mUserList, mListener);
                recyclerView.setAdapter(friendAdapter);
            }
        } else {
            ToastUtils.showShort("You must be logged in to see friends");
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                if (MainActivity.mIsLoggedIn)
                    ToastUtils.showShort("Switch to 'my profile' to logout");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(User user);
    }
}
