package IOSystem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import objects.MainChapter;
import objects.templates.Container;

/**
 * This class is used to operate with text files. The format of all files
 * containing necessary data of every {@link objects.none} is supposed to look
 * like json, but the code is all thought and writen by the author.
 *
 * @author Josef Litoš
 */
public class Formatter {

   /**
    * This method has to be called at the start of the program.
    */
   public static void loadSettings() {
      File setts = new File("settings.dat");
      if (setts.exists()) {
         settings = (Map<String, String>) serialize("settings.dat");
         createDir(objDir = settings.get("objdir"));
      } else {
         createDir(objDir = "School objects\\");
         settings.put("objdir", "School objects\\");
         settings.put("language", "cz");
         settings.put("randomType", "true");
         deserializeTo("settings.dat", settings);
      }
   }
   /**
    * Path to the directory that contains all {@link MainChapter hierarchies's}
    * folders and data
    */
   static String objDir;
   public static final String CLASS = "class", NAME = "name", SUCCESS = "s",
           FAIL = "f", CHILDREN = "cdrn", DESC = "desc";
   static Map<String, String> settings = new java.util.HashMap<>();

   public static String getPath() {
      return mkRealPath(objDir);
   }

   /**
    * @param key tag of the setting
    * @return the value of the given key or {@code null} if settings doesn't
    * contain that key
    * @see Map#get(java.lang.Object)
    */
   public static String getSetting(String key) {
      return settings.get(key);
   }

   /**
    * @param key tag to be added or overrided in the settings of this program
    * @param value value associated with the given tag
    * @see Map#put(java.lang.Object, java.lang.Object)
    */
   public static void putSetting(String key, String value) {
      settings.put(key, value);
      deserializeTo("settings.dat", settings);
   }

   /**
    * Removes the given key from the {@link Formatter#settings}.
    *
    * @param key the tag that will be removed
    */
   public static void removeSetting(String key) {
      settings.remove(key);
      deserializeTo("settings.dat", settings);
   }

   /**
    * This method changes the directory of saves of hierarchies.
    *
    * @param path the directory for this application
    */
   public static void changeDir(String path) {
      if (path.charAt(0) == System.getProperty("user.dir").charAt(0)) {
         path = path.replaceFirst(System.getProperty("user.dir").charAt(0) + ":", "§");
      }
      objDir = ("".equals(path) ? "" : path + "\\")
              + (path.contains("School objects") ? "" : "School objects\\");
      putSetting("objdir", objDir);
      createDir(objDir);
   }

   /**
    * Makes a real path.
    *
    * @param path § on index 0 changes to current disc name
    * @return the corrected path
    */
   public static String mkRealPath(String path) {
      return path.charAt(0) == '§' ? path.replaceFirst("§",
              System.getProperty("user.dir").charAt(0) + ":") : path;
   }

   /**
    * Makes sure the given path is a created directory.
    *
    * @param dirStr § on index 0 changes to current disc name
    * @return the created file
    */
   public static File createDir(String dirStr) {
      dirStr = mkRealPath(dirStr);
      File dir = new File(dirStr);
      if (!dir.exists()) {
         dir.mkdir();
         try {
            dir.createNewFile();
         } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
         }
      }
      return dir;
   }

   public static void deserializeTo(String destination, Object toSave) {
      try (ObjectOutputStream oos = new ObjectOutputStream(
              new java.io.FileOutputStream(destination))) {
         oos.writeObject(toSave);
      } catch (IOException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   public static Object serialize(String filePath) {
      try (ObjectInputStream ois = new ObjectInputStream(
              new java.io.FileInputStream(filePath))) {
         return ois.readObject();
      } catch (IOException | ClassNotFoundException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   public static String loadFile(File toLoad) {
      StringBuilder sb = new StringBuilder();
      try (FileReader fr = new FileReader(toLoad)) {
         char[] buffer = new char[1024];
         int amount;
         while ((amount = fr.read(buffer)) != -1) {
            sb.append(buffer, 0, amount);
         }
      } catch (IOException ex) {
         throw new IllegalArgumentException(ex);
      }
      return sb.toString();
   }

   public static void saveFile(String toSave, File save) {
      try (FileWriter fw = new FileWriter(save)) {
         fw.write(toSave);
      } catch (IOException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   /**
    * Object containing all neccessary data for creating a
    * {@link MainChapter hierarchy} {@link objects.templates.BasicData element}.
    */
   public static class Data {

      public String name;
      public MainChapter identifier;
      public int[] sf;
      public String description = "";
      public Object[] tagVals;
      public Container par;

      Data(String name, MainChapter identifier, int s, int f, String description, Container parent, Object... tagValues) {
         this(name, identifier, s, f, description, parent);
         tagVals = tagValues;
      }

      public Data(String name, MainChapter identifier, int s, int f, String description, Container parent) {
         this(name, identifier, s, f, description);
         par = parent;
      }

      public Data(String name, MainChapter identifier, String description, Container parent) {
         this(name, identifier, description);
         par = parent;
      }

      public Data(String name, MainChapter identifier, int s, int f, Container parent) {
         this(name, identifier, s, f);
         par = parent;
      }

      public Data(String name, MainChapter identifier, int s, int f, String description) {
         this(name, identifier, description);
         this.sf = new int[]{s, f};
      }

      public Data(String name, MainChapter identifier, int s, int f) {
         this(name, identifier);
         this.sf = new int[]{s, f};
      }

      public Data(String name, MainChapter identifier, String description) {
         this(name, identifier);
         this.description = description == null ? "" : description;
      }

      public Data(String name, MainChapter identifier, Container parent) {
         this(name, identifier);
         par = parent;
      }

      public Data(String name, MainChapter identifier) {
         this.name = name;
         this.identifier = identifier;
      }

      @Override
      public String toString() {
         return name;
      }
   }
}
