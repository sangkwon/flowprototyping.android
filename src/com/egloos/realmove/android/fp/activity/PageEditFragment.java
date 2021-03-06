
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.L;
import com.egloos.realmove.android.fp.db.DBAdapter;
import com.egloos.realmove.android.fp.db.ProjectHolder;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.view.LinkImageEditView;
import com.egloos.realmove.android.fp.view.LinkImageEditView.OnLinkChangeListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class PageEditFragment extends BaseFragment implements OnLinkChangeListener, PageListFragment.SelectCallback {

	private static final String TAG = PageEditFragment.class.getSimpleName();

	public static final String EXTRA_PAGE_ID = "pageId";

	private static final int REQ_CODE_PLAY = 1;

	private Project mProject;
	private Page mPage;

	private int mWidth;
	private int mHeight;
	private LinkImageEditView mPageView;

	private ActionBar mActionBar;

	public static PageEditFragment newInstance(int projectId, int pageId) {
		PageEditFragment fragment = new PageEditFragment();

		Bundle args = new Bundle();
		args.putInt(PageListFragment.EXTRA_PROJECT_ID, projectId);
		args.putInt(EXTRA_PAGE_ID, pageId);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		final View view = inflater.inflate(R.layout.page_edit_fragment, container, false);
		mPageView = (LinkImageEditView) view.findViewById(R.id.page);
		mPageView.setOnLinkChangeListener(this);

		prepareCache();

		mActionBar = getBaseActivity().getSupportActionBar();

		// Hide title text and set home as up
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		/* // Hide and show the ActionBar as the visibility changes mPageView.setOnSystemUiVisibilityChangeListener( new
		 * View.OnSystemUiVisibilityChangeListener() {
		 * @Override public void onSystemUiVisibilityChange(int vis) { if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) { mActionBar.hide(); } else {
		 * mActionBar.show(); } } }); if (Utils.hasJellyBean()) { // Start low profile mode and hide ActionBar
		 * mPageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); } */

		// mActionBar.hide();

		int projectId = getArguments().getInt(PageListFragment.EXTRA_PROJECT_ID, -1);
		int pageId = getArguments().getInt(EXTRA_PAGE_ID);
		load(projectId, pageId);

		return view;
	}

	private void load(final int projectId, final int pageId) {
		ProjectHolder.getInstance().load(getActivity(), projectId, new ProjectHolder.Callback() {
			@Override
			public void onLoad(Project project) {
				if (project == null) {
					Toast.makeText(getActivity(), R.string.fail_to_load_page, Toast.LENGTH_SHORT).show();
					finishActivity();
				}

				mProject = project;
				mPage = project.findPage(pageId);
				if (mPage == null) {
					Toast.makeText(getActivity(), R.string.fail_to_load_page, Toast.LENGTH_SHORT).show();
					finishActivity();
					return;
				}

				mActionBar.setTitle(mPage.getName());

				ImageLoader.getInstance().displayImage(mPage.getImageUri(), mPageView, new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// do nothing
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						onLoadImage(false, mPageView, null);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						onLoadImage(true, mPageView, loadedImage);
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						onLoadImage(false, mPageView, null);
					}
				});
			}
		});
	}

	private void prepareCache() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mWidth = displayMetrics.widthPixels;
		mHeight = displayMetrics.heightPixels;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	public void onLoadImage(boolean success, final ImageView pageView, final Bitmap bd) {
		if (success) {
			ArrayList<Link> links = mPage.getLinks();
			if (links == null) {
				links = new ArrayList<Link>();
				mPage.setLinks(links);
			}
			mPageView.setLinks(links);
			mPageView.setLinkShow(true);
		}
	}

	enum Command {
		INSERT, DELETE, MODIFY
	}

	private class LinkTask extends AsyncTask<Void, Void, Boolean> {

		private Link mLink;
		private Command mCommand;

		public LinkTask(Link link, Command command) {
			mLink = link;
			mCommand = command;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			synchronized (getActivity()) {
				DBAdapter db = null;
				try {
					db = new DBAdapter(getActivity()).open();
					switch (mCommand) {
						case DELETE:
							db.deleteLink(mLink.getId());
							break;
						case INSERT:
							db.insertLink(mLink);
							break;
						case MODIFY:
							db.updateLink(mLink);
							break;
						default:
							break;
					}
					return true;
				} catch (Exception ex) {
					L.e(TAG, ex);
					return false;
				} finally {
					if (db != null)
						db.close();
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				mPageView.invalidate();
			}
			super.onPostExecute(result);
		}

	}

	@Override
	public void linkAdded(Link link) {
		L.d(TAG, "linkAdded()");
		link.setPageId(mPage.getId());
		new LinkTask(link, Command.INSERT).execute();
	}

	@Override
	public void linkRemoved(Link link) {
		L.d(TAG, "linkRemoved()");
		new LinkTask(link, Command.DELETE).execute();
	}

	@Override
	public void linkModified(Link link) {
		L.d(TAG, "linkModified()");
		new LinkTask(link, Command.MODIFY).execute();
	}

	private Link mSelectedLink;

	@Override
	public void requestSetTarget(Link link) {
		if (link == null)
			return;

		mSelectedLink = link;

		PageListFragment fragment = PageListFragment.newInstance(mPage.getProjectId(), link.getTargetPageId(), PageListFragment.Mode.ONLY_SELECT);
		fragment.show(getActivity().getSupportFragmentManager(), PageListFragment.TAG_DIALOG);
		fragment.setSelectCallback(new PageListFragment.SelectCallback() {
			public void pageSelected(Page page) {
				if (page != null) {
					mSelectedLink.setTargetPageId(page.getId());
					new LinkTask(mSelectedLink, Command.MODIFY).execute();
				}
			}
		});
	}

	@Override
	public void pageSelected(Page page) {
		L.d(TAG, "pageSelected");
		if (page != null && mSelectedLink != null) {
			mSelectedLink.setTargetPageId(page.getId());
		}

		mSelectedLink = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_page_edit_option, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.play: {
				Intent intent = new Intent(getActivity(), PlayActivity.class);
				intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, mPage.getProjectId());
				intent.putExtra(PageListFragment.EXTRA_SELECTED_PAGE_ID, mPage.getId());
				startActivityForResult(intent, REQ_CODE_PLAY);
				return true;
			}
			case R.id.add: {
				mPageView.createNewLink();
				return true;
			}
			case R.id.remove_unlink: {
				ArrayList<Link> links = mPage.getLinks();
				if (links != null) {
					ArrayList<Link> linkRemove = new ArrayList<Link>();
					for (Link link : links) {
						if (link.getTargetPageId() == Link.NO_TARGET_SPECIFIED) {
							linkRemove.add(link);
						}
					}

					if (linkRemove.size() > 0) {
						links.removeAll(linkRemove);
						mPageView.invalidate();
					}
				}
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQ_CODE_PLAY) {
				int pageId = data.getIntExtra(PageListFragment.EXTRA_SELECTED_PAGE_ID, mPage.getId());
				int projectId = data.getIntExtra(PageListFragment.EXTRA_PROJECT_ID, -1);
				if (projectId != -1 && pageId != mPage.getId()) {
					load(projectId, pageId);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
