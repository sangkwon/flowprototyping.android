
package com.egloos.realmove.android.fp.activity;

import com.aviary.android.feather.FeatherActivity;
import com.aviary.android.feather.library.Constants;
import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.common.ImageUtil;
import com.egloos.realmove.android.fp.db.DBAdapter;
import com.egloos.realmove.android.fp.db.ProjectHolder;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.util.ProjectManager;
import com.egloos.realmove.android.fp.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PageListFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, ProjectHolder.Callback {

	public static final String TAG = PageListFragment.class.getSimpleName();
	public static final String TAG_DIALOG = "DIALOG_" + PageListFragment.class.getSimpleName();

	public static final String EXTRA_PROJECT_ID = "projectId";
	public static final String EXTRA_SELECTED_PAGE_ID = "selected_page_id";

	public static final String SP_NAME = "fp";
	public static final String SP_KEY_WORKING_PROJ = "working_proj";

	private static final int REQ_CODE_ADD_PAGE_FROM_GALLERY = 2;
	private static final int REQ_CODE_ADD_PAGE_FROM_CAMERA = 3;
	private static final int REQ_CODE_ADD_PAGE_AVIARY = 4;
	private static final int REQ_CODE_PAGE_EDIT = 100;

	public enum Mode {
		/** 수정 등이 가능한 평상시 모드 */
		NORMAL,

		/** 페이지 선택만 가능한 대화상자 모드 */
		ONLY_SELECT
	}

	private static Mode mMode = Mode.NORMAL;

	private Project mProject;
	private int mSelectedPageId;

	private PageListAdapter mAdapter;
	private ActionMode mActionMode;
	private View mContentView;
	private GridView mGridView;

	private Uri mImageCaptureUri;

	public static PageListFragment newInstance(int projectId, int pageId) {
		return newInstance(projectId, pageId, Mode.NORMAL);
	}

	public static PageListFragment newInstance(int projectId, int pageId, Mode mode) {
		PageListFragment instance = new PageListFragment();

		Bundle args = new Bundle();
		args.putInt(PageListFragment.EXTRA_PROJECT_ID, projectId);
		args.putInt(PageListFragment.EXTRA_SELECTED_PAGE_ID, pageId);
		instance.setArguments(args);

		mMode = mode;

		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu((mMode == Mode.NORMAL));

		mAdapter = new PageListAdapter(getActivity(), mMode);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		FpLog.d(TAG, "onCreateDialog()");

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.select_page_to_link);

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
		mGridView = (GridView) view.findViewById(R.id.grid);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// FpLog.e(TAG, "onGlobalLayout()");
				if (mAdapter.getNumColumns() == 0) {
					final int numColumns = (int) Math.floor(mGridView.getWidth() / (mAdapter.getWidth() + mAdapter.getColumnSpacing()));
					if (numColumns > 0) {
						final int columnWidth = (mGridView.getWidth() / numColumns) - mAdapter.getColumnSpacing();
						final int columnHeight = (int) (columnWidth * 1.1f);

						mAdapter.setNumColumns(numColumns);
						mAdapter.setWidth(columnWidth);
						mAdapter.setHeight(columnHeight);
						FpLog.d(TAG, "GridView size reset: width=" + columnWidth + " height=" + columnHeight);
					}
				}
			}
		});

		return view;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FpLog.d(TAG, "onCreateView()");

		int projectId = getArguments().getInt(PageListFragment.EXTRA_PROJECT_ID, 0);
		mSelectedPageId = getArguments().getInt(PageListFragment.EXTRA_SELECTED_PAGE_ID, Link.NO_TARGET_SPECIFIED);

		load(projectId);

		if (mMode == Mode.NORMAL) {
			ActionBar actionBar = getBaseActivity().getSupportActionBar();
			actionBar.setHomeButtonEnabled(true);
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
	public void onDestroy() {
		mGridView = null;
		mAdapter = null;
		super.onDestroy();
	}

	private void load(int projectId) {
		ProjectHolder.getInstance().load(getActivity(), projectId, this);
	}

	public void onLoad(Project tmpProj) {
		if (tmpProj == null) {
			Toast.makeText(getActivity(), R.string.error_on_loading_project, Toast.LENGTH_SHORT).show();
			finishActivity();
			return;
		}

		mProject = tmpProj;
		mAdapter.setPages(mProject);
		mAdapter.setSelectedPageId(mSelectedPageId);
		mAdapter.notifyDataSetChanged();

		new Thread() {
			@Override
			public void run() {
				if (getActivity() != null) {
					storeWorkingProject(getActivity(), mProject.getId());
				}
			}
		}.start();

		if (mMode == Mode.NORMAL) {
			ActionBar actionBar = getBaseActivity().getSupportActionBar();
			actionBar.setTitle(mProject.getSubject());
			// setListBackground();
		}

	}

	private static void storeWorkingProject(Context context, int id) {
		try {
			SharedPreferences pref = context.getSharedPreferences(SP_NAME, 0);
			Editor editor = pref.edit();
			editor.putInt(SP_KEY_WORKING_PROJ, id);
			editor.commit();
		} catch (Exception ex) {
			FpLog.e(TAG, ex);
		}
	}

	public static int loadWorkingProjectId(Context context) {
		try {
			SharedPreferences pref = context.getSharedPreferences(SP_NAME, 0);
			int id = pref.getInt(SP_KEY_WORKING_PROJ, 0);
			return id;
		} catch (Exception ex) {
			FpLog.e(TAG, ex);
		}
		return 0;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_page_list_option, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem edit = menu.findItem(R.id.edit);

		if (mAdapter.getCount() == 0) {
			edit.setEnabled(false);
		} else {
			edit.setEnabled(true);
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.gallery: {
				startAddPageGallery();
				return true;
			}
			case R.id.camera: {
				startAddPageCamera();
				return true;
			}
			// case R.id.web: {
			// Toast.makeText(getActivity(), R.string.not_implemented_yet, Toast.LENGTH_SHORT)
			// .show();
			// break;
			// }
			case R.id.play:
				if (mProject.size() > 1) {
					Intent intent = new Intent(getActivity(), PlayActivity.class);
					intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, mProject.getId());
					intent.putExtra(PageListFragment.EXTRA_SELECTED_PAGE_ID, mProject.get(0).getId());
					startActivity(intent);
				}
				return true;
			case R.id.edit:
				setActionMode(true);
				return true;

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

	private void startAviary(Uri uri) {
		try {
			Intent newIntent = new Intent(getActivity(), FeatherActivity.class);
			newIntent.setData(uri);
			newIntent.putExtra(Constants.EXTRA_OUTPUT_QUALITY, 88);
			startActivityForResult(newIntent, REQ_CODE_ADD_PAGE_AVIARY);
		} catch (Exception ex) {
			Toast.makeText(getActivity(), R.string.fail_to_add, Toast.LENGTH_SHORT).show();
			FpLog.e(TAG, ex);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		FpLog.d(TAG, "onActivityResult() ", requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQ_CODE_ADD_PAGE_FROM_GALLERY: {
					Uri uri = data.getData();
					startAviary(uri);
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
					// TODO 반영하기
					break;
				}
				case REQ_CODE_ADD_PAGE_AVIARY: {
					Uri uri = data.getData();
					CopyPictureTask task = new CopyPictureTask();
					task.execute(uri);
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

	/**
	 * 프로젝트에 사진추가. worker thread 에서 구동됨.
	 * 
	 * @param name
	 * @param orgImagePath
	 * @return
	 */
	boolean addToProject(String name, String orgImagePath) {
		synchronized (getActivity()) {
			Page page = new Page();
			page.setName(name);
			page.setProjectId(mProject.getId());

			String orgFileName = Util.getFileName(orgImagePath);
			String orgFileExt = Util.getFileExtension(orgImagePath);
			File newFile = new File(ProjectManager.getProjectPath(mProject.getId()), orgFileName + orgFileExt);
			int count = 1;
			while (newFile.exists() && count < 100) {
				newFile = new File(ProjectManager.getProjectPath(mProject.getId()), orgFileName + "_" + count + orgFileExt);
				count++;
			}

			if (count >= 100)
				return false;

			String newPath = newFile.getAbsolutePath();
			page.setImageUri("file://" + newPath);

			boolean success = false;

			if (Util.copyFile(orgImagePath, newPath)) {
				mProject.add(page);
				DBAdapter db = null;
				try {
					db = new DBAdapter(getActivity()).open();
					success = db.insertPage(page) >= 0;

					if (mProject.getMainImage() == null || mProject.getMainImage().length() == 0) {
						defineProjectMainImage(db);
					}

				} catch (Exception e) {
					FpLog.e(TAG, e);
				} finally {
					if (db != null)
						db.close();
				}

			}

			return success;
		}
	}

	/**
	 * 프로젝트의 메인 이미지를 고른다.
	 * 
	 * @param db
	 * @throws Exception
	 */
	private void defineProjectMainImage(DBAdapter db) throws Exception {
		if (mProject.size() > 0) {
			mProject.setMainImage(mProject.get(0).getImageUri());
		} else {
			mProject.setMainImage(null);
		}
		db.updateProject(mProject);
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
		if (clickedPage != null) {
			/* page selected */
			if (mMode == Mode.NORMAL) {
				if (mActionMode != null) {
					selectPageOn(clickedPage);
				} else {
					Intent intent = new Intent(getActivity(), PageEditActivity.class);
					intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, mProject.getId());
					intent.putExtra(PageEditFragment.EXTRA_PAGE_ID, clickedPage.getId());
					startActivityForResult(intent, REQ_CODE_PAGE_EDIT);
				}
			} else if (mMode == Mode.ONLY_SELECT) {
				if (getDialog() != null)
					this.getDialog().dismiss();

				if (mSelectCallback != null) {
					mSelectCallback.pageSelected(clickedPage);
				}
			}
		} else {
			/* page add selected */
			showPageAddPopupMenu(view);
		}
	}

	private void showPageAddPopupMenu(View view) {
		PopupMenu popup = new PopupMenu(getActivity(), view);
		android.view.Menu menu = popup.getMenu();
		popup.getMenuInflater().inflate(R.menu.menu_page_list_add_popup, menu);
		for (int i = 0; i < menu.size(); i++) {
			android.view.MenuItem item = menu.getItem(i);
			item.setOnMenuItemClickListener(listener);
		}
		popup.show();
	}

	private void startAddPageCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File path = getCameraPath();
		String filename = new SimpleDateFormat("yyyyMMdd_HH:mm:ss_S").format(new Date()) + ".jpg";

		Util.prepareDir(path);
		mImageCaptureUri = Uri.fromFile(new File(path, filename));

		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		startActivityForResult(intent, REQ_CODE_ADD_PAGE_FROM_CAMERA);
	}

	private void startAddPageGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, REQ_CODE_ADD_PAGE_FROM_GALLERY);
	}

	android.view.MenuItem.OnMenuItemClickListener listener = new android.view.MenuItem.OnMenuItemClickListener() {
		public boolean onMenuItemClick(android.view.MenuItem item) {
			switch (item.getItemId()) {
				case R.id.gallery: {
					startAddPageGallery();
					return true;
				}
				case R.id.camera: {
					startAddPageCamera();
					return true;
				}
			}
			return false;
		}

	};

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mMode == Mode.NORMAL) {
			Page page = mAdapter.getItem(position);
			if (page != null)
				selectPageOn(page);
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
			mActionMode.setTitle(getActivity().getResources().getString(R.string.title_selected, selectedCount));
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
			mActionMode = getBaseActivity().startSupportActionMode(new ActionModeCallback());
		} else {
			mActionMode.finish();
		}
	}

	private class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(R.string.menu_remove).setIcon(android.R.drawable.ic_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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
					builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (mAdapter.countSelectedPage() > 0) {
								RemovePageTask task = new RemovePageTask();
								task.execute(mAdapter.getSelectedPages().toArray(new Page[0]));
							} else {
								Toast.makeText(getActivity(), R.string.no_page_selected, Toast.LENGTH_SHORT).show();
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
			mAdapter.setActionMode(true);
			mAdapter.notifyDataSetChanged();

			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			mAdapter.clearSelection();
			mAdapter.notifyDataSetChanged();

			mAdapter.setActionMode(false);
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
			DBAdapter db = null;
			SQLiteDatabase _db = null;
			try {
				db = new DBAdapter(getActivity()).open();

				_db = db.getDb();
				_db.beginTransaction();

				for (Page page : pages) {
					mAdapter.togglePageSelection(page);

					db.deleteLinkOf(page.getId());

					db.deletePage(page.getId());

					for (Page src : mProject) {
						if (src.getId() != page.getId()) {
							if (src.getLinks() != null) {
								for (Link link : src.getLinks()) {
									if (link.getTargetPageId() == page.getId()) {
										link.setTargetPageId(Link.NO_TARGET_SPECIFIED);
										db.updateLink(link);
									}
								}
							}
						}
					}

					mProject.remove(page);
					new File(page.getImageUri()).delete();
				}
				_db.setTransactionSuccessful();

				defineProjectMainImage(db);
			} catch (Exception ex) {
				FpLog.e(TAG, ex);
			} finally {
				if (_db != null)
					_db.endTransaction();

				if (db != null)
					db.close();

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

	public void setSelectCallback(SelectCallback selectCallback) {
		this.mSelectCallback = selectCallback;
	}

	public interface SelectCallback {
		public void pageSelected(Page page);
	}

	private void setListBackground() {
		FpLog.d(TAG, "setListBackground()");

		final String path = mProject == null ? null : mProject.getMainImage();
		if (path == null)
			return;

		new AsyncTask<Void, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Void... params) {
				FpLog.d(TAG, "doInBackground()", path);
				Bitmap bitmap = ImageLoader.getInstance().loadImageSync(path);
				if (bitmap == null)
					return null;
				Bitmap newBitmap = ImageUtil.fastblur(bitmap, 70);

				bitmap.recycle();

				return newBitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				FpLog.d(TAG, "onPostExecute()", result);
				if (result != null) {
					try {
						mGridView.setBackgroundDrawable(new BitmapDrawable(getActivity().getResources(), result));
					} catch (Exception ex) {
						FpLog.e(TAG, ex);
					}
				}
				super.onPostExecute(result);
			}
		}.execute();
	}

}
