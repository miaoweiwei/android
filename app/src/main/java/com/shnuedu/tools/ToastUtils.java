package com.shnuedu.tools;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.app.AlertDialog;

import com.shnuedu.goodmother.R;

public class ToastUtils {
    public static void showToast(Context context, String msg) {
        showToast(context, msg, "提示", R.mipmap.ic_launcher_round);
    }

    public static void showToast(Context activity, String msg, String title) {
        showToast(activity, msg, title, R.mipmap.ic_launcher_round);
    }

    public static void showToast(Context context, String msg, String title, @DrawableRes int iconId) {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setIcon(iconId);
        messageBox.setTitle(title);
        messageBox.setMessage(msg);
        messageBox.create();
        //messageBox.show();
        System.out.println(msg);
    }
}
