
package com.egloos.realmove.android.fp.view;

import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.model.RectPosition;
import com.egloos.realmove.android.fp.util.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * 링크들을 주어진 width, height 에 맞게 화면에 표시해준다. page 이미지 위에 겹쳐쓸 예정.
 * 
 * @author Sangkwon
 */
public class LinkImageView extends ImageView {

	private static final String TAG = LinkImageView.class.getSimpleName();

	protected ArrayList<Link> mLinks;
	protected boolean mLinkShow = false;
	protected OnLinkClickListener mListener;

	protected final float CORNER_WIDTH; // 선택할 떄에 코너선택으로 판별할 폭

	public LinkImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		CORNER_WIDTH = Util.dpToPx(context, 15F);

	}

	public void setOnLinkClickListener(OnLinkClickListener listener) {
		this.mListener = listener;
	}

	public void setLinks(ArrayList<Link> links) {
		mLinks = links;
	}

	public void setLinkShow(boolean mLinkShow) {
		this.mLinkShow = mLinkShow;
		invalidate();
	}

	/** these for performance of onDraw() */
	Paint paint = new Paint();
	Rect rect = new Rect();
	RectPosition dstRect = new RectPosition();

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mLinks != null) {
			if (mLinkShow) {
				paint.setColor(0x8033b5e5);

				int len = mLinks.size();
				for (int i = 0; i < len; i++) {
					drawLink(canvas, paint, mLinks.get(i));
				}
			}
		}
	}

	protected void drawLink(Canvas canvas, Paint paint, Link link) {
		link.getPosition(dstRect, canvas.getWidth(), canvas.getHeight());

		rect.set(dstRect.getLeft(), dstRect.getTop(), dstRect.getRight(), dstRect.getBottom());

		canvas.drawRect(rect, paint);
	}

	protected Link getTouchedLink(float x, float y) {
		if (mLinks != null) {
			int width = getWidth();
			int height = getHeight();

			if (width == 0 || height == 0)
				return null;

			int len = mLinks.size();
			for (int i = 0; i < len; i++) {
				Link link = mLinks.get(i);
				link.getPosition(dstRect, width, height);

				if (x >= dstRect.getLeft() - CORNER_WIDTH && x <= dstRect.getRight() + CORNER_WIDTH && y >= dstRect.getTop() - CORNER_WIDTH
						&& y <= dstRect.getBottom() + CORNER_WIDTH) {
					return link;
				}
			}
		}
		return null;
	}

	private Link selected;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// FpLog.d(TAG, "onTouchEvent", event.getAction());
		if (mListener != null) {
			Link link = getTouchedLink(event.getX(), event.getY());
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					selected = link;
					break;
				case MotionEvent.ACTION_UP:
					if (selected == link) {
						mListener.onLinkClicked(link);
					}
					selected = null;
					break;

			}
		}
		return true;
	}

	public interface OnLinkClickListener {
		public boolean onLinkClicked(Link link);
	}

}
