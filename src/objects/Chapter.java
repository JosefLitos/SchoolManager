/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formater.ReadChildren.dumpSpace;
import static IOSystem.Formater.ReadChildren.read;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Josef Litoš
 */
public class Chapter extends Element {

   public static final Map<MainChapter, List<Chapter>> ELEMENTS = new HashMap<>();

   protected final List<Element> children = new ArrayList<>();

   /**
    *
    * @param name name of the chapter
    * @param parent the parent of this Chapter
    * @param identifier main object of this hierarchy and parent of this object
    * @param sf the number of successes and fails in this chapter
    */
   public Chapter(String name, Chapter parent, MainChapter identifier, int[] sf) {
      super(name, identifier, sf);
      parent.children.add(this);
      if (ELEMENTS.get(identifier) == null) {
         ELEMENTS.put(identifier, new LinkedList<>());
      }
      ELEMENTS.get(identifier).add(this);
   }

   protected Chapter(String name, MainChapter identifier, int[] sf) {
      super(name, identifier, sf);
   }

   public boolean hasChild() {
      return !children.isEmpty();
   }

   @Override
   public void destroy(Chapter parent) {
      parent.children.remove(this);
      children.forEach((E) -> {
         E.destroy(this);
      });
      ELEMENTS.get(identifier).remove(this);
   }

   @Override
   public Element[] getChildren() {
      return children.toArray(new Element[children.size()]);
   }

   @Override
   public StringBuilder write(int tabs, Element currentParent) {
      tabs(tabs++, "{ ").add(this, true, true, true, true, true);
      boolean first = true;
      for (Element e : children) {
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }
         e.write(tabs, this);
      }
      return sb.append(" ] }");
   }

   public static void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) {
      Chapter ch = new Chapter(name, parent, identifier, sf);
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
