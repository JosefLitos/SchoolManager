/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formater.WriteChildren.tabs;
import static IOSystem.Formater.*;
import static IOSystem.Formater.ReadChildren.dumpSpace;
import static IOSystem.Formater.ReadChildren.next;
import static IOSystem.Formater.WriteChildren.mkSafe;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author InvisibleManCZ
 */
public class Reference extends Element {

   public static final Map<MainChapter, List<Reference>> ELEMENTS = new HashMap<>();

   public final String originalContainer;
   public final Element reference;
   int parentCount;

   public static final Reference mkElement(Element reference, MainChapter identifier, Chapter parent, String origin) {
      if (ELEMENTS.get(identifier) == null) {
         ELEMENTS.put(identifier, new ArrayList<>());
      }
      for (Reference r : ELEMENTS.get(identifier)) {
         if (reference == r.reference) {
            r.parentCount++;
            parent.children.add(r);
         }
         return r;
      }
      return new Reference(reference, identifier, parent, origin);
   }

   private Reference(Element reference, MainChapter identifier, Chapter parent, String origin) {
      super(reference.toString(), identifier, reference.sf);
      this.reference = reference;
      parent.children.add(this);
      originalContainer = origin;
      parentCount = 1;
      ELEMENTS.get(identifier).add(this);
   }

   @Override
   public void destroy(Chapter parent) {
      parent.children.remove(this);
      if (--parentCount == 0) {
         ELEMENTS.get(identifier).remove(this);
      }
   }

   @Override
   public Element[] getChildren() {
      return new Element[]{reference};
   }

   @Override
   public StringBuilder writeChildren(StringBuilder sb, int tabs, Element cp) {
      tabs(sb, tabs, "{ \"").append(CLASS).append("\": \"").append(
              reference.getClass().getName()).append("\", \"").append(NAME)
              .append("\": \"").append(mkSafe(reference)).append("\", \"origin\": \"")
              .append(mkSafe(originalContainer)).append("\" }");
      return sb;
   }

   public static void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) throws IOException {
      String[] info = new String[3];
      String holder;
      dumpSpace(s, '{', ' ', '\n', '\t');
      try {
         try {
            while (!(holder = next(s, '"', '"', ' ', ',')).contains("}")) {
               switch (holder) {
                  case CLASS:
                     info[0] = next(s, '"', '"', ':', ' ');
                     break;
                  case NAME:
                     info[1] = next(s, '"', '"', ':', ' ');
                     break;
                  case "origin":
                     info[2] = next(s, '"', '"', ':', ' ');
                     break;
                  default:
                     throw new IllegalArgumentException("Unknown field while getting value for " + holder + ", char num: " + START);
               }
            }
         } catch (IllegalArgumentException iae) {
            if (!iae.getMessage().contains("'}'")) {
               throw iae;
            }
         }
         boolean load = true;
         for (SaveChapter sch : SaveChapter.ELEMENTS.get(identifier)) {
            if (info[2].equals(sch.toString()) && sch.loaded) {
               load = false;
               break;
            }
         }
         if (load) {
            int pauseAt = START;
            loadSCh(new File(identifier.dir + "\\Chapters\\" + info[2] + ".json"), identifier);
            START = pauseAt;
         }
         next(s, '"', '"', ' ', '\n', '\t');
      } catch (IllegalArgumentException iae) {
         if (!iae.getMessage().contains("']'")) {
            throw iae;
         }
      }
      Map<MainChapter, List<Element>> elements = null;
      try {
         elements = (Map<MainChapter, List<Element>>) Class.forName(info[0]).getDeclaredField("ELEMENTS").get(null);
      } catch (ClassNotFoundException | NoSuchFieldException | SecurityException
              | IllegalArgumentException | IllegalAccessException ex) {
         Logger.getLogger(Reference.class.getName()).log(Level.SEVERE, null, ex);
      }
      for (Element e : elements.get(identifier)) {
         if (info[1].equals(e.toString())) {
            mkElement(e, identifier, parent, info[2]);
            return;
         }
      }
      throw new IllegalArgumentException("Reference to " + name + " of type " + info[0] + "doesn't exist in file " + info[2]);
   }

}
