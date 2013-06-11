
package com.egloos.realmove.android.fp.db;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

@Deprecated
public class LoadPageTask extends AsyncTask<Integer, Void, Page> {

	private static final String TAG = LoadPageTask.class.getSimpleName();

	private Context mContext;
	private Callback mCallback;
	private Dialog mProgressDialog;

	public LoadPageTask(Context context, Callback callback) {
		mContext = context;
		mCallback = callback;
	}

	@Override
	protected void onPreExecute() {
		// mProgressDialog = LoadingDialog.show(mContext);
		super.onPreExecute();
	}

	@Override
	protected Page doInBackground(Integer... params) {
		int pageId = params[0];

		DBAdapter db = null;
		try {
			db = new DBAdapter(mContext).open();

			Page page = db.selectPage(pageId);

			ArrayList<Link> links = db.selectLinks(pageId);
			if (links == null)
				return page;

			for (Link link : links) {
				page.add(link);
			}

			return page;
		} catch (Exception ex) {
			FpLog.e(TAG, ex);
		} finally {
			if (db != null)
				db.close();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Page page) {
		if (mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception ex) {
				FpLog.e(TAG, ex);
			}
			mProgressDialog = null;
		}

		if (mCallback != null) {
			mCallback.onLoad(page);
		}
		super.onPostExecute(page);
	}

	public interface Callback {
		public void onLoad(Page page);
	}
}
