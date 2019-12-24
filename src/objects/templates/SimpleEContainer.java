package objects.templates;

import IOSystem.Formatter;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of a hierarchy object and basic implementation of a
 * {@link Container}. Doesn't care about its parent.
 *
 * @author Josef Litoš
 */
public abstract class SimpleEContainer extends Element implements Container {

   public SimpleEContainer(Formatter.Data d) {
      super(d);
      if ((parent = d.par) == null) {
         throw new IllegalArgumentException("All objects have to have a parent!");
      }
   }

   /**
    * Contains all objects, which belong to this object.
    */
   protected final List<BasicData> children = new LinkedList<>();
   protected Container parent;

   @Override
   public BasicData[] getChildren() {
      return children.toArray(new BasicData[0]);
   }

   @Override
   public BasicData[] getChildren(Container c) {
      return parent == c || c == null ? getChildren() : null;
   }

   @Override
   public void putChild(Container c, BasicData e) {
      if (parent == c || c == null) {
         children.add(e);
      }
   }

   @Override
   public boolean removeChild(Container c, BasicData e) {
      return parent == c || c == null ? children.remove(e) : false;
   }

   @Override
   public Container removeChild(BasicData e) {
      if (children.remove(e)) {
         return parent;
      }
      return null;
   }
}
