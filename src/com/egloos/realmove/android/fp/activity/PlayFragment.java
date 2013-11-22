
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.BaseActivity;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.db.ProjectHolder;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.view.LinkImagePlayView;
import com.egloos.realmove.android.fp.view.LinkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class PlayFragment extends BaseFragment implements LinkImageView.OnLinkClickListener {

	private static final String TAG = PlayFragment.class.getSimpleName();

	private int mWidth;
	private int mHeight;
	private LinkImagePlayView mPageView;

	private ActionBar mActionBar;

	private Project mProject;
	private Page mPage;

	enum State {
		PRELOAD, AFTERLOAD
	}

	private State state = State.PRELOAD;

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public static PlayFragment newInstance(int projectId, int pageId) {
		PlayFragment fragment = new PlayFragment();

		Bundle args = new Bundle();
		args.putInt(PageListFragment.EXTRA_PROJECT_ID, projectId);
		args.putInt(PageListFragment.EXTRA_SELECTED_PAGE_ID, pageId);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.play_fragment, container, false);
		mPageView = (LinkImagePlayView) view.findViewById(R.id.page);
		mPageView.setOnLinkClickListener(this);
		mPageView.setLinkShow(false);

		prepareCache();

		mActionBar = getBaseActivity().getSupportActionBar();
		mActionBar.hide();

		load(getArguments().getInt(PageListFragment.EXTRA_PROJECT_ID));

		return view;
	}

	private void prepareCache() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mWidth = displayMetrics.widthPixels;
		mHeight = displayMetrics.heightPixels;
	}

	private void load(int projectId) {
		ProjectHolder.getInstance().load(mContext, projectId, new ProjectHolder.Callback() {
			public void onLoad(Project project) {
				if (project == null) {
					Toast.makeText(getActivity(), R.string.error_on_loading_project, Toast.LENGTH_SHORT).show();
					finishActivity();
					return;
				}
				mProject = project;
				int pageId = getArguments().getInt(PageListFragment.EXTRA_SELECTED_PAGE_ID);
				if (pageId <= 0) {
					pageId = mProject.get(0).getId();
				}
				displayPage(pageId);
			}
		});
	}

	protected void displayPage(int pageId) {
		if (pageId > 0) {
			mPage = mProject.findPage(pageId);
		} else {
			mPage = mProject.get(0);
		}

		if (mPage != null) {
			mPageView.setLinks(mPage.getLinks());

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
			setState(State.PRELOAD);
		}
	}

	public void onLoadImage(boolean success, ImageView imageView, final Bitmap bitmap) {
		setState(State.AFTERLOAD);

		if (!success) {
			// TODO what?
			return;
		}

		if (mContext != null) {
			Intent data = new Intent();
			data.putExtra(PageListFragment.EXTRA_PROJECT_ID, mProject.getId());
			data.putExtra(PageListFragment.EXTRA_SELECTED_PAGE_ID, mPage.getId());
			mContext.setResult(Activity.RESULT_OK, data);
		}
	}

	@Override
	public boolean onLinkClicked(Link link) {
		FpLog.d(TAG, "onClickLink()", link);
		if (link != null) {
			int pageId = link.getTargetPageId();
			if (pageId != Link.NO_TARGET_SPECIFIED) {
				displayPage(pageId);
			}
		}
		return false;
	}

}
