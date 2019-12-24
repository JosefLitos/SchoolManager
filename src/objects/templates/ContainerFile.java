package objects.templates;

import IOSystem.Formatter;
import java.io.File;

/**
 * Instance of this class save their content into its own file.
 *
 * @author Joset Litoš
 */
public interface ContainerFile extends Container {

   /**
    *
    * @param name name to be tested for ability to be saved as a file
    * @return whether this name can be used as a name for this object
    */
   public static boolean isCorrect(String name) {
      if (name.length() > 150) {
         throw new IllegalArgumentException("Name can't be longer than 150 characters");
      } else if ("/|\\:\"?*§$".chars().anyMatch((ch) -> name.contains("" + (char) ch))) {
         throw new IllegalArgumentException("Name can't contain /|\\:\"?*§$");
      }
      return true;
   }

   /**
    *
    * @return the File that this object is saved in.
    */
   File getSaveFile();

   /**
    * Saves this object into its {@link #getSaveFile() own file}.
    */
   default void save() {
      Formatter.saveFile(writeData(new StringBuilder(), 0, null).toString(), getSaveFile());
   }

   /**
    *
    * @return if this object has already loaded all its content from its
    * {@link #getSaveFile() file}.
    */
   boolean isLoaded();

   /**
    * Loads this object from its {@link #getSaveFile() own file}.
    */
   default void load() {
      if (!isLoaded()) {
         IOSystem.ReadElement.loadSch(getSaveFile(), getIdentifier(), null);
      }
   }
}
