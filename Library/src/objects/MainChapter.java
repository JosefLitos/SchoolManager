package objects;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import IOSystem.Formatter;
import IOSystem.Formatter.IOSystem.GeneralPath;
import IOSystem.Formatter.Reactioner;
import IOSystem.ReadElement;
import objects.templates.BasicData;
import objects.templates.Container;
import objects.templates.ContainerFile;


/**
 * Head object of hierarchy of all {@link BasicData elemetary} objects. The
 * hierarchy is stored in its own folder under name of this object in the
 * specified {@link Formatter#getSubjectsDir()}  directory}. Should be
 * {@link #name named} after the school object this hierarchy represents.
 *
 * @author Josef Litoš
 */
public class MainChapter extends objects.templates.BasicElement implements ContainerFile {

	/**
	 * Contains all loaded hierarchies. read-only data
	 */
	public static final List<MainChapter> ELEMENTS = new LinkedList<>();
	/**
	 * This file contains everything about this object and its {@link #children content}
	 * together with its own {@link #settings settings}.
	 */
	protected GeneralPath dir;
	protected GeneralPath settsPath;
	protected GeneralPath chapDir;

	public GeneralPath getDir() {
		return dir;
	}
	
	public GeneralPath getChapDir() {
		return chapDir;
	}

	/**
	 * Contains properties, options, or anything else that has something to do
	 * with this hierarchy.
	 */
	protected Map<String, Object> settings = new java.util.HashMap<>();

	public Object getSetting(String key) {
		return settings.get(key);
	}

	public void putSetting(String key, Object value) {
		settings.put(key, value);
		settsPath.deserialize(settings);
	}

	public void removeSetting(String key) {
		settings.remove(key);
		settsPath.deserialize(settings);
	}

	@Override
	public GeneralPath getSaveFile() {
		return dir.getChild("main.json");
	}

	/**
	 * Only this constructor creates the head object of the hierarchy. The
	 * hierarchy files are saved in its {@link #dir directory}.
	 *
	 * @param d must contain {@link #name name} of this hierarchy.
	 */
	public MainChapter(Formatter.Data d) {
		this(d, ContainerFile.isCorrect(d.name) ? Formatter.getSubjectsDir().getChild(d.name) : null);
	}

	public MainChapter(Formatter.Data d, GeneralPath dir) {
		super(d);
		description = d.description;
		this.dir = dir;
		settsPath = dir.getChild("setts.dat");
		if (settsPath.exists())
			settings = (Map<String, Object>)settsPath.serialize();
		else settsPath.deserialize(settings);
		chapDir = dir.getChild("Chapters");
		ELEMENTS.add(this);
	}

	@Override
	public int[] refreshSF() {
		if (children.isEmpty()) load(false);
		int[] sf = {0, 0};
		for (BasicData bd : getChildren()) {
			int[] childSF =
					bd instanceof Container ? ((Container) bd).refreshSF() : bd.getSF();
			sf[0] += childSF[0];
			sf[1] += childSF[1];
		}
		return (this.sf = sf).clone();
	}

	/**
	 * Deletes the whole subject, this action can't be reversed.
	 *
	 * @param parent no parent
	 * @return if the main directory was successfully deleted
	 */
	@Override
	public boolean destroy(Container parent) {
		for (int i = children.size() - 1; i >= 0; i--) children.get(i).destroy(this);
		children.clear();
		ELEMENTS.remove(this);
		return dir.delete();
	}


	@Override
	public BasicData setName(Container none, String name) {
		ContainerFile.isCorrect(name);
		GeneralPath newDir = Formatter.getSubjectsDir().getChild(name);
		if (Reference.ELEMENTS.get(this) != null)
			for (Reference ref : Reference.ELEMENTS.get(this)) ref.pathStr[0] = name;
		for (byte i = 0; i < 5; i++) {
			newDir = dir.moveTo(newDir);
			if (newDir != dir) {
				if (newDir == null) return null;
				dir = newDir;
				settsPath = dir.getChild("setts.dat");
				chapDir = dir.getChild("Chapters");
				this.name = name;
				save();
				return this;
			}
		}
		return this;
	}

	/**
	 * This method removes this object from the
	 * {@link #ELEMENTS list}. It disconnects from the net.
	 */
	public void close() {
		ELEMENTS.remove(this);
		SaveChapter.ELEMENTS.remove(this);
		Chapter.ELEMENTS.remove(this);
		Word.ELEMENTS.remove(this);
		Picture.ELEMENTS.remove(this);
		Reference.ELEMENTS.remove(this);
		Runtime.getRuntime().gc();
	}

	/**
	 * Contains its children.
	 */
	protected final List<BasicData> children = new LinkedList<>();

	@Override
	public boolean move(Container op, Container np, Container npp) {
		throw new UnsupportedOperationException("MainChapter can't be moved - no parent.");
	}

	@Override
	public boolean move(Container op, Container opp, Container np, Container npp) {
		throw new UnsupportedOperationException("MainChapter can't be moved - no parent.");
	}

	@Override
	public BasicData[] getChildren() {
		if (loaded < 2 && children.isEmpty()) load(false);
		return children.toArray(new BasicData[children.size()]);
	}

	@Override
	public BasicData[] getChildren(Container c) {
		if (loaded < 2 && children.isEmpty()) load(false);
		return getChildren();
	}

	@Override
	public boolean putChild(Container c, BasicData e) {
		if (loaded < 2 && children.isEmpty()) load(false);
		return children.add(e);
	}

	@Override
	public boolean putChild(Container c, BasicData e, int index) {
		if (loaded < 2 && children.isEmpty()) load(false);
		children.add(index, e);
		return true;
	}
	
	@Override
	public boolean replaceChild(Container c, BasicData old, BasicData repl) {
		if (loaded < 2 && children.isEmpty()) load(false);
		int index = children.indexOf(old);
		if (index > -1) {
			children.set(index, repl);
			return true;
		} else return false;
	}

	@Override
	public boolean removeChild(Container c, BasicData e) {
		return children.remove(e);
	}

	@Override
	public Container removeChild(BasicData e) {
		children.remove(e);
		return this;
	}

	/**
	 * The description for this object.
	 */
	protected String description;

	@Override
	public String getDesc(Container none) {
		return description;
	}

	@Override
	public String putDesc(Container none, String desc) {
		String old = description;
		description = desc;
		return old;
	}

	@Override
	public MainChapter getIdentifier() {
		return this;
	}

	private volatile boolean saving;

	@Override
	public void save(Reactioner rtr, boolean thread) {
		Runnable r = () -> {
			try {
				if (saving) return;
				saving = true;
				getSaveFile().save(
					 writeData(new ContentWriter().startWritingItem(this, null)).endWritingItem().toString());
				saving = false;
			} catch (Exception e) {
				if (rtr != null) rtr.react(e, getSaveFile(), this);
			}
		};
		if (thread) new Thread(r, "MCh save").start();
		else r.run();
	}

	// 0: nothing loaded, 1: loaded main.json file, 2: loaded all saved chapters
	private volatile int loaded;
	private boolean loading;

	/**
	 * In this implementation of {@link ContainerFile} this method loads all
	 * {@link SaveChapter file chapters} belonging to this object.
	 */
	@Override
	public void load(Reactioner rtr, boolean thread) {
		if (loaded > 1) return;
		if (thread) new Thread(() -> load0(rtr, true), "MCh load").start();
		else load0(rtr, false);
	}

	private void load0(Reactioner rtr, boolean thread) {
		if (children.isEmpty()) {
			if (!getSaveFile().exists() || loaded > 0) {
				loaded = 2;
				return;
			}
			synchronized (this) {
				if (loading) {
					while (!thread) try {
						wait();
					} catch (Exception e) {
					}
					return;
				}
				loading = true;
			}
			try {
				ReadElement.loadFile(getSaveFile(), this, this);
				if (SaveChapter.ELEMENTS.get(this) == null) loaded = 2;
				else loaded = 1;
				synchronized (this) {
					loading = false;
					notifyAll();
				}
			} catch (Exception e) {
				if (rtr != null) rtr.react(e, getSaveFile(), this);
				return;
			}
		} else {
			List<SaveChapter> schs = SaveChapter.ELEMENTS.get(this);
			if (schs == null) return;
			int size;
			do for (int i = (size = schs.size()) - 1; i >= 0; i--)
				if (!schs.get(i).isLoaded()) schs.get(i).load(rtr, thread);
			while (size < schs.size());
			loaded = 2;
		}
		if (Formatter.defaultReacts.get("MChLoaded") != null)
			Formatter.defaultReacts.get("MChLoaded").react(this);
	}

	@Override
	public boolean isLoaded() {
		return loaded > 1;
	}

	@Override
	public int getRatio() {
		return children.isEmpty() ? -2 : super.getRatio();
	}

	@Override
	public ContentWriter writeData(ContentWriter cw) {
		settsPath.deserialize(settings);
		return cw.addClass().addName().addSF().addDesc().addChildren();
	}

	/**
	 * Implementation of
	 * {@link ReadElement#readData(ReadElement.Content, Container) loading from String}.
	 */
	public static BasicData readData(ReadElement.Content src, Container parent) {
		Formatter.Data d = src.getData(parent);
		MainChapter mch = (MainChapter) parent;
		if (mch == null) {
			for (MainChapter m : ELEMENTS) if (m.name.equals(d.name)) return m;
			mch = new MainChapter(d);
		} else {
			mch.description = d.description;
			mch.sf = d.sf;
		}
		src.identifier = mch;
		for (BasicData bd : src.getChildren(mch)) mch.children.add(bd);
		return mch;
	}
}
