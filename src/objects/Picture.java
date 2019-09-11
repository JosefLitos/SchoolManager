/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formater.*;
import static IOSystem.Formater.ReadChildren.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Josef Litoš
 */
public class Picture extends TwoSided<Picture> {

   public static final Map<MainChapter, List<Picture>> IMAGES = new HashMap<>();
   public static final Map<MainChapter, List<Picture>> ELEMENTS = new HashMap<>();

   /**
    * The only allowed way to create Picture objects. Automaticaly controls its
    * existence and returns the proper Picture.
    *
    * @param name name of the currently being created picture
    * @param images the files containing an image each
    * @param parent the Chapter which this Picture belongs to
    * @param identifier the file containing this word
    * @param sfs the successes and fails for every image
    * @param iSF the number of successes and fails for this picture
    * @param isNew if the object to be created is new or just loading hierarchy
    * @return new
    * {@linkplain #Picture(java.lang.String, objects.Chapter, objects.MainChapter, int, int, boolean) Picture object}
    * if the name doesn't exist yet, otherwise returns the picture object with
    * the same name and adds the new images.
    */
   public static final Picture mkElement(String name, List<File> images, Chapter parent,
           MainChapter identifier, List<int[]> sfs, int[] iSF, boolean isNew) throws IOException {
      if (ELEMENTS.get(identifier) == null) {
         ELEMENTS.put(identifier, new ArrayList<>());
         IMAGES.put(identifier, new ArrayList<>());
      }
      for (Picture p : ELEMENTS.get(identifier)) {
         if (name.equals(p.toString())) {
            condition:
            {
               for (Chapter prnt : p.children.keySet()) {
                  if (prnt == parent) {
                     break condition;
                  }
               }
               if (isNew) {
                  String search = name + ":parCount:";
                  identifier.pictures = identifier.pictures.replace(search + p.parentCount,
                          search + ++p.parentCount);
               }
               parent.children.add(p);
            }
            p.addImages(images, parent, identifier, sfs, isNew);
            return p;
         }
      }
      return new Picture(name, images, parent, identifier, sfs, iSF, isNew);
   }

   /**
    * This constructor is used only to create images.
    */
   private Picture(Picture pic, Chapter parent, MainChapter identifier, int[] sf,
           File save, File destination, boolean isNew) throws IOException {
      super(destination.getName(), identifier, sf, false, IMAGES);
      children.put(parent, new Picture[]{pic});
      picParentCount(isNew);
      if (isNew) {
         if (!destination.exists()) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(save))) {
               byte[] buffer = new byte[8192];
               int amount;
               while ((amount = bis.read(buffer)) != -1) {
                  bos.write(buffer, 0, amount);
               }
            }
         }
      }
   }

   /**
    * This constructor is used only to create Pictures.
    */
   private Picture(String name, List<File> images, Chapter parent, MainChapter identifier,
           List<int[]> sfs, int[] pSF, boolean isNew) throws IOException {
      super(name, identifier, pSF, true, ELEMENTS);
      picParentCount(isNew);
      addImages(images, parent, identifier, sfs, isNew);
      parent.children.add(this);
   }

   private void picParentCount(boolean isNew) {
      String search = name + ":parCount:";
      try {
         parentCount = Integer.parseInt(getData(identifier.pictures, search));
      } catch (ArrayIndexOutOfBoundsException ex) {
         parentCount = 0;
         identifier.pictures += search + "0\n";//kam s tim
      }
      if (isNew) {
         identifier.pictures = identifier.pictures.replace(search + parentCount,
                 search + ++parentCount);
      }
   }

   /**
    * This method allows you for potencial doubling of an image.
    *
    * @param images names of imgs for this picture in the specified Chapter
    * @param parent Chapter containing this picture
    * @param sfs the successes and fails for every image
    */
   private void addImages(List<File> images, Chapter parent, MainChapter identifier,
           List<int[]> sfs, boolean isNew) throws IOException {
      if (children.get(parent) == null) {
         children.put(parent, new Picture[0]);
      }
      Picture[] imgs = Arrays.copyOf(children.get(parent), images.size() + children.get(parent).length);
      int differ = imgs.length - images.size();
      for (int i = 0; i < images.size(); i++) {
         File pic;
         if (isNew) {
            int serialINum = -1;
            String[] fix = images.get(i).toString().split("\\.");
            fix[1] = '.' + fix[fix.length - 1];
            fix[0] = identifier.dir.getPath() + "\\Pictures\\" + this.name + ' ';
            do {
               serialINum++;
               pic = new File(fix[0] + serialINum + fix[1]);
            } while (pic.exists());
         } else {
            pic = images.get(i);
         }
         imgs[i + differ] = new Picture(this, parent, identifier, (sfs == null ? null : sfs.get(i)), images.get(i),
                 pic, isNew);
      }
      children.put(parent, imgs);
   }

   @Override
   public void destroy(Chapter parent) {
      String search = name + ":parCount:";
      identifier.pictures = identifier.pictures.replace(search + parentCount + '\n',
              (--parentCount == 0 ? "" : (search + parentCount + '\n')));
      if (isMain) {
         for (Picture child : children.get(parent)) {
            child.remove(parent, this);
            child.destroy(parent);
         }
         children.remove(parent);
         parent.children.remove(this);
      }
      if (parentCount == 0) {
         if (isMain) {
            ELEMENTS.get(identifier).remove(this);
         } else {
            IMAGES.get(identifier).remove(this);
            new File(identifier.dir.getPath() + "\\Pictures\\" + name).delete();
         }
      }
   }

   @Override
   public Picture[] getChildren() {
      List<Picture> chdrn = new ArrayList<>();
      children.values().forEach((t) -> chdrn.addAll(Arrays.asList(t)));
      return chdrn.toArray(new Picture[chdrn.size()]);
   }

   private void remove(Chapter parent, Picture toRem) {
      Picture[] prev = children.get(parent);
      Picture[] chdrn = new Picture[prev.length - 1];
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
   public void removeChild(Picture child, Chapter parent) {
      if (isMain) {
         child.destroy(parent);
         remove(parent, child);
      } else {
         throw new IllegalArgumentException("Child can be removed only by main Picture");
      }
   }

   public static void readChildren(String s, String name, Chapter parent,
           MainChapter identifier, int[] sf, String desc) throws IOException {
      List<File> imgs = new LinkedList<>();
      List<String> descs = new ArrayList<>();
      List<int[]> sfs = new LinkedList<>();
      try {
         while (dumpSpace(s, '{', ' ', ',', '\n', '\t')) {
            File img = null;
            String holder, dscr = null;
            int[] isf = new int[2];
            try {
               while (!(holder = next(s, '"', '"', ' ', ',')).contains("}")) {
                  switch (holder) {
                     case NAME:
                        img = new File(next(s, '"', '"', ' ', ':'));
                        break;
                     case SUCCESS:
                        isf[0] = Integer.parseInt(next(s, ' ', ',', ':'));
                        break;
                     case FAIL:
                        isf[1] = Integer.parseInt(next(s, ' ', ' ', ':'));
                        break;
                     case DESC:
                        dscr = next(s, '"', '"', ':', ' ');
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
            imgs.add(img);
            descs.add(dscr);
            sfs.add(isf);
         }
      } catch (IllegalArgumentException iae) {
         if (!iae.getMessage().contains("']'")) {
            throw iae;
         }
      }
      mkElement(name, imgs, parent, identifier, sfs, sf, false).description = desc;
      for (int i = 0; i < imgs.size(); i++) {
         if (descs.get(i) == null) {
            imgs.remove(i);
            descs.remove(i);
            i--;
         }
      }
      IMAGES.get(identifier).forEach((img) -> {
         for (int i = imgs.size() - 1; i >= 0; i--) {
            if (imgs.get(i).getName().equals(img.toString())) {
               img.description = descs.get(i);
            }
         }
      });
   }
}
