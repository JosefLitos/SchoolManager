package objects;

import java.util.Arrays;
import IOSystem.Formatter;
import IOSystem.Formatter.Data;
import IOSystem.ReadElement;
import static IOSystem.SimpleReader.simpleLoad;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import objects.templates.BasicData;
import objects.templates.Container;
import testing.NameReader;
import testing.Test;

/**
 * Class containing all type of tests used to test the program functionality.
 * Most of the tests use data on the disc the project is in.
 *
 * @author Josef Litoš
 */
public class TestingClass {

   static char D = System.getProperty("user.dir").charAt(0);

   public static void main(String[] args) {
      Formatter.loadSettings();
      createTestingDataBase(new MainChapter(new Data("try", null, 2, 3)));
      createRandomSF(ReadElement.loadMch(new File(Formatter.getPath() + "NJ")));
      testNameReader();
      testTest(ReadElement.loadMch(new File(Formatter.getPath() + "NJ")));
      seeWordContent(ReadElement.loadMch(new File(Formatter.getPath() + "NJ")));
      testSimpleReader(new MainChapter(new Data("NJ", null)));
   }

   static <T> LinkedList<T> list(T... d) {
      LinkedList<T> l = new LinkedList<>();
      l.addAll(Arrays.asList(d));
      return l;
   }

   public static BasicData mkE(Container par, Container parPar, BasicData src) {
      if (!par.hasChild(parPar, src)) {
         par.putChild(parPar, src);
      }
      return src;
   }

   static void createTestingDataBase(MainChapter mch) {
      String imgs = "děložné rostliny\\";
      SaveChapter sch1 = (SaveChapter) mkE(mch, null, SaveChapter.mkElement(new Data("8.5.", mch, "test", mch)));
      Chapter ch1 = (Chapter) mkE(sch1, mch, new Chapter(new Data("Society", mch, 6, 7, "", sch1)));
      mkE(sch1, mch, Word.mkElement(new Data("hard", mch, sch1), list(new Data("těžk/ý/á/é", mch, 1, 5, sch1), new Data("těžce", mch, sch1))));
      mkE(sch1, mch, Word.mkElement(new Data("hardly", mch, sch1), list(new Data("sotva", mch, sch1), new Data("stěží", mch, sch1))));
      mkE(ch1, sch1, Word.mkElement(new Data("hard", mch, ch1), list(new Data("sotva", mch, ch1), new Data("náročn\"/ý/á/é/ě", mch, ch1))));
      mkE(sch1, mch, Word.mkElement(new Data("hardly", mch, sch1), list(new Data("test1", mch, sch1), new Data("test2", mch, sch1))));
      Chapter ch2 = (Chapter) mkE(sch1, mch, new Chapter(new Data("noShow", mch, sch1)));
      Picture pic = (Picture) mkE(ch2, sch1, Picture.mkElement(new Data("bříza", mch, "biologie", ch2), list(new Data(imgs + "bříza bělokorá 5.jpg", mch, ch2),
              new Data(imgs + "bříza bělokorá 4.jpg", mch, ch2))));
      mkE(mch, null, SaveChapter.mkElement(new Data("6.8.", mch, 4, 5, mch)));
      Picture pic2 = (Picture) mkE(sch1, mch, Picture.mkElement(new Data("bříza bělokorá", mch, sch1), list(new Data(imgs + "bříza bělokorá 2.jpg", mch, sch1),
              new Data(imgs + "bříza bělokorá 3.jpg", mch, sch1)), true));
      SaveChapter sch3 = (SaveChapter) mkE(mch, null, SaveChapter.mkElement(new Data("reftest", mch, mch)));
      mkE(sch3, mch, Reference.mkElement(pic, list(mch, sch3), new Container[]{mch, sch1, ch2})).isEmpty(pic);
      mkE(sch1, mch, Picture.mkElement(new Data("bříza", mch, sch1), list(new Data(imgs + "bříza bělokorá 1.jpg", mch, sch1))));
      pic2.setName("bříza");
      pic.removeChild(ch2, pic.children.get(ch2).get(1));
      pic2.setName("nothing");
      pic.removeChild(ch2, pic.children.get(ch2).get(0));
      Picture.clean(mch);
      if (!mch.setName("finalTouches")) {
         mch.save();
      }
   }

   static void createRandomSF(MainChapter mch) {
      mch.load();
      SaveChapter.ELEMENTS.get(mch).forEach((sch) -> rnd(sch));
      mch.save();
   }
   static Random rnd = new Random();

   static void rnd(Container parent) {
      for (BasicData bd : parent.getChildren()) {
         if (bd instanceof Container) {
            if (bd instanceof Word) {
               for (int i = rnd.nextInt(7); i > 0; i--) {
                  bd.addSF(rnd.nextBoolean());
               }
//               bd.addSF(true);
            } else {
               rnd((Container) bd);
            }
         }
      }
   }

   static void testNameReader() {
      sA("They/(He and she) moved.");
      sA("/I/You smile.");
      sA("(I/You smile.)/(Smile!)");
      sA("I 'm/am here/there.");
      sA("(We/(May you) 're)/He's /out.");
      sA("(high/free)way");
      sA("Sie geht mir auf (die Nerven)/(den Wecker/Keks/Sack).");
   }

   static void sA(String str) {
      System.out.println(Arrays.toString(NameReader.readName(str)));
   }

   static void testTest(MainChapter mch) {
      Test.getDefaultTime();
      mch.load();
      Test<Word> t = new Test<>(Word.class);
      List<Test.SrcPath<Word>> x;
      List<Container> path = new LinkedList<>();
      path.add(mch);
      path.add((SaveChapter) mch.getChildren()[0]);
      x = t.getContent(path);
      path.clear();
      path.add(mch);
      path.add((Container) mch.getChildren()[1]);
      path.add((Chapter) ((SaveChapter) mch.getChildren()[1]).getChildren()[2]);
      x.addAll(t.getContent(path));
      Test.setClever(true);
      t.setTested(10, null, Test.getDefaultTime(), x);
      System.out.println(t.getTestSrc());
   }

   static void seeWordContent(MainChapter mch) {
      mch.load();
      for (BasicData sch : mch.getChildren()) {
         int i = 0;
         for (BasicData ch : ((Container) sch).getChildren()) {
            i += ((Container) ch).getChildren().length;
            System.out.println(" " + ch.toString() + "\t" + ((Container) ch).getChildren().length + "\t" + i);
         }
         System.out.println(sch + "\t" + i + '\n');
      }
      mch.save();
   }

   static void testSimpleReader(MainChapter mch) {
//      System.out.println(Arrays.toString(sortLoad(new File(D + ":\\skola\\" + mch + ".txt"), mch, null, 0, -1, 95, 1, new String[]{"", ". 100"}, true)));
      System.out.println(Arrays.toString(simpleLoad(new File(D + ":\\skola\\" + mch + ".txt"), mch, null, 0, -1, -1, true)));
      mch.save();
   }
}
