
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.activity.PageListFragment.Mode;
import com.egloos.realmove.android.fp.model.Page;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

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
	GridView.LayoutParams mImageViewLayoutParams = new GridView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT,
			android.widget.AbsListView.LayoutParams.MATCH_PARENT);
	private int mNumColumns;

	private int mSelectedPageId;

	private boolean mActionMode = false;
	private Mode mMode;

	public PageListAdapter(Context context, Mode mode) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mWidth = context.getResources().getDimensionPixelSize(R.dimen.page_thumbnail_size);
		mHeight = mWidth;
		mColumnSpacing = context.getResources().getDimensionPixelSize(R.dimen.page_thumbnail_spacing);
		mMode = mode;
	}

	public void setPages(ArrayList<Page> pages) {
		this.mPages = pages;
	}

	public void setActionMode(boolean actionMode) {
		mActionMode = actionMode;
	}

	@Override
	public int getCount() {
		if (mPages == null) {
			return 0;
		} else {
			int size = mPages.size();

			if (!mActionMode && mMode == Mode.NORMAL)
				size++;

			return size;
		}
	}

	@Override
	public Page getItem(int position) {
		if (mPages == null || position == mPages.size())
			return null;
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
			holder.add = view.findViewById(R.id.add);
			holder.selectedBox = view.findViewById(R.id.selectedBox);

			view.setLayoutParams(mImageViewLayoutParams);

			view.setTag(holder);
		}

		if (view.getLayoutParams().height != mHeight) {
			view.setLayoutParams(mImageViewLayoutParams);
		}

		Page page = getItem(position);
		if (page == null) {
			/* Add Page */
			holder.text.setVisibility(View.GONE);
			holder.selectedBox.setVisibility(View.GONE);
			holder.thumb.setVisibility(View.GONE);
			holder.add.setVisibility(View.VISIBLE);
		} else {
			holder.text.setVisibility(View.VISIBLE);
			holder.thumb.setVisibility(View.VISIBLE);
			holder.add.setVisibility(View.GONE);
			holder.text.setText(page.getName());
			boolean selected = page.getId() == mSelectedPageId || mSelectedPages.contains(page);
			holder.selectedBox.setVisibility(selected ? View.VISIBLE : View.GONE);
			ImageLoader.getInstance().displayImage(page.getImageUri(), holder.thumb);
			holder.thumb.setBackgroundDrawable(null);
			holder.thumb.setScaleType(ScaleType.CENTER_CROP);
			holder.thumb.setPadding(0, 0, 0, 0);
		}

		return view;
	}

	class ViewHolder {
		ImageView thumb;
		TextView text;
		View selectedBox;
		View add;
	}

	/**
	 * Sets the item height. Useful for when we know the column width so the height can be set to match.
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		if (height == mHeight) {
			return;
		}
		mHeight = height;
		mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mHeight);
		notifyDataSetChanged();
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int width) {
		this.mWidth = width;
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
