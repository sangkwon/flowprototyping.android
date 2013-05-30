
package com.egloos.realmove.android.fp.common;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Context;
import android.os.Bundle;

public class BaseFragmentActivity extends SherlockFragmentActivity {

    protected Context mContext = null;
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

}
