package xyz.acrylicstyle.doubletimecommandsbungee.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ConfigProvider {

	public File file;
	public String path;
	public Configuration configuration;
	public ConfigProvider(String path) throws IOException {
		this.path = path;
		this.file = new File(this.path);
		this.file.mkdirs();
		if (!this.file.exists()) this.file.createNewFile();
		this.configuration = this.load(this.file);
	}

	public void save(Configuration configuration, File file) throws IOException {
		ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file); // Avoid NullPointerException
	}

	public Configuration load(File file) throws IOException {
		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	}

	public void save() throws IOException {
		this.save(this.configuration, this.file);
	}

	public void set(String path, Object value) throws IOException {
		this.configuration.set(path, value);
	}

	public void setThenSave(String path, Object value) throws IOException {
		this.set(path, value);
		this.save();
	}

	public static void setThenSave(String path, Object value, File file) throws IOException {
		ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		Configuration config = provider.load(file);
		file.mkdirs();
		if (!file.exists()) file.createNewFile();
		config.set(path, value);
		provider.save(config, file);
	}

	public static Boolean getBoolean(String path, Boolean def, String pluginName) throws FileNotFoundException, IOException {
		return getBoolean(path, def, new File("./plugins/" + pluginName + "/config.yml"));
	}

	public static Boolean getBoolean(String path, Boolean def, File file) throws FileNotFoundException, IOException {
		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file).getBoolean(path, def);
	}

	public static String getString(String path, String def, String pluginName) throws FileNotFoundException, IOException {
		return getString(path, def, new File("./plugins/" + pluginName + "/config.yml"));
	}

	public static String getString(String path, String def, File file) throws FileNotFoundException, IOException {
		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file).getString(path, def);
	}

	public static void setThenSave(String path, Object value, String pluginName) throws IOException {
		setThenSave(path, value, new File("./plugins/" + pluginName + "/config.yml"));
	}

	public List<?> getList(String string, List<?> def) {
		return this.configuration.getList(string, def);
	}
}
