
package com.egloos.realmove.android.fp.view;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.egloos.realmove.android.fp.model.Link;
import com.egloos.realmove.android.fp.pagelist.PageListActivity;
import com.egloos.realmove.android.fp.pagelist.PageListFragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LinkSelectDialog extends Dialog {

    public static final String EXTRA_SELECTED_PAGE_ID = "selectedPageId";

    private Link mLink;

    private LinkSelectDialog(Context context, Link link) {
        super(context);
        this.mLink = link;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static void show(Context context, FragmentManager fm, int projectId, Link link) {
        if (link == null)
            return;

        PageListFragment fragment = PageListFragment.newInstance(projectId, link.getTargetPageId());
        fragment.show(fm, "Page List");
    }

    static class PageListDialogFragment extends SherlockDialogFragment {

        static PageListDialogFragment newInstance(int projectId, int pageId) {
            PageListDialogFragment instance = new PageListDialogFragment();

            Bundle args = new Bundle();
            args.putInt(PageListFragment.EXTRA_PROJECT_ID, projectId);
            args.putInt(EXTRA_SELECTED_PAGE_ID, pageId);
            instance.setArguments(args);

            return instance;
        }

        private int mProjectId;
        private int mSelectedPageId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mProjectId = savedInstanceState.getInt(PageListFragment.EXTRA_PROJECT_ID);
            mSelectedPageId = savedInstanceState.getInt(EXTRA_SELECTED_PAGE_ID);

            setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // View v = inflater.inflate(R.layout.fragment_dialog, container, false);
            // View tv = v.findViewById(R.id.text);
            // ((TextView) tv).setText("Dialog #" + mNum + ": using style "
            // + getNameForNum(mNum));
            //
            // // Watch for button clicks.
            // Button button = (Button) v.findViewById(R.id.show);
            // button.setOnClickListener(new OnClickListener() {
            // public void onClick(View v) {
            // // When button is clicked, call up to owning activity.
            // ((FragmentDialog) getActivity()).showDialog();
            // }
            // });
            //
            // return v;

            return null;
        }
    }

}
