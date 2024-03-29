package com.schlmgr.gui.fragments;

import static android.widget.Toast.makeText;
import static com.schlmgr.gui.Controller.CONTEXT;
import static com.schlmgr.gui.Controller.activity;
import static com.schlmgr.gui.Controller.dp;
import static com.schlmgr.gui.CurrentData.backLog;
import static com.schlmgr.gui.CurrentData.finishLoad;
import static com.schlmgr.gui.list.HierarchyItemModel.convert;
import static IOSystem.Formatter.defaultReacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.schlmgr.R;
import com.schlmgr.gui.AndroidIOSystem;
import com.schlmgr.gui.Controller;
import com.schlmgr.gui.CurrentData;
import com.schlmgr.gui.CurrentData.EasyList;
import com.schlmgr.gui.ExplorerStuff;
import com.schlmgr.gui.UriPath;
import com.schlmgr.gui.list.AbstractPopupRecyclerAdapter;
import com.schlmgr.gui.list.HierarchyAdapter;
import com.schlmgr.gui.list.HierarchyItemModel;
import com.schlmgr.gui.list.ImageAdapter;
import com.schlmgr.gui.list.ImageItemModel;
import com.schlmgr.gui.list.ImagePopupRecyclerAdapter;
import com.schlmgr.gui.list.SearchAdapter;
import com.schlmgr.gui.list.SearchAdapter.OnItemActionListener;
import com.schlmgr.gui.list.SearchItemModel;
import com.schlmgr.gui.list.TranslatePopupRecyclerAdapter;
import com.schlmgr.gui.popup.ContinuePopup;
import com.schlmgr.gui.popup.CreatorPopup;
import com.schlmgr.gui.popup.CreatorPopup.Includer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import IOSystem.Formatter;
import IOSystem.Formatter.Data;
import IOSystem.ReadElement.ContentReader;
import IOSystem.SimpleReader;
import IOSystem.SimpleWriter;
import objects.Chapter;
import objects.MainChapter;
import objects.Picture;
import objects.Reference;
import objects.SaveChapter;
import objects.Word;
import objects.templates.BasicData;
import objects.templates.Container;
import objects.templates.ContainerFile;
import objects.templates.SemiElementContainer;
import objects.templates.TwoSided;

public class MainFragment extends Fragment
		implements Controller.ControlListener, OnItemActionListener {

	@SuppressLint("StaticFieldLeak")
	private static LinearLayout selectOpts;
	@SuppressLint("StaticFieldLeak")
	private static TextView edit;
	@SuppressLint("StaticFieldLeak")
	private static TextView delete;
	@SuppressLint("StaticFieldLeak")
	private static TextView reference;
	@SuppressLint("StaticFieldLeak")
	private static TextView cut;

	private LinearLayout pasteOpts;
	private TextView paste;

	private static Drawable icDelete;
	private static Drawable icDelete_disabled;
	private static Drawable icReference;
	private static Drawable icReference_disabled;
	private static Drawable icCut;
	private static Drawable icCut_disabled;
	private static Drawable icEdit;
	private static Drawable icEdit_disabled;
	private static Drawable icPaste;
	private static Drawable icPaste_disabled;

	private static long backTime;
	@SuppressLint("StaticFieldLeak")
	public static ExplorerStuff es;
	public static ViewState VS = new ViewState();

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle oldState) {
		VS.mfInstance = this;
		View root = inflater.inflate(R.layout.fragment_main, container, false);
		selectOpts = root.findViewById(R.id.objects_select);
		delete = root.findViewById(R.id.select_delete);
		reference = root.findViewById(R.id.select_reference);
		cut = root.findViewById(R.id.select_cut);
		edit = root.findViewById(R.id.select_rename);
		pasteOpts = root.findViewById(R.id.objects_paster);
		root.findViewById(R.id.objects_cancel).setOnClickListener(v -> {
			VS.pasteData = null;
			Controller.toggleSelectBtn(true);
			pasteOpts.setVisibility(View.GONE);
		});
		paste = root.findViewById(R.id.objects_paste);

		if (icCut == null) {
			Resources res = activity.getResources();
			(icDelete = res.getDrawable(R.drawable.ic_delete))
					.setBounds(0, 0, (int) (dp * 40), (int) (dp * 40));
			(icDelete_disabled = res.getDrawable(R.drawable.ic_delete_disabled))
					.setBounds(0, 0, (int) (dp * 40), (int) (dp * 40));
			(icReference = res.getDrawable(R.drawable.ic_reference))
					.setBounds(0, 0, (int) (dp * 40), (int) (dp * 40));
			(icReference_disabled = res.getDrawable(R.drawable.ic_reference_disabled))
					.setBounds(0, 0, (int) (dp * 40), (int) (dp * 40));
			(icCut = res.getDrawable(R.drawable.ic_cut))
					.setBounds(0, 0, (int) (dp * 40), (int) (dp * 40));
			(icCut_disabled = res.getDrawable(R.drawable.ic_cut_disabled))
					.setBounds(0, 0, (int) (dp * 40), (int) (dp * 40));
			(icEdit = res.getDrawable(R.drawable.ic_edit))
					.setBounds(0, 0, (int) (dp * 35), (int) (dp * 35));
			(icEdit_disabled = res.getDrawable(R.drawable.ic_edit_disabled))
					.setBounds(0, 0, (int) (dp * 35), (int) (dp * 35));
			(icPaste = res.getDrawable(R.drawable.ic_paste))
					.setBounds(0, 0, (int) (dp * 33), (int) (dp * 33));
			(icPaste_disabled = res.getDrawable(R.drawable.ic_paste_disabled))
					.setBounds(0, 0, (int) (dp * 33), (int) (dp * 33));
			if (VERSION.SDK_INT < 21) {
				DrawableCompat.setTint(DrawableCompat.wrap(icDelete_disabled), 0x55FFFFFF);
				DrawableCompat.setTint(DrawableCompat.wrap(icReference_disabled), 0x55FFFFFF);
				DrawableCompat.setTint(DrawableCompat.wrap(icCut_disabled), 0x55FFFFFF);
				DrawableCompat.setTint(DrawableCompat.wrap(icEdit_disabled), 0x55FFFFFF);
				DrawableCompat.setTint(DrawableCompat.wrap(icPaste_disabled), 0x55FFFFFF);
			}
		}

		new Thread(() -> {
			reference.setOnClickListener(v -> {
				int search = 0;
				EasyList<Container> list = null;
				for (HierarchyItemModel him : VS.contentAdapter.list)
					if (him.isSelected()) {
						if (search == 0 && VS.contentAdapter.search) {
							list = ((SearchItemModel) him).path;
							search++;
						} else if (him.bd instanceof Reference) {
							list = EasyList.convert(((Reference) him.bd).getRefPath());
							break;
						} else {
							search += 10;
							break;
						}
					}
				if (search < 2) {
					backLog.add(true, null, list);
					setContent(list.get(-1), list.get(-2), 10);
					es.onChange(true);
					VS.contentAdapter.selected = -1;
					setSelectOpts(false);
				} else move(true);
			});
			cut.setOnClickListener(v -> move(false));
			edit.setOnClickListener(none -> {
				HierarchyItemModel him, item1 = null;
				for (HierarchyItemModel item : VS.contentAdapter.list)
					if (item.isSelected()) {
						item1 = item;
						break;
					}
				him = item1;
				if (him.bd instanceof Container && !(him.bd instanceof TwoSided)) {
					boolean isMch = him.bd instanceof MainChapter;
					boolean isSch = him.bd instanceof SaveChapter;
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.edit), (li, cp) -> {
						if (cp.et_name.getText().toString().isEmpty()) {
							cp.et_name.setText(him.bd.getName());
							cp.et_desc.setText(him.bd.getDesc(him.parent));
						}
						View v = isMch ? null : li.inflate(R.layout.new_chapter, null);
						if (!isMch) {
							((CheckBox) v.findViewById(R.id.chapter_file))
									.setChecked(isSch);
							cp.np.setValue(him.position);
							cp.np.setMaxValue(cp.np.getMaxValue() - 1);
						} else cp.npLayout.setVisibility(View.GONE);
						cp.ok.setOnClickListener(x -> {
							int position = him.position;
							String name = cp.et_name.getText().toString();
							if (name.isEmpty()) return;
							cp.dismiss();
							try {
								him.bd.putDesc(him.parent,
										cp.et_desc.getText().toString().replace("\\t", "\t"));
								boolean sch = isMch ||
										((CheckBox) cp.view.findViewById(R.id.chapter_file))
												.isChecked();
								if (!name.equals(him.bd.getName())) {
									if (him.bd instanceof ContainerFile
											|| sch) ContainerFile.isCorrect(name);
									BasicData newBD = him.bd.setName(him.parent, name);
									if (newBD != him.bd) {
										if (him.bd instanceof ContainerFile) CurrentData.changed.remove(him.bd);
										him.bd = newBD;
									}
								}
								if (!isMch) {
									if (sch != isSch)
										him.bd = ((SemiElementContainer) him.bd).convert();
									position = cp.np.getValue();
									if (position != him.position) him.parent.putChild(him.parent.removeChild(him.bd),
											him.bd, position - 1);
									CurrentData.save(backLog.path);
								} else ((MainChapter) him.bd).save();
								VS.contentAdapter.selected = -1;
								setSelectOpts(false);
								him.update();
								if (position == him.position) backLog.adapter.notifyItemChanged(position - 1);
								else {
									backLog.adapter.list.add(position - 1, backLog.adapter.list.remove(him.position - 1));
									backLog.adapter.notifyItemMoved(him.position - 1, position - 1);
									him.position = position;
								}
							} catch (IllegalArgumentException iae) {
								if (!iae.getMessage().contains("Name can't")) throw iae;
								defaultReacts.get(ContainerFile.class + ":name")
										.react(iae.getMessage().contains("longer"));
							}
						});
						return v;
					}));
				} else if (him.bd instanceof TwoSided) {
					boolean pic = him.bd instanceof Picture;
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.edit), new Includer() {
						int position = him.position;
						AbstractPopupRecyclerAdapter content;

						@Override
						public View onInclude(LayoutInflater li, CreatorPopup cp) {
							LinearLayout ll = (LinearLayout) li.inflate(R.layout.new_twosided, null);
							if (content == null)
								content = pic ? new ImagePopupRecyclerAdapter(him, cp)
										: new TranslatePopupRecyclerAdapter(him, cp);
							Runnable onClick = content.onClick(ll);
							cp.np.setValue(him.position);
							cp.np.setMaxValue(cp.np.getMaxValue() - 1);
							cp.ok.setOnClickListener(v -> {
								onClick.run();
								position = cp.np.getValue();
								if (position != him.position) {
									him.parent.putChild(him.parent.removeChild(him.bd),
											him.bd, position - 1);
									if (content.toRemove != null) CurrentData.save(backLog.path);
								}
								if (content.toRemove != null) return;
								CurrentData.save(backLog.path);
								VS.contentAdapter.selected = -1;
								setSelectOpts(false);
								him.update();
								if (position == him.position) backLog.adapter.notifyItemChanged(position - 1);
								else {
									backLog.adapter.list.add(position - 1, backLog.adapter.list.remove(him.position - 1));
									backLog.adapter.notifyItemMoved(him.position - 1, position - 1);
									him.position = position;
								}
								cp.dismiss();
							});
							return ll;
						}
					}));
				}
			});
			delete.setOnClickListener(v -> {
				if (((SearchAdapter) backLog.adapter).selected > 0)
					new ContinuePopup(getString(R.string.continue_delete), () -> root.post(() -> {
						boolean left = false;
						for (int i = VS.contentAdapter.list.size() - 1; i >= 0; i--) {
							HierarchyItemModel him = VS.contentAdapter.list.get(i);
							if (him.isSelected()) {
								if (him.bd.destroy(him.parent)) {
									VS.contentAdapter.list.remove(i);
									VS.contentAdapter.notifyItemRemoved(i);
									if (him.bd instanceof ContainerFile) CurrentData.changed.remove(him.bd);
								} else left = true;
							}
						}
						if (!backLog.path.isEmpty()) CurrentData.save(backLog.path);
						if (VS.contentAdapter instanceof HierarchyAdapter && !backLog.path.isEmpty())
							es.setInfo(backLog.path.get(-1), backLog.path.get(-2));
						if (!left) {
							VS.contentAdapter.selected = -1;
							es.rv.postDelayed(() -> VS.contentAdapter.notifyDataSetChanged(), 200);
							selectOpts.setVisibility(View.GONE);
						} else Toast.makeText(CONTEXT, R.string.popup_delete_fail, Toast.LENGTH_SHORT).show();
					}));
			});
			if (backLog.adapter instanceof SearchAdapter && VS.contentAdapter.selected > -1) {
				selectOpts.setVisibility(View.VISIBLE);
				for (HierarchyItemModel him : (List<? extends HierarchyItemModel>) backLog.adapter.list)
					if (him.isSelected() && him.bd instanceof Reference) {
						tglEnabled(edit, false);
						return;
					}
				if (VS.contentAdapter.selected > 1) tglEnabled(edit, false);
			}
		}, "select options setter").start();

		es = new ExplorerStuff(this, this::setContent, () -> {
			VS.contentAdapter.selected = -1;
			setSelectOpts(false);
		}, this::setVisibleOpts, VS, backLog, getContext(),
				root.findViewById(R.id.explorer_path_handler), root.findViewById(R.id.explorer_list),
				root.findViewById(R.id.explorer_path), root.findViewById(R.id.explorer_info_handler),
				root.findViewById(R.id.explorer_info), root.findViewById(R.id.explorer_search),
				root.findViewById(R.id.touch_outside), root.findViewById(R.id.search_collapser),
				this::updateBackContent);
		Formatter.defaultReacts.put("MChLoaded", (o) -> {
			if (backLog.path.isEmpty()) root.post(() -> backLog.adapter.notifyDataSetChanged());
		});
		if (backLog.adapter == null) {
			setContent(backLog.path.get(-1), backLog.path.get(-2), backLog.path.size());
			es.setInfo(backLog.path.get(-1), backLog.path.get(-2));
		} else {
			es.rv.setAdapter(backLog.adapter);
			if (!VS.sv_visible) es.searchView.setVisibility(View.GONE);
			es.onChange(true);
		}
		setVisibleOpts();
		pasteOpts.setVisibility(VS.pasteData != null ? View.VISIBLE : View.GONE);
		return root;
	}

	/**
	 * Sets up move-mode.
	 *
	 * @param ref if the selected items will be referenced or moved.
	 */
	private void move(boolean ref) {
		VS.pasteData = new ViewState.PasteData(ref, VS.contentAdapter);
		Controller.toggleSelectBtn(false);
		pasteOpts.setVisibility(View.VISIBLE);
		tglEnabled(paste, false);
		boolean search = VS.contentAdapter.search;
		for (HierarchyItemModel him : VS.contentAdapter.list)
			if (him.isSelected()) VS.pasteData.src.add(him);
		for (BasicData bd : backLog.path) VS.pasteData.srcPath.add((Container) bd);
		paste.setOnClickListener(v -> {
			Container npp = backLog.path.get(-2);
			Container np = backLog.path.get(-1);
			boolean searchNow = VS.contentAdapter.search;
			for (BasicData bd : np.getChildren(npp)) VS.pasteData.src.remove(bd);
			if (ref) {
				List<Container> cp = new ArrayList<>(backLog.path.size());
				for (BasicData bd : backLog.path) cp.add((Container) bd);
				for (HierarchyItemModel him : VS.pasteData.src) {
					Reference r;
					try {
						r = Reference.mkElement(him.bd, cp,
								(search ? ((SearchItemModel) him).path : VS.pasteData.srcPath)
										.toArray(new Container[0]));
					} catch (IllegalArgumentException iae) {
						continue;
					}
					np.putChild(npp, r);
					if (!searchNow)
						backLog.adapter.addItem(new HierarchyItemModel(r, np, backLog.adapter.list.size()));
				}
			} else {
				List<ContainerFile> toSave = new LinkedList<>();
				for (HierarchyItemModel him : VS.pasteData.src) {
					him.bd.move(him.parent, VS.pasteData.srcPath.size() == 1 ? null :
							search ? ((SearchItemModel) him).path.get(-2)
									: VS.pasteData.srcPath.get(VS.pasteData.srcPath.size() - 2), np, npp);
					if (search) {
						List<? extends BasicData> path = ((SearchItemModel) him).path;
						for (int i = path.size() - 1; i >= 0; i--)
							if (path.get(i) instanceof ContainerFile) {
								if (!toSave.contains(path.get(i))) toSave.add((ContainerFile) path.get(i));
								break;
							}
					}
					if (!searchNow) {
						if (search)
							backLog.adapter.addItem(new HierarchyItemModel(him.bd, np, backLog.adapter.list.size()));
						else {
							backLog.adapter.addItem(him);
							him.parent = np;
							him.position = backLog.adapter.getItemCount();
						}
					}
				}
				if (!search) {
					for (int i = VS.pasteData.srcPath.size() - 1; i >= 0; i--)
						if (VS.pasteData.srcPath.get(i) instanceof ContainerFile) {
							toSave.add((ContainerFile) VS.pasteData.srcPath.get(i));
							break;
						}
				}
				for (ContainerFile cf : toSave)
					try {
						cf.save();
					} catch (Exception e) {
						defaultReacts.get(ContainerFile.class + ":save").react(e, cf.getSaveFile(), cf);
						CurrentData.changed.add(cf);
					}
			}
			CurrentData.save(backLog.path);
			VS.pasteData.srcView.list.removeAll(VS.pasteData.src);
			int i = 0;
			for (Object him :
					VS.pasteData.srcView.list) {
				((HierarchyItemModel) him).position = i++;
			}
			VS.pasteData.srcView.notifyDataSetChanged();
			VS.pasteData = null;
			Controller.toggleSelectBtn(true);
			pasteOpts.setVisibility(View.GONE);
		});
		VS.contentAdapter.selected = -1;
		setSelectOpts(false);
	}

	/**
	 * Controls the visibility of selecting options.
	 *
	 * @param change if an item has been clicked
	 */
	private void setSelectOpts(boolean change) {
		selectOpts.setVisibility(VS.contentAdapter.selected > -1 ? View.VISIBLE : View.GONE);
		setVisibleOpts();
		if (VS.contentAdapter.selected == -1 && !change) {
			VS.contentAdapter.ref = 0;
			for (HierarchyItemModel him : VS.contentAdapter.list) him.setSelected(false);
			VS.contentAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Controls the usability of the {@link #selectOpts} buttons.
	 */
	private void setVisibleOpts() {
		if (VS.contentAdapter == null) return;
		boolean notObj = backLog.path.size() > 0;
		int selected = VS.contentAdapter.selected;
		tglEnabled(delete, selected > 0);
		tglEnabled(reference, selected > 0 && notObj && (VS.contentAdapter.ref < 2 && selected < 2
				|| VS.contentAdapter.ref < 1));
		tglEnabled(cut, selected > 0 && notObj && VS.contentAdapter.ref < 1);
		tglEnabled(edit, selected == 1 && VS.contentAdapter.ref < 1);
	}

	/**
	 * Toggles the usability of the given button.
	 */
	private void tglEnabled(TextView tv, boolean enabled) {
		if (tv.isEnabled() == enabled) return;
		if (tv == reference)
			tv.setCompoundDrawables(null, enabled ? icReference : icReference_disabled, null, null);
		else if (tv == cut)
			tv.setCompoundDrawables(null, enabled ? icCut : icCut_disabled, null, null);
		else if (tv == edit)
			tv.setCompoundDrawables(null, enabled ? icEdit : icEdit_disabled, null, null);
		else if (tv == delete)
			tv.setCompoundDrawables(null, enabled ? icDelete : icDelete_disabled, null, null);
		else if (tv == paste)
			tv.setCompoundDrawables(null, enabled ? icPaste : icPaste_disabled, null, null);
		tv.setTextColor(enabled ? 0xFFFFFFFF : 0x66FFFFFF);
		tv.setEnabled(enabled);
	}

	@Override
	public void run() {
		VS.sv_focused = false;
		es.searchView.clearFocus();
		if (Controller.isActive(this) && VS.contentAdapter != null
				&& VS.contentAdapter.selected > -1) {
			VS.contentAdapter.selected = -1;
			setSelectOpts(false);
		} else if (Controller.isActive(this) && !backLog.path.isEmpty()) {
			if (VS.pasteData != null && backLog.path.size() == 1 && !VS.contentAdapter.search) {
				pasteOpts.setVisibility(View.GONE);
				VS.pasteData = null;
			} else updateBackContent(1);
		} else {
			if (!Controller.isActive(this)) {
				Controller.defaultBack.run();
				backTime = 0;
			} else if (System.currentTimeMillis() - backTime > 3000) {
				backTime = System.currentTimeMillis();
				Toast.makeText(getContext(), R.string.press_exit, Toast.LENGTH_SHORT).show();
			} else Controller.defaultBack.run();
			if (Controller.isActive(this)) {
				if (!backLog.path.isEmpty())
					Controller.activity.getSupportActionBar().setTitle(backLog.path.get(-1).toString());
				Controller.setMenuRes(VS.menuRes);
				if (!VS.sv_visible) es.searchView.setVisibility(View.GONE);
				else es.updateSearch(false);
			}
		}
	}

	public void setContent(BasicData bd, Container parent, int size) {
		boolean container = false;
		if (size == 0) {
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				finishLoad();
				if (defaultReacts.get("removeSchNames") != null)
					defaultReacts.get("removeSchNames").react();
			}, "MCh finishLoad").start();
			es.rv.setAdapter(backLog.adapter = VS.contentAdapter = new HierarchyAdapter(es.rv,
					this, convert(new ArrayList<>(MainChapter.ELEMENTS), null),
					this::setVisibleOpts));
			prepareObjectsContent();
		} else if (bd instanceof Container)
			// If opened item is a Picture holder
			if (!(container = !(bd instanceof TwoSided)) && bd instanceof Picture && VS.pasteData == null) {
				Controller.setMenuRes(VS.menuRes = 0);
				Controller.toggleSelectBtn(false);
				es.updateSearch(true);
				ArrayList<ImageItemModel> list = new ArrayList<>();
				BasicData[] pics = ((Picture) bd).getChildren(parent);
				for (int i = 1; i < pics.length; i += 2)
					list.add(new ImageItemModel((Picture) pics[i - 1], (Picture) pics[i]));
				if (pics.length % 2 == 1)
					list.add(new ImageItemModel((Picture) pics[pics.length - 1], null));
				es.rv.setAdapter(backLog.adapter = new ImageAdapter(es.rv, list));
				VS.contentAdapter = null;
				Controller.activity.getSupportActionBar().setTitle(bd.toString());
			}
		if (container) {
			if (VS.contentAdapter.list != null)
				for (HierarchyItemModel him : VS.contentAdapter.list) him.setSelected(false);
			es.rv.setAdapter(backLog.adapter = VS.contentAdapter = new HierarchyAdapter(es.rv,
					this, convert(((Container) bd).getChildren(parent), (Container) bd),
					this::setVisibleOpts));
			prepareContainerContent((Container) bd);
		}
	}

	public void updateBackContent(int skip) {
		es.updateBackPath(skip);
		if (backLog.path.isEmpty()) prepareObjectsContent();
		else prepareContainerContent(backLog.path.get(-1));
	}

	private void prepareObjectsContent() {
		Controller.setMenuRes(VS.menuRes = R.menu.more_main);
		es.updateSearch(true);
		Controller.activity.getSupportActionBar().setTitle(getString(R.string.menu_objects));
	}

	private void prepareContainerContent(Container bd) {
		if (VS.pasteData != null) test:{
			if (VS.pasteData.referencing) {
				if (VS.pasteData.src.get(0) instanceof SearchItemModel) {
					for (SearchItemModel source
							: (List<SearchItemModel>) (List<? extends HierarchyItemModel>) VS.pasteData.src) {
						if (source.bd instanceof Container) for (BasicData currentParent : backLog.path)
							if (currentParent == source.bd) {
								tglEnabled(paste, false);
								break test;
							}
						boolean match;
						for (BasicData currentParent : backLog.path) {
							match = false;
							for (Container parent : source.path)
								if (currentParent == parent) {
									match = true;
									break;
								}
							if (!match) {
								tglEnabled(paste, true);
								break test;
							}
						}
						tglEnabled(paste, false);
					}
				} else {
					boolean match;
					for (BasicData currentParent : backLog.path) {
						match = false;
						for (HierarchyItemModel source : VS.pasteData.src)
							if (currentParent == source.bd) {
								match = true;
								break;
							}
						if (!match) for (Container parent : VS.pasteData.srcPath)
							if (currentParent == parent) {
								match = true;
								break;
							}
						if (!match) {
							tglEnabled(paste, true);
							break test;
						}
					}
					tglEnabled(paste, false);
				}
			} else {
				for (BasicData parent : backLog.path) {
					if (parent instanceof MainChapter) continue;
					for (HierarchyItemModel source : VS.pasteData.src) {
						try {
							if (parent == source.bd.getThis()) {
								tglEnabled(paste, false);
								break test;
							}
						} catch (IllegalArgumentException ex) {
						}
					}
				}
				tglEnabled(paste, true);
			}
		}
		//else tglEnabled(paste, false);
		Controller.setMenuRes(VS.menuRes = bd instanceof MainChapter
				? R.menu.more_mch : R.menu.more_container);
		if (VS.pasteData == null) Controller.toggleSelectBtn(true);
		es.updateSearch(false);
		Controller.activity.getSupportActionBar().setTitle(bd.toString());
	}

	@Override
	public void onClick(View v) { //control of the 'select' button
		if (backLog.adapter instanceof SearchAdapter) {
			if (VS.contentAdapter.selected > -1) {
				if (VS.contentAdapter.selected < VS.contentAdapter.list.size()) {
					VS.contentAdapter.selected = VS.contentAdapter.list.size();
					for (HierarchyItemModel him : VS.contentAdapter.list)
						if (!him.isSelected()) {
							if (him.bd instanceof Reference) VS.contentAdapter.ref++;
							him.setSelected(true);
						}
					setSelectOpts(true);
				} else {
					VS.contentAdapter.selected = -1;
					setSelectOpts(false);
				}
			} else {
				VS.contentAdapter.selected = 0;
				setSelectOpts(false);
			}
			VS.contentAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(HierarchyItemModel item) {
		BasicData bd = item.bd;
		if (bd instanceof Word) {
			if (HierarchyItemModel.flipAllOnClick) {
				boolean flip = !item.flipped;
				for (HierarchyItemModel item1 : VS.contentAdapter.list)
					if (item1.flipped != flip) item1.flip();
			} else item.flip();
			VS.contentAdapter.notifyDataSetChanged();
			return;
		} else {
			boolean ref;
			if (ref = bd instanceof Reference) {
				try {
					if (item instanceof SearchItemModel) ((SearchItemModel) item).setNew(
							bd.getThis(), Arrays.asList(((Reference) bd).getRefPath()));
					else item.setNew(bd.getThis(), ((Reference) bd).getRefPathAt(-1));
					if (bd.getThis() instanceof Word) {
						VS.contentAdapter.notifyDataSetChanged();
						return;
					} else {
						backLog.add(true, null, EasyList.convert(((Reference) bd).getRefPath()));
						backLog.path.add((Container) (bd = bd.getThis()));
					}
				} catch (Exception e) {
					return;
				}
			} else if (ref = VS.contentAdapter.search) {
				EasyList<Container> list = new EasyList<>();
				list.addAll(((SearchItemModel) item).path);
				list.add((Container) item.bd);
				backLog.add(true, null, list);
			} else backLog.add(false, (Container) bd, null);
			VS.contentAdapter.selected = -1;
			setSelectOpts(true);
			setContent(bd, item.parent, backLog.path.size());
			es.onChange(ref);
		}
	}

	@Override
	public boolean onItemLongClick(HierarchyItemModel item) {
		if (VS.pasteData == null) {
			boolean selected = !item.isSelected();
			if (VS.contentAdapter.selected == -1) VS.contentAdapter.selected = 0;
			if (item.bd instanceof Reference)
				VS.contentAdapter.ref += selected ? 1 : -1;
			VS.contentAdapter.selected += selected ? 1 : -1;
			setSelectOpts(false);
			item.setSelected(selected);
			VS.contentAdapter.notifyDataSetChanged();
		} else return false;
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		new Thread(() -> {
			boolean word = false;
			switch (item.getItemId()) {
				case R.id.more_new_mch:
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.new_mch), (x, cp) -> {
						cp.npLayout.setVisibility(View.GONE);
						cp.ok.setOnClickListener(v -> {
							String name = cp.et_name.getText().toString();
							if (name.isEmpty()) return;
							try {
								ContainerFile.isCorrect(name);
							} catch (IllegalArgumentException iae) {
								if (!iae.getMessage().contains("Name can't")) throw iae;
								defaultReacts.get(ContainerFile.class + ":name")
										.react(iae.getMessage().contains("longer"));
								return;
							}
							backLog.adapter.addItem(new HierarchyItemModel(new MainChapter(
									new Data(name, null).addDesc(cp.et_desc.getText().toString()
											.replace("\\t", "\t"))), null, backLog.adapter.list.size() + 1));
							cp.dismiss();
						});
						return null;
					}));
					break;
				case R.id.more_new_container:
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.new_chapter), (li, cp) -> {
						cp.ok.setOnClickListener(v -> {
							String name = cp.et_name.getText().toString();
							if (name.isEmpty()) return;
							Container par = backLog.path.get(-1);
							try {
								Data d = new Data(name, (MainChapter) backLog.path.get(0))
										.addDesc(cp.et_desc.getText().toString().replace("\\t", "\t"))
										.addPar(par);
								Container ch = ((CheckBox) cp.view.findViewById(R.id.chapter_file))
										.isChecked() ? SaveChapter.mkElement(d) : new Chapter(d);
								int pos = cp.np.getValue();
								backLog.adapter.addItem(pos - 1,
										new HierarchyItemModel(ch, par, pos));
								par.putChild(backLog.path.get(-2), ch, pos - 1);
								cp.dismiss();
							} catch (IllegalArgumentException iae) {
								if (!iae.getMessage().contains("Name can't")) throw iae;
								defaultReacts.get(ContainerFile.class + ":name")
										.react(iae.getMessage().contains("longer"));
							}
						});
						return li.inflate(R.layout.new_chapter, null);
					}));
					break;
				case R.id.more_new_word:
					word = true;
				case R.id.more_new_picture:
					if (!word && !AndroidIOSystem.requestWrite()) return;
					boolean pic = !word;
					activity.runOnUiThread(() -> new CreatorPopup(getString(
							pic ? R.string.new_picture : R.string.new_word), new Includer() {

						AbstractPopupRecyclerAdapter content;

						@Override
						public View onInclude(LayoutInflater li, CreatorPopup cp) {
							LinearLayout ll = (LinearLayout) li.inflate(R.layout.new_twosided, null);
							if (content == null) content = pic ? new ImagePopupRecyclerAdapter(null, cp)
									: new TranslatePopupRecyclerAdapter(null, cp);
							Runnable onClick = content.onClick(ll);
							cp.ok.setOnClickListener(v -> {
								onClick.run();
								if (content.toRemove != null) return;
								backLog.adapter.notifyDataSetChanged();
								CurrentData.save(backLog.path);
								cp.dismiss();
							});
							return ll;
						}
					}));
					break;
				case R.id.sort_alpha_AZ:
					sort(1, true);
					break;
				case R.id.sort_alpha_ZA:
					sort(1, false);
					break;
				case R.id.sort_sf_01:
					sort(2, true);
					break;
				case R.id.sort_sf_10:
					sort(2, false);
					break;
				case R.id.sort_length_01:
					sort(3, true);
					break;
				case R.id.sort_length_10:
					sort(3, false);
					break;
				case R.id.sort_default:
					sort(4, true);
					break;
				case R.id.more_import_sch:
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("*/*");
					i.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(Intent.createChooser(i,
							activity.getString(R.string.action_chooser_file)), SCH_READ);
					break;
				case R.id.more_import_word:
					i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("text/plain");
					i.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(Intent.createChooser(i,
							activity.getString(R.string.action_chooser_file)), WORD_READ);
					break;
				case R.id.more_export_word:
					i = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain");
					i.putExtra(Intent.EXTRA_TITLE, backLog.path.get(-1).getName() + ".txt");
					i.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(i, WORD_WRITE);
					break;
				case R.id.more_import_mch:
					startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
							activity.getString(R.string.action_chooser_dir)), IMPORT_DIR);
					break;
				case R.id.more_change_dir:
					startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
							activity.getString(R.string.action_chooser_dir)), CHANGE_DIR);
					break;
				case R.id.more_sf_revaluate:
					List<Container> currentPath = (List<Container>) backLog.path.clone();
					Container opened = backLog.path.get(-1);
					int[] sf = opened.getSF();
					int[] refreshSF = opened.refreshSF();
					if (sf[0] != refreshSF[0] || sf[1] != refreshSF[1]) {
						if (backLog.path.get(-1) == opened) es.rv.post(() -> {
							backLog.adapter.notifyDataSetChanged();
							es.setInfo(opened, currentPath.get(currentPath.size() - 2));
						});
						CurrentData.save(currentPath);
					}
					break;
				case R.id.more_clean:
					MainChapter mch = (MainChapter) backLog.path.get(0);
					try {
						Picture.clean(mch);
						SaveChapter.clean(mch);
						mch.save(false);
						activity.runOnUiThread(() -> makeText(CONTEXT, mch.getName()
								+ activity.getString(R.string.action_sw_success), Toast.LENGTH_SHORT).show());
					} catch (IllegalArgumentException iae) {
					}
			}
		}, "MFrag onMenuItemClick").start();
		return true;
	}

	private void sort(int type, boolean rising) {
		List<HierarchyItemModel> list = (List<HierarchyItemModel>) VS.contentAdapter.list;
		if (type != 4) Collections.sort(list, type == 1 ?
				(a, b) -> (rising ? 1 : -1) * (a.bd.getName().compareToIgnoreCase(b.bd.getName()))
				: type == 2 ? (a, b) -> a.bd.getRatio() == b.bd.getRatio() ? 0 :
				(a.bd.getRatio() > b.bd.getRatio() == rising) ? 1 : -1
				: (a, b) -> a.bd.getName().length() == b.bd.getName().length() ? 0 :
				(a.bd.getName().length() > b.bd.getName().length() == rising) ? 1 : -1);
		else {
			HierarchyItemModel[] hims = new HierarchyItemModel[list.size()];
			for (HierarchyItemModel him : list) hims[him.position - 1] = him;
			list.clear();
			Collections.addAll(list, hims);
		}
		es.rv.post(() -> backLog.adapter.notifyDataSetChanged());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			new Thread(() -> {
				switch (requestCode) {
					case WORD_READ:
						try {
							List<BasicData> currentPath = (List<BasicData>) backLog.path.clone();
							Container self = backLog.path.get(-1),
									par = backLog.path.get(-2);
							SimpleReader content = new SimpleReader(new UriPath(data.getData(), false).load(), self, par);
							if (backLog.adapter instanceof HierarchyAdapter) {
								HierarchyAdapter ha = (HierarchyAdapter) backLog.adapter;
								int old = ha.list.size();
								int pos = old;
								for (BasicData bd : content.added)
									ha.addItem(new HierarchyItemModel(bd, self, pos++));
								es.rv.post(() -> es.setInfo(self, par));
							}
							defaultReacts.get(SimpleReader.class + ":success").react(content.result);
							CurrentData.save(currentPath);
						} catch (Exception e) {
							if (e instanceof IllegalArgumentException) return;
							defaultReacts.get(ContainerFile.class + ":load")
									.react(e, data.getData(), backLog.path.get(-1));
						}
						break;
					case WORD_WRITE:
						if (VS.contentAdapter.selected < 1) {
							new SimpleWriter(new UriPath(data.getData(), false), new Container[]{
									backLog.path.get(-1), backLog.path.get(-2)});
							break;
						}
						Container[][] toExport = new Container[VS.contentAdapter.selected][2];
						int i = 0;
						for (HierarchyItemModel him : VS.contentAdapter.list)
							if (him.isSelected() && him.bd instanceof Container) {
								toExport[i][0] = (Container) him.bd;
								toExport[i++][1] = him.parent;
							}
						VS.contentAdapter.selected = -1;
						setSelectOpts(false);
						new SimpleWriter(new UriPath(data.getData(), false), toExport);
						break;
					case IMAGE_PICK:
						defaultReacts.get("NotifyNewImage")
								.react(new UriPath(data.getData(), false));
						break;
					case SCH_READ:
						try {
							BasicData bd = new ContentReader(new UriPath(data.getData(), false).load(),
									(MainChapter) backLog.path.get(0)).mContent.getItem(backLog.path.get(-1));
							backLog.path.get(-1).putChild(backLog.path.get(-2), bd);
							if (backLog.adapter instanceof HierarchyAdapter) {
								HierarchyAdapter ha = (HierarchyAdapter) backLog.adapter;
								ha.addItem(new HierarchyItemModel(bd, backLog.path.get(-1), ha.list.size()));
								es.rv.post(() -> es.setInfo(backLog.path.get(-1), backLog.path.get(-2)));
							}
							CurrentData.save(backLog.path);
						} catch (Exception e) {
							defaultReacts.get("uncaught").react(Thread.currentThread(), e);
						}
						break;
					case IMPORT_DIR:
					case CHANGE_DIR:
						Uri path = data.getData();
						CONTEXT.getContentResolver().takePersistableUriPermission(path, Intent
								.FLAG_GRANT_READ_URI_PERMISSION | Intent
								.FLAG_GRANT_WRITE_URI_PERMISSION);
						UriPath file = new UriPath(path, true);
						if (requestCode == IMPORT_DIR) {
							if (file.getChild("main.json") != null && file.getChild("setts.dat") != null)
								CurrentData.ImportedMchs.importMch(file);
							CurrentData.createMchs();
							if (backLog.path.isEmpty())
								activity.runOnUiThread(() -> MainFragment.VS.mfInstance.setContent(null, null, 0));
						} else {
							if (Formatter.changeDir(file)) {
								VS.pasteData = null;
								backLog.clear();
								CurrentData.createMchs();
								activity.runOnUiThread(() -> {
									MainFragment.VS.mfInstance.setContent(null, null, 0);
									Toast.makeText(
											CONTEXT, getString(R.string.choose_dir), Toast.LENGTH_SHORT).show();
								});
							} else activity.runOnUiThread(
									() -> Toast.makeText(CONTEXT, "Error", Toast.LENGTH_SHORT).show());
						}
				}
				super.onActivityResult(requestCode, resultCode, data);
			}, "MFrag onActivityResult").start();
		}
	}

	@Override
	public void onResume() {
		Controller.setCurrentControl(this, VS.menuRes, VS.pasteData == null, true);
		if (!backLog.path.isEmpty())
			Controller.activity.getSupportActionBar().setTitle(backLog.path.get(-1).toString());
		super.onResume();
		backLog.adapter.update(es.rv);
	}

	public static final int WORD_WRITE = 1;
	public static final int WORD_READ = 2;
	public static final int STORAGE_PERMISSION = 3;
	public static final int IMAGE_PICK = 4;
	public static final int SCH_READ = 5;
	public static final int IMPORT_DIR = 7;
	public static final int CHANGE_DIR = 8;

	public static class ViewState extends ExplorerStuff.ViewState {
		private int menuRes;
		public MainFragment mfInstance;
		private PasteData pasteData;

		private static class PasteData {
			final boolean referencing;
			final List<Container> srcPath = new ArrayList<>();
			final List<HierarchyItemModel> src = new ArrayList<>();
			final SearchAdapter srcView;

			private PasteData(boolean referencing, SearchAdapter currentAdapter) {
				this.referencing = referencing;
				srcView = currentAdapter;
			}
		}
	}
}
