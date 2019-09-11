/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import IOSystem.Formater;
import static IOSystem.Formater.*;
import static IOSystem.Formater.ReadChildren.*;
import static IOSystem.Formater.WriteChildren.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Josef Litoš
 */
public class MainChapter extends SaveChapter {

   public static final Set<MainChapter> ELEMENTS = new HashSet<>();
   protected final List<SaveChapter> children = new ArrayList<>();

   public final File dir;
   String pictures;

   /**
    *
    * @param name name of the hierarchy
    * @param sf the number of successes and fails for this hierarchy
    */
   public MainChapter(String name, int[] sf) throws IOException {
      super(name, IOSystem.Formater.getPath() + name + '\\' + name + ".json", sf);
      ELEMENTS.add(this);
      dir = createDir(IOSystem.Formater.getPath() + name);
      createDir(dir.getPath() + "\\Pictures");
      createDir(dir.getPath() + "\\Chapters");
      File pics = new File(dir + "\\pictures.json");
      pictures = (pics.exists() ? loadFile(pics) : "");
   }

   static void n() {
   }

   @Override
   public void destroy(Chapter parent) {
      if (parent != null) {
         throw new IllegalArgumentException("This chapter can't have a parent, but got: " + parent);
      }
      ELEMENTS.remove(this);
      dir.delete();
   }

   @Override
   public SaveChapter[] getChildren() {
      return children.toArray(new SaveChapter[children.size()]);
   }

   /**
    * This methos saves this MainChapter and then removes itself from the list.
    */
   public void close() throws IOException {
      Formater.save(this);
      ELEMENTS.remove(this);
   }

   @Override
   public StringBuilder writeChildren(StringBuilder sb, int tabs, Element cp) {
      try {
         saveFile(pictures, new File(dir + "\\pictures.json"));
      } catch (IOException ex) {
         throw new IllegalArgumentException("Soemthing has gone wrong:\n" + ex.getMessage());
      }
      boolean first = true;
      for (SaveChapter sch : children) {
         if (!sch.loaded || sch.children.isEmpty()) {
            continue;
         }
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }
         tabs(sb, tabs, "{ \"").append(NAME).append("\": \"").append(mkSafe(sch)).append("\", \"")
                 .append(SUCCESS).append("\": ").append(sch.getSuccess()).append(", \"")
                 .append(FAIL).append("\": ").append(sch.getFail()).append(" }");
      }
      return sb;
   }

   public static void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) throws IOException {
      MainChapter mch = new MainChapter(name, sf);
      mch.description = desc;
      List<String> names = new ArrayList<>();
      List<int[]> sfs = new LinkedList<>();
      try {
         while (dumpSpace(s, '{', ' ', ',', '\n', '\t')) {
            String sch = null;
            int[] schsf = new int[2];
            for (byte i = 0; i < 3; i++) {
               switch (next(s, '"', '"', ' ', ',')) {
                  case NAME:
                     sch = next(s, '"', '"', ' ', ':');
                     break;
                  case SUCCESS:
                     schsf[0] = Integer.parseInt(next(s, ' ', ',', ':'));
                     break;
                  case FAIL:
                     schsf[1] = Integer.parseInt(next(s, ' ', ' ', ':'));
                     break;
                  default:
                     throw new IllegalArgumentException("Unknown field, char num: " + START);
               }
            }
            dumpSpace(s, '}', ' ');
            names.add(sch);
            sfs.add(schsf);
         }
      } catch (IllegalArgumentException iae) {
         if (!iae.getMessage().contains("']'")) {
            throw iae;
         }
      }
      for (int i = 0; i < sfs.size(); i++) {
         SaveChapter.mkElement(names.get(i), mch, sfs.get(i)).loaded = false;
      }
   }
}
