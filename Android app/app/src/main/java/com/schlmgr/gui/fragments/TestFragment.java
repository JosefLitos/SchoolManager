package com.schlmgr.gui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.schlmgr.gui.CurrentData.EasyList;
import com.schlmgr.R;
import com.schlmgr.gui.Controller;
import com.schlmgr.gui.Controller.ControlListener;
import com.schlmgr.gui.list.SearchItemModel;

import java.util.LinkedList;
import java.util.List;

import IOSystem.Formatter;
import objects.Picture;
import objects.templates.Container;
import testing.Test;

import static android.view.KeyEvent.ACTION_UP;
import static com.schlmgr.gui.CurrentData.backLog;
import static com.schlmgr.gui.Controller.dp;

public class TestFragment extends Fragment implements ControlListener {

	private boolean wordTest;
	private int amount;
	private int time;
	private ListView lv;
	private Button start;

	private static final List<SearchItemModel> list = new LinkedList<>();

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_test, container, false);
		ImageSwitcher type = root.findViewById(R.id.test_type);
		type.setFactory(() -> {
			ImageView iv = new ImageView(getContext());
			iv.setLayoutParams(new LayoutParams((int) (40 * dp), (int) (40 * dp)));
			return iv;
		});
		type.setImageResource((wordTest = (Boolean) Formatter.getSetting("testTypePicture"))
				? R.drawable.ic_image : R.drawable.ic_word);
		type.setOnClickListener(v -> type.setImageResource((wordTest = !wordTest)
				? R.drawable.ic_image : R.drawable.ic_word));

		EditText amount = root.findViewById(R.id.test_amount);
		amount.setText("" + Test.getAmount(), BufferType.EDITABLE);
		amount.setOnEditorActionListener((v, actionId, event) -> {
			if (event.getAction() == ACTION_UP)
				this.amount = Integer.parseInt(amount.getText().toString());
			return true;
		});

		EditText time = root.findViewById(R.id.test_time);
		time.setText("" + Test.getDefaultTime(), BufferType.EDITABLE);
		time.setOnEditorActionListener((v, actionId, event) -> {
			if (event.getAction() == ACTION_UP)
				this.time = Integer.parseInt(time.getText().toString());
			return true;
		});
		(lv = root.findViewById(R.id.test_list)).setAdapter(new Adapter(new LinkedList<>()));
		(start = root.findViewById(R.id.start)).setOnClickListener(v -> {
			//TODO: start the test itself
		});
		return root;
	}

	private class Adapter extends ArrayAdapter<SearchItemModel> {

		//		final List<SearchItemModel> list; TODO: MainFragment
		final LayoutInflater li;

		Adapter(@NonNull List<SearchItemModel> objects) {
			super(TestFragment.this.getContext(), R.layout.item_adder, R.id.item_adder_name, list);
//			list = objects;
			li = LayoutInflater.from(getContext());
			if (list.isEmpty() || list.get(0) != null) list.add(list.size(), null);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int index, @Nullable View view, @NonNull ViewGroup parent) {
			view = li.inflate(R.layout.item_adder, parent, false);
			SearchItemModel item = list.get(index);
			TextView tv = view.findViewById(R.id.item_adder_name);
			Drawable ic;
			if (item == null) {
				(ic = getResources().getDrawable(R.drawable.ic_add)).setBounds(0, 0, (int) (dp * 30), (int) (dp * 30));
				tv.setText(getString(R.string.test_add));
				tv.setOnClickListener(v -> {
					//TODO: open MainFragment and choose an item
					if (!backLog.path.isEmpty()) {
						EasyList<Container> path = new EasyList<>();
						for (int i = 0; i < backLog.path.size() - 1; i++)
							path.add((Container) backLog.path.get(i));
						list.add(1, new SearchItemModel(backLog.path.get(-1), path, -1));
					}
					notifyDataSetChanged();
				});
				view.findViewById(R.id.item_adder_remove).setVisibility(View.GONE);
			} else {
				tv.setText(item.bd.getName());
				if (item.bd instanceof Picture)
					(ic = item.ic).setBounds((int) dp, 0, (int) (dp * 33), (int) (dp * 33));
				else (ic = item.ic).setBounds(0, 0, (int) (dp * 30), (int) (dp * 30));
				view.findViewById(R.id.item_adder_remove).setOnClickListener(v -> {
					list.remove(index);
					notifyDataSetChanged();
				});
			}
			tv.setCompoundDrawablesRelative(ic, null, null, null);
			return view;
		}
	}

	@Override
	public void onResume() {
		Controller.setCurrentControl(this, 0, false);
		super.onResume();
	}

	@Override
	public void run() {
		Controller.defaultBack.run();
	}
}