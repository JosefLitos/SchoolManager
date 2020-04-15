package com.schlmgr.gui.Popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.schlmgr.gui.Controller;

import java.util.LinkedList;

import static com.schlmgr.gui.Controller.activity;

public abstract class AbstractPopup {

	public static boolean isActive;
	public static boolean isShowing;

	private static final LinkedList<AbstractPopup> showed = new LinkedList<>();

	public static void clean() {
		for (AbstractPopup ap : showed) ap.dismiss(false);
		showed.clear();
	}

	public static void clear() {
		for (AbstractPopup ap : showed) ap.dismiss(true);
		showed.clear();
	}

	final int resId;
	public PopupWindow pw;
	private final Runnable creator = () -> create();

	protected AbstractPopup(int rI) {
		resId = rI;
		Controller.addPopupRepaint(creator);
	}

	public void dismiss() {
		dismiss(true);
	}

	public void dismiss(boolean forever) {
		pw.dismiss();
		showed.remove(pw);
		isActive = false;
		if (forever) Controller.removePopupRepaint(creator);
	}

	protected void create() {
		isActive = isShowing = true;
		activity.runOnUiThread(() -> {
			View view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resId, null);
			pw = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
			pw.setOnDismissListener(() -> isShowing = false);
			view.setOnClickListener(v -> dismiss(true));
			pw.setBackgroundDrawable(new ColorDrawable(0x90000000));
			addContent(view);
			showed.add(this);
			pw.showAtLocation(view, Gravity.CENTER, 0, 0);
		});
	}

	abstract protected void addContent(View view);
}
