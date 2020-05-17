package sh.blockparty.template.spigotplugin;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;


public class SpigotPlugin extends JavaPlugin {
    private File configFile;
    private FileConfiguration config;

    public FileConfiguration getConfig() {
        return this.config;
    }

    @Override
    public void onEnable() {
        createConfig();
        getCommand("mpx").setExecutor(new MpxCommand(this));
    }

    private void createConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
         }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

}
