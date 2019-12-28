package objects;

import IOSystem.Formatter.Data;
import static IOSystem.WriteElement.obj;
import static IOSystem.WriteElement.str;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import objects.templates.BasicData;
import objects.templates.Container;
import objects.templates.ContainerFile;

/**
 * References to another {@link BasicData} instance other than
 * {@link MainChapter} and its extensions.
 *
 * @author Josef Litoš
 */
public final class Reference implements BasicData {

   /**
    * Contains all instances of this class created. All References are sorted by
    * the {@link MainChapter hierarchy} they belong to. read-only data
    */
   public static final Map<MainChapter, List<Reference>> ELEMENTS = new java.util.HashMap<>();
   /**
    * Full path to the {@link #reference referenced object}.
    */
   protected final Container[] path;

   public Container[] getRefPath() {
      return path.clone();
   }
   /**
    * The referenced object.
    */
   public final BasicData reference;
   /**
    * The number of times this object is being stored in any {@link Container}.
    */
   int parentCount;

   /**
    *
    * @param ref the referenced element
    * @param path path to the parent of this object
    * @param refPath path starting from MainChapter (inclusive) to the
    * referenced object (exclusive)
    * @return new instance of this class
    */
   public static final Reference mkElement(BasicData ref, List<Container> path, Container[] refPath) {
      if (ref instanceof MainChapter) {
         throw new IllegalArgumentException("Hierarchy can't be referenced!");
      } else if (ref instanceof Container && path.stream().anyMatch((e) -> (e == ref))) {
         throw new IllegalArgumentException("Can't reference " + ref + ",\nwith path: " + path);
      }
      if (ELEMENTS.get(ref.getIdentifier()) == null) {
         ELEMENTS.put(ref.getIdentifier(), new java.util.ArrayList<>());
      }
      for (Reference r : ELEMENTS.get(ref.getIdentifier())) {
         if (ref == r.reference && refPath.length == r.path.length) {
            boolean found = true;
            for (int i = 0; i < refPath.length; i++) {
               if (refPath[i] != r.path[i]) {
                  found = false;
                  break;
               }
            }
            if (found) {
               r.parentCount++;
               return r;
            }
         }
      }
      return new Reference(ref, refPath);
   }

   private Reference(BasicData ref, Container[] refPath) {
      this.reference = ref;
      path = refPath;
      parentCount = 1;
      ELEMENTS.get(getIdentifier()).add(this);
   }

   private String mkPath() {
      String ret = "";
      for (Container c : path) {
         ret += "¤" + c;
      }
      return ret.substring(1);
   }

   private static BasicData usePath(String[] path, Container[] pathRes, int index) {
      BasicData found = find(path[index], pathRes[index - 1], index > 1 ? pathRes[index - 2] : null);
      if (index == path.length) {
         return found;
      }
      pathRes[index++] = (Container) found;
      return usePath(path, pathRes, index);
   }

   private static BasicData find(String name, Container par, Container parpar) {
      if (par instanceof ContainerFile) {
         ((ContainerFile) par).load();
      }
      for (BasicData bd : par.getChildren(parpar)) {
         if (name.equals(bd.getName())) {
            return bd;
         }
      }
      throw new IllegalArgumentException("Name " + name + " couldn't be found in Container " + par + " in " + parpar);
   }

   @Override
   public boolean isEmpty(Container c) {
      return reference.isEmpty(path[path.length - 1]);
   }

   @Override
   public MainChapter getIdentifier() {
      return reference.getIdentifier();
   }

   @Override
   public boolean setName(String name) {
      return reference.setName(name);
   }

   @Override
   public String getName() {
      return reference.getName();
   }

   @Override
   public int[] getSF() {
      return reference.getSF();
   }

   @Override
   public void addSF(boolean success) {
      reference.addSF(success);
   }

   @Override
   public String getDesc(Container c) {
      return reference.getDesc(path[path.length - 1]);
   }

   @Override
   public String putDesc(Container c, String desc) {
      return reference.putDesc(path[path.length - 1], desc);
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public boolean destroy(Container parent) {
      if (--parentCount == 0) {
         ELEMENTS.get(getIdentifier()).remove(this);
      }
      return parent.removeChild(this) != null || parent instanceof MainChapter;
   }

   @Override
   public StringBuilder writeData(StringBuilder sb, int tabs, Container cp) {
      tabs(sb, tabs, '{').add(sb, this, cp, true, true, false, false, str("refCls", "origin"), obj(reference.getClass().getName(), mkPath()), false);
      return sb.append('}');
   }

   /**
    * Implementation of
    * {@link IOSystem.ReadElement#readData(IOSystem.ReadElement.Source, objects.templates.Container) loading from String}.
    */
   public static void readData(IOSystem.ReadElement.Source src, Container parent) {
      Data data = IOSystem.ReadElement.get(src, true, false, false, false, parent, "refCls", "origin");
      Container[] org = new Container[((String) data.tagVals[1]).split(".").length];
      org[0] = src.i;
      BasicData ref = usePath(((String) data.tagVals[1]).split("¤"), org, 0);
      mkElement(ref, Arrays.asList(new Container[]{parent.getIdentifier(), parent}), org);
   }
}
