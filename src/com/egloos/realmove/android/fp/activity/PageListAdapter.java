
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.model.Page;
import com.example.android.bitmapfun.util.ImageFetcher;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class PageListAdapter extends BaseAdapter {

    private static final String TAG = PageListAdapter.class.getSimpleName();

    private ArrayList<Page> mPages;
    private HashSet<Page> mSelectedPages = new HashSet<Page>();

    private LayoutInflater mInflater;
    private int mWidth;
    private int mHeight;
    private int mColumnSpacing;
    private ImageFetcher mImageFetcher;
    GridView.LayoutParams mImageViewLayoutParams = new GridView.LayoutParams(
            android.widget.AbsListView.LayoutParams.MATCH_PARENT,
            android.widget.AbsListView.LayoutParams.MATCH_PARENT);
    private int mNumColumns;

    private int mSelectedPageId;

    public PageListAdapter(Context context, ImageFetcher imageFetcher) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mWidth = context.getResources().getDimensionPixelSize(R.dimen.page_thumbnail_size);
        mHeight = mWidth;
        mColumnSpacing = context.getResources().getDimensionPixelSize(
                R.dimen.page_thumbnail_spacing);

        mImageFetcher = imageFetcher;
    }

    public void setPages(ArrayList<Page> pages) {
        this.mPages = pages;
    }

    @Override
    public int getCount() {
        int size = mPages != null ? mPages.size() : 0;
        // if (size > 0)
        // size = 1;
        return size;
    }

    @Override
    public Page getItem(int position) {
        return mPages.get(position);
        // return pages.get(pages.size() - 1);
    }

    @Override
    public long getItemId(int position) {
        // is not using
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Logger.d(TAG, "getView()", position);
        View view = convertView;
        ViewHolder holder;
        if (view != null && view.getTag() instanceof ViewHolder) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.page_list_row, null);
            holder = new ViewHolder();
            holder.thumb = (ImageView) view.findViewById(R.id.thumb);
            holder.text = (TextView) view.findViewById(R.id.text);
            holder.selectedBox = view.findViewById(R.id.selectedBox);

            view.setLayoutParams(mImageViewLayoutParams);

            view.setTag(holder);
        }

        if (view.getLayoutParams().height != mHeight) {
            view.setLayoutParams(mImageViewLayoutParams);
        }

        Page page = getItem(position);
        holder.text.setText(page.getName());
        boolean selected = page.getId() == mSelectedPageId || mSelectedPages.contains(page);
        holder.selectedBox.setVisibility(selected ? View.VISIBLE : View.GONE);
        mImageFetcher.loadImage(Uri.fromFile(new File(page.getImagePath())), holder.thumb);

        return view;
    }

    class ViewHolder {
        ImageView thumb;
        TextView text;
        View selectedBox;
    }

    /**
     * Sets the item height. Useful for when we know the column width so the height can be set to
     * match.
     * 
     * @param height
     */
    public void setHeight(int height) {
        if (height == mHeight) {
            return;
        }
        mHeight = height;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mHeight);
        mImageFetcher.setHeight(height);
        notifyDataSetChanged();
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
        mImageFetcher.setWidth(width);
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    public int getColumnSpacing() {
        return mColumnSpacing;
    }

    /**
     * @param page
     * @return 선택되었으면 true, 선택해제되면 false
     */
    public boolean togglePageSelection(Page page) {
        if (mSelectedPages.contains(page)) {
            mSelectedPages.remove(page);
            return false;
        } else {
            mSelectedPages.add(page);
            return true;
        }
    }

    /**
     * 선택된 페이지 개수
     * 
     * @return
     */
    public int countSelectedPage() {
        return mSelectedPages.size();
    }

    public void clearSelection() {
        mSelectedPages.clear();
    }

    public HashSet<Page> getSelectedPages() {
        return mSelectedPages;
    }

    public void setSelectedPageId(int selectedPageId) {
        mSelectedPageId = selectedPageId;
    }

}
