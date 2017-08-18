package com.eric.update_app.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class UpdateDialog {
    public static void showDialog(Context context, String title, String msg, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener canclelistener) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", listener)
                .setNegativeButton("取消", canclelistener)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
