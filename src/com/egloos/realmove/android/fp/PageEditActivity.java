
package com.egloos.realmove.android.fp;

import com.actionbarsherlock.view.Window;
import com.egloos.realmove.android.fp.common.BaseFragmentActivity;
import com.egloos.realmove.android.fp.model.Page;
import com.egloos.realmove.android.fp.pagelist.PageListFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class PageEditActivity extends BaseFragmentActivity {

    private static final String TAG = PageEditActivity.class.getSimpleName();

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

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        Bundle extra = getIntent().getExtras();
        Page page = (Page) extra.get("page");

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mFragment = PageEditFragment.newInstance(page);
            ft.add(android.R.id.content, mFragment, TAG);
            ft.commit();
        }

    }

}