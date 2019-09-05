/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formater.ReadChildren.dumpSpace;
import static IOSystem.Formater.ReadChildren.read;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author InvisibleManCZ
 */
public class SaveChapter extends Chapter {

   public static final Map<MainChapter, List<SaveChapter>> ELEMENTS = new HashMap<>();

   public boolean loaded = true;
   public final String save;

   public static final SaveChapter mkElement(String name, MainChapter identifier, int[] sf) {
      if (ELEMENTS.get(identifier) == null) {
         ELEMENTS.put(identifier, new LinkedList<>());
      }
      for (SaveChapter sch : ELEMENTS.get(identifier)) {
         if (name.equals(sch.toString())) {
            sch.loaded = true;
            return sch;
         }
      }
      return new SaveChapter(name, identifier, sf);
   }

   private SaveChapter(String name, MainChapter identifier, int[] sf) {
      super(name, identifier, sf);
      ELEMENTS.get(identifier).add(this);
      identifier.children.add(this);
      save = identifier.dir.getAbsolutePath() + "\\Chapters\\" + name + ".json";
   }

   /**
    * This constructor is only for MainChapter
    *
    * @param name name of the hierarchy
    * @param save the save-file path
    * @param sf the number of successes and fails for this hierarchy
    */
   SaveChapter(String name, String save, int[] sf) {
      super(name, null, sf);
      this.save = save;
   }

   @Override
   public void destroy(Chapter parent) {
      if (parent != null && parent != identifier) {
         throw new IllegalArgumentException("This chapter can't have different parent than identifier, but got: " + parent);
      }
      identifier.children.remove(this);
      ELEMENTS.get(identifier).remove(this);
      new File(save).delete();
   }

   public static void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) throws IOException {
      SaveChapter ch = mkElement(name, identifier, sf);
      ch.description = desc;
      try {
         while (dumpSpace(s, '{', ' ', ',', '\n', '\t')) {
            read(s, ch, identifier);
         }
      } catch (IllegalArgumentException iae) {
         if (!iae.getMessage().contains("']'")) {
            throw iae;
         }
      }
   }
}
