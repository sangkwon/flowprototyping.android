
package com.egloos.realmove.android.fp.activity;

import com.actionbarsherlock.app.ActionBar;
import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.db.ProjectHolder;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.model.Project;
import com.egloos.realmove.android.fp.view.LinkImageView;
import com.egloos.realmove.android.fp.view.LinkImageView.OnLinkClickListener;
import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class PlayFragment extends BaseFragment implements ImageWorker.Callback,
        LinkImageView.OnLinkClickListener {

    private static final String TAG = PlayFragment.class.getSimpleName();

    private int mWidth;
    private int mHeight;
    private LinkImageView mPageView;
    private ImageWorker mImageFetcher;

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
        mPageView = (LinkImageView) view.findViewById(R.id.page);
        mPageView.setOnLinkClickListener(this);
        mPageView.setLinkShow(true);

        prepareCache();

        mActionBar = getSherlockActivity().getSupportActionBar();
        mActionBar.hide();

        load(getArguments().getInt(PageListFragment.EXTRA_PROJECT_ID));

        return view;
    }

    private void prepareCache() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;

        FpLog.d(TAG, "prepareCache()", mWidth, mHeight);

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mContext,
                "page_view");
        cacheParams.setMemCacheSizePercent(0.25f);

        mImageFetcher = new ImageFetcher(mContext, mWidth, mHeight);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        // mImageFetcher.setImageFadeIn(false);
        mImageFetcher.setCallback(this);
    }

    private void load(int projectId) {
        ProjectHolder.getInstance().load(mContext, projectId, new ProjectHolder.Callback() {
            public void onLoad(Project project) {
                if (project == null) {
                    Toast.makeText(getActivity(), R.string.error_on_loading_project,
                            Toast.LENGTH_SHORT)
                            .show();
                    finishActivity();
                    return;
                }
                mProject = project;
                displayPage(getArguments().getInt(PageListFragment.EXTRA_SELECTED_PAGE_ID));
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

            mImageFetcher.loadImage(Uri.fromFile(new File(mPage.getImagePath())), mPageView);
            setState(State.PRELOAD);
        }
    }

    @Override
    public void onLoadImage(boolean success, ImageView imageView, final BitmapDrawable bd) {
        setState(State.AFTERLOAD);

        if (!success) {
            // TODO what?
            return;
        }

    }

    @Override
    public boolean onClickLink(Link link) {
        FpLog.d(TAG, "onClickLink()", link);
        if (link != null) {
            int pageId = link.getTargetPageId();
            displayPage(pageId);
        }
        return false;
    }

}