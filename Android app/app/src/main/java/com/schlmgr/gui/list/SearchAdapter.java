package com.schlmgr.gui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schlmgr.R;
import com.schlmgr.gui.activity.SelectItemsActivity;
import com.schlmgr.gui.list.SearchAdapter.ItemHolder;
import com.schlmgr.gui.popup.TextPopup;

import java.util.List;

import objects.Reference;

import static com.schlmgr.gui.activity.MainActivity.ic_check_empty;
import static com.schlmgr.gui.activity.MainActivity.ic_check_filled;
import static com.schlmgr.gui.list.HierarchyItemModel.show_desc;

public class SearchAdapter<I extends HierarchyItemModel>
		extends OpenListAdapter<I, ItemHolder> {
	public final List<I> list;
	public int selected;
	public Runnable occ;
	public int ref;
	final boolean selectActivity;
	protected final OnItemActionListener listener;
	public final boolean search;
	public int firstItemPos;

	public interface OnItemActionListener {
		void onItemClick(HierarchyItemModel item);

		boolean onItemLongClick(HierarchyItemModel item);
	}

	public SearchAdapter(RecyclerView parent, OnItemActionListener l,
	                     @NonNull List<I> objects, Runnable occ) {
		super(objects);
		update(parent);
		search = !(this instanceof HierarchyAdapter);
		listener = l;
		list = objects;
		this.occ = occ;
		selected = (selectActivity = l instanceof SelectItemsActivity) ? 0 : -1;
		for (HierarchyItemModel him : objects) {
			if (him.isSelected()) {
				if (selected == -1) selected += 2;
				else selected++;
			}
		}
	}

	@NonNull
	@Override
	public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ItemHolder(LayoutInflater.from(
				parent.getContext()).inflate(R.layout.item_search, parent, false));
	}

	public class ItemHolder extends RecyclerView.ViewHolder {

		final TextView name, position, desc;
		final ImageView info, check;
		HierarchyItemModel last;

		ItemHolder(@NonNull View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.item_name);
			name.setOnClickListener(v -> listener.onItemClick(last));
			name.setOnLongClickListener(v -> listener.onItemLongClick(last));
			position = itemView.findViewById(R.id.item_pos);
			position.setOnClickListener(v -> listener.onItemClick(last));
			position.setOnLongClickListener(v -> listener.onItemLongClick(last));
			desc = itemView.findViewById(R.id.item_desc);
			desc.setOnClickListener(v -> listener.onItemClick(last));
			desc.setOnLongClickListener(v -> listener.onItemLongClick(last));
			info = itemView.findViewById(R.id.item_info);
			info.setOnClickListener((v) -> new TextPopup(last.info, last.info));
			info.setOnLongClickListener(v -> listener.onItemLongClick(last));
			check = itemView.findViewById(R.id.item_check);
			check.setOnClickListener(v -> {
				check.setImageDrawable((last.selected = !last.selected)
						? ic_check_filled : ic_check_empty);
				selected += last.selected ? 1 : -1;
				if (last.bd instanceof Reference) ref += last.selected ? 1 : -1;
				if (occ != null) occ.run();
			});
			check.setOnLongClickListener(v -> listener.onItemLongClick(last));
		}

		protected void setData(int pos) {
			HierarchyItemModel item = list.get(pos);
			name.setText(item.toShow);
			if (!selectActivity) {
				info.setVisibility(show_desc || item.info.isEmpty()
						? View.GONE : View.VISIBLE);
				desc.setVisibility(show_desc && !item.info.isEmpty()
						? View.VISIBLE : View.GONE);
				if (show_desc && !item.info.isEmpty()) desc.setText(item.info);
			}
			position.setText((pos + 1) + ".");
			position.setBackgroundColor(item.bd instanceof Reference ?
					0x1a6ab8 : background(item.bd.getRatio()));
			if (last == null || last.ic != item.ic)
				position.setCompoundDrawablesRelative(null, null, item.ic, null);
			if (selected > -1 || selectActivity) {
				check.setVisibility(View.VISIBLE);
				check.setImageDrawable(item.selected ? ic_check_filled : ic_check_empty);
			} else check.setVisibility(View.GONE);
			last = item;
		}
	}

	@Override
	public void update(RecyclerView parent) {
		super.update(parent);
		parent.scrollToPosition(firstItemPos);
	}

	@Override
	public void onBindViewHolder(@NonNull SearchAdapter.ItemHolder holder, int position) {
		holder.setData(position);
	}

	/**
	 * Chooses a color depending on the success rate. No color for no children. Blue for no rate.
	 *
	 * @param sf success rate, -1 for no rating, -2 for no children
	 * @return the color for the parameter
	 */
	public static int background(int sf) {
		if (sf == -2) return 0;
		if (sf == -1) return 0x801a6ab8;
		if (sf == 0) return 0x30FF0000;
		if (sf == 50) return 0xf0FFFF00;
		if (sf == 100) return 0x6000FF00;
		String ret;
		if (sf < 50) {
			ret = Integer.toHexString(sf * 256 / 50);
			return Integer.parseInt("40FF" + (ret.length() == 1 ? '0' + ret : ret) + "22", 16);
		} else {
			ret = Integer.toHexString((100 - sf) * 256 / 50);
			return Integer.parseInt("60" + (ret.length() == 1 ? '0' + ret : ret) + "FF22", 16);
		}
	}
}
