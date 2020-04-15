package com.schlmgr.gui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.schlmgr.R;
import com.schlmgr.gui.AndroidIOSystem;
import com.schlmgr.gui.Controller;
import com.schlmgr.gui.CurrentData;
import com.schlmgr.gui.CurrentData.EasyList;
import com.schlmgr.gui.Popup.ContinuePopup;
import com.schlmgr.gui.Popup.CreatorPopup;
import com.schlmgr.gui.Popup.CreatorPopup.Includer;
import com.schlmgr.gui.Popup.FullPicture;
import com.schlmgr.gui.list.HierarchyAdapter;
import com.schlmgr.gui.list.HierarchyItemModel;
import com.schlmgr.gui.list.ImageAdapter;
import com.schlmgr.gui.list.ImageItemModel;
import com.schlmgr.gui.list.OpenListAdapter;
import com.schlmgr.gui.list.SearchAdapter;
import com.schlmgr.gui.list.SearchItemModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import IOSystem.Formatter;
import IOSystem.Formatter.Data;
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
import objects.templates.TwoSided;
import testing.NameReader;

import static IOSystem.Formatter.defaultReacts;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.schlmgr.gui.Controller.activity;
import static com.schlmgr.gui.Controller.dp;
import static com.schlmgr.gui.CurrentData.backLog;
import static com.schlmgr.gui.CurrentData.finishLoad;
import static com.schlmgr.gui.list.HierarchyItemModel.convert;

public class MainFragment extends Fragment implements Controller.ControlListener, OnItemClickListener, OnItemLongClickListener {
	private HorizontalScrollView hsv;
	private LinearLayout path;
	private ScrollView sv_info;
	private TextView info;
	private SearchView sv;
	private ListView lv;
	private boolean opened;
	private int height_def;
	private SVController svc;

	private LinearLayout selectOpts;
	private TextView edit;
	private TextView delete;
	private TextView reference;
	private TextView cut;

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

	private final Runnable onCheckChange = () -> {
		OpenListAdapter ola = (OpenListAdapter) VS.aa;
		boolean notObj = backLog.path.size() > 0;
		tglEnabled(delete, ola.selected > 0);
		tglEnabled(reference, ola.selected > 0 && notObj && ola.ref < 2);
		tglEnabled(cut, ola.selected > 0 && notObj && ola.ref < 1);
		tglEnabled(edit, ola.selected == 1 && ola.ref < 1);
	};

	public static ViewState VS = new ViewState();

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		VS.mfInstance = this;
		View root = inflater.inflate(R.layout.fragment_main, container, false);
		hsv = root.findViewById(R.id.objects_path_handler);
		path = root.findViewById(R.id.objects_path);
		sv_info = root.findViewById(R.id.objects_info_handler);
		info = root.findViewById(R.id.objects_info);
		lv = root.findViewById(R.id.objects_list);
		sv = root.findViewById(R.id.objects_search);
		selectOpts = root.findViewById(R.id.objects_select);
		delete = root.findViewById(R.id.select_delete);
		reference = root.findViewById(R.id.select_reference);
		cut = root.findViewById(R.id.select_cut);
		edit = root.findViewById(R.id.select_rename);
		pasteOpts = root.findViewById(R.id.objects_paster);
		root.findViewById(R.id.objects_cancel).setOnClickListener(v -> {
			VS.paster = false;
			pasteOpts.setVisibility(View.GONE);//TODO: did I forget something?
		});
		paste = root.findViewById(R.id.objects_paste);
		if (icCut == null) {
			Resources res = activity.getResources();
			(icDelete = res.getDrawable(R.drawable.ic_delete))
					.setBounds((int) dp, 0, (int) (dp * 40), (int) (dp * 40));
			(icDelete_disabled = res.getDrawable(R.drawable.ic_delete_disabled))
					.setBounds((int) dp, 0, (int) (dp * 40), (int) (dp * 40));
			(icReference = res.getDrawable(R.drawable.ic_reference))
					.setBounds((int) dp, 0, (int) (dp * 40), (int) (dp * 40));
			(icReference_disabled = res.getDrawable(R.drawable.ic_reference_disabled))
					.setBounds((int) dp, 0, (int) (dp * 40), (int) (dp * 40));
			(icCut = res.getDrawable(R.drawable.ic_cut))
					.setBounds((int) dp, 0, (int) (dp * 40), (int) (dp * 40));
			(icCut_disabled = res.getDrawable(R.drawable.ic_cut_disabled))
					.setBounds((int) dp, 0, (int) (dp * 40), (int) (dp * 40));
			(icEdit = res.getDrawable(R.drawable.ic_edit))
					.setBounds((int) dp, 0, (int) (dp * 35), (int) (dp * 35));
			(icEdit_disabled = res.getDrawable(R.drawable.ic_edit_disabled))
					.setBounds((int) dp, 0, (int) (dp * 35), (int) (dp * 35));
			(icPaste = res.getDrawable(R.drawable.ic_paste))
					.setBounds((int) dp, 0, (int) (dp * 33), (int) (dp * 33));
			(icPaste_disabled = res.getDrawable(R.drawable.ic_paste_disabled))
					.setBounds((int) dp, 0, (int) (dp * 33), (int) (dp * 33));
		}

		new Thread(() -> {
			delete.setOnClickListener(v -> {
				if (((OpenListAdapter) VS.aa).selected > 0)
					new ContinuePopup(getString(R.string.continue_delete), () -> {
						OpenListAdapter ola = (OpenListAdapter) VS.aa;
						List<HierarchyItemModel> list = ola.list;
						for (int i = list.size() - 1; i >= 0; i--) {
							HierarchyItemModel him = list.get(i);
							if (him.isSelected()) {
								if (backLog.path.isEmpty()) him.bd.destroy(null);
								else {
									him.parent.removeChild((Container) (him instanceof SearchItemModel ?
											((SearchItemModel) him).path.get(-2)
											: backLog.path.get(-2)), him.bd);
								}
								list.remove(i);
							}
						}
						if (!backLog.path.isEmpty()) CurrentData.save();
						root.post(() -> {
							ola.notifyDataSetChanged();
							ola.selected = -1;
							selectOpts.setVisibility(View.GONE);
						});
					});
			});
			reference.setOnClickListener(v -> {
				for (HierarchyItemModel him : ((OpenListAdapter<? extends HierarchyItemModel>) VS.aa).list)
					if (him.isSelected()) {
						if (!(him.bd instanceof Reference)) break;
						backLog.add(true, null, EasyList.convert(((Reference) him.bd).getRefPath()));
						EasyList<Container> list = EasyList.convert(((Reference) him.bd).getRefPath());
						setContainerContent(list.get(-1), list.get(-2));
						onChange(true);
						setSelectOpts(-1, false);
						return;
					}
				move(true);
			});
			cut.setOnClickListener(v -> move(false));
			edit.setOnClickListener(none -> {
				for (HierarchyItemModel him : ((OpenListAdapter<? extends HierarchyItemModel>) VS.aa).list) {
					if (him.isSelected()) {
						if (him.bd instanceof Container && !(him.bd instanceof TwoSided)) {
							activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.edit), (x, cp) -> {
								if (cp.et_name.getText().toString().isEmpty()) {
									cp.et_name.setText(him.bd.getName());
									cp.et_desc.setText(him.bd.getDesc(him.parent));
								}
								cp.ok.setOnClickListener(v -> {
									String name = cp.et_name.getText().toString();
									if (name.isEmpty()) return;
									try {
										if (!name.equals(him.bd.getName())) {
											if (him.bd instanceof ContainerFile) ContainerFile.isCorrect(name);
											him.bd.setName(him.parent, name);
										}
										him.bd.putDesc(him.parent, cp.et_desc.getText().toString());
										if (him.bd instanceof MainChapter) ((MainChapter) him.bd).save();
										else CurrentData.save();
										VS.mfInstance.setSelectOpts(-1, false);
										VS.aa.notifyDataSetChanged();
										cp.dismiss();
									} catch (IllegalArgumentException iae) {
										defaultReacts.get(ContainerFile.class + ":name").react(iae.getMessage().contains("longer"));
									}
								});
								return null;
							}));
						} else if (him.bd instanceof Word) {
							activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.edit), new Includer() {
								class Translate {
									final Word trl;
									View v;

									Translate(Word trl) {
										this.trl = trl;
									}
								}

								List<Translate> list;
								List<Word> toRemove = new ArrayList<>();

								void addView(int index, LayoutInflater li, LinearLayout ll) {
									View view = li.inflate(R.layout.item_new_translate, ll, false);
									Translate item = list.get(index);
									((TextView) view.findViewById(R.id.item_trl_header)).setText(
											activity.getString(R.string.data_translate) + " " + (list.size() - index));
									if (item.v != null || item.trl != null) {
										((TextView) view.findViewById(R.id.item_adder_name)).setText(
												item.v != null ? ((TextView) item.v.findViewById(R.id.item_adder_name))
														.getText().toString() : item.trl.getName());
										((TextView) view.findViewById(R.id.item_adder_desc)).setText(
												item.v != null ? ((TextView) item.v.findViewById(R.id.item_adder_desc))
														.getText().toString() : item.trl.getDesc(him.parent));
									}
									item.v = view;
									view.findViewById(R.id.item_adder_remove).setOnClickListener(v -> {
										Translate t = list.remove(index);
										if (t.trl != null) toRemove.add(t.trl);
										ll.removeView(view);
									});
									ll.addView(view, 2);
								}

								@Override
								public View onInclude(LayoutInflater li, CreatorPopup cp) {
									LinearLayout ll = (LinearLayout) li.inflate(R.layout.new_twosided, null);
									if (list == null) {
										list = new ArrayList<>();
										for (TwoSided trl : ((Word) him.bd).getChildren(him.parent)) {
											list.add(0, new Translate((Word) trl));
											addView(0, li, ll);
										}
										cp.et_name.setText(him.bd.getName());
										cp.et_desc.setText(him.bd.getDesc(him.parent));
									} else for (int i = list.size() - 1; i >= 0; i--) addView(i, li, ll);
									TextView tv = ll.findViewById(R.id.new_add);
									tv.setText(R.string.add_word);
									tv.setOnClickListener(v -> {
										list.add(0, new Translate(null));
										addView(0, li, ll);
									});
									cp.ok.setOnClickListener(v -> {
										String name = cp.et_name.getText().toString();
										if (name.isEmpty() || list.isEmpty()) return;
										MainChapter mch = (MainChapter) backLog.path.get(0);
										try {
											for (Translate item : list) {
												String[] trls = SimpleReader.nameResolver(((TextView)
														item.v.findViewById(R.id.item_adder_name)).getText().toString());
												String[] trlDescs = SimpleReader.nameResolver(((TextView)
														item.v.findViewById(R.id.item_adder_desc)).getText().toString());
												if (item.trl != null && trls.length == 1) {
													item.trl.putDesc(him.parent, trlDescs[0]);
													item.trl.setName(him.parent, trls[0]);
												} else for (int i = 0; i < trls.length; i++)
													Word.mkTranslate(new Data(trls[i], mch, i < trlDescs.length
															? trlDescs[i] : null, him.parent), (Word) him.bd);
											}
											for (Word w : toRemove)
												((Word) him.bd).removeChild(him.parent, w);
											him.bd.putDesc(him.parent, cp.et_desc.getText().toString());
											if (!him.bd.getName().equals(name)) {
												him.bd.setName(him.parent, name);
												for (Word w : Word.ELEMENTS.get(mch))
													if (name.equals(w.getName())) {
														him.bd = w;
														break;
													}
											}
											CurrentData.save();
											VS.mfInstance.setSelectOpts(-1, false);
											VS.aa.notifyDataSetChanged();
											cp.dismiss();
										} catch (IllegalArgumentException iae) {
											System.out.println("An Exception occurred:\n" + iae.getMessage() + "\n" + Formatter.getStackTrace(iae));
										}
									});
									return ll;
								}
							}));
						} else if (him.bd instanceof Picture) {
							if (!AndroidIOSystem.requestWrite()) return;
							activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.edit), new Includer() {
								class Image {

									final Picture img;
									final File f;
									final Bitmap bm;

									Image(File file) {
										f = file;
										bm = BitmapFactory.decodeFile(f.toString());
										img = null;
									}

									Image(TwoSided pic) {
										img = (Picture) pic;
										f = img.getFile();
										bm = BitmapFactory.decodeFile(f.toString());
									}
								}

								List<Image> list;
								List<Picture> toRemove = new ArrayList<>();

								void addView(int index, LayoutInflater li, LinearLayout ll) {
									View view = li.inflate(R.layout.item_new_image, ll, false);
									Image item = list.get(index);
									((TextView) view.findViewById(R.id.item_adder_name)).setText(item.f.getName());
									ImageView iv = view.findViewById(R.id.item_img);
									iv.setImageBitmap(item.bm);
									iv.setOnClickListener(v -> new FullPicture(item.bm));
									view.findViewById(R.id.item_adder_remove).setOnClickListener(v -> {
										Image i = list.remove(index);
										if (i.img != null) toRemove.add(i.img);
										ll.removeView(view);
									});
									ll.addView(view, 2);
								}

								@Override
								public View onInclude(LayoutInflater li, CreatorPopup cp) {
									LinearLayout ll = (LinearLayout) li.inflate(R.layout.new_twosided, null);
									TextView tv = ll.findViewById(R.id.new_add);
									tv.setText(R.string.add_picture);
									tv.setOnClickListener(v -> VS.mfInstance.startActivityForResult(
											new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI), IMAGE_PICK));
									if (list == null) {
										list = new ArrayList<>();
										for (TwoSided img : ((Picture) him.bd).getChildren(him.parent)) {
											list.add(0, new Image(img));
											addView(0, li, ll);
										}
										cp.et_name.setText(him.bd.getName());
										cp.et_desc.setText(him.bd.getDesc(him.parent));
									} else for (int i = list.size() - 1; i >= 0; i--) addView(i, li, ll);
									defaultReacts.put("NotifyNewImage", (o) -> {
										File file = ((File) o[0]);
										if (!file.exists()) return;
										for (Image img : list) if (img.f.equals(file)) return;
										Image img = new Image(file);
										list.add(0, img);
										ll.post(() -> addView(0, li, ll));
									});
									cp.ok.setOnClickListener(v -> {
										String name = cp.et_name.getText().toString();
										if (name.isEmpty() || list.isEmpty()) return;
										MainChapter mch = (MainChapter) backLog.path.get(0);
										for (Image i : list)
											if (i.img == null)
												Picture.mkImage(new Data(i.f.getAbsolutePath(), mch, him.parent), (Picture) him.bd);
										try {
											for (Picture p : toRemove)
												((Picture) him.bd).removeChild(him.parent, p);
											him.bd.putDesc(him.parent, cp.et_desc.getText().toString());
											if (!him.bd.getName().equals(name)) {
												him.bd.setName(him.parent, name);
												for (Picture p : Picture.ELEMENTS.get(mch))
													if (name.equals(p.getName())) {
														him.bd = p;
														break;
													}
											}
											CurrentData.save();
											VS.mfInstance.setSelectOpts(-1, false);
											VS.aa.notifyDataSetChanged();
											cp.dismiss();
										} catch (IllegalArgumentException iae) {
											System.out.println("An Exception occurred:\n" + iae.getMessage() + "\n" + Formatter.getStackTrace(iae));
										}
									});
									return ll;
								}
							}));
						}
						break;
					}
				}
			});
			if (VS.aa instanceof OpenListAdapter && ((OpenListAdapter) VS.aa).selected > -1) {
				selectOpts.setVisibility(View.VISIBLE);
				for (HierarchyItemModel him : ((OpenListAdapter<? extends HierarchyItemModel>) VS.aa).list)
					if (him.isSelected() && him.bd instanceof Reference) {
						tglEnabled(edit, false);
						return;
					}
				if (((OpenListAdapter) VS.aa).selected > 1) tglEnabled(edit, false);
			}
		}).start();
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		lv.setOnScrollListener(svc = new SVController(sv));
		root.findViewById(R.id.touch_outside).setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN && sv.getVisibility() == View.VISIBLE) {
				Rect outRect = new Rect();
				sv.getGlobalVisibleRect(outRect);
				if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
					if (VS.sv_focused) {
						VS.sv_focused = false;
						VS.query = sv.getQuery().toString();
						sv.onActionViewCollapsed();
					}
				} else if (!VS.sv_focused) {
					v.performClick();
					VS.sv_focused = true;
					sv.onActionViewExpanded();
					sv.setQuery(VS.query, false);
					return true;
				}
			}
			return false;
		});
		hsv.setHorizontalScrollBarEnabled(false);
		sv.setQueryHint(getString(R.string.search));
		sv.setOnQueryTextListener(new Searcher());
		info.setOnClickListener((v) -> sv_info.setLayoutParams(
				new LayoutParams(MATCH_PARENT, realHeight(opened = !opened))));
		Formatter.defaultReacts.put("MChLoaded", (o) -> {
			if (backLog.path.isEmpty())
				root.post(() -> VS.aa.notifyDataSetChanged());
		});
		if (VS.aa == null) {
			setContent(backLog.path.get(-1), (Container) backLog.path.get(-2), backLog.path.size());
			setInfo(backLog.path.get(-1), (Container) backLog.path.get(-2));
		} else {
			lv.setAdapter(VS.aa);
			if (!VS.sv_visible) sv.setVisibility(View.GONE);
			onChange(true);
		}
		return root;
	}

	private void move(boolean ref) {
		VS.paster = true;
		Controller.toggleSelectBtn(false);
		pasteOpts.setVisibility(View.VISIBLE);
		tglEnabled(paste, true);
		boolean search = VS.aa instanceof SearchAdapter;
		List<HierarchyItemModel> list = new ArrayList<>();
		for (HierarchyItemModel him : ((OpenListAdapter<? extends HierarchyItemModel>) VS.aa).list)
			if (him.isSelected()) list.add(him);
		List<Container> original = new ArrayList<>();
		for (BasicData bd : backLog.path) original.add((Container) bd);
		paste.setOnClickListener(v -> {
			if (backLog.path.size() == original.size()) {
				int i = 0;
				boolean ok = false;
				for (BasicData bd : backLog.path) if (ok = bd != original.get(i++)) break;
				if (!ok) return;
			}
			Container npp = (Container) backLog.path.get(-2);
			Container np = (Container) backLog.path.get(-1);
			boolean searchNow = VS.aa instanceof SearchAdapter;
			for (BasicData bd : np.getChildren(npp)) list.remove(bd);
			if (ref) {
				List<Container> cp = new ArrayList<>(backLog.path.size());
				for (BasicData bd : backLog.path) cp.add((Container) bd);
				for (HierarchyItemModel him : list) {
					Reference r;
					try {
						r = Reference.mkElement(him.bd, cp,
								(search ? (List<Container>) ((SearchItemModel) him).path : original).toArray(new Container[0]));
					} catch (IllegalArgumentException iae) {
						continue;
					}
					np.putChild(npp, r);
					if (!searchNow) VS.aa.add(new HierarchyItemModel(r, np, VS.aa.getCount()));
				}
			} else {
				List<ContainerFile> toSave = new LinkedList<>();
				for (HierarchyItemModel him : list) {
					him.bd.move(him.parent, search ? (Container) ((SearchItemModel) him).path.get(-2)
							: original.get(original.size() - 2), np, npp);
					if (search) {
						List<? extends BasicData> path = ((SearchItemModel) him).path;
						for (int i = path.size() - 1; i >= 0; i--)
							if (path.get(i) instanceof ContainerFile) {
								if (!toSave.contains(path.get(i))) toSave.add((ContainerFile) path.get(i));
								break;
							}
					}
					if (!searchNow) {
						if (search) VS.aa.add(new HierarchyItemModel(him.bd, np, VS.aa.getCount()));
						else {
							VS.aa.add(him);
							him.parent = np;
						}
					}
				}
				if (!search) {
					for (int i = original.size() - 1; i >= 0; i--)
						if (original.get(i) instanceof ContainerFile) {
							toSave.add((ContainerFile) original.get(i));
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
			CurrentData.save();
			VS.paster = false;
			pasteOpts.setVisibility(View.GONE);
		});
		setSelectOpts(-1, false);
	}

	/**
	 * Controls the visibility of selecting options
	 *
	 * @param visibility -1 for GONE, 0 for VISIBLE, 2 for hidden rename button
	 * @param change     if an item has been clicked
	 */
	private void setSelectOpts(int visibility, boolean change) {
		selectOpts.setVisibility(visibility > -1 ? View.VISIBLE : View.GONE);
		boolean notObj = backLog.path.size() > 0;
		OpenListAdapter<? extends HierarchyItemModel> ola = (OpenListAdapter<? extends HierarchyItemModel>) VS.aa;
		tglEnabled(delete, visibility > 0);
		tglEnabled(reference, visibility > 0 && notObj && ola.ref < 2);
		tglEnabled(cut, visibility > 0 && notObj && ola.ref < 1);
		tglEnabled(edit, visibility == 1 && ola.ref < 1);
		if (visibility == -1 && !change) {
			ola.selected = -1;
			ola.ref = 0;
			for (HierarchyItemModel him : ola.list) him.setSelected(false);
		}
	}

	private void tglEnabled(TextView tv, boolean enabled) {
		if (tv.isClickable() == enabled) return;
		if (tv == delete)
			tv.setCompoundDrawables(null, enabled ? icDelete : icDelete_disabled, null, null);
		else if (tv == reference)
			tv.setCompoundDrawables(null, enabled ? icReference : icReference_disabled, null, null);
		else if (tv == cut)
			tv.setCompoundDrawables(null, enabled ? icCut : icCut_disabled, null, null);
		else if (tv == edit)
			tv.setCompoundDrawables(null, enabled ? icEdit : icEdit_disabled, null, null);
		else if (tv == paste)
			tv.setCompoundDrawables(null, enabled ? icPaste : icPaste_disabled, null, null);
		tv.setTextColor(enabled ? 0xFFFFFFFF : 0x66FFFFFF);
		tv.setClickable(enabled);
	}

	@Override
	public void run() {
		VS.sv_focused = false;
		sv.clearFocus();
		if (Controller.isActive(this) && VS.aa instanceof OpenListAdapter &&
				((OpenListAdapter) VS.aa).selected > -1) setSelectOpts(-1, false);
		else if (Controller.isActive(this) && !backLog.path.isEmpty()) {
			if (VS.paster && backLog.path.size() == 1) {
				pasteOpts.setVisibility(View.GONE);
				VS.paster = false;
				return;
			}
			boolean change = !backLog.remove();
			setContent(backLog.path.get(-1), (Container) backLog.path.get(-2), backLog.path.size());
			if (change) onChange(true);
			else {
				VS.breadCrumbs--;
				path.removeViews(VS.breadCrumbs * 2, 2);
				setInfo(backLog.path.get(-1), (Container) backLog.path.get(-2));
			}
		} else {
			Controller.defaultBack.run();
			if (Controller.isActive(this)) {
				if (!backLog.path.isEmpty())
					Controller.activity.getSupportActionBar().setTitle(backLog.path.get(-1).toString());
				Controller.setMenuRes(VS.menuRes);
				if (!VS.sv_visible) sv.setVisibility(View.GONE);
				else svc.update(false);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (VS.aa instanceof OpenListAdapter) {
			OpenListAdapter ola = (OpenListAdapter) VS.aa;
			setSelectOpts(ola.selected = ola.selected > -1 ? -1 : 0, false);
			if (ola.selected == 0) ola.notifyDataSetChanged();
		}
	}

	public static class SVController implements OnScrollListener {

		final SearchView sv;
		private boolean visible;
		public int fvi;

		private SVController(SearchView sv) {
			this.sv = sv;
		}

		public void update(boolean gone) {
			visible = VS.sv_visible = !gone;
			sv.setVisibility(gone ? View.GONE : View.VISIBLE);
			fvi = 0;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisible, int visICount, int size) {
			if (VS.sv_visible && Math.abs(fvi - firstVisible) > 2) {
				if (visible && firstVisible > fvi) {
					sv.setVisibility(View.GONE);
					visible = false;
				} else if (!visible && firstVisible < fvi) {
					sv.setVisibility(View.VISIBLE);
					visible = true;
				}
				fvi = firstVisible;
			}
		}
	}

	class Searcher implements OnQueryTextListener {

		@Override
		public boolean onQueryTextSubmit(String query) {
			sv.onActionViewCollapsed();
			VS.query = query;
			VS.sv_focused = false;
			EasyList<Container> c = new EasyList<>();
			for (BasicData bd : backLog.path) c.add((Container) bd);
			new Finder(c, query);
			return true;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			return true;
		}

		private class Finder {

			String comp;
			Pattern p;
			byte threads = 1;
			Correct correct;
			long start;
			Set<BasicData> set = new HashSet<>();

			Finder(EasyList<Container> parents, String compare) {
				if (compare.charAt(0) == '\\') {
					switch (compare.charAt(1)) {
						case 'r':
							try {
								p = Pattern.compile(compare.substring(2));
							} catch (Exception e) {
								String msg = getString(R.string.pattern_err) + '\n' + e.getMessage();
								AndroidIOSystem.showMsg(msg, msg);
								return;
							}
							correct = (name) -> p.matcher(name).matches();
							break;
						case 's':
							correct = (name) -> {
								if (name.startsWith(comp)) return true;
								for (String parsed : NameReader.readName(comp))
									if (name.startsWith(parsed)) return true;
								return false;
							};
							comp = compare.substring(2);
							break;
						case 'e':
							comp = compare.substring(2);
							correct = (name) -> {
								if (name.endsWith(comp)) return true;
								for (String parsed : NameReader.readName(comp))
									if (name.endsWith(parsed)) return true;
								return false;
							};
							break;
						case '\\':
							compare = compare.substring(2);
						default:
							comp = compare;
							correct = (name) -> {
								if (name.contains(comp)) return true;
								for (String parsed : NameReader.readName(comp))
									if (name.contains(parsed)) return true;
								return false;
							};
					}
				} else {
					correct = (name) -> {
						if (name.toLowerCase().contains(comp)) return true;
						for (String parsed : NameReader.readName(comp))
							if (name.toLowerCase().contains(parsed)) return true;
						return false;
					};
					comp = compare.toLowerCase();
				}
				lv.setAdapter(VS.aa = new SearchAdapter(getContext(), new ArrayList<>(), onCheckChange));
				Controller.setMenuRes(VS.menuRes = R.menu.more_search);
				svc.update(false);
				info.setText(getString(R.string.data_child_count) + 0);
				sv_info.setLayoutParams(new LayoutParams(MATCH_PARENT, (int) (18 * dp)));
				start = System.currentTimeMillis();
				new Thread(() -> {
					search(parents.get(-1).getChildren(parents.get(-2)), parents, false);
					threads--;
				}).start();
			}

			void search(BasicData[] src, EasyList<Container> path, boolean threaded) {
				if (src == null) return;
				EasyList<Container> path2 = new EasyList<>();
				path2.addAll(path);
				boolean ref = false;
				for (BasicData bd : src) {
					if (bd instanceof Reference) try {
						bd.getThis();
					} catch (Exception e) {
						continue;
					}
					boolean found;
					if (VS.aa instanceof SearchAdapter) found = correct(bd, path);
					else return;
					if (bd instanceof Reference) {
						ref = true;
						path2.clear();
						Collections.addAll(path2, ((Reference) bd).getRefPath());
						bd = bd.getThis();
					} else if (ref) {
						path2.clear();
						path2.addAll(path);
						ref = false;
					}
					if (bd instanceof Container) {
						if (bd instanceof TwoSided) {
							if (bd instanceof Word && !found)
								for (BasicData trl : ((Word) bd).getChildren(path2.get(-1)))
									correct(trl, path2);
						} else {
							EasyList<Container> list = new EasyList<>();
							list.addAll(path2);
							list.add((Container) bd);
							if (!threaded && threads < 3) {
								threads++;
								Container c = (Container) bd;
								new Thread(() -> {
									search(c.getChildren(path2.get(-1)), list, true);
									threads--;
								}).start();
							} else {
								search(((Container) bd).getChildren(path2.get(-1)), list, threaded);
							}
						}
					}
				}
			}

			boolean correct(BasicData bd, EasyList<Container> path) {
				boolean yes = false;
				if (correct.verify(bd.toString())) yes = true;
				else for (String name : NameReader.readName(bd)) if (correct.verify(name)) yes = true;
				if (yes && set.add(bd)) {
					EasyList<Container> copy = new EasyList<>();
					copy.addAll(path);
					lv.post(() -> {
						VS.aa.add(new SearchItemModel(
								bd, copy, ((SearchAdapter) VS.aa).list.size() + 1));
						info.setText(getString(R.string.data_child_count) + set.size() + ";\t" +
								getString(R.string.time) + (System.currentTimeMillis() - start) + "ms");
					});
				}
				return yes;
			}
		}
	}

	interface Correct {
		boolean verify(String name);
	}

	@Override
	public void onItemClick(AdapterView<?> par, View v, int pos, long id) {
		HierarchyItemModel him = (HierarchyItemModel) par.getItemAtPosition(pos);
		BasicData bd = him.bd;
		if (bd instanceof Word) {
			if (HierarchyItemModel.flipAllOnClick) {
				boolean flip = !him.isFlipped();
				for (HierarchyItemModel item : ((OpenListAdapter<?>) VS.aa).list)
					if (item.isFlipped() != flip) item.flip();
			} else him.flip();
			VS.aa.notifyDataSetChanged();
			return;
		} else {
			boolean ref;
			if (ref = bd instanceof Reference) {
				try {
					him.setNew(bd.getThis(), ((Reference) bd).getRefPathAt(-1));
					if (bd.getThis() instanceof Word) {
						VS.aa.notifyDataSetChanged();
						return;
					} else {
						backLog.add(true, null, EasyList.convert(((Reference) bd).getRefPath()));
						backLog.path.add(bd = bd.getThis());
					}
				} catch (Exception e) {
					return;
				}
			} else if (!(ref = VS.aa instanceof SearchAdapter)) {
				backLog.add(false, bd, null);
			} else {
				backLog.add(true, null, (EasyList<BasicData>) ((SearchItemModel) him).path);
				backLog.path.add(him.bd);
			}
			setSelectOpts(-1, true);
			setContent(bd, him.parent, backLog.path.size());
			onChange(ref);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (VS.aa instanceof SearchAdapter) {
			SearchItemModel sim = (SearchItemModel) parent.getItemAtPosition(position);
			backLog.add(true, null, (EasyList<BasicData>) sim.path);
			EasyList<Container> c = (EasyList<Container>) sim.path;
			setContainerContent(c.get(-1), c.get(-2));
			onChange(true);
		} else if (VS.aa instanceof HierarchyAdapter && !VS.paster) {
			HierarchyAdapter ha = (HierarchyAdapter) VS.aa;
			boolean selected = !ha.list.get(position).isSelected();
			if (ha.selected == -1) ha.selected = 0;
			if (ha.list.get(position).bd instanceof Reference) ha.ref += selected ? 1 : -1;
			setSelectOpts(ha.selected += selected ? 1 : -1, false);
			ha.list.get(position).setSelected(selected);
			ha.notifyDataSetChanged();
		} else return false;
		return true;
	}

	public void setContent(BasicData bd, Container parent, int size) {
		if (size == 0) setObjectsContent();
		else if (size == 1) setContainerContent((Container) bd, null);
		else if (bd instanceof Container)
			if (bd instanceof TwoSided) {
				if (bd instanceof Picture && !VS.paster) setPictureContent(bd, parent);
			} else setContainerContent((Container) bd, parent);
	}

	private void setObjectsContent() {
		new Thread(() -> {
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
			finishLoad();
		}).start();
		Controller.setMenuRes(VS.menuRes = R.menu.more_main);
		svc.update(true);
		lv.setAdapter(VS.aa = new HierarchyAdapter(getContext(),
				convert(new ArrayList<>(MainChapter.ELEMENTS), null), onCheckChange));
		Controller.activity.getSupportActionBar().setTitle(getString(R.string.menu_objects));
	}

	private void setContainerContent(Container bd, Container parent) {
		Controller.setMenuRes(VS.menuRes = R.menu.more_container);
		if (!VS.paster) Controller.toggleSelectBtn(true);
		svc.update(false);
		lv.setAdapter(VS.aa = new HierarchyAdapter(getContext(), convert(bd.getChildren(parent), bd), onCheckChange));
		Controller.activity.getSupportActionBar().setTitle(bd.toString());
	}

	private void setPictureContent(BasicData bd, Container parent) {
		Controller.setMenuRes(VS.menuRes = 0);
		Controller.toggleSelectBtn(false);
		svc.update(true);
		ArrayList<ImageItemModel> list = new ArrayList<>();
		BasicData[] pics = ((Picture) bd).getChildren(parent);
		for (int i = 1; i < pics.length; i += 2)
			list.add(new ImageItemModel((Picture) pics[i - 1], (Picture) pics[i]));
		if (pics.length % 2 == 1) list.add(new ImageItemModel((Picture) pics[pics.length - 1], null));
		lv.setAdapter(VS.aa = new ImageAdapter(getContext(), list, parent));
		Controller.activity.getSupportActionBar().setTitle(bd.toString());
	}

	private void onChange(boolean allChanged) {
		if (allChanged) {
			path.removeAllViews();
			VS.breadCrumbs = 0;
			for (BasicData bd : backLog.path) addPathButton(bd);
		} else addPathButton(backLog.path.get(-1));
		setInfo(backLog.path.get(-1), (Container) backLog.path.get(-2));
	}

	private void setInfo(BasicData bd, Container parent) {
		String txt;
		if (!backLog.path.isEmpty()) {
			String desc = bd.getDesc(parent);
			info.setText(txt = (getString(R.string.data_child_count) + lv.getAdapter().getCount()
					+ ", " + getString(R.string.success_rate) + ": " + bd.getRatio()
					+ (desc.isEmpty() ? '%' : "%\n" + desc)));
		} else
			info.setText(txt = (getString(R.string.data_child_count) + lv.getAdapter().getCount()));
		hsv.post(() -> hsv.fullScroll(View.FOCUS_RIGHT));
		info.setClickable((height_def = height(txt)) >= 3);
		sv_info.setLayoutParams(new LayoutParams(MATCH_PARENT, realHeight(opened)));
		sv_info.post(() -> sv_info.fullScroll(View.FOCUS_UP));
	}

	private void addPathButton(BasicData bd) {
		TextView btn = new Button(getContext());
		btn.setTextSize(17);
		btn.setAllCaps(false);
		btn.setBackground(null);
		btn.setTextColor(0xFFFFFFFF);
		btn.setPadding(0, 0, 0, 0);
		btn.setText(bd.toString());
		btn.setTypeface(null);
		btn.setLayoutParams(new LayoutParams((int)
				(btn.getPaint().measureText(bd.toString()) + dp * 8), MATCH_PARENT));
		btn.setOnClickListener((v) -> {
			if (backLog.path.get(-1) == bd) return;
			setSelectOpts(-1, true);
			EasyList<BasicData> path = new EasyList<>();
			boolean noChange;
			for (BasicData e : backLog.path) {
				path.add(e);
				if (e == bd) break;
			}
			int diff = backLog.path.size() - path.size();
			if (noChange = diff <= backLog.onePath.get(-1)) for (; diff > 0; diff--) {
				backLog.remove();
				VS.breadCrumbs--;
				this.path.removeViews(VS.breadCrumbs * 2, 2);
			}
			else backLog.add(true, null, path);
			setContent(bd, (Container) backLog.path.get(-2), backLog.path.size());
			if (noChange) setInfo(backLog.path.get(-1), (Container) backLog.path.get(-2));
			else onChange(true);
		});
		path.addView(btn);
		VS.breadCrumbs++;
		btn = new TextView(getContext());
		btn.setBackground(getResources().getDrawable(R.drawable.ic_bread_crumbs));
		btn.setLayoutParams(new LayoutParams((int) (dp * 7), MATCH_PARENT));
		btn.setScaleX(2.4f);
		path.addView(btn);
	}

	private int realHeight(boolean opened) {
		return height_def >= 3 && opened ? LayoutParams.WRAP_CONTENT :
				(int) (getResources().getDimension(R.dimen.dp) * (height_def == 1 ? 19 : 36));
	}

	private byte height(String src) {
		byte i = 1;
		for (char ch : src.toCharArray()) if (ch == '\n' && ++i > 2) break;
		return i;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		new Thread(() -> {
			switch (item.getItemId()) {
				case R.id.more_new_mch:
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.new_mch), (x, cp) -> {
						cp.ok.setOnClickListener(v -> {
							String name = cp.et_name.getText().toString();
							try {
								if (name.isEmpty()) return;
								ContainerFile.isCorrect(name);
							} catch (IllegalArgumentException iae) {
								defaultReacts.get(ContainerFile.class + ":name").react(iae.getMessage().contains("longer"));
								return;
							}
							try {
								VS.aa.add(new HierarchyItemModel(new MainChapter(new Data(
										name, null, cp.et_desc.getText().toString())), null, lv.getCount() + 1));
								VS.aa.notifyDataSetChanged();
								cp.dismiss();
							} catch (IllegalArgumentException iae) {
								defaultReacts.get(ContainerFile.class + ":save").react(iae, name, name);
							}
						});
						return null;
					}));
					break;
				case R.id.more_new_container:
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.new_chapter), (li, cp) -> {
						cp.ok.setOnClickListener(v -> {
							String name = cp.et_name.getText().toString();
							if (name.isEmpty()) return;
							Container par = (Container) backLog.path.get(-1);
							try {
								Data d = new Data(name, (MainChapter) backLog.path.get(0), cp.et_desc.getText().toString(), par);
								Container ch = ((CheckBox) cp.view.findViewById(R.id.chapter_file))
										.isChecked() ? SaveChapter.mkElement(d) : new Chapter(d);
								VS.aa.add(new HierarchyItemModel(ch, par, lv.getCount() + 1));
								par.putChild((Container) backLog.path.get(-2), ch);
								cp.dismiss();
							} catch (IllegalArgumentException iae) {
								if (iae.getMessage().contains("Name can't"))
									defaultReacts.get(ContainerFile.class + ":name").react(iae.getMessage().contains("longer"));
							}
						});
						return li.inflate(R.layout.new_chapter, null);
					}));
					break;
				case R.id.more_new_word:
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.new_word), new Includer() {

						List<View> list;

						void addView(int index, LayoutInflater li, LinearLayout ll) {
							View view = li.inflate(R.layout.item_new_translate, ll, false);
							View item = list.get(index);
							((TextView) view.findViewById(R.id.item_trl_header)).setText(
									activity.getString(R.string.data_translate) + " " + (list.size() - index));
							if (item != null) {
								((TextView) view.findViewById(R.id.item_adder_name)).setText(((TextView)
										item.findViewById(R.id.item_adder_name)).getText().toString());
								((TextView) view.findViewById(R.id.item_adder_desc)).setText(((TextView)
										item.findViewById(R.id.item_adder_desc)).getText().toString());
							}
							list.set(index, view);
							view.findViewById(R.id.item_adder_remove).setOnClickListener(v -> {
								list.remove(index);
								ll.removeView(view);
							});
							ll.addView(view, 2);
						}

						@Override
						public View onInclude(LayoutInflater li, CreatorPopup cp) {
							LinearLayout ll = (LinearLayout) li.inflate(R.layout.new_twosided, null);
							if (list == null) list = new ArrayList<>();
							else for (int i = list.size() - 1; i >= 0; i--) addView(i, li, ll);
							TextView tv = ll.findViewById(R.id.new_add);
							tv.setText(R.string.add_word);
							tv.setOnClickListener(v -> {
								list.add(0, null);
								addView(0, li, ll);
							});
							cp.ok.setOnClickListener(v -> {
								String name = cp.et_name.getText().toString();
								if (name.isEmpty() || list.isEmpty()) return;
								Container par = (Container) backLog.path.get(-1);
								MainChapter mch = (MainChapter) backLog.path.get(0);
								LinkedList<Data> translates = new LinkedList<>();
								try {
									for (View view : list) {
										String[] trls = SimpleReader.nameResolver(((TextView)
												view.findViewById(R.id.item_adder_name)).getText().toString());
										String[] trlDescs = SimpleReader.nameResolver(((TextView)
												view.findViewById(R.id.item_adder_desc)).getText().toString());
										for (int i = 0; i < trls.length; i++)
											translates.add(new Data(trls[i], mch, i < trlDescs.length ? trlDescs[i] : null, par));
									}
									String[] names = SimpleReader.nameResolver(name);
									String[] descs = SimpleReader.nameResolver(cp.et_desc.getText().toString());
									Data d = new Data(null, mch, par);
									for (int i = 0; i < names.length; i++) {
										d.name = names[i];
										d.description = i < descs.length ? descs[i] : null;
										Word w = Word.mkElement(d, translates);
										VS.aa.add(new HierarchyItemModel(w, par, lv.getCount() + 1));
										par.putChild((Container) backLog.path.get(-2), w);
									}
									CurrentData.save();
									cp.dismiss();
								} catch (IllegalArgumentException iae) {
									System.out.println("An Exception occurred:\n" + iae.getMessage() + "\n" + Formatter.getStackTrace(iae));
								}
							});
							return ll;
						}
					}));
					break;
				case R.id.more_new_picture:
					if (!AndroidIOSystem.requestWrite()) return;
					activity.runOnUiThread(() -> new CreatorPopup(getString(R.string.new_picture), new Includer() {
						class Image {
							final File f;
							final Bitmap bm;

							Image(File file) {
								f = file;
								bm = BitmapFactory.decodeFile(f.toString());
							}
						}

						List<Image> list;

						void addView(int index, LayoutInflater li, LinearLayout ll) {
							View view = li.inflate(R.layout.item_new_image, ll, false);
							Image item = list.get(index);
							((TextView) view.findViewById(R.id.item_adder_name)).setText(item.f.getName());
							ImageView iv = view.findViewById(R.id.item_img);
							iv.setImageBitmap(item.bm);
							iv.setOnClickListener(v -> new FullPicture(item.bm));
							view.findViewById(R.id.item_adder_remove).setOnClickListener(v -> {
								list.remove(index);
								ll.removeView(view);
							});
							ll.addView(view, 2);
						}

						@Override
						public View onInclude(LayoutInflater li, CreatorPopup cp) {
							LinearLayout ll = (LinearLayout) li.inflate(R.layout.new_twosided, null);
							TextView tv = ll.findViewById(R.id.new_add);
							tv.setText(R.string.add_picture);
							tv.setOnClickListener(v -> VS.mfInstance.startActivityForResult(
									new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI), IMAGE_PICK));
							if (list == null) list = new ArrayList<>();
							else for (int i = list.size() - 1; i >= 0; i--) addView(i, li, ll);
							defaultReacts.put("NotifyNewImage", (o) -> {
								File file = ((File) o[0]);
								if (!file.exists()) return;
								for (Image img : list) if (img.f.equals(file)) return;
								Image img = new Image(file);
								list.add(0, img);
								ll.post(() -> addView(0, li, ll));
							});
							cp.ok.setOnClickListener(v -> {
								String name = cp.et_name.getText().toString();
								if (name.isEmpty() || list.isEmpty()) return;
								String desc = cp.et_desc.getText().toString();
								Container par = (Container) backLog.path.get(-1);
								MainChapter mch = (MainChapter) backLog.path.get(0);
								LinkedList<Data> images = new LinkedList<>();
								for (Image i : list)
									images.add(new Data(i.f.getAbsolutePath(), mch, par));
								try {
									Picture p = Picture.mkElement(new Data(name, mch, desc, par), images);
									VS.aa.add(new HierarchyItemModel(p, par, lv.getCount() + 1));
									par.putChild((Container) backLog.path.get(-2), p);
									CurrentData.save();
									cp.dismiss();
								} catch (IllegalArgumentException iae) {
									System.out.println("An Exception occurred:\n" + iae.getMessage() + "\n" + Formatter.getStackTrace(iae));
								}
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
				case R.id.more_import_word:
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("*/*");
					i.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(Intent.createChooser(i,
							activity.getString(R.string.action_chooser_file)), FILE_READ);
					break;
				case R.id.more_export_word:
					i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					i.setType("*/*");
					i.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(Intent.createChooser(i,
							activity.getString(R.string.action_chooser_file)), FILE_WRITE);
					break;
			}
		}).start();
		return true;
	}

	private void sort(int type, boolean rising) {
		List<HierarchyItemModel> list = (List<HierarchyItemModel>) ((OpenListAdapter<?>) VS.aa).list;
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
		lv.post(() -> VS.aa.notifyDataSetChanged());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			new Thread(() -> {
				switch (requestCode) {
					case FILE_READ:
						try {
							defaultReacts.get(SimpleReader.class + ":success").react(
									SimpleReader.simpleLoad(Formatter.loadFile(activity.getContentResolver()
													.openInputStream(data.getData())), (Container) backLog.path.get(-1),
											(Container) backLog.path.get(-2), 0, -1, -1));
							CurrentData.save();
						} catch (Exception e) {
							if (e instanceof IllegalArgumentException) return;
							defaultReacts.get(ContainerFile.class + ":load").react(e,
									data.getData().getPath(), backLog.path.get(-1));
						}
						break;
					case FILE_WRITE:
						try {
							SimpleWriter.saveWords(activity.getContentResolver()
											.openOutputStream(data.getData(), "wa"),
									(Container) backLog.path.get(-2), (Container) backLog.path.get(-1));
						} catch (Exception e) {
							defaultReacts.get(ContainerFile.class + ":save").react(e,
									data.getData().getPath(), backLog.path.get(-1));
						}
						break;
					case IMAGE_PICK:
						defaultReacts.get("NotifyNewImage").react(Controller.getFileFromUri(data.getData()));
				}
				super.onActivityResult(requestCode, resultCode, data);
			}).start();
		}
	}

	@Override
	public void onResume() {
		Controller.setCurrentControl(this, VS.menuRes, !VS.paster);
		super.onResume();
	}

	public static final int FILE_WRITE = 1;
	public static final int FILE_READ = 2;
	public static final int STORAGE_PERMISSION = 3;
	public static final int IMAGE_PICK = 4;

	public static class ViewState {
		private ArrayAdapter aa;
		private int breadCrumbs = 0;
		private boolean sv_focused;
		private boolean sv_visible;
		private int menuRes;
		private String query;
		private boolean paster;
		public MainFragment mfInstance;
	}
}
