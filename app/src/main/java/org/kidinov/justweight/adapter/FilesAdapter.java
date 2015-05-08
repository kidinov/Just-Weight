package org.kidinov.justweight.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.kidinov.justweight.R;
import org.kidinov.justweight.dialog.FilePickerDialogFragment;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;

/**
 * Created by akid on 17/04/15.
 */
public class FilesAdapter extends RecyclerView.Adapter {

    public static File file;
    private Context ctx;
    private LayoutInflater li;
    private RecyclerView rv;
    private FilePickerDialogFragment fragment;

    public FilesAdapter(RecyclerView rv, File file, FilePickerDialogFragment fragment) {
        this.ctx = rv.getContext();
        this.rv = rv;
        FilesAdapter.file = file;
        this.fragment = fragment;
        li = LayoutInflater.from(ctx);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(li.inflate(R.layout.file_item, parent, false), rv, file, fragment);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int index) {
        ViewHolder h = (ViewHolder) holder;
        h.bind(index);
    }

    @Override
    public int getItemCount() {
        if (file.list() != null) {
            return file.list().length + 1;
        } else {
            return 1;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View v;
        private final TextView text;
        private final ImageView image;
        private final File file;
        private FilePickerDialogFragment fragment;
        private final List<File> files;
        private final RecyclerView rv;

        public ViewHolder(View v, RecyclerView rv, File file, FilePickerDialogFragment fragment) {
            super(v);
            this.v = v;
            this.file = file;
            this.fragment = fragment;
            this.files = Arrays.asList(file.listFiles());
            Collections.sort(files, (File f1, File f2) -> {
                if (f1.isDirectory() && f2.isDirectory()) {
                    return f1.getName().compareTo(f2.getName());
                } else if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.getName().compareTo(f2.getName());
                }
            });
            this.rv = rv;
            text = ((TextView) v.findViewById(R.id.text));
            image = (ImageView) v.findViewById(R.id.image);

        }

        public void bind(int index) {
            if (index == 0) {
                text.setText("...");
                image.setImageResource(R.drawable.back);
                v.setOnClickListener(view -> {
                    if (file.getParentFile() != null && file.getParentFile().exists()) {
                        SlideInBottomAnimationAdapter adapter = new SlideInBottomAnimationAdapter(new FilesAdapter(rv, file.getParentFile(), fragment));
                        adapter.setFirstOnly(true);
                        rv.setAdapter(adapter);
                    }
                });
            } else {
                File f = files.get(index - 1);
                text.setText(f.getName());
                image.setImageResource(f.isDirectory() ? R.drawable.folder : R.drawable.file);
                v.setOnClickListener(view -> {
                    if (f.isDirectory()) {
                        SlideInBottomAnimationAdapter adapter = new SlideInBottomAnimationAdapter(new FilesAdapter(rv, f, fragment));
                        adapter.setFirstOnly(true);
                        rv.setAdapter(adapter);
                        if (fragment.operation != FilePickerDialogFragment.EXPORT) fragment.fileNameEt.setText("");
                    } else if (!f.isDirectory() && fragment.operation == FilePickerDialogFragment.IMPORT) {
                        fragment.fileNameEt.setText(f.getName());
                        fragment.positiveClickAction();
                    }
                });
            }
        }
    }
}
