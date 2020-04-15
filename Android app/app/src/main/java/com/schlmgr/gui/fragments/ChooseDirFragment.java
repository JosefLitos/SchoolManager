package com.schlmgr.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.schlmgr.R;
import com.schlmgr.gui.AndroidIOSystem;
import com.schlmgr.gui.Controller;
import com.schlmgr.gui.Controller.ControlListener;
import com.schlmgr.gui.CurrentData;
import com.schlmgr.gui.CurrentData.EasyList;
import com.schlmgr.gui.list.DirAdapter;

import java.io.File;
import java.util.LinkedList;

import IOSystem.Formatter;

import static com.schlmgr.gui.Controller.CONTEXT;
import static com.schlmgr.gui.Controller.activity;
import static com.schlmgr.gui.Controller.dp;
import static com.schlmgr.gui.CurrentData.backLog;

public class ChooseDirFragment extends Fragment implements ControlListener, OnItemClickListener, OnItemLongClickListener {

	private HorizontalScrollView path_handler;
	private LinearLayout path;
	private ListView list;
	public static ViewState VS = new ViewState();

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_choose_dir, container, false);
		path_handler = root.findViewById(R.id.dir_path_handler);
		path = root.findViewById(R.id.dir_path);
		list = root.findViewById(R.id.dir_list);
		path_handler.setHorizontalScrollBarEnabled(false);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		File f;
		if (VS.first) {
			VS.first = false;
			f = resetView();
		} else if (VS.path.isEmpty()) f = null;
		else {
			f = VS.path.get(-1);
			File[] dirPath = VS.path.toArray(new File[0]);
			VS.path.clear();
			for (int i = 0; i < dirPath.length; i++)
				addPathButton(dirPath[i], i == 0 ? DirAdapter.storageName(dirPath[i]) : dirPath[i].getName());
		}
		makeFiles(f);
		return root;
	}

	private File resetView() {
		File f = Formatter.getPath();
		String path;
		if (f.getAbsolutePath().contains("/0")) {
			path = AndroidIOSystem.visibleFilePath(f.getAbsolutePath());
		} else {
			path = f.getAbsolutePath().substring(AndroidIOSystem.storageDir.length());
			path = path.substring(path.indexOf(File.separatorChar, 1));
		}
		String[] s = path.split(File.separator);
		f = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().indexOf(path)));
		addPathButton(f, DirAdapter.storageName(f));
		for (int i = 1; i < s.length; i++) addPathButton(f = new File(f, s[i]), f.getName());
		return f;
	}

	@Override
	public void onItemClick(AdapterView<?> par, View v, int index, long id) {
		File file = (File) par.getItemAtPosition(index);
		DirAdapter adapter = (DirAdapter) par.getAdapter();
		addPathButton(file, adapter.names == null ? file.getName() : adapter.names.get(index));
		makeFiles(file);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> par, View v, int index, long id) {
		if (Formatter.changeDir(((File) par.getItemAtPosition(index)).getAbsolutePath())) {
			MainFragment.VS = new MainFragment.ViewState();
			backLog.clear();
			CurrentData.createMchs();
			activity.runOnUiThread(() -> Toast.makeText(
					CONTEXT, getString(R.string.choose_dir), Toast.LENGTH_SHORT).show());
		}
		Controller.defaultBack.run();
		return true;
	}

	@Override
	public void run() {
		if (!VS.path.isEmpty() && Controller.isActive(this)) {
			if (makeFiles(VS.path.get(VS.path.size() == 1 ? -1 : -2))) {
				VS.path.remove(-1);
				this.path.removeViews(VS.path.size() * 2, 2);
			}
		} else Controller.defaultBack.run();
	}

	private boolean makeFiles(File parent) {
		if (!AndroidIOSystem.requestWrite()) return false;
		LinkedList<File> files = new LinkedList<>();
		if (parent == null) {
			list.setAdapter(new DirAdapter(getContext(), files, true));
		} else if (parent.listFiles() != null) {
			for (File f : parent.listFiles())
				if (f.isDirectory()) {
					String name = f.getName();
					int i = files.size() - 1;
					for (; i >= 0 && files.get(i).getName().compareToIgnoreCase(name) > 0; i--) ;
					files.add(i + 1, f);
				}
			list.setAdapter(new DirAdapter(getContext(), files, false));
		}
		return true;
	}

	private void addPathButton(File file, String name) {
		TextView btn = new Button(getContext());
		btn.setTextSize(17);
		btn.setAllCaps(false);
		btn.setBackground(null);
		btn.setTextColor(0xFFFFFFFF);
		btn.setPadding(0, 0, 0, 0);
		btn.setText(name);
		btn.setTypeface(null);
		btn.setLayoutParams(new LayoutParams((int)
				(btn.getPaint().measureText(name) + dp * 8), LayoutParams.MATCH_PARENT));
		btn.setOnClickListener((v) -> {
			if (VS.path.get(-1) == file || !makeFiles(file)) return;
			int diff = 0;
			for (; file != VS.path.get(-1); diff++) VS.path.remove(-1);
			try {
				this.path.removeViews(VS.path.size() * 2, diff * 2);
			} catch (NullPointerException e) {
				this.path.removeViews(VS.path.size() * 2, diff * 2);
			}
		});
		path.addView(btn);
		VS.path.add(file);
		btn = new TextView(getContext());
		btn.setBackground(getResources().getDrawable(R.drawable.ic_bread_crumbs));
		btn.setLayoutParams(new LayoutParams((int) (dp * 7), LayoutParams.MATCH_PARENT));
		btn.setScaleX(2.4f);
		path.addView(btn);
		path_handler.post(() -> path_handler.fullScroll(View.FOCUS_RIGHT));
	}

	@Override
	public void onResume() {
		Controller.setCurrentControl(this, R.menu.more_choose_dir, false);
		super.onResume();
	}

	public static class ViewState {
		final EasyList<File> path = new EasyList<>();
		boolean first = true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.more_dir_default:
				Formatter.resetDir();
				MainFragment.VS = new MainFragment.ViewState();
				backLog.clear();
				CurrentData.createMchs();
				activity.runOnUiThread(() -> Toast.makeText(
						CONTEXT, getString(R.string.choose_dir), Toast.LENGTH_SHORT).show());
				Controller.defaultBack.run();
				break;
			case R.id.more_dir_view:
				VS.path.clear();
				path.removeAllViews();
				makeFiles(resetView());
		}
		return true;
	}
}