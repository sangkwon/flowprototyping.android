
package com.egloos.realmove.android.fp.common;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.activity.PageListFragment;
import com.egloos.realmove.android.fp.activity.PlayActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends SherlockDialogFragment {
    protected Activity mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // FpLog.d(this.getClass().getSimpleName(), "onCreate() called");
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // FpLog.d(this.getClass().getSimpleName(), "onCreateView() called");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // FpLog.d(this.getClass().getSimpleName(), "onCreateDialog() called");
        return super.onCreateDialog(savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        // FpLog.d(this.getClass().getSimpleName(), "onActivityCreated() called");
        super.onActivityCreated(savedInstanceState);
    }

    protected void finishActivity() {
        mContext.finish();
    }

    protected void finishMe() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onDetach() {
        // FpLog.d(this.getClass().getSimpleName(), "onDetach() called");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        // FpLog.d(this.getClass().getSimpleName(), "onDestroy() called");
        super.onDestroy();
        mContext = null;
    }

    @Override
    public void onDestroyView() {
        // FpLog.d(this.getClass().getSimpleName(), "onDestroyView() called");
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        // FpLog.d(this.getClass().getSimpleName(), "onPause() called");
        super.onPause();
    }

    @Override
    public void onResume() {
        // FpLog.d(this.getClass().getSimpleName(), "onResume() called");
        super.onResume();
    }

    
}
