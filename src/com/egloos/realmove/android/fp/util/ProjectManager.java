
package com.egloos.realmove.android.fp.util;

import com.egloos.realmove.android.fp.App;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Project;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ProjectManager {

	private static final String TAG = ProjectManager.class.getSimpleName();

	private static final String METAFILE_NAME = "project.json";

	public static File getProjectPath(int projectId) {
		return App.getInstacne().getApplicationContext().getDir("proj_" + projectId, 0);
	}

	public static Project load(int projectId) {
		FileReader fi = null;
		try {
			File metaFile = new File(getProjectPath(projectId), METAFILE_NAME);
			StringWriter sw = new StringWriter();
			fi = new FileReader(metaFile);
			char[] buf = new char[1024];
			while (true) {
				int len = fi.read(buf);
				if (len <= 0)
					break;
				sw.write(buf);
			}

			sw.close();
			JSONObject jsonObj = new JSONObject(sw.toString());
			return Project.fromJSONObject(jsonObj);
		} catch (Exception ex) {
			FpLog.e(TAG, ex);
		} finally {
			try {
				if (fi != null)
					fi.close();
			} catch (Exception e) {
				// do nothing
			}
		}
		return null;
	}

	public static boolean writeMetaFile(Project project) {
		PrintWriter out = null;
		try {
			FpLog.d(TAG, "saveMetaFile()");
			File path = getProjectPath(project.getId());
			if (!Util.prepareDir(path))
				return false;

			File metaFile = new File(path, METAFILE_NAME);
			JSONObject json = project.toJSONObject();
			out = new PrintWriter(new FileWriter(metaFile));
			out.print(json.toString());

			return true;
		} catch (Exception ex) {
			FpLog.e(TAG, ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// do nothing
				}
			}
		}
		return false;
	}

	public static Project createSampleProject() {
		Project tmpProj = new Project();
		tmpProj.setId(1);
		tmpProj.setSubject("Sample");

		if (ProjectManager.writeMetaFile(tmpProj))
			return tmpProj;
		else
			return null;
	}

}
