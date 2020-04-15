package com.schlmgr.gui.list;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class OpenListAdapter<T extends HierarchyItemModel> extends ArrayAdapter<T> {
	public final List<T> list;
	public int selected = -1;
	Runnable occ;
	public int ref;

	public OpenListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<T> objects, Runnable occ) {
		super(context, resource, textViewResourceId, objects);
		list = objects;
		this.occ = occ;
	}
}
