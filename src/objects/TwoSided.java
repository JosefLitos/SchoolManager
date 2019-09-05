/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formater.WriteChildren.tabs;
import static IOSystem.Formater.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author InvisibleManCZ
 * @param <T> object which has two versions, but only one is supposed to
 * manipulate with
 */
public abstract class TwoSided<T extends TwoSided> extends Element {

   protected final Map<Chapter, T[]> children = new HashMap<>();
   protected final boolean isMain;
   protected int parentCount;

   protected TwoSided(String name, MainChapter identifier, int[] sf, boolean isMain, Map<MainChapter, List<T>> NET) {
      super(name, identifier, sf);
      this.isMain = isMain;
      NET.get(identifier).add((T) this);
      parentCount = 1;
   }

   public T[] getChildren(Chapter cdrnHolder) {
      return children.get(cdrnHolder).clone();
   }

   abstract public void removeChild(T child, Chapter parent);

   @Override
   public StringBuilder writeChildren(StringBuilder sb, int tabs, Element cp) {
      boolean first = true;
      for (T e : children.get((Chapter) cp)) {
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }
         tabs(sb, tabs, "{ \"").append(NAME).append("\": \"").append(e.toString()
                 .replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\""))
                 .append("\", \"").append(SUCCESS).append("\": ").append(e.sf[0])
                 .append(", \"").append(FAIL).append("\": ").append(e.sf[1]);
         if (!e.description.equals("")) {
            sb.append(" , \"").append(DESC).append("\": \"").append(e.description
                    .replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"")).append('"');
         }
         sb.append(" }");
      }
      return sb;
   }
}
