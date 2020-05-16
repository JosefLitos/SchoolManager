package com.schlmgr.gui;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.schlmgr.R;
import com.schlmgr.gui.CurrentData.BackLog;
import com.schlmgr.gui.CurrentData.EasyList;
import com.schlmgr.gui.activity.SelectItemsActivity;
import com.schlmgr.gui.list.AbstractContainerAdapter;
import com.schlmgr.gui.list.HierarchyItemModel;
import com.schlmgr.gui.list.SearchAdapter;
import com.schlmgr.gui.list.SearchItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import objects.Reference;
import objects.Word;
import objects.templates.BasicData;
import objects.templates.Container;
import objects.templates.TwoSided;
import testing.NameReader;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.schlmgr.gui.Controller.activity;
import static com.schlmgr.gui.Controller.dp;

/**
 * This class contains all data that are identical for both {@link com.schlmgr.gui.fragments.MainFragment}
 * and {@link SelectItemsActivity}.
 */
public class ExplorerStuff {

	public final Content content;
	public final Runnable onSearchSubmit;
	private final boolean selectMode;

	private final HorizontalScrollView hsv;
	public final LinearLayout path;
	private final ScrollView infoScroll;
	private final TextView info;
	public final SearchView searchView;
	public final ListView lv;
	private boolean opened;
	private int height_def;
	public final SVController searchControl;
	public final Context context;
	private final Runnable onCheckChange;
	private final BackLog backLog;
	private final ViewState VS;

	public ExplorerStuff(boolean selectActivity, Content onItemClick, Runnable oss, Runnable onCheck,
	                     ViewState vs, BackLog bl, Context c, HorizontalScrollView hsv, ListView lv,
	                     LinearLayout path, ScrollView infoScroll, TextView info,
	                     SearchView searchView, View touchOutside) {
		selectMode = selectActivity;
		content = onItemClick;
		backLog = bl;
		VS = vs;
		context = c;
		onSearchSubmit = oss;
		onCheckChange = onCheck;
		this.hsv = hsv;
		this.lv = lv;
		this.path = path;
		this.infoScroll = infoScroll;
		this.info = info;
		(this.searchView = searchView).setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) AndroidIOSystem.hideKeyboardFrom(v);
		});
		lv.setOnScrollListener(searchControl = new SVController());
		touchOutside.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN
					&& searchView.getVisibility() == View.VISIBLE) {
				Rect outRect = new Rect();
				searchView.getGlobalVisibleRect(outRect);
				if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
					if (VS.sv_focused) {
						VS.sv_focused = false;
						VS.query = searchView.getQuery().toString();
						searchView.onActionViewCollapsed();
					}
				} else if (!VS.sv_focused) {
					v.performClick();
					VS.sv_focused = true;
					searchView.onActionViewExpanded();
					searchView.setQuery(VS.query, false);
					return true;
				}
			}
			return false;
		});
		hsv.setHorizontalScrollBarEnabled(false);
		searchView.setOnQueryTextListener(new Searcher());
		info.setOnClickListener((v) -> infoScroll.setLayoutParams(
				new LayoutParams(MATCH_PARENT, realHeight(opened = !opened))));
	}

	public class SVController implements OnScrollListener {

		public boolean visible;
		public int fvi;

		public void update(boolean gone) {
			visible = VS.sv_visible = !gone;
			searchView.setVisibility(gone ? View.GONE : View.VISIBLE);
			fvi = 0;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisible, int visICount, int size) {
			if (VS.sv_visible && Math.abs(fvi - firstVisible) > 2) {
				if (visible && firstVisible > fvi) {
					searchView.setVisibility(View.GONE);
					visible = false;
				} else if (!visible && firstVisible < fvi) {
					searchView.setVisibility(View.VISIBLE);
					visible = true;
				}
				fvi = firstVisible;
			}
		}
	}

	public class Searcher implements OnQueryTextListener {

		@Override
		public boolean onQueryTextSubmit(String query) {
			searchView.onActionViewCollapsed();
			VS.query = query;
			VS.sv_focused = false;
			if (onSearchSubmit != null) onSearchSubmit.run();
			EasyList<Container> c = new EasyList<>();
			for (BasicData bd : backLog.path) c.add((Container) bd);
			new Searcher.Finder(c, query);
			return true;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			return false;
		}

		/**
		 * Searches for all occurrences matching the compare sequence.
		 */
		class Finder {

			String comp;
			Pattern p;
			volatile int threads = 1;
			Correct correct;
			long start;
			Set<BasicData> set = new HashSet<>();

			/**
			 * Prepares and starts the searching, resolves the comparing method by prefix.
			 *
			 * @param path    path to the element who's content will be searched
			 * @param compare the sequence with optional prefix to be used as comparator
			 */
			Finder(EasyList<Container> path, String compare) {
				if (compare.charAt(0) == '\\') {
					switch (compare.charAt(1)) {
						case 'r':
							try {
								p = Pattern.compile(compare.substring(2));
							} catch (Exception e) {
								String msg = activity.getString(R.string.pattern_err) + '\n' + e.getMessage();
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
				lv.setAdapter(VS.mAdapter = VS.contentAdapter = new SearchAdapter(
						context, new ArrayList<>(), onCheckChange, selectMode));
				searchControl.update(false);
				info.setText(activity.getString(R.string.data_child_count) + 0);
				infoScroll.setLayoutParams(new LayoutParams(MATCH_PARENT, (int) (18 * dp)));
				start = System.currentTimeMillis();
				new Thread(() -> {
					search(path.get(-1).getChildren(path.get(-2)), path, false);
					threads--;
				}).start();
			}

			/**
			 * Searches the given content for matching elements.
			 */
			private void search(BasicData[] src, EasyList<Container> path, boolean threaded) {
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
					if (VS.mAdapter instanceof SearchAdapter) found = correct(bd, path);
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

			/**
			 * Validates the given element by the selected {@link #correct comparing} method.
			 */
			boolean correct(BasicData bd, EasyList<Container> path) {
				boolean yes = false;
				if (correct.verify(bd.toString())) yes = true;
				else for (String name : NameReader.readName(bd)) if (correct.verify(name)) yes = true;
				if (yes && set.add(bd)) {
					EasyList<Container> copy = new EasyList<>();
					copy.addAll(path);
					lv.post(() -> {
						VS.mAdapter.add(new SearchItemModel(bd, copy,
								((AbstractContainerAdapter) VS.mAdapter).list.size() + 1));
						info.setText(activity.getString(R.string.data_child_count) + set.size() + ";\t" +
								activity.getString(R.string.time) + (System.currentTimeMillis() - start) + "ms");
					});
				}
				return yes;
			}
		}
	}

	interface Correct {
		boolean verify(String name);
	}

	/**
	 * When the {@link #lv ListView's} content changes.
	 *
	 * @param allChanged if the currently displayed path to the element has to be fully altered.
	 */
	public void onChange(boolean allChanged) {
		if (allChanged) {
			path.removeAllViews();
			VS.breadCrumbs = 0;
			for (BasicData bd : backLog.path) addPathButton(bd);
		} else addPathButton(backLog.path.get(-1));
		setInfo(backLog.path.get(-1), (Container) backLog.path.get(-2));
	}

	/**
	 * Adds another item to the {@link #path breadCrumbs} tracker.
	 *
	 * @param bd the item to be added
	 */
	private void addPathButton(BasicData bd) {
		TextView btn = new Button(context);
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
			content.setContent((Container) bd, (Container) backLog.path.get(-2), backLog.path.size());
			if (noChange) setInfo(backLog.path.get(-1), (Container) backLog.path.get(-2));
			else onChange(true);
		});
		path.addView(btn);
		VS.breadCrumbs++;
		btn = new TextView(context);
		btn.setBackground(activity.getResources().getDrawable(R.drawable.ic_bread_crumbs));
		btn.setLayoutParams(new LayoutParams((int) (dp * 7), MATCH_PARENT));
		btn.setScaleX(2.4f);
		path.addView(btn);
	}

	/**
	 * Setts the {@link #infoScroll}
	 *
	 * @param bd
	 * @param parent
	 */
	public void setInfo(BasicData bd, Container parent) {
		String txt;
		if (!backLog.path.isEmpty()) {
			String desc = bd.getDesc(parent);
			info.setText(txt = (activity.getString(R.string.data_child_count) + lv.getAdapter().getCount()
					+ ", " + activity.getString(R.string.success_rate) + ": " + bd.getRatio()
					+ (desc.isEmpty() ? '%' : "%\n" + desc)));
		} else
			info.setText(txt = (activity.getString(R.string.data_child_count) + lv.getAdapter().getCount()));
		hsv.post(() -> hsv.fullScroll(View.FOCUS_RIGHT));
		info.setClickable((height_def = height(txt)) >= 3);
		infoScroll.setLayoutParams(new LayoutParams(MATCH_PARENT, realHeight(opened)));
		infoScroll.post(() -> infoScroll.fullScroll(View.FOCUS_UP));
	}

	/**
	 * Calculates the height parameter for {@link android.view.ViewGroup.LayoutParams#LayoutParams(int, int)}.
	 *
	 * @param opened toggles the unlimited height option - opened means maximum height
	 */
	private int realHeight(boolean opened) {
		return height_def >= 3 && opened ? LayoutParams.WRAP_CONTENT :
				(int) (activity.getResources().getDimension(R.dimen.dp) * (height_def == 1 ? 19 : 36));
	}

	/**
	 * Returns the height code for the given String, height code represents lines of the view.
	 * 1 when no '\n' chars, 2 for 1 '\n' char and 3 for more than 1 '\n' char.
	 */
	public int height(String src) {
		int i = 1;
		for (char ch : src.toCharArray()) if (ch == '\n' && ++i > 2) break;
		return i;
	}

	public static class ViewState {
		public ArrayAdapter mAdapter;
		/**
		 * This adapter is the {@link #mAdapter} if not an {@link com.schlmgr.gui.list.ImageAdapter}.
		 */
		public AbstractContainerAdapter<? extends HierarchyItemModel> contentAdapter;
		public int breadCrumbs = 0;
		public boolean sv_focused;
		public boolean sv_visible;
		private String query;
	}

	public interface Content {
		/**
		 * What to do with the selected item.
		 *
		 * @param bd         the selected item
		 * @param parent     the item's parent
		 * @param pathLength length of the path to the item
		 */
		void setContent(BasicData bd, Container parent, int pathLength);
	}
}
