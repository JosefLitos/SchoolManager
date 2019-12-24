package objects.templates;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import objects.MainChapter;

/**
 * Default implementation of a hierarchy object and full implementation of a
 * {@link Container} that cares about its parent. Defines the system for objects
 * used in a {@link testing.Test}. They have their name and their other side,
 * both in its own instance of the same class.
 *
 * @author Josef Litoš
 * @param <T> object which has two versions, but only one is manipulatable with
 */
public abstract class TwoSided<T extends TwoSided> extends Element implements Container {

   /**
    * {@code true} if and only if the object is the main one (all its methods
    * can be used).
    */
   protected final boolean isMain;
   /**
    * Amount of parents this object is stored in
    */
   protected int parentCount;

   protected TwoSided(IOSystem.Formatter.Data bd, boolean isMain, Map<MainChapter, List<T>> NET) {
      super(bd);
      this.isMain = isMain;
      NET.get(identifier).add((T) this);
      parentCount = 1;
   }

   /**
    * Contains all objects, which belong to this object.
    */
   public final java.util.Map<Container, java.util.List<T>> children = new java.util.HashMap<>();

   @Override
   public TwoSided[] getChildren() {
      List<TwoSided> l = new LinkedList<>();
      children.keySet().stream().forEach((c) -> l.addAll(children.get(c)));
      return l.toArray(new TwoSided[0]);
   }

   @Override
   public TwoSided[] getChildren(Container c) {
      return children.get(c) == null ? null : children.get(c).toArray(new TwoSided[0]);
   }

   @Override
   public void putChild(Container c, BasicData e) {
      if (children.get(c) == null) {
         children.put(c, new LinkedList<>());
      }
      children.get(c).add((T) e);
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
      if (e instanceof TwoSided) {
         if (!isMain) {
            throw new IllegalArgumentException("Child can be removed only by main object");
         }
         for (Container c : children.keySet()) {
            if (children.get(c).remove(e)) {
               e.destroy(c);
               remove1(c, (T) e);
               return c;
            }
         }
      }
      return null;
   }

   /**
    * Called to end the proccess of removing a child from the main object.
    *
    * @param parent where the object is located
    * @param toRem the object to be removed
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
      return isMain ? children.get(c) == null || children.get(c).isEmpty() : false;
   }

   @Override
   public StringBuilder writeData(StringBuilder sb, int tabs, Container cp) {
      if (!isMain) {
         return tabs(sb, tabs, '{').add(sb, this, cp, false, true, true, true, null, null, false).append('}');
      }
      tabs(sb, tabs++, '{').add(sb, this, cp, true, true, true, true, null, null, true);
      return writeData0(sb, tabs, cp);
   }
}
