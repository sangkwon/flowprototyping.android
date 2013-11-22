
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.common.BaseActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class ProjectListActivity extends BaseActivity {
	private static final String TAG = ProjectListActivity.class.getSimpleName();

	private ProjectListFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mFragment = ProjectListFragment.newInstance();
			ft.add(android.R.id.content, mFragment, TAG);
			ft.commit();
		}
	}

}
