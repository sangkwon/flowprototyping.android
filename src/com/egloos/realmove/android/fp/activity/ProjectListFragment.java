
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.common.BaseFragment;
import com.egloos.realmove.android.fp.common.FpLog;
import com.egloos.realmove.android.fp.db.DBAdapter;
import com.egloos.realmove.android.fp.db.LoadProjectListTask;
import com.egloos.realmove.android.fp.db.LoadProjectListTask.Callback;
import com.egloos.realmove.android.fp.model.Project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class ProjectListFragment extends BaseFragment implements Callback, OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = ProjectListFragment.class.getSimpleName();

	private ProjectListAdapter mAdapter;

	private ArrayList<Project> mProjects;

	private ListView mListView;

	private Project mSelectedProject;

	public static ProjectListFragment newInstance() {
		return new ProjectListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mAdapter = new ProjectListAdapter(getActivity());
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

		mListView = (ListView) view.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		registerForContextMenu(mListView);
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
		new LoadProjectListTask(getActivity(), this).execute();
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
		mAdapter = null;
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_project_list_option, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * 프로젝트 정보입력창을 띄운다.
	 * 
	 * @param project 수정이라면 값을 준다. 추가라면 null
	 */
	private void showProjectInfoDialog(final Project project) {
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View textEntryView = factory.inflate(R.layout.input_project_dialog, null);
		if (project != null) {
			TextView txt = (TextView) textEntryView.findViewById(R.id.subject);
			txt.setText(project.getSubject());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(textEntryView).setTitle(R.string.input_project_info).setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							CharSequence txt = ((TextView) textEntryView.findViewById(R.id.subject)).getText();
							if (txt != null) {
								String subject = txt.toString().trim();
								if (subject.length() > 0) {
									if (project == null) {
										createProject(subject);
									} else {
										project.setSubject(subject);
										modifyProject(project);
									}
									return;
								}
							}

							try {
								Toast.makeText(getActivity(), R.string.error_subject_empty, Toast.LENGTH_SHORT).show();
							} catch (Exception ex) {
								// do nothing
							}
						}
					}

				});
		builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add: {
				showProjectInfoDialog(null);
				break;
			}
			case R.id.setting: {
				getActivity().startActivity(new Intent(getActivity(), SettingActivity.class));
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/* package */void createProject(String subject) {
		final Project project = new Project();
		project.setSubject(subject);
		project.setCreated(new Date());

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				DBAdapter db = null;
				try {
					db = new DBAdapter(getActivity()).open();
					db.insertProject(project);
				} catch (Exception e) {
					FpLog.e(TAG, e);
				} finally {
					if (db != null)
						db.close();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				load();
				super.onPostExecute(result);
			}

		}.execute();
	}

	/* package */void modifyProject(final Project project) {
		project.setUpdated(new Date());

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				DBAdapter db = null;
				try {
					db = new DBAdapter(getActivity()).open();
					db.updateProject(project);
				} catch (Exception e) {
					FpLog.e(TAG, e);
				} finally {
					if (db != null)
						db.close();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				load();
				super.onPostExecute(result);
			}

		}.execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Project project = mProjects.get(position);

		Intent intent = new Intent(getActivity(), PageListActivity.class);
		intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, project.getId());
		getActivity().startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mSelectedProject = mAdapter.getItem(position);
		mListView.showContextMenu();
		return false;
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
			case R.id.export: {

				break;
			}
			case R.id.remove: {

				break;
			}

			case R.id.modify: {
				showProjectInfoDialog(mSelectedProject);
				break;
			}

		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = new android.view.MenuInflater(getActivity());
		inflater.inflate(R.menu.menu_project_list_context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

}
