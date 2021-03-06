package IOSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import objects.MainChapter;
import objects.templates.Container;
import objects.templates.ContainerFile;
import testing.Test;

/**
 * This class is used to operate with text files. The format of all files containing necessary data
 * of every {@link objects.templates.BasicData element} is json, but the code was implemented by the
 * author without using any templates.
 *
 * @author Josef Litoš
 */
public class Formatter {

	/**
	 * Default {@link Reactioner object} made for answering on various actions, keys should be in
	 * format:
	 * <p>
	 * fullClassName + ':' + specification (if necessary);
	 * <p>
	 * or just specification, if it is for global usage.
	 */
	public static final Map<String, Reactioner> defaultReacts = new HashMap<>();

	/**
	 * Used as file I/O interface.
	 */
	private static IOSystem ios;

	public static IOSystem getIOSystem() {
		return ios;
	}

	/**
	 * Reacts on any event from specified locations in the program given various parameters.
	 */
	public interface Reactioner {

		void react(Object... moreInfo);
	}

	public static final String CLASS = "class", NAME = "name", SUCCESS = "s",
		 FAIL = "f", CHILDREN = "cdrn", DESC = "desc";

	/**
	 * @return path to directory currently set for containing objects.
	 */
	public static File getPath() {
		return ios.objDir;
	}

	/**
	 * @param key tag of the setting
	 * @return the value of the given key or {@code null} if settings doesn't contain that key
	 * @see Map#get(Object)
	 */
	public static Object getSetting(String key) {
		return ios.settings.get(key);
	}

	/**
	 * @param key tag to be added or overrated in the settings of this program
	 * @param value value associated with the given tag
	 * @see Map#put(Object, Object)
	 */
	public static void putSetting(String key, Object value) {
		ios.settings.put(key, value);
		deserialize(ios.setts, ios.settings);
	}

	/**
	 * Removes the given key from the {@link IOSystem#settings}.
	 *
	 * @param key the tag that will be removed
	 */
	public static void removeSetting(String key) {
		ios.settings.remove(key);
		deserialize(ios.setts, ios.settings);
	}

	/**
	 * This method changes the directory of saves of hierarchies.
	 *
	 * @param path the directory for this application
	 * @return if the dir was changed, {@code false} when given same dir
	 */
	public static boolean changeDir(String path) {
		if (ios.objDir.getAbsolutePath().equals(path)) {
			return false;
		}
		while (MainChapter.ELEMENTS.size() > 0) {
			MainChapter.ELEMENTS.get(0).close();
		}
		(ios.objDir = new File(path)).mkdirs();
		putSetting("objdir", ios.changeDir(path));
		return true;
	}

	/**
	 * Restores the default objects directory.
	 */
	public static void resetDir() {
		changeDir(ios.getDefaultObjectsDir());
	}

	public static void deserialize(File filePath, Object toSave) {
		deserialize(filePath.toString(), toSave, false);
	}

	public static void deserialize(String filePath, Object toSave, boolean internal) {
		ios.deserialize(filePath, toSave, internal);
	}

	public static Object serialize(File filePath) {
		return serialize(filePath.toString(), false);
	}

	public static Object serialize(String filePath, boolean internal) {
		return ios.serialize(filePath, internal);
	}

	public static String loadFile(File source) {
		try {
			return ios.readStream(new FileInputStream(source));
		} catch (Exception e) {
			Formatter.defaultReacts.get(ContainerFile.class + ":load")
				 .react(e, source, source.getPath());
		}
		return null;
	}

	public static void saveFile(String toSave, File filePath) {
		ios.writeFile(toSave, filePath);
	}

	/**
	 * @param e Exception to be rendered
	 * @return complete road of the Exception and its causes
	 */
	public static String getStackTrace(Throwable e) {
		OutputStream os = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(os));
		return os.toString();
	}

	/**
	 * File operations depend on platform, this class defines all methods required to run the program
	 * and can be platform dependant.
	 * <p>
	 * Only the firstly created instance will be used during the run.
	 * <p>
	 * Also all of them have to create all necessary {@link Formatter#defaultReacts} by implementing
	 * the method {@link #mkDefaultReacts()} for the library to work.
	 */
	public static abstract class IOSystem {

		/**
		 * Settings file of the program, contains all {@link #settings hierarchy independant variables}.
		 */
		protected File setts;
		/**
		 * All file-stored variables and options for the program.
		 */
		protected Map<String, Object> settings = new HashMap<>();

		/**
		 * Path to the directory that contains all {@link MainChapter hierarchies's} folders and data.
		 */
		protected File objDir;

		/**
		 * Loads all stored variables and defines its extension as the {@link Formatter#ios}.
		 *
		 * @param settsFile {@link #setts}
		 */
		protected IOSystem(File settsFile) {
			if (Formatter.ios != null) {
				return;
			}
			Formatter.ios = this;
			mkDefaultReacts();
			setts = settsFile;
			if (setts.exists()) {
				try {
					settings = (Map<String, Object>) serialize(setts.getName(), true);
					try {
						(objDir = new File(
							 mkRealPath(settings.get("objdir").toString()))).mkdirs();
					} catch (Exception e) {
						defaultReacts.get(Formatter.class + ":newSrcDir")
							 .react(e, settings.get("objdir"));
						(objDir = new File("School objects")).mkdirs();
					}
					Object value;
					if ((value = settings.get("defaultTestTime")) != null) {
						Test.setDefaultTime((Integer) value);
					} else {
						settings.put("defaultTestTime", 180);
					}
					if ((value = settings.get("isClever")) != null) {
						Test.setClever((Boolean) value);
					} else {
						settings.put("isClever", true);
					}
					if ((value = settings.get("testAmount")) != null) {
						Test.setAmount((Integer) value);
					} else {
						settings.put("testAmount", 10);
					}
					setDefaults(false);
				} catch (Exception e) {
					defaultReacts.get(Formatter.class + ":newSrcDir").react(e, settsFile);
					(objDir = new File(getDefaultObjectsDir())).mkdirs();
				}
			}
			if (objDir == null) {
				(objDir = new File(getDefaultObjectsDir())).mkdirs();
				settings.put("objdir", objDir.getAbsolutePath());
				settings.put("defaultTestTime", 180);
				settings.put("isClever", true);
				settings.put("testAmount", 10);
				setDefaults(true);
				deserialize(setts.getAbsolutePath(), settings, true);
			}
		}

		/**
		 * Loads and sets all outside-library used values stored in {@link #settings}.
		 *
		 * @param first if the program returns to its first-launch state (reset)
		 */
		protected void setDefaults(boolean first) {
		}

		/**
		 * Creates the default exception handlers for various cases.<p>
		 * This method is completely platform dependant.
		 */
		protected abstract void mkDefaultReacts();

		/**
		 * Default way to save files.
		 *
		 * @param toSave content to be written
		 * @param file the file that is being written
		 */
		protected void writeFile(String toSave, File file) {
			try (OutputStreamWriter osw 
				 = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)){
				osw.write(toSave);
			} catch (IOException ex) {
				throw new IllegalArgumentException(ex);
			}
		}

		protected abstract void deserialize(
			 String filePath, Object toSave, boolean internal);

		protected abstract Object serialize(String filePath, boolean internal);

		/**
		 * @return default {@link #objDir objects storage}
		 */
		public abstract String getDefaultObjectsDir();

		/**
		 * Any necessary actions needed when the {@link #objDir objects storage} is changed.
		 *
		 * @param path the new objects storage
		 * @return the path that will be saved to the {@link #setts settings}
		 */
		protected String changeDir(String path) {
			return path;
		}

		/**
		 * Makes a real path.
		 *
		 * @param path § on index 0 changes to current disc name (for USB disc portability)
		 * @return the corrected path
		 */
		public abstract String mkRealPath(String path);

		protected abstract String readStream(InputStream source) throws Exception;
	}

	/**
	 * Object containing all necessary data for creating a {@link MainChapter hierarchy}
	 * {@link objects.templates.BasicData element}. Used for every hierarchy object creating.
	 */
	public static class Data {

		public String name;
		public MainChapter identifier;
		public int[] sf;
		public String description = "";
		public Map<String, Object> tagVals;
		public Container par;

		public Data(String name, MainChapter identifier) {
			this.name = name;
			this.identifier = identifier;
		}

		public Data addSF(int[] successAndFail) {
			sf = successAndFail;
			return this;
		}

		public Data addDesc(String description) {
			this.description = description == null ? "" : description;
			return this;
		}

		public Data addPar(Container parent) {
			par = parent;
			return this;
		}

		public Data addExtra(Map<String, Object> tagValues) {
			tagVals = tagValues;
			return this;
		}

		@Override
		public String toString() {
			return "name=\"" + name + "\", description=" + description + "\", success="
				 + sf[0] + ", fail=" + sf[1] + ", parent=" + par + ", extra="
				 + tagVals.toString();
		}
	}

	/**
	 * Used to avoid data loss, corruption and possible concurrent modification exceptions.
	 */
	public static class Synchronizer {

		private final Map<MainChapter, Integer> hashes = new HashMap<>();

		/**
		 * All keys' of {@link MainChapter#ELEMENTS} hashCodes, which values are currently being used.
		 */
		private final List<Integer> USED = new LinkedList<>();

		public void waitForAccess(MainChapter lock) {
			Integer hashCode = hashes.get(lock);
			if (hashCode == null) {
				hashes.put(lock, hashCode = lock.hashCode());
			}
			synchronized (hashCode) {
				if (USED.contains(hashCode)) try {
					while (USED.contains(hashCode)) {
						hashCode.wait();
					}
				} catch (InterruptedException ie) {
					throw new IllegalThreadStateException("Interrupting is not allowed!");
				}
				USED.add(hashCode);
			}
		}

		public void endAccess(MainChapter unlock) {
			Integer hashCode = hashes.get(unlock);
			synchronized (hashCode) {
				USED.remove(hashCode);
				hashCode.notify();
			}
		}
	}
}
