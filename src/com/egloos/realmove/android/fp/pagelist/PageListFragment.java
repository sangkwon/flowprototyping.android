
package com.egloos.realmove.android.fp.pagelist;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.egloos.realmove.android.fp.PageEditActivity;
import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.db.ProjectLoadTask;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.util.ProjectManager;
import com.egloos.realmove.android.fp.util.Util;
import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PageListFragment extends BaseFragment implements OnItemClickListener,
        OnItemLongClickListener {

    public static final String TAG = PageListFragment.class.getSimpleName();

    public static final String EXTRA_PROJECT_ID = "projectId";

    public static final String SP_NAME = "fp";
    public static final String SP_KEY_WORKING_PROJ = "working_proj";

    private static final int REQ_CODE_ADD_PAGE_FROM_GALLERY = 2;
    private static final int REQ_CODE_ADD_PAGE_FROM_CAMERA = 3;
    private static final int REQ_CODE_PAGE_EDIT = 100;

    public enum Mode {
        /** 수정 등이 가능한 평상시 모드 */
        NORMAL,

        /** 페이지 선택만 가능한 대화상자 모드 */
        SELECT
    }

    private static Mode mMode = Mode.NORMAL;

    private PageListAdapter mAdapter;
    private Project mProject;
    private ImageFetcher mImageFetcher;
    private ActionMode mActionMode;
    private View mContentView;

    private Uri mImageCaptureUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
                "page_list");
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.page_thumbnail_size);
        mImageFetcher = new ImageFetcher(getActivity(), size, size);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);

        mAdapter = new PageListAdapter(getActivity(), mImageFetcher);
    }

    public static PageListFragment newInstance(int projectId, int pageId) {
        return newInstance(projectId, pageId, Mode.NORMAL);
    }

    public static PageListFragment newInstance(int projectId, int pageId, Mode mode) {
        PageListFragment instance = new PageListFragment();

        Bundle args = new Bundle();
        args.putInt(PageListFragment.EXTRA_PROJECT_ID, projectId);
        args.putInt("selectedPageId", pageId);
        instance.setArguments(args);

        mMode = mode;

        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FpLog.d(TAG, "onCreateDialog()");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_page_to_link);

        if (mContentView == null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            mContentView = createView(inflater, null);
            builder.setView(mContentView);
        }

        return builder.create();
    }

    /**
     * 실제 view 를 생성한다.
     * 
     * @param inflater
     * @param container
     * @return
     */
    private View createView(LayoutInflater inflater, ViewGroup container) {
        FpLog.d(TAG, "createView()");
        final View view = inflater.inflate(R.layout.page_list_fragment, container, false);
        final GridView gridView = (GridView) view.findViewById(R.id.grid);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

        gridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // FpLog.e(TAG, "onGlobalLayout()");
                        if (mAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(gridView.getWidth()
                                    / (mAdapter.getWidth() + mAdapter.getColumnSpacing()));
                            if (numColumns > 0) {
                                final int columnWidth = (gridView.getWidth() / numColumns)
                                        - mAdapter.getColumnSpacing();
                                final int columnHeight = (int) (columnWidth * 1.1f);

                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setWidth(columnWidth);
                                mAdapter.setHeight(columnHeight);
                                FpLog.d(TAG, "GridView size reset: width=" + columnWidth
                                        + " height=" + columnHeight);
                            }
                        }
                    }
                });

        return view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FpLog.d(TAG, "onCreateView()");

        int projectId = getActivity().getIntent().getIntExtra(PageListFragment.EXTRA_PROJECT_ID, 0);

        if (projectId > 0) {
            load(projectId);
        } else {
            loadWorkingProject();
        }

        /* Dialog인 경우에는 onCreateDialog()에서 이미 생성되었다 */
        if (mContentView == null) {
            mContentView = createView(inflater, container);
            return mContentView;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mImageFetcher != null) {
            mImageFetcher.closeCache();
            mImageFetcher = null;
        }

        mAdapter = null;
        super.onDestroy();
    }

    private void load(int projectId) {
        ProjectLoadTask mLoadTask = new ProjectLoadTask(mContext, new ProjectLoadTask.Callback() {
            @Override
            public void onLoad(Project project) {
                onProjectLoad(project);
            }
        });
        mLoadTask.execute(projectId);
    }

    private void loadWorkingProject() {
        int workingProjectId = 0;

        // TODO SharedPreference 에서 읽기
        workingProjectId = 1;

        if (workingProjectId > 0) {
            load(workingProjectId);
        } else {
            load(-1);
        }
    }

    void onProjectLoad(Project tmpProj) {
        if (tmpProj == null) {
            Toast.makeText(getActivity(), R.string.error_on_loading_project, Toast.LENGTH_SHORT)
                    .show();

            // TODO what?

            return;
        }

        mProject = tmpProj;
        mAdapter.setPages(mProject);
        mAdapter.notifyDataSetChanged();
    }

    class SaveTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ProjectManager.writeMetaFile(mProject);
            return null;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.page_list_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO "Edit" 메뉴는 상황에 따라 diable 시킴
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gallery: {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, REQ_CODE_ADD_PAGE_FROM_GALLERY);
                break;
            }
            case R.id.camera: {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File path = getCameraPath();
                String filename = new SimpleDateFormat("yyyyMMdd_HH:mm:ss_S").format(new Date())
                        + ".jpg";

                Util.prepareDir(path);
                mImageCaptureUri = Uri.fromFile(new File(path, filename));

                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, REQ_CODE_ADD_PAGE_FROM_CAMERA);
                break;
            }
            case R.id.web: {
                Toast.makeText(getActivity(), R.string.not_implemented_yet, Toast.LENGTH_SHORT)
                        .show();
                break;
            }
            case R.id.play:

                break;
            case R.id.edit:
                setActionMode(true);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void imageScan(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private static File getCameraPath() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return path;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FpLog.d(TAG, "onActivityResult() ", requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQ_CODE_ADD_PAGE_FROM_GALLERY: {
                    Uri uri = data.getData();
                    if (uri != null) {
                        CopyPictureTask task = new CopyPictureTask();
                        task.execute(uri);
                    }
                    break;
                }
                case REQ_CODE_ADD_PAGE_FROM_CAMERA: {
                    imageScan(mImageCaptureUri);
                    CopyPictureTask task = new CopyPictureTask();
                    task.execute(mImageCaptureUri);
                    mImageCaptureUri = null;
                    break;
                }
                case REQ_CODE_PAGE_EDIT: {
                    load(mProject.getId());
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean copyGalleryImage(Uri imageUri) {
        Cursor cursor = null;
        try {
            String[] proj = {
                    MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME
            };
            cursor = getActivity().getContentResolver().query(imageUri, proj, null, null, null);
            if (null != cursor) {
                int pathColIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int nameColIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                if (cursor.moveToFirst()) {
                    String path = cursor.getString(pathColIdx);
                    String name = cursor.getString(nameColIdx);
                    FpLog.d(TAG, "copyGalleryImage() ", "name=", name, "path=", path);
                    if (path != null && name != null) {
                        return addToProject(name, path);
                    }
                }
            }
        } catch (Exception ex) {
            FpLog.e(TAG, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;

    }

    boolean addToProject(String name, String orgImagePath) {
        synchronized (getActivity()) {
            int newId = mProject.findNewPageId();
            if (newId < 0) {
                return false;
            }
            Page page = new Page();
            page.setId(newId);
            page.setName(name);
            page.setProjectId(mProject.getId());

            String orgFileName = Util.getFileName(orgImagePath);
            String orgFileExt = Util.getFileExtension(orgImagePath);
            File newFile = new File(ProjectManager.getProjectPath(mProject.getId()), orgFileName
                    + "." + orgFileExt);
            int count = 1;
            while (newFile.exists() && count < 100) {
                newFile = new File(ProjectManager.getProjectPath(mProject.getId()), orgFileName
                        + "_" + count + "." + orgFileExt);
                count++;
            }

            if (count >= 100)
                return false;

            String newPath = newFile.getAbsolutePath();
            page.setImagePath(newPath);

            boolean success = false;

            // TODO image resize needed?
            if (Util.copyFile(orgImagePath, newPath)) {
                mProject.add(page);
                success = ProjectManager.writeMetaFile(mProject);
            }

            return success;
        }
    }

    boolean copyFileImage(Uri imageUri) {
        List<String> names = imageUri.getPathSegments();
        String name = names.get(names.size() - 1);
        String path = imageUri.getPath();
        return addToProject(name, path);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Page clickedPage = mAdapter.getItem(position);
        if (mMode == Mode.NORMAL) {
            if (mActionMode != null) {
                selectPageOn(clickedPage);
            } else {
                Intent intent = new Intent(getActivity(), PageEditActivity.class);
                intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, mProject.getId());
                // intent.putExtra(PageEditActivity.EXTRA_PAGE_POS, position);
                intent.putExtra("page", clickedPage);
                startActivityForResult(intent, REQ_CODE_PAGE_EDIT);
            }
        } else if (mMode == Mode.SELECT) {
            if (getDialog() != null)
                this.getDialog().dismiss();

            if (mSelectCallback != null) {
                mSelectCallback.pageSelected(clickedPage);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mMode == Mode.NORMAL) {
            selectPageOn(mAdapter.getItem(position));
        }
        return true;
    }

    void resetActionMode() {
        int selectedCount = mAdapter.countSelectedPage();

        if (selectedCount == 0 && mActionMode != null) {
            setActionMode(false);
        } else if (selectedCount > 0 && mActionMode == null) {
            setActionMode(true);
        }

        if (mActionMode != null) {
            mActionMode.setTitle(getActivity().getResources().getString(R.string.title_selected,
                    selectedCount));
        }

    }

    private boolean selectPageOn(Page page) {
        mAdapter.togglePageSelection(page);
        mAdapter.notifyDataSetChanged();

        resetActionMode();

        return true;
    }

    private void setActionMode(boolean actionOn) {
        if (actionOn) {
            mActionMode = getSherlockActivity().startActionMode(new ActionModeCallback());
        } else {
            mActionMode.finish();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(R.string.menu_remove).setIcon(android.R.drawable.ic_menu_delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Resources res = getActivity().getResources();
            if (res.getString(R.string.menu_remove).equals(item.getTitle())) {
                int count = mAdapter.countSelectedPage();
                if (count > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.remove_all_selected_pages);
                    builder.setPositiveButton(R.string.remove,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mAdapter.countSelectedPage() > 0) {
                                        RemovePageTask task = new RemovePageTask();
                                        task.execute(mAdapter.getSelectedPages().toArray(
                                                new Page[0]));
                                    } else {
                                        Toast.makeText(getActivity(), R.string.no_page_selected,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.no_page_selected);
                    builder.setPositiveButton(R.string.remove, null);
                    builder.show();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // do nothing
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mAdapter.clearSelection();
            mAdapter.notifyDataSetChanged();
        }

    }

    class RemovePageTask extends AsyncTask<Page, Void, Void> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null, null);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Page... pages) {
            for (Page page : pages) {
                mAdapter.togglePageSelection(page);

                mProject.remove(page);
                ProjectManager.writeMetaFile(mProject);

                new File(page.getImagePath()).delete();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

            mAdapter.notifyDataSetChanged();
            resetActionMode();

            super.onPostExecute(result);
        }
    }

    class CopyPictureTask extends AsyncTask<Uri, Void, Boolean> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null, null);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            Uri imageUri = params[0];
            boolean success = false;
            if (imageUri.getScheme().startsWith("content")) {
                success = copyGalleryImage(imageUri);
            } else {
                success = copyFileImage(imageUri);
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

            if (success) {
                mAdapter.notifyDataSetChanged();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.fail_to_add);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }

            super.onPostExecute(success);
        }
    }

    private SelectCallback mSelectCallback;

    public void setmSelectCallback(SelectCallback mSelectCallback) {
        this.mSelectCallback = mSelectCallback;
    }

    public interface SelectCallback {
        public void pageSelected(Page page);
    }

}
