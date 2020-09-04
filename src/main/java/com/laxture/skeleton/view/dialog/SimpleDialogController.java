package com.laxture.skeleton.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;

import com.laxture.skeleton.R;
import com.laxture.lib.RuntimeContext;
import com.laxture.lib.util.UnHandledException;
import com.laxture.lib.view.date.WheelDatePicker;

import org.joda.time.DateTime;

public class SimpleDialogController extends DialogController {

    public static final String DIALOG_PROGRESS = "dialog_progress";
    public static final String DIALOG_ALERT = "dialog_alert";

    public SimpleDialogController(Activity activity) {
        super(activity);
    }

    public void showProgressDialog(String progressMessage) {
        showProgressDialog(progressMessage, false);
    }

    public void showProgressDialog(String progressMessage, boolean cancelable) {
        showDialog(DIALOG_PROGRESS, progressMessage, cancelable);
    }

    public void setProgressDialogText(String text) {
        Dialog dialog = getDialog(DIALOG_PROGRESS);
        if (dialog == null || !(dialog instanceof ProgressDialog)) return;
        ProgressDialog progressDialog = (ProgressDialog) dialog;
        progressDialog.setMessage(text);
    }

    public void dismissProgressDialog() {
        dismissDialog(DIALOG_PROGRESS);
    }

    public void showAlertDialog(String alertMessage) {
        showDialog(DIALOG_ALERT, alertMessage);
    }

    public void dismissAlertDialog() {
        dismissDialog(DIALOG_ALERT);
    }

    @Override
    public Dialog prepareDialog(String dialogName, Object...params) {

        if (DIALOG_PROGRESS.equals(dialogName)) {
            boolean cancelable = params.length >= 2 ? (Boolean) params[1] : false;
            return getProgressDialog(dialogName,
                    params[0].toString(),
                    cancelable, null);

        } else if (DIALOG_ALERT.equals(dialogName)) {
            return getAlertDialog(dialogName,
                    null,
                    params[0].toString(),
                    RuntimeContext.getString(R.string.label_ok),
                    true, null);

        } else throw new UnHandledException("Unknown dialog name");
    }

    //*************************************************************************
    //  Util Dialog - Text Input
    //*************************************************************************

    public interface OnTextSetListener {
        void onTextSet(EditText editText, String text);
    }

    public static AlertDialog buildTextInputDialog(Context context, String title,
                                                   final OnTextSetListener callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.text_input_dialog)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog textInputDialog = (AlertDialog) dialog;
                        EditText editText = (EditText) textInputDialog.findViewById(R.id.input_text);
                        if (callBack != null) {
                            editText.clearFocus();
                            callBack.onTextSet(editText, editText.getText().toString());
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public static EditText getEditTextInDialog(AlertDialog dialog) {
        return (EditText) dialog.findViewById(R.id.input_text);
    }

    //*************************************************************************
    //  Util Dialog - Text Input
    //*************************************************************************

    public interface OnDateSetListener {
        void onDateSet(WheelDatePicker mDatePicker, DateTime dateTime);
    }

    public static AlertDialog buildWheelDatePickerDialog(Context context, String title,
                                                         final OnDateSetListener callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.wheel_date_picker_dialog)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_menu_my_calendar)
                .setCancelable(true)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog datePickerDialog = (AlertDialog) dialog;
                        WheelDatePicker datePicker = (WheelDatePicker) datePickerDialog.findViewById(R.id.customDatePicker);
                        if (callBack != null) {
                            callBack.onDateSet(datePicker, datePicker.getCurrentTime());
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    public static WheelDatePicker getWheelDatePickerInDialog(AlertDialog dialog) {
        return (WheelDatePicker) dialog.findViewById(R.id.customDatePicker);
    }

}
