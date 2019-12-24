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
   protected final java.util.List<BasicData> children = new java.util.ArrayList<>();

   @Override
   public BasicData[] getChildren() {
      return children.toArray(new BasicData[children.size()]);
   }

   @Override
   public BasicData[] getChildren(Container none) {
      return getChildren();
   }

   @Override
   public void putChild(Container none, BasicData e) {
      children.add(e);
   }

   @Override
   public boolean removeChild(Container none, BasicData e) {
      removeChild(e);
      return true;
   }

   @Override
   public Container removeChild(BasicData e) {
      children.remove(e);
      return null;
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
   }
}