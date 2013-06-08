
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragmentActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class PlayActivity extends BaseFragmentActivity {

    private static final String TAG = PlayActivity.class.getSimpleName();

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }

}
