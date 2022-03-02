package com.example.socialmediaapp;

import android.app.DatePickerDialog;
import android.content.Context;

public class DatePickerWithNeutral extends DatePickerDialog {

    public DatePickerWithNeutral(Context context, DatePickerDialog.OnDateSetListener callBack,
                                 int year, int monthOfYear, int dayOfMonth) {
        super(context, 0, callBack, year, monthOfYear, dayOfMonth);

        setButton(BUTTON_POSITIVE, ("Ok"), this);
        setButton(BUTTON_NEUTRAL, ("Something"), this); // ADD THIS
        setButton(BUTTON_NEGATIVE, ("Cancel"), this);
    }
}
