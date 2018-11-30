package com.github.neone35.ageversary.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;


public class DatePickerDialogFragment extends DialogFragment {

    public DatePickerDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DatePickerDialogFragment newInstance(String title) {
        DatePickerDialogFragment frag = new DatePickerDialogFragment();
        Bundle inArgs = new Bundle();
        inArgs.putString("title", title);
        frag.setArguments(inArgs);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Activity needs to implement this interface
        DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) getActivity();
        DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), listener, year, month, day);

        String customTitle = null;
        if (getArguments() != null) {
            customTitle = getArguments().getString("title", "Choose a value");
        }
        datePickerDialog.setTitle(customTitle);
        long timeNow = c.getTimeInMillis();
        long years126 = 4000000000000L;
        datePickerDialog.getDatePicker().setMaxDate(timeNow);
        datePickerDialog.getDatePicker().setMinDate(timeNow - years126);

        return datePickerDialog;
    }

}
