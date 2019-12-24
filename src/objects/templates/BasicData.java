package objects.templates;

import objects.MainChapter;

/**
 * Basic element of the school object project element hierarchy. Defines the
 * basics of all hierarchy-usable objects.
 *
 * @see MainChapter
 * @author Josef Litoš
 */
public interface BasicData extends IOSystem.WriteElement {

   /**
    * Destroys the part of this object contained in the specified parent. If
    * this object is not contained in the parent, nothing happens.
    *
    * @param parent the parent to remove this object from
    * @return {@code true} if this object has been successfully removed
    */
   boolean destroy(Container parent);

   /**
    *
    * @param parent parent of this object
    * @return {@code true} if this object is empty (or has no meaning) in the
    * specified parent
    */
   default boolean isEmpty(Container parent) {
      return false;
   }

   /**
    *
    * @return the main hierarchy object that this object belongs to
    */
   MainChapter getIdentifier();

   /**
    * Alteres the current name of this object.
    *
    * @param name the new name for this object
    * @return if the name has been set successfully
    */
   boolean setName(String name);

   String getName();

   /**
    *
    * @return the ratio of successes and fails for this object in percentage
    */
   default int getRatio() {
      int[] sf = getSF();
      return sf[0] == 0 ? 0 : (sf[1] == 0 ? 1 : (100 * sf[0] / (sf[0] + sf[1])));
   }

   int[] getSF();

   /**
    *
    * @return the amount of tests runned on this object
    */
   default int getSFCount() {
      return getSF()[0] + getSF()[1];
   }

   /**
    *
    * @param success if the test for this object was successful
    */
   void addSF(boolean success);

   String getDesc(Container c);

   String putDesc(Container c, String desc);
}
