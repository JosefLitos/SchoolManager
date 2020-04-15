package com.schlmgr.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView.BufferType;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.schlmgr.R;
import com.schlmgr.gui.Controller;
import com.schlmgr.gui.Controller.ControlListener;
import com.schlmgr.gui.list.HierarchyItemModel;

import IOSystem.Formatter;
import testing.Test;

import static android.view.KeyEvent.ACTION_UP;

public class SettingsFragment extends Fragment implements ControlListener {

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_settings, container, false);

		CompoundButton parse = root.findViewById(R.id.setts_him_parse);
		parse.setChecked(HierarchyItemModel.parse);
		parse.setOnClickListener(v -> HierarchyItemModel.setParse(parse.isChecked()));

		CompoundButton defFlip = root.findViewById(R.id.setts_him_flipped);
		defFlip.setChecked(HierarchyItemModel.defFlip);
		defFlip.setOnClickListener(v -> HierarchyItemModel.setDefFlip(defFlip.isChecked()));

		CompoundButton flipAll = root.findViewById(R.id.setts_him_allflip);
		flipAll.setChecked(HierarchyItemModel.flipAllOnClick);
		flipAll.setOnClickListener(v -> HierarchyItemModel.setFlipAllOnClick(flipAll.isChecked()));

		EditText amount = root.findViewById(R.id.setts_test_amount);
		amount.setText("" + Test.getAmount(), BufferType.EDITABLE);
		amount.setOnEditorActionListener((v, actionId, event) -> {
			if (event.getAction() == ACTION_UP)
				Test.setAmount(Integer.parseInt(amount.getText().toString()));
			return true;
		});

		EditText time = root.findViewById(R.id.setts_test_time);
		time.setText("" + Test.getDefaultTime(), BufferType.EDITABLE);
		time.setOnEditorActionListener((v, actionId, event) -> {
			if (event.getAction() == ACTION_UP)
				Test.setDefaultTime(Integer.parseInt(time.getText().toString()));
			return true;
		});

		CompoundButton clever = root.findViewById(R.id.setts_test_clever);
		clever.setChecked(Test.isClever());
		clever.setOnClickListener(v -> Test.setClever(clever.isChecked()));

		CompoundButton testPic = root.findViewById(R.id.setts_test_type);
		testPic.setChecked((Boolean) Formatter.getSetting("testTypePicture"));
		testPic.setOnClickListener(v -> Formatter.putSetting("testTypePicture", testPic.isChecked()));
		return root;
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