
package com.egloos.realmove.android.fp.common;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
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

}
