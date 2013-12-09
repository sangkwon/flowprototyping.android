
package com.egloos.realmove.android.fp.model;

import com.egloos.realmove.android.fp.common.L;

import org.json.JSONObject;

import java.io.Serializable;

public class RectPosition implements Serializable {

	private static final long serialVersionUID = 855534910307849255L;

	private static final String TAG = RectPosition.class.getSimpleName();

	private int top, bottom, left, right;

	public RectPosition() {
		// do nothing
	}

	public RectPosition(int left, int top, int right, int bottom) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public JSONObject toJSONObject() {
		try {
			JSONObject jsonObj = new JSONObject();

			jsonObj.put("top", this.getTop());
			jsonObj.put("left", this.getLeft());
			jsonObj.put("bottom", this.getBottom());
			jsonObj.put("right", this.getRight());

			return jsonObj;
		} catch (Exception ex) {
			L.e(TAG, ex);
			return null;
		}
	}

	public static RectPosition fromJSONObject(JSONObject jsonObj) {
		if (jsonObj == null)
			return null;
		try {
			RectPosition rect = new RectPosition();
			rect.setTop(jsonObj.getInt("top"));
			rect.setLeft(jsonObj.getInt("left"));
			rect.setBottom(jsonObj.getInt("bottom"));
			rect.setRight(jsonObj.getInt("right"));
			return rect;
		} catch (Exception ex) {
			L.e(TAG, ex);
		}
		return null;
	}

	@Override
	public String toString() {
		return "RectPosition [top=" + top + ", bottom=" + bottom + ", left=" + left + ", right=" + right + "]";
	}

}
