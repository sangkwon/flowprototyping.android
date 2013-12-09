
package com.egloos.realmove.android.fp.common;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// FpLog.d(this.getClass().getSimpleName(), "onCreate() called");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
		try {
			getActivity().finish();
		} catch (Exception ex) {
			// do nothing
		}
	}

	protected void finishMe() {
		try {
			getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
		} catch (Exception ex) {
			// do nothing
		}
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

	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}

}
