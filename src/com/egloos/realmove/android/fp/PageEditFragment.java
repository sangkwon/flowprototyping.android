
package com.egloos.realmove.android.fp;

import com.actionbarsherlock.app.ActionBar;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.view.LinkImageEditView;
import com.egloos.realmove.android.fp.view.LinkImageEditView.OnLinkChangeListener;
import com.egloos.realmove.android.fp.view.LinkSelectDialog;
import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.example.android.bitmapfun.util.ImageWorker.Callback;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class PageEditFragment extends BaseFragment implements OnLinkChangeListener, Callback {

    private static final String TAG = PageEditFragment.class.getSimpleName();

    private Page mPage;

    private int mWidth;
    private int mHeight;
    private ImageWorker mImageFetcher;
    private LinkImageEditView mPageView;

    private ActionBar mActionBar;

    public static PageEditFragment newInstance(Page page) {
        PageEditFragment fragment = new PageEditFragment();
        fragment.mPage = page;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.page_edit_fragment, container, false);
        mPageView = (LinkImageEditView) view.findViewById(R.id.page);
        mPageView.setOnLinkChangeListener(this);

        prepareCache();

        mActionBar = getSherlockActivity().getSupportActionBar();

        // Hide title text and set home as up
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        /*
         * // Hide and show the ActionBar as the visibility changes
         * mPageView.setOnSystemUiVisibilityChangeListener( new
         * View.OnSystemUiVisibilityChangeListener() {
         * @Override public void onSystemUiVisibilityChange(int vis) { if ((vis &
         * View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) { mActionBar.hide(); } else { mActionBar.show(); }
         * } }); if (Utils.hasJellyBean()) { // Start low profile mode and hide ActionBar
         * mPageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); }
         */

        mActionBar.hide();

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
        mImageFetcher.setImageFadeIn(true);
        mImageFetcher.loadImage(Uri.fromFile(new File(mPage.getImagePath())), mPageView);
        mImageFetcher.setCallback(this);
    }

    @Override
    public void onDestroyView() {
        if (mImageFetcher != null) {
            mImageFetcher.closeCache();
            mImageFetcher = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onLoadImage(boolean success, final ImageView pageView, final Bitmap bitmap) {
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

    // TODO 이 부분 어디에 붙여야 하나? onDetachView() ???
    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_BACK) {
    // if (dirtyFlag) {
    // Intent intent = new Intent();
    // intent.putExtra("page", mPage);
    // setResult(RESULT_OK, intent);
    // return super.onKeyDown(keyCode, event); // don't consume
    // }
    // }
    //
    // return super.onKeyDown(keyCode, event);
    // }

    private boolean dirtyFlag = false;

    @Override
    public void linkAdded(Link link) {
        dirtyFlag = true;
    }

    @Override
    public void linkRemoved(Link link) {
        dirtyFlag = true;
    }

    @Override
    public void linkModified(Link link) {
        dirtyFlag = true;
    }

    @Override
    public void requestSetTarget(Link link) {
        LinkSelectDialog.show(getActivity(), getActivity().getSupportFragmentManager(),
                mPage.getProjectId(), link);
    }

}