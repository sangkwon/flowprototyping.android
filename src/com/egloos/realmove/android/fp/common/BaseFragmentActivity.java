
package com.egloos.realmove.android.fp.common;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;

public class BaseFragmentActivity extends SherlockFragmentActivity {

	protected BaseFragmentActivity mContext = null;
	protected boolean mActivate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FpLog.d(this.getClass().getSimpleName(), "onCreate() called", this.getIntent());

		FpLog.setStrictPolicy();

		mContext = this;
	}

	@Override
	protected void onResume() {
		mActivate = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		mActivate = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mContext = null;

		super.onDestroy();
	}

	@Override
	public boolean onNavigateUp() {
		FpLog.d(this.getClass().getSimpleName(), "onNavigateUp()");
		return super.onNavigateUp();
	}

	@Override
	public boolean navigateUpToFromChild(Activity child, Intent upIntent) {
		FpLog.d(this.getClass().getSimpleName(), "navigateUpToFromChild()");
		return super.navigateUpToFromChild(child, upIntent);
	}

	@Override
	public void onPrepareNavigateUpTaskStack(TaskStackBuilder builder) {
		FpLog.d(this.getClass().getSimpleName(), "onPrepareNavigateUpTaskStack()");
		super.onPrepareNavigateUpTaskStack(builder);
	}

	@Override
	public void onCreateNavigateUpTaskStack(TaskStackBuilder builder) {
		FpLog.d(this.getClass().getSimpleName(), "onCreateNavigateUpTaskStack()");
		super.onCreateNavigateUpTaskStack(builder);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				navigateUp();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (navigateUp())
				return true;
			// else path thru
		}
		return super.onKeyUp(keyCode, event);
	}

	protected boolean navigateUp() {
		FpLog.d(this.getClass().getSimpleName(), "navigateUp()");

		Intent upIntent = NavUtils.getParentActivityIntent(mContext);

		if (upIntent == null)
			return false;

		if (NavUtils.shouldUpRecreateTask(mContext, upIntent)) {
			TaskStackBuilder.create(mContext).addNextIntentWithParentStack(upIntent).startActivities();
		} else {
			NavUtils.navigateUpTo(mContext, upIntent);
		}

		return true;
	}

}
