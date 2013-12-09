
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseActivity;
import com.egloos.realmove.android.fp.common.L;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class PlayActivity extends BaseActivity {

	private static final String TAG = PlayActivity.class.getSimpleName();

	private Fragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		int projectId = getIntent().getIntExtra(PageListFragment.EXTRA_PROJECT_ID, -1);
		if (projectId == -1) {
			Toast.makeText(this, R.string.wrong_project, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		int pageId = getIntent().getIntExtra(PageListFragment.EXTRA_SELECTED_PAGE_ID, -1);

		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mFragment = PlayFragment.newInstance(projectId, pageId);
			ft.add(android.R.id.content, mFragment, TAG);
			ft.commit();
		}
		
        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
	}

	private float gap = 0;

	private float calcGap(MotionEvent event) {
		float x = Math.abs(event.getX(1) - event.getX(0));
		float y = Math.abs(event.getY(1) - event.getY(0));
		return (float) Math.sqrt(x * x + y * y);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// FpLog.d(TAG, "dispatchTouchEvent()", event.getAction(), event.getPointerCount());
		if (event.getPointerCount() == 2) {
			L.d(TAG, "dispatchTouchEvent()", event.getAction() & MotionEvent.ACTION_MASK, event.getX(0), event.getX(1));
			int action = event.getAction();
			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_DOWN: {
					gap = calcGap(event);
					break;
				}

				case MotionEvent.ACTION_POINTER_UP: {
					float g = calcGap(event);
					if (g < gap && (g / gap < 0.5f)) {
						finish();
					}
					break;
				}
			}
		}
		return super.dispatchTouchEvent(event);
	}
}
