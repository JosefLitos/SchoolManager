package com.schlmgr.gui.list;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import IOSystem.Formatter;
import objects.MainChapter;
import objects.Picture;
import objects.Reference;
import objects.Word;
import objects.templates.BasicData;
import objects.templates.Container;
import objects.templates.TwoSided;
import testing.NameReader;

public class HierarchyItemModel {

	public static List<HierarchyItemModel> convert(BasicData[] content, Container parent) {
		if (content == null || content.length == 0) return new ArrayList<>(0);
		return convert(new ArrayList<>(Arrays.asList(content)), parent);
	}

	public static <T extends BasicData> List<HierarchyItemModel>
	convert(List<T> list, Container parent) {
		List<HierarchyItemModel> ret = new ArrayList<>(list.size());
		int pos = 1;
		for (BasicData bd : list) ret.add(new HierarchyItemModel(bd, parent, pos++));
		return ret;
	}

	public static boolean show_desc = false;
	public static boolean parse = true;
	public static boolean defFlip = true;
	public static boolean flipAllOnClick;

	public static void setShowDesc(boolean yes) {
		Formatter.putSetting("doShowDesc", show_desc = yes);
	}

	public static void setParse(boolean yes) {
		Formatter.putSetting("parseNames", parse = yes);
	}

	public static void setDefFlip(boolean yes) {
		Formatter.putSetting("flipWord", defFlip = yes);
	}

	public static void setFlipAllOnClick(boolean on) {
		Formatter.putSetting("flipAllOnClick", flipAllOnClick = on);
	}

	public boolean flipped;
	public int position;
	boolean selected;

	public void setSelected(boolean isSelected) {
		selected = isSelected;
	}

	public boolean isSelected() {
		return selected;
	}

	public BasicData bd;
	public Container parent;
	public Drawable ic;
	public String toShow;
	String info = "";

	public void flip() {
		if (!(bd instanceof Word)) return;
		if (flipped = !flipped) toShow = translates();
		else {
			toShow = nameParser(bd.getName());
			info = bd.getDesc(parent);
		}
	}

	public void update() {
		if (!(bd instanceof Word)) {
			info = bd.getDesc(parent);
			toShow = nameParser(bd.getName());
			return;
		}
		if (flipped) toShow = translates();
		else {
			toShow = nameParser(bd.getName());
			info = bd.getDesc(parent);
		}
	}

	protected String translates() {
		StringBuilder desc = new StringBuilder();
		StringBuilder trls = new StringBuilder();
		for (BasicData trl : ((Word) bd).getChildren(parent)) {
			trls.append('\n').append(nameParser(trl.getName()));
			if (!trl.getDesc(parent).isEmpty())
				desc.append('\n').append(trl.getName()).append(':')
						.append(' ').append(trl.getDesc(parent));
		}
		info = desc.length() > 0 ? desc.substring(1) : "";
		return trls.substring(1);
	}

	public static String nameParser(String name) {
		if (parse && (name.contains("\\/") || name.contains(")") && name.contains("/"))) {
			String[] names = NameReader.readName(name);
			if (names.length == 1) name = names[0];
			else {
				StringBuilder sb = new StringBuilder();
				for (String s : names) sb.append(s.length() > 18 ? '\n' : '\\').append(s);
				name = sb.substring(1);
			}
		}
		return name;
	}

	public void setNew(BasicData item, Container parent) {
		setNew(item, parent, true);
	}

	public void setNew(BasicData item, Container parent, boolean flip) {
		bd = item;
		this.parent = parent;
		toShow = nameParser(bd.getName());
		flipped = false;
		if (bd instanceof Container) {
			if (bd instanceof TwoSided) {
				if (bd instanceof Picture) {
					ic = icPic;
					info = bd.getDesc(parent);
				} else if (bd instanceof Word) {
					ic = icWord;
					flipped = !defFlip;
					if (flip) flip();
					else info = bd.getDesc(parent);
				}
			} else {
				if (bd instanceof MainChapter) ic = icMCh;
				else ic = icChap;
				info = bd.getDesc(parent);
			}
		} else if (bd instanceof Reference) ic = icRef;
	}

	public static Drawable icPic;
	public static Drawable icWord;
	public static Drawable icChap;
	public static Drawable icMCh;
	public static Drawable icRef;

	public HierarchyItemModel(BasicData item, Container parent, int position) {
		this(item, parent, position, true);
	}

	public HierarchyItemModel(BasicData item, Container parent, int position, boolean flip) {
		this.position = position;
		setNew(item, parent, flip);
	}
}
