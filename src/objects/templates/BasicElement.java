package objects.templates;

/**
 * Basic implementation of the simplest hierarchy object.
 *
 * @author Josef Litoš
 */
public abstract class BasicElement implements BasicData {

   public BasicElement(IOSystem.Formatter.Data d) {
      name = d.name;
      sf = d.sf == null ? new int[]{0, 0} : d.sf;
   }

   /**
    * Name of this object.
    */
   protected String name;

   @Override
   public boolean setName(String name) {
      this.name = name;
      return true;
   }

   @Override
   public String getName() {
      return name;
   }

   /**
    * Success and Fail for this object.
    */
   protected int[] sf;

   @Override
   public int[] getSF() {
      return sf;
   }

   @Override
   public void addSF(boolean scs) {
      sf[scs ? 0 : 1]++;
   }

   @Override
   public String toString() {
      return getName();
   }
}