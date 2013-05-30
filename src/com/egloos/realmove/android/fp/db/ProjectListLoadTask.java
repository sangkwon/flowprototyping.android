
package com.egloos.realmove.android.fp.db;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.util.ProjectManager;
import com.egloos.realmove.android.fp.view.LoadingDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

public class ProjectListLoadTask extends AsyncTask<Void, Void, ArrayList<Project>> {

    private static final String TAG = ProjectListLoadTask.class.getSimpleName();

    private Context mContext;
    private Callback mCallback;
    private Dialog mProgressDialog;

    public ProjectListLoadTask(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = LoadingDialog.show(mContext);
        super.onPreExecute();
    }

    @Override
    protected ArrayList<Project> doInBackground(Void... params) {
        DBAdapter db = null;
        try {
            db = new DBAdapter(mContext).open();
            return db.selectProjects();
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (db != null)
                db.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Project> tmpProj) {
        if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception ex) {
                FpLog.e(TAG, ex);
            }
            mProgressDialog = null;
        }

        if (mCallback != null) {
            mCallback.onLoad(tmpProj);
        }
        super.onPostExecute(tmpProj);
    }

    public interface Callback {
        public void onLoad(ArrayList<Project> projects);
    }
}
