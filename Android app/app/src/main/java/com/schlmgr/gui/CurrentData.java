package com.schlmgr.gui;

import com.google.android.material.snackbar.Snackbar;
import com.schlmgr.R;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import IOSystem.Formatter;
import IOSystem.Formatter.Data;
import objects.MainChapter;
import objects.templates.BasicData;
import objects.templates.ContainerFile;

import static IOSystem.Formatter.defaultReacts;

public class CurrentData {

	public static final BackLog backLog = new BackLog();
	public static final Set<ContainerFile> changed = new HashSet<>();

	public static class BackLog {
		/**
		 * Path to the currently displayed element (inclusive).
		 */
		public EasyList<BasicData> path = new EasyList<>();
		/**
		 * The previous paths to the displayed elements.
		 */
		private final EasyList<EasyList<BasicData>> prevPaths = new EasyList<>();
		/**
		 * The amounts of callbacks, before the current paths get replaced with an older ones.
		 */
		public final EasyList<Byte> onePath = new EasyList<>();

		public void add(Boolean newPath, BasicData bd, EasyList<BasicData> currPath) {
			if (newPath == null) if (newPath = currPath.size() == path.size())
				for (int i = path.size() - 1; i >= 0; i--) {
					if (path.get(i) != currPath.get(i)) {
						newPath = false;
						break;
					}
				}
			if (newPath) {
				prevPaths.add(path);
				path = currPath;
				onePath.add((byte) 0);
			} else {
				path.add(bd);
				onePath.add((byte) (onePath.remove(-1) + 1));
			}
		}

		public void clear() {
			path.clear();
			prevPaths.clear();
			onePath.clear();
			onePath.add((byte) 0);
		}

		/**
		 * @return if the path list didn't change
		 */
		public boolean remove() {
			boolean ret;
			if (ret = onePath.get(-1) > 0) {
				path.remove(-1);
				onePath.add((byte) (onePath.remove(-1) - 1));
			} else {
				onePath.remove(-1);
				if (onePath.isEmpty()) {
					onePath.add((byte) 0);
					path = new EasyList<>();
				} else path = prevPaths.remove(-1);
			}
			return ret;
		}
	}

	public static class EasyList<T> extends LinkedList<T> {

		public static <E> EasyList<E> convert(E[] source) {
			EasyList<E> ret = new EasyList<>();
			for (E e : source) ret.add(e);
			return ret;
		}

		@Override
		public T get(int index) {
			if (isEmpty()) return null;
			if (index < 0) return size() + index < 0 ? null : super.get(size() + index);
			return size() > index ? super.get(index) : null;
		}

		@Override
		public T remove(int index) {
			if (index < 0) return super.remove(size() + index);
			return super.remove(index);
		}
	}

	private static final LinkedList<MainChapter> toLoad = new LinkedList<>();

	public static void finishLoad() {
		synchronized (toLoad) {
			boolean thread = false;
			for (MainChapter mch : toLoad) {
				if (mch.isEmpty(null)) try {
					mch.load(thread = !thread);
				} catch (Exception e) {
				}
			}
			toLoad.clear();
		}
	}

	public static void createMchs() {
		synchronized (toLoad) {
			if (Formatter.getPath().listFiles() != null) for (File f : Formatter.getPath().listFiles())
				load:{
					for (MainChapter mch : MainChapter.ELEMENTS)
						if (mch.getName().equals(f.getName())) break load;
					if (new File(f, "setts.dat").exists())
						toLoad.add(new MainChapter(new Data(f.getName(), null)));
				}
			for (File f : ImportedMchs.get())
				load:{
					for (MainChapter mch : MainChapter.ELEMENTS)
						if (mch.getName().equals(f.getName())) break load;
					if (f.exists()) toLoad.add(new MainChapter(new Data(f.getName(), null), f));
				}
		}
	}

	public static class ImportedMchs {
		public static Set<File> importedMchs;

		public static void importMch(File mchDir) {
			checkLoaded();
			importedMchs.add(mchDir);
			save();
		}

		public static void removeMch(File mchDir) {
			importedMchs.remove(mchDir);
			save();
		}

		private static void checkLoaded() {
			if (importedMchs == null) {
				importedMchs = new HashSet<>();
				backLog.onePath.add((byte) 0);
				String imds = (String) Formatter.getSetting("importedMchDirs");
				if (imds != null) for (String s : imds.split(";"))
					try {
						importedMchs.add(new File(s));
					} catch (Exception e) {
						Snackbar.make(Controller.activity.getCurrentFocus(),
								Controller.activity.getString(R.string.object_not_found) + s,
								Snackbar.LENGTH_LONG).setAction("Action", null).show();
					}
			}
		}

		private static void save() {
			if (importedMchs.isEmpty()) Formatter.removeSetting("importedMchDirs");
			else {
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (File f : get()) {
					if (first) first = false;
					else sb.append(';');
					sb.append(f.getPath());
				}
				Formatter.putSetting("importedMchDirs", sb.toString());
			}
		}

		public static File[] get() {
			checkLoaded();
			return importedMchs.toArray(new File[0]);
		}
	}

	public static void save() {
		new Thread(() -> {
			for (int i = backLog.path.size() - 1; i >= 0; i--)
				if (backLog.path.get(i) instanceof ContainerFile) {
					ContainerFile cf = (ContainerFile) backLog.path.get(i);
					try {
						cf.save();
					} catch (Exception e) {
						defaultReacts.get(ContainerFile.class + ":save").react(e, cf.getSaveFile(), cf);
						changed.add(cf);
					}
					return;
				}
		}).start();
	}
}
