
package com.egloos.realmove.android.fp.db;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.util.ProjectManager;
import com.egloos.realmove.android.fp.view.LoadingDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

public class ProjectLoadTask extends AsyncTask<Integer, Void, Project> {

    private static final String TAG = ProjectLoadTask.class.getSimpleName();

    private Context mContext;
    private Callback mCallback;
    private Dialog mProgressDialog;

    public ProjectLoadTask(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = LoadingDialog.show(mContext);
        super.onPreExecute();
    }

    @Override
    protected Project doInBackground(Integer... params) {
        int projectId = params[0];
        if (projectId == -1) {
            return ProjectManager.createSampleProject();
        } else {
            return ProjectManager.load(projectId);
        }
    }

    @Override
    protected void onPostExecute(Project tmpProj) {
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
        public void onLoad(Project project);
    }
}
