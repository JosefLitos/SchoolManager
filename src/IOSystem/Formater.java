/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IOSystem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.Chapter;
import objects.Element;
import objects.MainChapter;
import objects.SaveChapter;

/**
 * This class is used to write and get MainChapters and their content. The
 * format of the saved files is to be same as json, but the code is all thought
 * and writen by the author of this project. Noones code has been used to write
 * this or even think how to do it. No code inspiration has been taken from
 * anyone.
 *
 * @author Josef Litoš
 */
public class Formater {

   static String objDir;
   static final String DIR = "SchlMgr\\";
   public static final String CLASS = "class", NAME = "name", SUCCESS = "s", FAIL = "f", CHILDREN = "cdrn", DESC = "desc";
   static String settings;

   public static String getPath() {
      return objDir;
   }

   public static String getSettings() {
      return settings;
   }

   public static void setSettings(String newSetts) throws IOException {
      saveFile(settings = newSetts, new File(DIR + "options.txt"));
   }

   /**
    * This method changes the directory of saves of hierarchies.
    *
    * @param path the directory for this application
    * @throws IOException If an I/O error occured
    */
   public static void changeDir(File path) throws IOException {
      String s = ("".equals(path.getPath()) ? "" : path.getPath() + "\\")
              + (path.getName().equals("School objects") ? "" : "School objects\\");
      saveFile(settings = settings.replace("objdir:" + objDir, "objdir:" + s), new File(DIR + "options.txt"));
      setDir(s);
   }

   private static void setDir(String path) throws IOException {
      createDir(objDir = path);
   }

   /**
    * This method has to be called at the start of the program.
    *
    * @throws IOException if an i/o error occures
    */
   public static void loadSettings() throws IOException {
      createDir(DIR);
      File setts = new File(DIR + "options.txt");
      if (setts.exists()) {
         settings = loadFile(setts);
         setDir(getData(settings, "objdir:"));
      } else {
         setDir(DIR + "School objects\\");
         saveFile(settings = new StringBuilder().append("objdir:").append(objDir).append("\nlanguage:cz\n").toString(), new File(DIR + "options.txt"));
      }
   }

   public static String getData(String source, String objective) {
      return getData(source, objective, "\n");
   }

   public static String getData(String source, String objective, String end) {
      return source.split(objective)[1].split(end)[0];
   }

   public static String loadFile(File toLoad) throws IOException {
      StringBuilder sb = new StringBuilder();
      try (FileReader fr = new FileReader(toLoad)) {
         char[] buffer = new char[1024];
         int amount;
         while ((amount = fr.read(buffer)) != -1) {
            sb.append(buffer, 0, amount);
         }
      }
      return sb.toString();
   }

   public static void saveFile(String toSave, File save) throws IOException {
      try (FileWriter fw = new FileWriter(save)) {
         fw.write(toSave);
      }
   }

   public static File createDir(String dirStr) throws IOException {
      File dir = new File(dirStr);
      if (!dir.exists()) {
         dir.mkdir();
         dir.createNewFile();
      }
      return dir;
   }

   /**
    *
    * @param toSave MainChapter that you want to save to file
    * @throws IOException If an I/O error occurs
    */
   public static void save(SaveChapter toSave) throws IOException {
      saveFile(WriteChildren.write(new StringBuilder(), 0, toSave, null).toString(), new File(toSave.save));
   }

   public static void saveAll(MainChapter toSave) throws IOException {
      save(toSave);
      for (SaveChapter sch : toSave.getChildren()) {
         if (sch.loaded && sch.hasChild()) {
            save(sch);
         }
      }
   }

   public interface WriteChildren {

      /**
       *
       * @param sb the textfield, where everything is written
       * @param tabs the spaces from start of the line
       * @param e the currently written element
       * @param cp current parent of the written object
       * @return the written form of this object
       */
      static StringBuilder write(StringBuilder sb, int tabs, Element e, Element cp) {
         tabs(sb, tabs, "{ \"").append(CLASS).append("\": \"").append(e.getClass()
                 .getName()).append("\", \"").append(NAME)
                 .append("\": \"").append(mkSafe(e)).append("\", \"").append(SUCCESS)
                 .append("\": ").append(e.getSuccess()).append(", \"").append(FAIL)
                 .append("\": ").append(e.getFail()).append(", \"");
         if (!e.description.equals("")) {
            sb.append(DESC).append("\": \"").append(mkSafe(e.description)).append("\", \"");
         }
         sb.append(CHILDREN).append("\": [");
         e.writeChildren(sb, ++tabs, cp);
         return sb.append(" ] }");
      }

      static String mkSafe(Object obj) {
         return obj.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
      }

      static StringBuilder tabs(StringBuilder sb, int tabs, String toWrite) {
         sb.append('\n');
         for (int i = tabs; i > 0; i--) {
            sb.append('\t');
         }
         return sb.append(toWrite);
      }

      /**
       * This method is for Formater class to write Element's children, for
       * different implementations of Element class can occure different ways of
       * writing
       *
       * @param sb object containing the full text
       * @param tabs current amount of spaces on every new line
       * @param currentParent parent of the object providing this method
       * @return the same object as paramter sb
       */
      public StringBuilder writeChildren(StringBuilder sb, int tabs, Element currentParent);

   }

   public static int START;

   public static MainChapter loadMain(File save) throws Exception {
      loadSCh(save, null);
      String name = save.getName().split("\\.")[0];
      for (MainChapter mch : MainChapter.ELEMENTS) {
         if (mch.toString().equals(name)) {
            return mch;
         }
      }
      return null;
   }

   public static void loadSCh(File toLoad, MainChapter identifier) throws IOException {
      String s = loadFile(toLoad);
      START = 0;
      ReadChildren.dumpSpace(s, '{', ' ', '\t', '\n');
      ReadChildren.read(s, null, identifier);
   }

   public interface ReadChildren {

      static void read(String s, Chapter parent, MainChapter identifier) throws IOException {
         String[] info = {null, null, ""};
         int[] sf = new int[2];
         String holder;
         while (!(holder = next(s, '"', '"', ' ', ',')).equals(CHILDREN)) {
            switch (holder) {
               case CLASS:
                  info[0] = next(s, '"', '"', ':', ' ');
                  break;
               case NAME:
                  info[1] = next(s, '"', '"', ':', ' ');
                  break;
               case SUCCESS:
                  sf[0] = Integer.parseInt(next(s, ' ', ',', ':'));
                  break;
               case FAIL:
                  sf[1] = Integer.parseInt(next(s, ' ', ',', ':'));
                  break;
               case DESC:
                  info[2] = next(s, '"', '"', ':', ' ');
                  break;
               default:
                  throw new IllegalArgumentException("Unknown field while getting value for " + holder + ", char num: " + START);
            }
         }
         dumpSpace(s, '[', ' ', ':');
         try {
            Class.forName(info[0]).getDeclaredMethod("readChildren", String.class, String.class, Chapter.class, MainChapter.class, int[].class, String.class).invoke(null, s, info[1], parent, identifier, sf, info[2]);
         } catch (IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException
                 | SecurityException | ClassNotFoundException ex) {
            Logger.getLogger(Formater.class.getName()).log(Level.SEVERE, null, ex);
         }
         dumpSpace(s, '}', ' ');
      }

      public static boolean dumpSpace(String s, char end, char... ignore) {
         char ch;
         boolean ctn = false;
         do {
            synchronized (s) {
               ch = s.charAt(START++);
            }
            for (char c : ignore) {
               if (ctn = (c == ch)) {
                  break;
               }
            }
         } while (ctn);
         if (ch != end) {
            throw new IllegalArgumentException("Unknown field, char  '" + ch + "', num: " + START);
         } else {
            return true;
         }
      }

      public static String next(String s, char prefix, char sufix, char... ignore) {
         StringBuilder sb = new StringBuilder();
         dumpSpace(s, prefix, ignore);
         char ch;
         synchronized (s) {
            while ((ch = s.charAt(START++)) != sufix) {
               if (ch == '\\') {
                  ch = s.charAt(START++);
               }
               sb.append(ch);
            }
         }
         return sb.toString();
      }

      public void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) throws IOException;
   }

   public static void main(String[] args) throws Exception {
      loadSettings();
      MainChapter mch = loadMain(new File(objDir + "test\\test.json"));
      SaveChapter sch = mch.getChildren()[1];
      loadSCh(new File(sch.save), sch.identifier);
      saveAll(mch);
   }
}
