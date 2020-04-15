package com.schlmgr.gui.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.schlmgr.R;
import com.schlmgr.gui.Popup.TextPopup;

import java.util.List;

import objects.Picture;
import objects.Reference;

import static com.schlmgr.gui.Controller.activity;
import static com.schlmgr.gui.Controller.dp;

public class HierarchyAdapter extends OpenListAdapter<HierarchyItemModel> {
	private LayoutInflater li;
	public List<HierarchyItemModel> list;

	public HierarchyAdapter(@NonNull Context context, @NonNull List<HierarchyItemModel> objects, Runnable occ) {
		super(context, R.layout.item_hierarchy, R.id.h_item_name, objects, occ);
		li = LayoutInflater.from(context);
		list = objects;
	}

	public View getView(int index, View view, ViewGroup parent) {
		if (view == null) view = li.inflate(R.layout.item_hierarchy, parent, false);
		HierarchyItemModel item = list.get(index);
		((TextView) view.findViewById(R.id.h_item_name)).setText(item.toShow);
		TextView num = view.findViewById(R.id.h_item_number);
		num.setBackgroundColor(item.bd instanceof Reference ? 0x400000FF : background(item.bd.getRatio()));
		num.setText((index + 1) + ".");
		if (item.bd instanceof Picture)
			item.ic.setBounds((int) dp, 0, (int) (dp * 33), (int) (dp * 33));
		else item.ic.setBounds(0, 0, (int) (dp * 30), (int) (dp * 30));
		num.setCompoundDrawablesRelative(null, null, item.ic, null);
		if (!item.info.isEmpty()) {
			View iv = view.findViewById(R.id.h_item_info);
			iv.setVisibility(View.VISIBLE);
			iv.setOnClickListener((v) -> new TextPopup(item.info, item.info));
		} else view.findViewById(R.id.h_item_info).setVisibility(View.GONE);
		if (selected > -1) {
			ImageView select = view.findViewById(R.id.h_item_selected);
			select.setVisibility(View.VISIBLE);
//			DrawableCompat.setTint(DrawableCompat.wrap(checkedIcon), getResources().getColor(R.color.colorPrimaryHeader));
			select.setImageDrawable(!item.selected ? activity.getResources().getDrawable(R.drawable.ic_check_box_empty)
					: activity.getResources().getDrawable(R.drawable.ic_check_box_filled));
			select.setOnClickListener(v -> {
				select.setImageDrawable(!(item.selected = !item.selected) ? activity.getResources().getDrawable(
						R.drawable.ic_check_box_empty) : activity.getResources().getDrawable(R.drawable.ic_check_box_filled));
				selected += item.selected ? 1 : -1;
				if (item.bd instanceof Reference) ref += item.selected ? 1 : -1;
				occ.run();
			});
		} else view.findViewById(R.id.h_item_selected).setVisibility(View.GONE);
		return view;
	}

	public static int background(int sf) {
		if (sf == -2) return 0;
		if (sf == -1) return 0x400000FF;
		if (sf == 0) return 0x40FF0000;
		if (sf == 50) return 0x40FFFF00;
		if (sf == 100) return 0x4000FF00;
		String ret;
		if (sf < 50) {
			ret = Integer.toHexString(sf * 256 / 50);
			return Integer.parseInt("40FF" + (ret.length() == 1 ? '0' + ret : ret) + "00", 16);
		} else {
			ret = Integer.toHexString((100 - sf) * 256 / 50);
			return Integer.parseInt("40" + (ret.length() == 1 ? '0' + ret : ret) + "FF00", 16);
		}
	}
}
