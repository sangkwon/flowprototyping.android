
package com.egloos.realmove.android.fp.activity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.db.ProjectListLoadTask;
import com.egloos.realmove.android.fp.db.ProjectListLoadTask.Callback;
import com.egloos.realmove.android.fp.model.Project;
import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ProjectListFragment extends BaseFragment implements OnItemClickListener, Callback {

    private static final String TAG = ProjectListFragment.class.getSimpleName();

    private ProjectListAdapter mAdapter;
    private ImageFetcher mImageFetcher;

    private ArrayList<Project> mProjects;

    public static ProjectListFragment newInstance() {
        return new ProjectListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
                "page_list");
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.page_thumbnail_size);
        mImageFetcher = new ImageFetcher(getActivity(), size, size);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);

        mAdapter = new ProjectListAdapter(getActivity(), mImageFetcher);
    }

    /**
     * 실제 view 를 생성한다.
     * 
     * @param inflater
     * @param container
     * @return
     */
    private View createView(LayoutInflater inflater, ViewGroup container) {
        FpLog.d(TAG, "createView()");
        final View view = inflater.inflate(R.layout.project_list_fragment, container, false);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FpLog.d(TAG, "onCreateView()");

        load();

        View view = createView(inflater, container);
        return view;
    }

    private void load() {
        new ProjectListLoadTask(getActivity(), this).execute();
    }

    @Override
    public void onLoad(ArrayList<Project> projects) {
        this.mProjects = projects;
        if (projects == null) {
            // do nothing
            return;
        }

        mAdapter.setProjects(projects);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (mImageFetcher != null) {
            mImageFetcher.closeCache();
            mImageFetcher = null;
        }

        mAdapter = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, PageListActivity.class);
        intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, mProjects.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_project_list_option, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add: {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                EditText text = new EditText(mContext);
                builder.setTitle("Insert project name")
                        .setView(text)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub

                                    }
                                });
                builder.create().show();
                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
