
package com.egloos.realmove.android.fp.view;

import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.view.LinkImageView.OnLinkClickListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

public class LinkImagePlayView extends LinkImageView implements OnLinkClickListener {

	private Link mBlinkingLink;
	private OnLinkClickListener mRealListener;

	public LinkImagePlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	enum BlinkState {
		READY, BLINKING
	}

	private BlinkState mBlinkState = BlinkState.READY;
	private Handler mHandler = new Handler();
	private Runnable mBlinkStopper = new Runnable() {
		public void run() {
			stopBlinkLink();
		}
	};

	/**
	 * 링크를 살짝 반짝거리게 한다.
	 */
	public void blinkLink() {
		if (mBlinkState == BlinkState.READY) {
			mBlinkState = BlinkState.BLINKING;
			mHandler.postDelayed(mBlinkStopper, 400);
			invalidate();
		}
	}

	public void stopBlinkLink() {
		if (mBlinkState == BlinkState.BLINKING) {
			mHandler.removeCallbacks(mBlinkStopper);
			mBlinkState = BlinkState.READY;
			invalidate();

			if (mBlinkingLink != null) {
				if (mRealListener != null) {
					post(new ClinkThread(mBlinkingLink));
				}
			}

			mBlinkingLink = null;
		}
	}

	class ClinkThread implements Runnable {

		Link mLink;

		public ClinkThread(Link link) {
			mLink = link;
		}

		@Override
		public void run() {
			mRealListener.onLinkClicked(mLink);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mLinks != null) {
			if (mBlinkState == BlinkState.BLINKING) {
				int len = mLinks.size();
				for (int i = 0; i < len; i++) {
					paint.setColor(0x4033b5e5);
					drawLink(canvas, paint, mLinks.get(i));
				}
			}
		}
	}

	Paint blinkPaint = new Paint();

	@Override
	protected void drawLink(Canvas canvas, Paint paint, Link link) {
		if (mBlinkState == BlinkState.BLINKING) {
			if (link == mBlinkingLink) {
				paint.setColor(0x40aa66cc);
			}
		}
		super.drawLink(canvas, paint, link);
	}

	@Override
	public void setImageResource(int resId) {
		stopBlinkLink();
		super.setImageResource(resId);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		stopBlinkLink();
		super.setImageDrawable(drawable);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		stopBlinkLink();
		super.setImageBitmap(bm);
	}

	@Override
	public void setOnLinkClickListener(OnLinkClickListener listener) {
		/* listener 는 mRealListener 로 바꾸고, imageView 에는 스스로를 세팅한다. */
		mRealListener = listener;
		super.setOnLinkClickListener(this);
	}

	@Override
	public boolean onLinkClicked(Link link) {
		if (link == null) {
			this.blinkLink();
		} else {
			mBlinkingLink = link;
			blinkLink();
		}
		return false;
	}

}
