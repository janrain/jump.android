package com.janrain.android.simpledemo;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static DatePickerFragment getInstance(Date currentDate) {
        final DatePickerFragment fragment = new DatePickerFragment();
        final Bundle arguments = new Bundle(1);

        final Calendar c = Calendar.getInstance();
        if (currentDate != null) {
            c.setTime(currentDate);
        }

        arguments.putInt("year", c.get(Calendar.YEAR));
        arguments.putInt("month", c.get(Calendar.MONTH));
        arguments.putInt("day", c.get(Calendar.DAY_OF_MONTH));
        fragment.setArguments(arguments);

        return fragment;
    }

    private DatePickerDialog.OnDateSetListener dateSetListener;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Bundle arguments = getArguments();
        int year = arguments.getInt("year");
        int month = arguments.getInt("month");
        int day = arguments.getInt("day");

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (dateSetListener != null) {
            dateSetListener.onDateSet(view, year, month, dayOfMonth);
        }
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        dateSetListener = listener;
    }
}
