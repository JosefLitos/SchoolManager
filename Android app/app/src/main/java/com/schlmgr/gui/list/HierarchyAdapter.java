package com.schlmgr.gui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schlmgr.R;
import com.schlmgr.gui.CurrentData;

import java.util.List;

import IOSystem.Formatter;
import objects.MainChapter;

public class HierarchyAdapter extends SearchAdapter<HierarchyItemModel> {

	public HierarchyAdapter(RecyclerView parent, OnItemActionListener listener,
													@NonNull List<HierarchyItemModel> objects, Runnable occ) {
		super(parent, listener, objects, occ);
	}

	@NonNull
	@Override
	public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new HIMHolder(LayoutInflater.from(
				parent.getContext()).inflate(R.layout.item_hierarchy, parent, false));
	}

	public class HIMHolder extends ItemHolder {

		final ImageView remove;

		public HIMHolder(@NonNull View itemView) {
			super(itemView);
			remove = itemView.findViewById(R.id.btn_remove);
			remove.setOnClickListener(v -> {
				MainChapter mch = (MainChapter) last.bd;
				CurrentData.ImportedMchs.removeMch(mch.getDir());
				mch.close();
				list.remove(last);
				notifyDataSetChanged();
			});
			remove.setOnLongClickListener(v -> listener.onItemLongClick(last));
		}

		@Override
		protected void setData(int pos) {
			super.setData(pos);
			remove.setVisibility(last.bd instanceof MainChapter && !((MainChapter) last.bd).getDir()
					.getOriginalName().contains(Formatter.getSubjectsDir().getOriginalName())
					? View.VISIBLE : View.GONE);
		}
	}
}
