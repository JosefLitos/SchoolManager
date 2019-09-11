package objects;

import static IOSystem.Formater.*;
import static IOSystem.Formater.ReadChildren.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Josef Litoš
 */
public class Word extends TwoSided<Word> {

   public static final Map<MainChapter, List<Word>> TRANSLATES = new HashMap<>();
   public static final Map<MainChapter, List<Word>> ELEMENTS = new HashMap<>();

   /**
    * The only allowed way to create Word objects. Automaticaly controls its
    * existence and returns the proper Word.
    *
    * @param name name of the currently being created word
    * @param translates translates for this object from one badge
    * @param parent the Chapter which this Word belongs to
    * @param identifier the file containing this word
    * @param sfs the successes and fails for every translate
    * @param wSF the number of successes and fails for this word
    * @return nes
    * {@linkplain #Word(java.lang.String, objects.Chapter, objects.MainChapter, int, int) Word object}
    * if the word doesn't exist yet, otherwise returns the word object with the
    * same name and adds the new translations.
    */
   public static final Word mkElement(String name, List<String> translates, Chapter parent, MainChapter identifier, List<int[]> sfs, int[] wSF) {
      if (ELEMENTS.get(identifier) == null) {
         ELEMENTS.put(identifier, new ArrayList<>());
         TRANSLATES.put(identifier, new ArrayList<>());
      }
      for (Word w : ELEMENTS.get(identifier)) {
         if (name.equals(w.toString())) {
            w.parentCount++;
            w.addTranslates(translates, parent, identifier, sfs);
            parent.children.add(w);
            return w;
         }
      }
      return new Word(name, translates, parent, identifier, sfs, wSF);
   }

   /**
    * This constructor is used only to create translates.
    */
   private Word(String name, Word word, Chapter parent, MainChapter identifier, int[] sf) {
      super(name, identifier, sf, false, TRANSLATES);
      children.put(parent, new Word[]{word});
   }

   /**
    * This constructor is used only to create Words.
    */
   private Word(String name, List<String> translates, Chapter parent, MainChapter identifier, List<int[]> sfs, int[] tSF) {
      super(name, identifier, tSF, true, ELEMENTS);
      addTranslates(translates, parent, identifier, sfs);
      parent.children.add(this);
   }

   /**
    * Checks for potencial doubling of translates.
    *
    * @param translates names of translates for this word in the specified
    * Chapter
    * @param parent Chapter containing this word
    * @param sfs number of successes and fails for each of the translates
    */
   private void addTranslates(List<String> translates, Chapter parent, MainChapter identifier, List<int[]> sfs) {
      Set<Integer> found = new HashSet<>();
      if (children.get(parent) == null) {
         children.put(parent, new Word[0]);
      }
      Word[] trls = Arrays.copyOf(children.get(parent), translates.size() + children.get(parent).length);
      TRANSLATES.get(identifier).forEach((t) -> {
         for (int j = translates.size() - 1; j >= 0; j--) {
            condition:
            if (translates.get(j).equals(t.toString())) {
               for (Integer i : found) {
                  if (i == j) {
                     break condition;
                  }
               }
               t.parentCount++;
               Word[] chldrn = Arrays.copyOf(t.children.get(parent), 1 + t.children.get(parent).length);
               chldrn[chldrn.length-1] = this;
               t.children.put(parent, chldrn);
               trls[j] = t;
               found.add(j);
               if (translates.size() == found.size()) {
                  return;
               }
            }
         }
      });
      for (int j = translates.size() - 1; j >= 0; j--) {
         control:
         {
            for (Integer i : found) {
               if (i <= j) {
                  found.remove(i);
                  if (i == j) {
                     break control;
                  }
               }
            }
            trls[j] = new Word(translates.get(j), this, parent, identifier, (sfs == null ? null : sfs.get(j)));
         }
      }
      children.put(parent, trls);
   }

   @Override
   public void destroy(Chapter parent) {
      if (isMain) {
         for (Word t : children.get(parent)) {
            t.resize(parent, this);
            t.destroy(parent);
         }
         children.remove(parent);
         parent.children.remove(this);
         if (--parentCount == 0) {
            ELEMENTS.get(identifier).remove(this);
         }
      } else {
         if (children.get(parent).length == 0 && --parentCount == 0) {
            TRANSLATES.get(identifier).remove(this);
         }
      }
   }

   private void resize(Chapter parent, Word toRem) {
      Word[] prev = children.get(parent);
      Word[] chdrn = new Word[prev.length - 1];
      for (int i = 0; i < chdrn.length; i++) {
         if (prev[i] == toRem) {
            chdrn[i] = prev[chdrn.length];
         } else {
            chdrn[i] = prev[i];
         }
      }
      children.put(parent, chdrn);
   }

   @Override
   public void removeChild(Word child, Chapter parent) {
      if (isMain) {
         child.destroy(parent);
         resize(parent, child);
      } else {
         throw new IllegalArgumentException("Child can be removed only by main Picture");
      }
   }

   @Override
   public Word[] getChildren() {
      List<Word> chdrn = new ArrayList<>();
      children.values().forEach((t) -> chdrn.addAll(Arrays.asList(t)));
      return chdrn.toArray(new Word[chdrn.size()]);
   }

   public static void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) throws IOException {
      List<String> translates = new ArrayList<>();
      List<String> descs = new ArrayList<>();
      List<int[]> sfs = new LinkedList<>();
      try {
         while (dumpSpace(s, '{', ' ', ',', '\n', '\t')) {
            String info[] = new String[2];
            int[] tsf = new int[2];
            String holder;
            try {
               while (!(holder = next(s, '"', '"', ' ', ',')).contains("'}'")) {
                  switch (holder) {
                     case NAME:
                        info[0] = next(s, '"', '"', ' ', ':');
                        break;
                     case SUCCESS:
                        tsf[0] = Integer.parseInt(next(s, ' ', ',', ':'));
                        break;
                     case FAIL:
                        tsf[1] = Integer.parseInt(next(s, ' ', ' ', ':'));
                        break;
                     case DESC:
                        info[1] = next(s, '"', '"', ':', ' ');
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
            translates.add(info[0]);
            descs.add(info[1]);
            sfs.add(tsf);
         }
      } catch (IllegalArgumentException iae) {
         if (!iae.getMessage().contains("']'")) {
            throw iae;
         }
      }
      mkElement(name, translates, parent, identifier, sfs, sf).description = desc;
      for (int i = translates.size() - 1; i >= 0; i--) {
         if (descs.get(i) == null) {
            translates.remove(i);
            descs.remove(i);
         }
      }
      TRANSLATES.get(identifier).forEach((t) -> {
         for (int i = translates.size() - 1; i >= 0; i--) {
            if (translates.get(i).equals(t.toString())) {
               t.description = descs.get(i);
            }
         }
      });
   }
}