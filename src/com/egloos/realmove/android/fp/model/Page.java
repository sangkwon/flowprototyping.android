
package com.egloos.realmove.android.fp.model;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.util.ProjectManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable {

    private static final long serialVersionUID = -7062598914015445090L;

    private final static String TAG = Page.class.getSimpleName();

    private int projectId;
    private int id;
    private ArrayList<Link> links;
    private String name;
    private String imagePath;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void add(Link link) {
        if (links == null)
            links = new ArrayList<Link>();
        links.add(link);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObj = new JSONObject();

            jsonObj.putOpt("id", this.getId());
            jsonObj.putOpt("name", this.getName());
            jsonObj.putOpt("imagePath", this.getImagePath());

            JSONArray linkObj = new JSONArray();
            if (links != null) {
                for (Link link : links) {
                    linkObj.put(link.toJSONObject());
                }
                jsonObj.put("links", linkObj);
            }

            return jsonObj;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
            return null;
        }
    }

    public static Page fromJSONObject(JSONObject jsonObj) {
        return fromJSONObject(jsonObj, 0);
    }

    public static Page fromJSONObject(JSONObject jsonObj, int projectId) {

        try {
            Page page = new Page();

            page.setId(jsonObj.getInt("id"));
            page.setName(jsonObj.optString("name"));
            page.setProjectId(projectId);
            page.setImagePath(jsonObj.optString("imagePath"));

            JSONArray linksObj = jsonObj.optJSONArray("links");
            if (linksObj != null) {
                for (int i = 0; i < linksObj.length(); i++) {
                    page.add(Link.fromJSONObject(linksObj.getJSONObject(i)));
                }
            }
            return page;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        }
        return null;
    }

}
