
package com.egloos.realmove.android.fp.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

public class LoadingDialog extends Dialog {

	public LoadingDialog(Context context) {
		super(context);
	}

	public static Dialog show(Context context) {
		ProgressDialog progressDialog = ProgressDialog.show(context, null, "Loading");
		return progressDialog;
	}

}
