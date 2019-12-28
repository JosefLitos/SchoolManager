/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import static IOSystem.Formatter.createDir;
import static IOSystem.Formatter.deserializeTo;
import static IOSystem.Formatter.getPath;
import static IOSystem.Formatter.serialize;
import java.io.File;
import java.util.List;
import java.util.Map;
import objects.templates.Container;
import objects.templates.BasicData;
import objects.templates.ContainerFile;

/**
 * Head object of hierarchy of all {@link BasicData elemetary} objects. The
 * hierarchy is stored in its own folder under name of this object in the
 * specified {@link IOSystem.Formatter#objDir directory}. Should be
 * {@link #name named} after the school object this hierarchy represents.
 *
 * @author Josef Litoš
 */
public class MainChapter extends objects.templates.SemiElementContainer implements ContainerFile {

   /**
    * Contains all loaded hierarchies. read-only data
    */
   public static final java.util.Set<MainChapter> ELEMENTS = new java.util.HashSet<>();
   /**
    * This file contains everything about this object and its
    * {@link #children content} together with its own
    * {@link #settings settings}.
    */
   File dir;

   public File getDir() {
      return dir;
   }
   /**
    * Contains propertions, options, or anything else that has something to do
    * with this hierarchy.
    */
   protected Map<String, Object> settings = new java.util.HashMap<>();

   public Object getSetting(String key) {
      return settings.get(key);
   }

   public void putSetting(String key, Object value) {
      settings.put(key, value);
      deserializeTo(dir + "\\setts.dat", settings);
   }

   public void removeSetting(String key) {
      settings.remove(key);
      deserializeTo(dir + "\\setts.dat", settings);
   }

   @Override
   public File getSaveFile() {
      return new File(dir + "\\main.json");
   }

   /**
    * Only this constructor creates the head object of the hierarchy. The
    * hierarchy files are saved in its {@link #dir directory}.
    *
    * @param d must contain {@link #name name} of this hierarchy.
    */
   public MainChapter(IOSystem.Formatter.Data d) {
      super(d);
      ContainerFile.isCorrect(name);
      dir = createDir(getPath() + name);
      File setts = new File(dir + "\\setts.dat");
      if (setts.exists()) {
         settings = (Map<String, Object>) serialize(dir + "\\setts.dat");
      } else {
         deserializeTo(dir + "\\setts.dat", settings);
         createDir(dir.getPath() + "\\Chapters");
      }
      ELEMENTS.add(this);
   }

   @Override
   public boolean destroy(Container parent) {
      children.forEach((bd) -> bd.destroy(this));
      children.clear();
      ELEMENTS.remove(this);
      return remFiles(dir);
   }

   private boolean remFiles(File src) {
      if (src.isDirectory()) {
         for (File f : src.listFiles()) {
            remFiles(f);
         }
      }
      return src.delete();
   }

   @Override
   public boolean setName(String name) {
      ContainerFile.isCorrect(name);
      File newDir = new File(getPath() + name);
      for (byte i = 0; i < 5; i++) {
         if (dir.renameTo(newDir)) {
            dir = newDir;
            this.name = name;
            save();
            return true;
         }
      }
      return false;
   }

   /**
    * This method saves this object and then removes itself from the
    * {@link #ELEMENTS list}.
    */
   public void close() {
      save();
      ELEMENTS.remove(this);
   }

   @Override
   public MainChapter getIdentifier() {
      return this;
   }

   /**
    * In this implementation of {@link ContainerFile} this method loads all
    * {@link SaveChapter file chapters} belonging to this object.
    */
   @Override
   public void load() {
      List<SaveChapter> schs = SaveChapter.ELEMENTS.get(this);
      if (schs == null) {
         return;
      }
      int size;
      do {
         for (int i = (size = schs.size()) - 1; i >= 0; i--) {
            if (!schs.get(i).isLoaded()) {
               schs.get(i).load();
            }
         }
      } while (size < schs.size());
   }

   @Override
   public boolean isLoaded() {
      return true;
   }

   @Override
   public StringBuilder writeData(StringBuilder sb, int tabs, Container cp) {
      deserializeTo(dir + "\\setts.dat", settings);
      sb.append('{');
      add(sb, this, null, true, true, true, true, null, null, true);
      return writeData0(sb, 1, cp);
   }

   /**
    * Implementation of
    * {@link IOSystem.ReadElement#readData(IOSystem.ReadElement.Source, objects.templates.Container) loading from String}.
    */
   public static BasicData readData(IOSystem.ReadElement.Source src, Container parent) {
      MainChapter mch = new MainChapter(IOSystem.ReadElement.get(src, true, true, true, true, null));
      src.i = mch;
      IOSystem.ReadElement.loadChildren(src, mch).forEach((e) -> mch.putChild(parent, e));
      return mch;
   }
}
