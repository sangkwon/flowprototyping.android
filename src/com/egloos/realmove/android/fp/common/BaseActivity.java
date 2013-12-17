
package com.egloos.realmove.android.fp.common;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;

public class BaseActivity extends ActionBarActivity {

	protected BaseActivity mContext = null;
	protected boolean mActivate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		L.d(this.getClass().getSimpleName(), "onCreate() called", this.getIntent());

		L.setStrictPolicy();

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
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
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
		L.d(this.getClass().getSimpleName(), "navigateUp()");

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
