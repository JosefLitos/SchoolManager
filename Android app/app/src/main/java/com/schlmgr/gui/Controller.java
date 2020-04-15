package com.schlmgr.gui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore.Images.Media;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu.OnMenuItemClickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.schlmgr.R;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Controller {
	public static AppCompatActivity activity;
	public static Runnable defaultBack;
	public static float dp;
	public static Context CONTEXT;

	private static final Controller control = new Controller();

	/**
	 * Should be only called by an Activity object
	 *
	 * @return helping handler of this app
	 */
	public static Controller getControl() {
		if (control.taken) return null;
		control.taken = true;
		return control;
	}

	public int menuRes;
	public ControlListener currentControl;
	public ImageView moreButton;
	public ImageView selectButton;
	public final List<Runnable> popupRepaint = new LinkedList<>();

	private boolean taken = false;

	public static boolean isActive(ControlListener test) {
		return test == control.currentControl;
	}

	public static void setCurrentControl(ControlListener o, int menuRes, boolean selectVisible) {
		control.currentControl = o;
		setMenuRes(menuRes);
		toggleSelectBtn(selectVisible);
	}

	public static void addPopupRepaint(Runnable forRepaint) {
		control.popupRepaint.add(forRepaint);
	}

	public static void removePopupRepaint(Runnable forRepaint) {
		control.popupRepaint.remove(forRepaint);
	}

	public static void clearPopupRepaint() {
		control.popupRepaint.clear();
	}

	public interface ControlListener extends Runnable, OnClickListener, OnMenuItemClickListener {
		@Override
		default void run() {
		}

		@Override
		default void onClick(View v) {
		}

		@Override
		default boolean onMenuItemClick(MenuItem item) {
			return false;
		}
	}

	public static void setMenuRes(int newMenuResource) {
		control.moreButton.setVisibility((control.menuRes = newMenuResource) == 0 ? View.GONE : View.VISIBLE);
	}

	public static void toggleSelectBtn(boolean visible) {
		control.selectButton.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	public static String translate(Class type) {
		if (type == objects.MainChapter.class)
			return activity.getString(R.string.hierarchy_mch);
		if (type == objects.SaveChapter.class
				|| type == objects.Chapter.class)
			return activity.getString(R.string.hierarchy_ch);
		if (type == objects.Reference.class)
			return activity.getString(R.string.hierarchy_ref);
		if (type == objects.Word.class)
			return activity.getString(R.string.hierarchy_word);
		if (type == objects.Picture.class)
			return activity.getString(R.string.hierarchy_picture);
		return type.getName();
	}

	/**
	 * Code to create a File from the given URI created by an Intent, has to be a file, not a folder.
	 * Original source code available at:
	 * https://stackoverflow.com/questions/6935497/android-get-gallery-image-uri-path
	 *
	 * @author Allan Jiang
	 */
	public static File getFileFromUri(Uri uri) {
		Looper.prepare();
		Cursor c = new CursorLoader(activity.getApplicationContext(), uri,
				new String[]{Media.DATA}, null, null, null).loadInBackground();
		int index = c.getColumnIndexOrThrow(Media.DATA);
		c.moveToFirst();
		return new File(c.getString(index));
	}
}
