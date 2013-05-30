
package com.egloos.realmove.android.fp.pagelist;

import com.egloos.realmove.android.fp.common.BaseFragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class PageListActivity extends BaseFragmentActivity {

    private static final String TAG = PageListActivity.class.getSimpleName();

    private PageListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mFragment = PageListFragment.newInstance(1, -1);
            ft.add(android.R.id.content, mFragment, TAG);
            ft.commit();
        }
    }

}
