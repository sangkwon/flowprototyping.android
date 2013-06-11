
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class PageListActivity extends BaseFragmentActivity {

	private static final String TAG = PageListActivity.class.getSimpleName();

	private PageListFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int projectId = getIntent().getIntExtra(PageListFragment.EXTRA_PROJECT_ID, 0);
		if (projectId == 0) {
			int workingProject = PageListFragment.loadWorkingProjectId(this);
			if (workingProject <= 0) {
				startActivity(new Intent(this, ProjectListActivity.class));
				overridePendingTransition(R.anim.hold, R.anim.hold);
				finish();
				return;
			}
			projectId = workingProject;
		}

		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mFragment = PageListFragment.newInstance(projectId, -1);
			ft.add(android.R.id.content, mFragment, TAG);
			ft.commit();
		}
	}

}
