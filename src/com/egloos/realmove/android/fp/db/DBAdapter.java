
package com.egloos.realmove.android.fp.db;

import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.model.RectPosition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBAdapter {

    private static final String TAG = DBAdapter.class.getSimpleName();

    private static final String DB_NAME = "fp";
    private static final int DB_VERSION = 1;

    private static final String TBL_PROJECTS = "project";
    private static final String TBL_PAGES = "pages";
    private static final String TBL_LINKS = "links";

    private static final String ID = "_id";
    private static final String SUBJECT = "subject";
    private static final String CREATED = "created";
    private static final String UPDATED = "updated";
    private static final String MAIN_IMAGE = "main_image";

    private static final String CREATE_TBL_PROJECT = "CREATE TABLE " + TBL_PROJECTS + " ("
            + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + SUBJECT + " TEXT,"
            + MAIN_IMAGE + " TEXT,"
            + CREATED + " INTEGER,"
            + UPDATED + " INTEGER);";

    private static final String PROJECT_ID = "proj_id";
    private static final String NAME = "name";
    private static final String IMAGE_PATH = "imagePath";

    private static final String CREATE_TBL_PAGE = "CREATE TABLE " + TBL_PAGES + " ("
            + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + PROJECT_ID + " INTEGER,"
            + NAME + " TEXT,"
            + IMAGE_PATH + " TEXT);";

    private static final String EVENT = "event";
    private static final String PAGE_ID = "page_id";
    private static final String ANIM = "anum";
    private static final String TARGET_PAGE_ID = "target_page_id";
    private static final String POS_LEFT = "pos_left";
    private static final String POS_TOP = "pos_top";
    private static final String POS_RIGHT = "pos_right";
    private static final String POS_BOTTOM = "pos_bottom";

    private static final String CREATE_TBL_LINK = "CREATE TABLE " + TBL_LINKS + " ("
            + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + PAGE_ID + " INTEGER,"
            + EVENT + " TEXT,"
            + ANIM + " TEXT,"
            + TARGET_PAGE_ID + " INTEGER,"
            + POS_LEFT + " INTEGER,"
            + POS_TOP + " INTEGER,"
            + POS_RIGHT + " INTEGER,"
            + POS_BOTTOM + " INTEGER);";

    class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            FpLog.i(TAG, "onCreate()");
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TBL_PROJECT);
                db.execSQL(CREATE_TBL_PAGE);
                db.execSQL(CREATE_TBL_LINK);

                // TODO insert sample project

                db.setTransactionSuccessful();
            } catch (Exception ex) {
                FpLog.e(TAG, ex);
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            FpLog.i(TAG, "onUpgrade()");
            db.beginTransaction();
            try {
                // TODO
            } catch (Exception e) {
                FpLog.e(TAG, e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private DatabaseHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase mDb;

    public DBAdapter(Context context) {
        dbHelper = new DatabaseHelper(context);
        this.mContext = context;
    }

    public DBAdapter open() throws Exception {
        try {
            dbHelper = new DatabaseHelper(mContext);
            mDb = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            FpLog.e(TAG, ex);
            throw ex;
        }
        return this;
    }

    public void close() {
        if (mDb != null)
            mDb.close();
        if (dbHelper != null)
            dbHelper.close();
    }

    public int getLastId(String tableName) {
        String[] projection = new String[] {
                "MAX(" + ID + ")"
        };
        Cursor cursor = mDb.query(tableName, projection, null, null, null, null, null, null);
        if (cursor.moveToNext()) {
            return cursor.getInt(0);
        }
        return 0;
    }

    public ArrayList<Project> selectProjects() throws Exception {
        Cursor cursor = null;
        try {
            String[] projection = new String[] {
                    ID, SUBJECT, MAIN_IMAGE, CREATED, UPDATED
            };
            cursor = mDb.query(TBL_PROJECTS, projection, null, null, null, null, null, null);
            ArrayList<Project> projects = new ArrayList<Project>();
            while (cursor.moveToNext()) {
                Project project = new Project();
                project.setId(cursor.getInt(0));
                project.setSubject(cursor.getString(1));
                project.setMainImage(cursor.getString(2));
                project.setCreated(cursor.getInt(3));
                project.setUpdated(cursor.getInt(4));
                projects.add(project);
            }
            return projects;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public ArrayList<Page> selectPages(int projectId) throws Exception {
        Cursor cursor = null;
        try {
            String[] projection = new String[] {
                    ID, PROJECT_ID, NAME, IMAGE_PATH
            };
            String[] selectionArgs = new String[] {
                    "" + projectId
            };
            cursor = mDb.query(TBL_PROJECTS, projection, "project_i 0--d=", selectionArgs, null,
                    null,
                    null, null);
            ArrayList<Page> pages = new ArrayList<Page>();
            while (cursor.moveToNext()) {
                Page page = new Page();
                page.setId(cursor.getInt(0));
                page.setProjectId(cursor.getInt(1));
                page.setName(cursor.getString(2));
                page.setImagePath(cursor.getString(3));
                pages.add(page);
            }
            return pages;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public ArrayList<Link> selectLinks(int[] pageIds) throws Exception {
        Cursor cursor = null;
        try {
            String[] projection = new String[] {
                    ID, PAGE_ID, EVENT, ANIM, TARGET_PAGE_ID, POS_LEFT, POS_TOP, POS_RIGHT,
                    POS_BOTTOM
            };
            String selection = null;
            for (int pageId : pageIds) {
                if (selection == null) {
                    selection = PAGE_ID + " in (";
                } else {
                    selection = selection + ",";
                }
                selection = selection + pageId;
            }
            cursor = mDb.query(TBL_PROJECTS, projection, selection, null, null, null, null, null);
            ArrayList<Link> links = new ArrayList<Link>();
            while (cursor.moveToNext()) {
                Link link = new Link();
                link.setId(cursor.getInt(0));
                link.setPageId(cursor.getInt(1));
                link.setEvent(Link.Event.valueOf(cursor.getString(2)));
                link.setAnim(Link.Anim.valueOf(cursor.getString(3)));
                link.setTargetPageId(cursor.getInt(4));
                RectPosition pos = new RectPosition();
                pos.setLeft(cursor.getInt(5));
                pos.setTop(cursor.getInt(6));
                pos.setRight(cursor.getInt(7));
                pos.setBottom(cursor.getInt(8));
                link.setPosition(pos);
                links.add(link);
            }
            return links;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public void updateProject(Project project) throws Exception {
        // TODO
    }

    public void updatePage(Page page) throws Exception {
        // TODO
    }

    public void updateLink(Link link) throws Exception {
        // TODO
    }

    public int insertProject(Project project) throws Exception {
        Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("SUBJECT", project.getSubject());
            values.put("MAIN_IMAGE", project.getSubject());
            values.put("CREATED", project.getSubject());
            values.put("UPDATED", project.getSubject());
            mDb.insert(TBL_PROJECTS, null, values);

            int newId = getLastId(TBL_PROJECTS);
            project.setId(newId);
            return newId;
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return -1;
    }

    public int insertPage(Page page) throws Exception {
        // TODO
        return getLastId(TBL_PAGES);
    }

    public int insertLink(Link link) throws Exception {
        // TODO
        return getLastId(TBL_LINKS);
    }

}
