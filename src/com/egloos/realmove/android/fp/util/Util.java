
package com.egloos.realmove.android.fp.util;

import com.egloos.realmove.android.fp.common.FpLog;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {

	private static final String TAG = Util.class.getSimpleName();

	private Util() {
		// do nothing
	}

	/**
	 * dp를 px로 변환
	 * 
	 * @param context
	 * @param dp
	 * @return
	 */
	public static float dpToPx(Context context, float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	/**
	 * px를 dp로 변환
	 * 
	 * @param context
	 * @param px
	 * @return
	 */
	public static float pxToDp(Context context, float px) {
		return (px * (160 / context.getResources().getDisplayMetrics().density));
	}

	/**
	 * 확장자만 반환
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileExtension(String path) {
		if (path == null)
			return null;
		int idx = path.lastIndexOf('.');
		if (idx < 0)
			return "";
		return path.substring(idx);
	}

	/**
	 * 확장자 없이 파일명만 반환
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		if (path == null)
			return null;
		String p = path;
		int idx = p.lastIndexOf('/');
		if (idx >= 0) {
			p = p.substring(idx + 1);
		}

		idx = p.lastIndexOf('.');
		if (idx < 0)
			return p;

		return p.substring(0, idx);
	}

	public static boolean copyFile(String oldPath, String newPath) {
		try {
			// TODO image resize needed?

			File oldFile = new File(oldPath);
			File newFile = new File(newPath);
			if (!newFile.exists()) {
				newFile.createNewFile();
			} else if (newFile.isDirectory()) {
				newFile.delete();
				newFile.createNewFile();
			}

			BufferedOutputStream fo = new BufferedOutputStream(new FileOutputStream(newFile));
			BufferedInputStream fi = new BufferedInputStream(new FileInputStream(oldFile));

			byte[] buffer = new byte[1024];
			while (true) {
				int len = fi.read(buffer);
				if (len <= 0)
					break;
				fo.write(buffer);
			}

			fo.close();
			fi.close();

			return true;
		} catch (IOException ex) {
			FpLog.e(TAG, ex);
		}
		return false;
	}

	/**
	 * @param path
	 * @return 준비되어 사용가능하다면 true
	 */
	public static boolean prepareDir(String path) {
		return prepareDir(new File(path));
	}

	public static boolean prepareDir(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory())
				return true;
			return false;
		} else {
			boolean created = dir.mkdir();
			FpLog.d(TAG, "prepareDir", dir, created);
			if (!created) {
				File parent = dir.getParentFile();
				if ("/".equals(parent.getAbsoluteFile()))
					return false;
				created = prepareDir(parent);
			}
			return created;
		}
	}

	public static DisplayMetrics getDisplayMetrics(Activity window) {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}
}
