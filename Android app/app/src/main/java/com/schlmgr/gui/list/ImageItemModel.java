package com.schlmgr.gui.list;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import objects.Picture;

public class ImageItemModel {
	public final Bitmap pic1;
	public final Picture p1;
	public final Bitmap pic2;
	public final Picture p2;

	public ImageItemModel(Picture p1, Picture p2) {
		pic1 = BitmapFactory.decodeFile(
				(this.p1 = p1).getIdentifier().getDir().getAbsolutePath() + "/Pictures/" + p1 + ".jpg");
		if (p2 == null) {
			this.p2 = null;
			pic2 = null;
		} else pic2 = BitmapFactory.decodeFile(
				(this.p2 = p2).getIdentifier().getDir().getAbsolutePath() + "/Pictures/" + p2 + ".jpg");
	}
}
