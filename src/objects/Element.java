/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import IOSystem.Formater;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import IOSystem.Formater.WriteChildren;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Josef Litoš
 */
public abstract class Element implements WriteChildren {

   /**
    * This variable contains every instance of the class it is inside sorted
    * under MainChapters as their identifiers.
    */
   public static final Map<MainChapter, List<Element>> ELEMENTS = new HashMap<>();

   /**
    * Success and Fail
    */
   protected int[] sf = {0, 0};
   public final MainChapter identifier;
   public String name;
   public String description = "";

   public int getSuccess() {
      return sf[0];
   }

   public int getFail() {
      return sf[1];
   }

   protected Element(String name, MainChapter identifier, int[] sucfail) {
      this.name = name;
      this.identifier = identifier;
      if (sucfail != null) {
         sf = sucfail;
      }
   }

   /**
    *
    * @param path the path to the Word with translate or Picture
    * @param did <code>true</code> if the user matched correctly
    */
   public static final void success(List<Element> path, boolean did) {
      for (Element e : path) {
         e.sf[did ? 0 : 1]++;
      }
   }

   /**
    *
    * @return % value of success in matching the {@link #children children}
    */
   public float success() {
      return sf[0] / (sf[0] + sf[1]) * 100;
   }

   /**
    * Removes itself from the specified Chapter and from the net.
    *
    * @param parent the Chapter which this Element is being removed from
    */
   abstract public void destroy(Chapter parent);

   /**
    *
    * @return Array of children in the specified Element
    */
   abstract public Element[] getChildren();

   @Override
   public String toString() {
      return name;
   }

   public static void main(String[] args) throws IOException, InterruptedException {
      Formater.loadSettings();
      //Formater.changeDir(new File(""));
      List<Element> e = new ArrayList<>();
      e.add(new MainChapter("test", new int[]{2, 3}));
      e.add(SaveChapter.mkElement("8.5.", (MainChapter) e.get(0), new int[]{4, 5}));
      e.add(new Chapter("Society", (Chapter) e.get(1), (MainChapter) e.get(0), new int[]{6, 7}));
      Word.mkElement("hard", Arrays.asList("těžk\\ý\\á\\é", "těžce"),
              (Chapter) e.get(1), (MainChapter) e.get(0), null, null);
      Word.mkElement("hardly", Arrays.asList("sotva", "stěží"),
              (Chapter) e.get(1), (MainChapter) e.get(0), null, null);
      Word.mkElement("hard", Arrays.asList("tvrd/ý/á/é/ě", "náročn\"/ý/á/é/ě"),
              (Chapter) e.get(2), (MainChapter) e.get(0), null, new int[]{1, 5}).children.get((Chapter) e.get(2))[1].description = "test2";
      e.add(Picture.mkElement("broskvoň", Arrays.asList(new File("D:\\asdf.jpg"),
              new File("D:\\_vyr_1013boskvon-Catherina.jpg")),
              (Chapter) e.get(2), (MainChapter) e.get(0), null, new int[]{7,4}, true));
      e.add(SaveChapter.mkElement("6.8.", (MainChapter) e.get(0), new int[]{4, 5}));
      Picture.mkElement("broskvoň", Arrays.asList(new File("D:\\Poznávačka\\k poznání\\broskvoň 1.jpg"),
              new File("D:\\Poznávačka\\k poznání\\broskvoň 2.jpg"),
              new File("D:\\Poznávačka\\k poznání\\broskvoň 3.jpg")),
              (Chapter) e.get(1), (MainChapter) e.get(0), null, new int[]{1, 5}, true).description = "biologie";
      ((Picture) e.get(3)).children.get((Chapter) e.get(2))[1].description = "test";
      Reference.mkElement(e.get(3), (MainChapter) e.get(0), SaveChapter.mkElement("reftest", (MainChapter) e.get(0), null), "8.5.");
      ((Picture) e.get(3)).removeChild(((Picture) e.get(3)).children.get((Chapter) e.get(2))[1], (Chapter) e.get(2));
      //((SaveChapter)e.get(1)).destroy(null);
      Formater.saveAll((MainChapter) e.get(0));
   }

   public static void readChildren(String s, String name, Chapter parent, MainChapter identifier, int[] sf, String desc) throws IOException {
      throw new UnsupportedOperationException("Can be called only in non-abstract objects");
   }
}
