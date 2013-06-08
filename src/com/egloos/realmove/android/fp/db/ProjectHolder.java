
package com.egloos.realmove.android.fp.db;

import com.egloos.realmove.android.fp.model.Project;

import android.content.Context;
import android.os.Handler;

/**
 * 프로젝트 정보를 메모리에 가져있음
 * 
 * @author sangkwon
 */
public class ProjectHolder {

    private Project mProject;
    private ProjectHolder instance;

    private ProjectHolder() {
        // for singleton
    }

    public ProjectHolder getInstance() {
        if (instance == null) {
            synchronized (ProjectHolder.class) {
                instance = new ProjectHolder();
            }
        }
        return instance;
    }

    public void load(final Context context, final int projectId, final Callback callback) {
        if (mProject != null && mProject.getId() == projectId) {
            invokeCallback(callback, mProject);
        }

        new LoadProjectTask(context, new LoadProjectTask.Callback() {
            public void onLoad(Project project) {
                mProject = project;
                invokeCallback(callback, project);
            }
        }).execute(projectId);
    }

    public void invokeCallback(final Callback callback, final Project project) {
        if (callback != null) {
            new Handler().post(new Runnable() {
                public void run() {
                    callback.onLoad(project);
                }
            });
        }
    }

    public interface Callback {
        public void onLoad(Project project);
    }
}
