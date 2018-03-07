package com.amaslov.android.ageversary.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;

import com.amaslov.android.ageversary.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class BirthdayDialogFragment extends DialogFragment {

    private DatePicker mDatePicker;

    public BirthdayDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static BirthdayDialogFragment newInstance(String title) {
        BirthdayDialogFragment frag = new BirthdayDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_birthday, container);
    }

    @Override
    public void onViewCreated(@Nonnull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mDatePicker = view.findViewById(R.id.dp_birthday);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
    }

    public interface BirthdayDialogListener {
        void onFinishEditDialog(String inputText);
    }

}
