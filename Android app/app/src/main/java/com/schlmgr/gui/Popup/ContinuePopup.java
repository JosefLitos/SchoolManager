package com.schlmgr.gui.Popup;

import android.view.View;
import android.widget.TextView;

import com.schlmgr.R;

public class ContinuePopup extends AbstractPopup {

	final String msg;
	final Thread onContinue;

	public ContinuePopup(String msg, Runnable onClick) {
		super(R.layout.popup_continue);
		this.msg = msg;
		onContinue = new Thread(onClick);
		create();
	}

	@Override
	protected void addContent(View view) {
		((TextView) view.findViewById(R.id.text)).setText(msg);
		view.findViewById(R.id.cancel).setOnClickListener(x -> dismiss());
		view.findViewById(R.id.ok).setOnClickListener(x -> {
			dismiss();
			onContinue.start();
		});
	}
}