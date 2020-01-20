package com.schlmgr.objects;

import com.schlmgr.IOSystem.Formatter.Data;
import com.schlmgr.IOSystem.Formatter.OnFailListener;
import com.schlmgr.IOSystem.ReadElement.Source;
import com.schlmgr.objects.templates.BasicData;
import com.schlmgr.objects.templates.Container;
import com.schlmgr.objects.templates.ContainerFile;
import com.schlmgr.objects.templates.SemiElementContainer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.schlmgr.IOSystem.Formatter.defaultOFLs;
import static com.schlmgr.IOSystem.WriteElement.obj;
import static com.schlmgr.IOSystem.WriteElement.str;

/**
 * Contains other hierarchy com.schlmgr.objects. Every instance of this class saves into its
 * own file.
 *
 * @author Josef Litoš
 */
public class SaveChapter extends SemiElementContainer implements ContainerFile {

	/**
	 * Contains all instances of this class created. All SaveChapters are sorted
	 * by the {@link MainChapter hierarchy} they belong to. read-only data
	 */
	public static final Map<MainChapter, java.util.List<SaveChapter>> ELEMENTS = new HashMap<>();

	private boolean loaded;

	@Override
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * The head hierarchy object which this object belongs to.
	 */
	protected final MainChapter identifier;

	@Override
	public MainChapter getIdentifier() {
		return identifier;
	}

	private byte hash;

	/**
	 * @param d necessary information to create the new object
	 * @return the created object
	 */
	public static final SaveChapter mkElement(Data d) {
		return mkElement(d, true);
	}

	protected static final SaveChapter mkElement(Data d, boolean full) {
		if (ELEMENTS.get(d.identifier) == null) {
			ELEMENTS.put(d.identifier, new java.util.LinkedList<>());
			if (d.identifier.getSetting("schNameCount") == null) {
				d.identifier.putSetting("schNameCount", new HashMap<String, Byte>());
				d.identifier.putSetting("schRemoved", false);
			}
		} else {
			short hash = d.tagVals == null || d.tagVals[0] == null ? 1 : (short) (long) d.tagVals[0];
			for (SaveChapter sch : ELEMENTS.get(d.identifier)) {
				if (d.name.equals(sch.toString()) && hash == (sch.hash + 129)) {
					sch.loaded = full;
					return sch;
				}
			}
		}
		return new SaveChapter(d, full);
	}

	protected SaveChapter(Data d, boolean full) {
		super(d);
		identifier = d.identifier;
		ContainerFile.isCorrect(name);
		parent = d.par;
		loaded = full;
		//hash-creator
		if (!full) {
			hash = (byte) (d.tagVals[0] == null ? -128 : ((long) d.tagVals[0]) - 129);
		} else {
			Byte b = ((Map<String, Byte>) identifier.getSetting("schNameCount")).get(name);
			((Map<String, Byte>) identifier.getSetting("schNameCount")).put(name, hash = (byte) (b == null ? -128 : (b + 1)));
			if (hash == 127) {
				throw new IllegalArgumentException("Maximum amount (255) for SaveChapters called: '"
						+ name + "' has been already reached!");
			}
			((Map<String, Byte>) identifier.getSetting("schNameCount")).put(name, hash);
		}
		ELEMENTS.get(d.identifier).add(this);
	}

	@Override
	public boolean isEmpty(Container c) {
		if (c != parent) return true;
		if (children.isEmpty()) return loaded;
		return ContainerFile.super.isEmpty(c);
	}

	@Override
	public boolean hasChild(BasicData e) {
		if (!loaded) load(false);
		return super.hasChild(e);
	}

	@Override
	public boolean hasChild(Container par, BasicData e) {
		if (!loaded) load(false);
		return super.hasChild(par, e);
	}

	@Override
	public Container removeChild(BasicData e) {
		if (!loaded) load(false);
		return super.removeChild(e);
	}

	@Override
	public boolean removeChild(Container c, BasicData e) {
		if (!loaded) load(false);
		return super.removeChild(c, e);
	}

	@Override
	public boolean putChild(Container c, BasicData e) {
		if (!loaded) load(false);
		return super.putChild(c, e);
	}

	@Override
	public BasicData[] getChildren() {
		if (!loaded) load(false);
		return super.getChildren();
	}

	/**
	 * Cleans the database numbering of SaveChapters. This is needed, when
	 * SaveChapters were removed and they weren't last of the given name.
	 *
	 * @param mch the hierarchy to be cleaned
	 */
	public static void clean(MainChapter mch) {
		if (!isCleanable(mch)) return;
		mch.load(false);
		String exceptions = "";
		File dir = new File(mch.getDir(), "Chapters");
		Map<String, Byte> schNC = new HashMap<>();
		for (SaveChapter sch : ELEMENTS.get(mch)) {
			short hash = 1;
			File src = new File(dir, sch.name + ".json");
			while (hash < sch.hash && !src.renameTo(new File(dir, sch.name + '[' + ++hash + "].json")))
				if (hash == 256) {
					exceptions += "\nFile '" + src + "' can't be renamed!";
					break;
				}
			if ((hash -= 129) < 127 && (schNC.get(sch.name) == null
					|| (sch.hash = (byte) hash) > schNC.get(sch.name))) schNC.put(sch.name, (byte) hash);
		}
		mch.putSetting("schNameCount", schNC);
		if (!exceptions.isEmpty()) throw new IllegalArgumentException(exceptions);
		mch.putSetting("schRemoved", false);
	}

	/**
	 * Tells, if the given hierarchy can get cleaned of SaveChapter file numbers.
	 *
	 * @param mch source
	 * @return {@code true} if an image has been deleted from the hierarchy
	 */
	public static boolean isCleanable(MainChapter mch) {
		return (boolean) mch.getSetting("schRemoved");
	}

	@Override
	public File getSaveFile() {
		return new File(new File(identifier.getDir(), "Chapters"), name
				+ (hash == -128 ? ".json" : ('[' + (hash + 129) + "].json")));
	}

	/**
	 * @param name the new name for this object
	 * @return {@code false} if the directory has to be {@link #clean(MainChapter) cleaned} first
	 */
	@Override
	public boolean setName(Container none, String name) {
		byte current = ((Map<String, Byte>) identifier.getSetting("schNameCount")).get(name);
		if (++current == 127)
			throw new IllegalArgumentException("Maximum amount (255) for SaveChapters called: '" + name
					+ "' has been already reached!");
		if (new File(new File(identifier.getDir(), "Chapters"), this.name
				+ (hash == -128 ? ".json" : ('[' + (hash + 129) + "].json")))
				.renameTo(new File(new File(identifier.getDir(), "Chapters"), name
						+ (current == -128 ? ".json" : ('.' + (current + 129) + ".json"))))) {
			((Map<String, Byte>) identifier.getSetting("schNameCount")).put(this.name = name, hash = current);
			return true;
		}
		return false;
	}

	@Override
	public boolean destroy(Container parent) {
		if (new File(new File(identifier.getDir(), "Chapters"), name
				+ (hash == -128 ? ".json" : ('[' + (hash + 129) + "].json"))).delete()) {
			identifier.putSetting("schRemoved", true);
			int i = ((Map<String, Byte>) identifier.getSetting("schNameCount")).get(name) - 1;
			if (i < -128) ((Map<String, Byte>) identifier.getSetting("schNameCount")).remove(name);
			else ((Map<String, Byte>) identifier.getSetting("schNameCount")).put(name, (byte) i);
			parent.removeChild(this);
			ELEMENTS.get(identifier).remove(this);
			return true;
		}
		return false;
	}

	@Override
	public StringBuilder writeData(StringBuilder sb, int tabs, Container cp) {
		if (tabs == 0) {
			sb.append('{');
			add(sb, this, cp, true, true, true, true,
					hash == -128 ? null : str("hash"), hash == -128 ? null : obj(hash + 129), true);
			return writeData0(sb, 1, cp);
		}
		if (loaded) save();
		tabs(sb, tabs++, '{').add(sb, this, cp, true, true, true, true,
				hash == -128 ? null : str("hash"), hash == -128 ? null : obj(hash + 129), false);
		return sb.append('}');
	}

	/**
	 * Implementation of
	 * {@link com.schlmgr.IOSystem.ReadElement#readData(com.schlmgr.IOSystem.ReadElement.Source, com.schlmgr.objects.templates.Container) loading from String}.
	 */
	public static BasicData readData(Source src, Container parent) {
		if (parent == null) {
			SaveChapter sch = mkElement(com.schlmgr.IOSystem.ReadElement.get(
					src, true, true, true, true, parent, "hash"), true);
			for (BasicData bd : com.schlmgr.IOSystem.ReadElement.loadChildren(src, sch))
				sch.putChild(parent, bd);
			return sch;
		}
		return mkElement(com.schlmgr.IOSystem.ReadElement.get(src, true, true, true, false, parent, "hash"), false);
	}
}