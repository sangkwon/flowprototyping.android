
package com.egloos.realmove.android.fp.model;

import com.egloos.realmove.android.fp.common.L;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Project extends ArrayList<Page> {

	private static final String TAG = Project.class.getSimpleName();

	private static final int MAX_PAGES = 30;

	private int id;
	private String subject;
	private String mainImage;
	private Date created;
	private Date updated;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setCreated(int timeSeconds) {
		if (timeSeconds > 0)
			setCreated(new Date(timeSeconds * 1000L));
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public void setUpdated(int timeSeconds) {
		if (timeSeconds > 0)
			setUpdated(new Date(timeSeconds * 1000L));
	}

	public Page findPage(int pageId) {
		for (Page page : this) {
			if (page.getId() == pageId)
				return page;
		}
		return null;
	}

	public int findNewPageId() {
		int maxPageId = 0;

		if (this.size() > MAX_PAGES)
			return -1;

		for (Page page : this) {
			if (page.getId() > maxPageId) {
				maxPageId = page.getId();
			}
		}

		return maxPageId + 1;
	}

	public JSONObject toJSONObject() {
		try {
			JSONObject jsonObj = new JSONObject();

			jsonObj.putOpt("id", this.getId());
			jsonObj.putOpt("subject", this.getSubject());
			jsonObj.putOpt("mainImage", this.getMainImage());
			if (this.getCreated() != null)
				jsonObj.putOpt("created", this.getCreated().getTime());
			if (this.getUpdated() != null)
				jsonObj.putOpt("updated", this.getUpdated().getTime());

			JSONArray pagesObj = new JSONArray();
			for (Page page : this) {
				pagesObj.put(page.toJSONObject());
			}
			jsonObj.put("pages", pagesObj);

			return jsonObj;
		} catch (Exception ex) {
			L.e(TAG, ex);
			return null;
		}
	}

	public static Date fromTime(long time) {
		if (time == Long.MIN_VALUE)
			return null;
		return new Date(time);
	}

	public static Project fromJSONObject(JSONObject jsonObj) {
		try {
			Project project = new Project();

			project.setId(jsonObj.getInt("id"));
			project.setSubject(jsonObj.optString("subject"));
			project.setMainImage(jsonObj.optString("mainImage"));
			project.setCreated(fromTime(jsonObj.optLong("created", Long.MIN_VALUE)));
			project.setUpdated(fromTime(jsonObj.optLong("updated", Long.MIN_VALUE)));

			JSONArray pagesObj = jsonObj.optJSONArray("pages");
			if (pagesObj != null) {
				for (int i = 0; i < pagesObj.length(); i++) {
					project.add(Page.fromJSONObject(pagesObj.getJSONObject(i), project.getId()));
				}
			}
			return project;
		} catch (Exception ex) {
			L.e(TAG, ex);
		}
		return null;
	}

	public String getMainImage() {
		return mainImage;
	}

	public void setMainImage(String mainImage) {
		this.mainImage = mainImage;
	}

}
