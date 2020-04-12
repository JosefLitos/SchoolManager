package objects.templates;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of a hierarchy object and full implementation of a {@link Container} that
 * cares about its parent. Defines the system for objects used in a {@link testing.Test}.
 * They have their name and their other side, both in its own instance of the same class.
 *
 * @param <T> object which has two versions, but only one is manipulable with
 * @author Josef Litoš
 */
public abstract class TwoSided<T extends TwoSided> extends Element implements Container {

	/**
	 * {@code true} if and only if the object is the more_main one (all its methods can be used).
	 */
	protected final boolean isMain;
	/**
	 * Amount of parents this object is stored in
	 */
	protected int parentCount;

	protected TwoSided(IOSystem.Formatter.Data bd, boolean isMain, Map<objects.MainChapter, List<T>> NET) {
		super(bd);
		this.isMain = isMain;
		NET.get(identifier).add((T) this);
		parentCount = 1;
	}

	/**
	 * Contains all objects, which belong to this object.
	 */
	public final Map<Container, List<T>> children = new java.util.HashMap<>();

	@Override
	public TwoSided[] getChildren() {
		List<TwoSided> l = new LinkedList<>();
		for (List<T> ch : children.values()) l.addAll(ch);
		return l.toArray(new TwoSided[0]);
	}

	@Override
	public TwoSided[] getChildren(Container c) {
		return children.get(c) == null ? null : children.get(c).toArray(new TwoSided[0]);
	}

	@Override
	public boolean putChild(Container c, BasicData e) {
		if (!(e instanceof TwoSided)) return false;
		if (children.get(c) == null) {
			parentCount++;
			children.put(c, new LinkedList<>());
		} else if (children.get(c).contains((T) e)) return false;
		return children.get(c).add((T) e);
	}

	@Override
	public boolean removeChild(Container parent, BasicData child) {
		if (isMain) {
			child.destroy(parent);
			remove1(parent, (T) child);
			return true;
		}
		throw new IllegalArgumentException("Child can be removed only by main object");
	}

	@Override
	public Container removeChild(BasicData e) {
		if (!(e instanceof TwoSided)) return null;
		if (!isMain) throw new IllegalArgumentException("Child can be removed only by main object");
		for (Container c : children.keySet())
			if (children.get(c).remove(e)) {
				e.destroy(c);
				remove1(c, (T) e);
				return c;
			}
		return null;
	}

	/**
	 * Called to end the process of removing a child from the more_main object.
	 *
	 * @param parent where the object is located
	 * @param toRem  the object to be removed
	 */
	protected void remove1(Container parent, T toRem) {
		children.get(parent).remove(toRem);
		if (children.get(parent).isEmpty()) {
			children.remove(parent);
			parent.removeChild(this);
		}
	}

	@Override
	public boolean isEmpty(Container c) {
		return isMain && (children.get(c) == null || children.get(c).isEmpty());
	}

	@Override
	public boolean move(Container op, Container np, Container npp) {
		if (isMain && super.move(op, np, npp)) {
			for (T ch : children.remove(op)) {
				putChild(np, ch);
				ch.remove1(op, this);
				ch.putChild(np, this);
			}
			return true;
		}
		return false;
	}

	@Override
	public StringBuilder writeData(StringBuilder sb, int tabs, Container cp) {
		tabs(sb, tabs++, '{').add(sb, this, cp, true, true, true, true, null, null, true);
		boolean first = true;
		for (BasicData bd : getChildren(cp))
			if (!bd.isEmpty(this)) {
				if (first)
					first = false;
				else sb.append(',');
				tabs(sb, tabs, '{').add(sb, bd, cp, false, true, true, true, null, null, false).append('}');
			}
		tabs(sb, tabs - 1, ']');
		return sb.append('}');
	}
}
