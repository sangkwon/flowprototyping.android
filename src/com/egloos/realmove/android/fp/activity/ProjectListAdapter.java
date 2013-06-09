
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;
import com.egloos.realmove.android.fp.model.Project;
import com.example.android.bitmapfun.util.ImageFetcher;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;

public class ProjectListAdapter extends BaseAdapter implements View.OnClickListener {

    private ImageFetcher mImageFetcher;
    private Context mContext;

    private ArrayList<Project> mProjects;
    private LayoutInflater mInflater;
    private Resources mResources;

    public ProjectListAdapter(Context context, ImageFetcher imageFetcher) {
        this.mImageFetcher = imageFetcher;
        this.mContext = context;

        this.mResources = mContext.getResources();

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setProjects(ArrayList<Project> projects) {
        this.mProjects = projects;
    }

    @Override
    public int getCount() {
        if (mProjects == null)
            return 0;
        return mProjects.size();
    }

    @Override
    public Project getItem(int position) {
        return mProjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view != null && view.getTag() instanceof ViewHolder) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.project_list_row, null);
            holder = new ViewHolder();
            holder.thumb = (ImageView) view.findViewById(R.id.thumb);
            holder.subject = (TextView) view.findViewById(R.id.subject);
            holder.created = (TextView) view.findViewById(R.id.created);
            holder.btnPlay = (Button) view.findViewById(R.id.play);
            holder.btnEdit = (Button) view.findViewById(R.id.edit);

            holder.btnPlay.setOnClickListener(this);
            holder.btnEdit.setOnClickListener(this);

            view.setTag(holder);
        }

        Project project = getItem(position);
        holder.subject.setText(project.getSubject());
        DateFormat df = DateFormat.getDateInstance();
        holder.created.setText(df.format(project.getCreated()));
        if (project.getMainImage() != null) {
            mImageFetcher.loadImage(Uri.fromFile(new File(project.getMainImage())), holder.thumb);
        } else {
            holder.thumb.setImageBitmap(null);
            holder.thumb.setImageDrawable(mResources.getDrawable(R.drawable.ic_launcher));
        }

        holder.btnPlay.setTag(project);
        holder.btnEdit.setTag(project);

        return view;
    }

    class ViewHolder {
        ImageView thumb;
        TextView subject;
        TextView created;
        Button btnPlay;
        Button btnEdit;
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag == null || !(tag instanceof Project)) {
            return;
        }

        Project project = (Project) tag;
        switch (view.getId()) {
            case R.id.play: {
                Intent intent = new Intent(mContext, PlayActivity.class);
                intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, project.getId());
                mContext.startActivity(intent);
                break;
            }
            case R.id.edit: {
                Intent intent = new Intent(mContext, PageListActivity.class);
                intent.putExtra(PageListFragment.EXTRA_PROJECT_ID, project.getId());
                mContext.startActivity(intent);
                break;
            }
        }
    }

}
