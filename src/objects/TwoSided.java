/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formater.*;
import static IOSystem.Formater.ReadChildren.dumpSpace;
import static IOSystem.Formater.ReadChildren.next;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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

   @Override
   public T[] getChildren() {
      List<T> chdrn = new ArrayList<>();
      children.values().forEach((t) -> chdrn.addAll(Arrays.asList(t)));
      return chdrn.toArray(mkArray(chdrn.size()));
   }

   abstract T[] mkArray(int size);

   public void removeChild(T child, Chapter parent) {
      if (isMain) {
         child.destroy(parent);
         remove(parent, child);
      } else {
         throw new IllegalArgumentException("Child can be removed only by main Picture");
      }
   }

   abstract void remove(Chapter parent, T child);

   @Override
   public StringBuilder write(int tabs, Element cp) {
      tabs(tabs++, "{ ").add(this, true, true, true, true, true);
      boolean first = true;
      for (T e : children.get((Chapter) cp)) {
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }
         tabs(tabs, "{ ").add(e, false, true, true, true, false).append(" }");
      }
      return sb.append(" ] }");
   }

   public static List[] readChildren(String s) {
      List<String> translates = new ArrayList<>();
      List<String> descs = new ArrayList<>();
      List<int[]> sfs = new LinkedList<>();
      try {
         while (dumpSpace(s, '{', ' ', ',', '\n', '\t')) {
            String info[] = new String[2];
            int[] esf = new int[2];
            String holder;
            try {
               while (!(holder = next(s, '"', '"', ' ', ',')).contains("'}'")) {
                  switch (holder) {
                     case NAME:
                        info[0] = next(s, '"', '"', ' ', ':');
                        break;
                     case SUCCESS:
                        esf[0] = Integer.parseInt(next(s, ' ', ',', ':'));
                        break;
                     case FAIL:
                        esf[1] = Integer.parseInt(next(s, ' ', ' ', ':'));
                        break;
                     case DESC:
                        info[1] = next(s, '"', '"', ':', ' ');
                        break;
                     default:
                        throw new IllegalArgumentException("Unknown field while getting value for "
                                + holder + ", char num: " + START);
                  }
               }
            } catch (IllegalArgumentException iae) {
               if (!iae.getMessage().contains("'}'")) {
                  throw iae;
               }
            }
            translates.add(info[0]);
            descs.add(info[1]);
            sfs.add(esf);
         }
      } catch (IllegalArgumentException iae) {
         if (!iae.getMessage().contains("']'")) {
            throw iae;
         }
      }
      return new List[]{translates, descs, sfs};
   }
}
