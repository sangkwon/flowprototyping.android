
package com.egloos.realmove.android.fp.db;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.view.LoadingDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PageListLoadTask extends AsyncTask<Integer, Void, Project> {

    private static final String TAG = PageListLoadTask.class.getSimpleName();

    private Context mContext;
    private Callback mCallback;
    private Dialog mProgressDialog;

    public PageListLoadTask(Context context, Callback callback) {
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

        DBAdapter db = null;
        try {
            db = new DBAdapter(mContext).open();
            Project project = db.selectProject(projectId);
            if (project == null)
                return null;

            ArrayList<Page> pages = db.selectPages(projectId);
            if (pages == null)
                return project;

            project.addAll(pages);

            int[] pageIds = new int[pages.size()];
            int count = 0;
            HashMap<Integer, Page> pageMap = new HashMap<Integer, Page>();
            for (Page page : pages) {
                pageIds[count++] = page.getId();
                pageMap.put(page.getId(), page);
            }

            ArrayList<Link> links = db.selectLinks(pageIds);
            if (links == null)
                return project;

            for (Link link : links) {
                Page page = pageMap.get(link.getPageId());
                page.add(link);
            }

            return project;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (db != null)
                db.close();
        }
        return null;
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
