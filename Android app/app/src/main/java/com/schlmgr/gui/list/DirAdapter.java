package com.schlmgr.gui.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.schlmgr.R;
import com.schlmgr.gui.AndroidIOSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.schlmgr.gui.Controller.activity;

public class DirAdapter extends ArrayAdapter<File> {
	private LayoutInflater li;
	public List<File> list;
	public List<String> names;

	public DirAdapter(@NonNull Context context, @NonNull List<File> objects, boolean storages) {
		super(context, R.layout.item_dir, objects);
		li = LayoutInflater.from(context);
		list = objects;
		if (storages) {
			names = new ArrayList<>(list.size());
			if (list.size() == 1) names.add(activity.getString(R.string.storage_internal));
			else {
				File suspicious = null;
				boolean foundInternal = false;
				for (File f : new File(AndroidIOSystem.storageDir).listFiles()) {
					try {
						if (f.listFiles() == null) continue;
						list.add((f.getName().equals("emulated")
								&& f.listFiles().length > 0) ? f = f.listFiles()[0] : f);
					} catch (Exception e) {
						continue;
					}
					switch (f.getName()) {
						case "usbotg":
							names.add(activity.getString(R.string.storage_usbotg));
							break;
						case "emulated":
						case "sdcard0":
						case "external0":
						case "0":
							if (foundInternal) {
								list.remove(f);
								break;
							}
							foundInternal = true;
							names.add(activity.getString(R.string.storage_internal));
							break;
						case "sdcard1":
						case "external1":
						case "ext":
							names.add(activity.getString(R.string.storage_external));
							break;
						case "sdcard":
						case "external":
							suspicious = f;
							break;
						default:
							names.add(f.getName());
					}
				}
				if (suspicious != null)
					if (names.contains(activity.getString(R.string.storage_external))) {
						names.add(activity.getString(R.string.storage_internal));
					} else if (foundInternal) {
						names.add(activity.getString(R.string.storage_external));
					}

			}
			if (!names.contains(activity.getString(R.string.storage_internal))) {
				list.add(new File(AndroidIOSystem.defDir));
				names.add(activity.getString(R.string.storage_internal));
			}
		}
	}

	public static String storageName(File f) {
		File[] files = new File(AndroidIOSystem.storageDir).listFiles();
		if (files.length == 1 || f.getName().equals("0"))
			return activity.getString(R.string.storage_internal);
		Boolean ext = null;
		File suspicious = null;
		for (File file : files) {
			switch (file.getName()) {
				case "usbotg":
					if (file.equals(f)) return activity.getString(R.string.storage_usbotg);
					break;
				case "sdcard0":
				case "external0":
				case "emulated":
					if (file.equals(f)) return activity.getString(R.string.storage_internal);
					if (ext == null) ext = false;
					break;
				case "sdcard1":
				case "external1":
				case "ext":
					if (file.equals(f)) return activity.getString(R.string.storage_external);
					ext = true;
					break;
				case "sdcard":
				case "external":
					if (file.equals(f)) {
						if (ext == null) suspicious = file;
						else
							return activity.getString(ext ? R.string.storage_internal : R.string.storage_external);
					}
					break;
				default:
					if (file.equals(f)) return file.getName();
			}
		}
		if (suspicious != null)
			return activity.getString(ext ? R.string.storage_internal : R.string.storage_external);
		return f.getName();
	}

	public View getView(int index, View view, ViewGroup parent) {
		if (view == null) view = li.inflate(R.layout.item_dir, parent, false);
		String name = names == null ? list.get(index).getName() : names.get(index);
		((TextView) view.findViewById(R.id.item_name)).setText(name);
		return view;
	}
}
