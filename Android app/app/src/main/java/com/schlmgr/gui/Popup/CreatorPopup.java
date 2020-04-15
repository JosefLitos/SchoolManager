package com.schlmgr.gui.Popup;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schlmgr.R;

import static com.schlmgr.gui.Controller.activity;

public class CreatorPopup extends AbstractPopup {

	public Button ok;
	public static EditText et_name;
	public static EditText et_desc;
	public View view;

	final String type;
	final Includer toInclude;

	public CreatorPopup(String header, Includer toInclude) {
		super(R.layout.popup_creator);
		type = header;
		this.toInclude = toInclude;
		create();
	}

	@Override
	protected void addContent(View view) {
		this.view = view;
		((TextView) view.findViewById(R.id.popup_new_type)).setText(type);
		String text = "";
		if (et_name != null) text = et_name.getText().toString();
		(et_name = view.findViewById(R.id.popup_new_name)).setText(text);
		if (et_desc != null) text = et_desc.getText().toString();
		(et_desc = view.findViewById(R.id.popup_new_desc)).setText(text);
		ok = view.findViewById(R.id.ok);
		view.findViewById(R.id.cancel).setOnClickListener(x -> dismiss());
		View include = toInclude.onInclude(activity.getLayoutInflater(), this);
		if (include != null) ((LinearLayout) et_desc.getParent()).addView(include, 3);
	}

	public void dismiss(boolean forever) {
		if (forever) et_name = et_desc = null;
		super.dismiss(forever);
	}

	public interface Includer {
		View onInclude(LayoutInflater li, CreatorPopup cp);
	}
}
