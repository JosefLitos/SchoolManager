package objects.templates;

import IOSystem.Formatter.Data;

/**
 * Basics of a hierarchy object and basic implementation of a {@link Container}.
 * Doesn't care about its parent.
 *
 * @author Josef Litoš
 */
public abstract class SemiElementContainer extends BasicElement implements Container {

	/**
	 * Contains its children.
	 */
	protected final java.util.List<BasicData> children = new java.util.LinkedList<>();
	/**
	 * The only real parent of this object.
	 */
	protected Container parent;

	/**
	 * Converts this object into a different Container object. Depends on implementation.
	 * 
	 * @return the converted object
	 */
	public abstract Container convert();
	
	@Override
	public int[] refreshSF() {
		int[] sf = {0, 0};
		for (BasicData bd : getChildren()) {
			int[] childSF =
					bd instanceof Container ? ((Container) bd).refreshSF() : bd.getSF();
			sf[0] += childSF[0];
			sf[1] += childSF[1];
		}
		return (this.sf = sf).clone();
	}

	@Override
	public boolean move(Container op, Container np, Container npp) {
		return super.move(op, parent = np, npp);
	}

	@Override
	public boolean move(Container op, Container opp, Container np, Container npp) {
		return super.move(op, opp, parent = np, npp);
	}

	@Override
	public BasicData[] getChildren() {
		return children.toArray(new BasicData[children.size()]);
	}

	@Override
	public BasicData[] getChildren(Container c) {
		return (parent == c || c == null) ? getChildren() : null;
	}

	@Override
	public boolean putChild(Container c, BasicData e) {
		return (parent == c || c == null) && children.add(e);
	}

	@Override
	public boolean putChild(Container c, BasicData e, int index) {
		if (parent == c || c == null) {
			children.add(index, e);
			return true;
		} else return false;
	}
	
	@Override
	public boolean replaceChild(Container c, BasicData old, BasicData repl) {
		int index = children.indexOf(old);
		if ((parent == c || c == null) && index > -1) {
			children.set(index, repl);
			return true;
		} else return false;
	}

	@Override
	public boolean removeChild(Container c, BasicData e) {
		return (parent == c || c == null) && children.remove(e);
	}

	@Override
	public Container removeChild(BasicData e) {
		return children.remove(e) ? parent : null;
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

	protected SemiElementContainer(Data d) {
		super(d);
		description = d.description;
		if ((parent = d.par) == null)
			throw new IllegalArgumentException("All objects have to have a parent!");
	}
}
