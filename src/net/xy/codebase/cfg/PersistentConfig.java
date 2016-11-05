package net.xy.codebase.cfg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * base config without real persistence
 *
 * @author Xyan
 *
 */
public class PersistentConfig extends Config<String, Object> {
	private IStoreListener obs;

	/**
	 * default based on HashMap
	 */
	public PersistentConfig() {
		super(new HashMap<String, Object>());
	}

	/**
	 * default with given map
	 */
	public PersistentConfig(final HashMap<String, Object> cfg) {
		super(cfg);
	}

	/**
	 * default with given map & parent
	 */
	public PersistentConfig(final HashMap<String, Object> cfg, final AbstractConfig<String, Object> parent) {
		super(cfg, parent);
	}

	/**
	 * default with parent
	 */
	public PersistentConfig(final AbstractConfig<String, Object> parent) {
		super(new HashMap<String, Object>(), parent);
	}

	public void setStoreListener(final IStoreListener obs) {
		this.obs = obs;
	}

	/**
	 * persists and changes the value
	 *
	 * @param key
	 * @param val
	 */
	public void storeValue(final String key, final Object val) {
		values.put(key, val);
		if (obs != null)
			obs.valueStored(key, val);
	}

	/**
	 * parses and adds values from an serialized HashMap
	 *
	 * @param cfgFile
	 */
	public void parse(final File cfgFile) {
		if (!cfgFile.exists())
			return;

		ObjectInputStream ois = null;
		try {
			final FileInputStream fis = new FileInputStream(cfgFile);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			ois = new ObjectInputStream(bis);
			@SuppressWarnings("unchecked")
			final HashMap<String, Object> cfg = (HashMap<String, Object>) ois.readObject();

			for (final Entry<String, Object> entry : cfg.entrySet())
				putValue(entry.getKey(), entry.getValue());
		} catch (final Exception e) {
			throw new IllegalStateException("Error deserializing", e);
		} finally {
			if (ois != null)
				try {
					ois.close();
				} catch (final IOException e) {
					throw new IllegalStateException("Error closing input", e);
				}
		}
	}

	/**
	 * stores this config content, not the parent, to the given file
	 *
	 * @param cfgFile
	 */
	public void store(final File cfgFile) {

		if (cfgFile.getParentFile() != null)
			cfgFile.getParentFile().mkdirs();

		ObjectOutputStream oos = null;
		try {
			final FileOutputStream fos = new FileOutputStream(cfgFile);
			final BufferedOutputStream bos = new BufferedOutputStream(fos);
			oos = new ObjectOutputStream(bos);
			oos.writeObject(values);
		} catch (final Exception e) {
			throw new IllegalStateException("Error deserializing", e);
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (final IOException e) {
					throw new IllegalStateException("Error closing input", e);
				}
		}
	}

	public static interface IStoreListener {
		public void valueStored(String key, Object val);
	}
}
