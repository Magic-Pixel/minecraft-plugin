package xyz.magicpixel.spigotplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class TabCompletion implements TabCompleter {
    SpigotPlugin plugin;
    String apiHost;

    public TabCompletion(SpigotPlugin plugin) throws InvalidConfigurationException {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.apiHost = config.getString("apiurl").replaceAll("/$", "");
    }

	@Override
	public List<String> onTabComplete(
        CommandSender sender,
        Command cmd,
        String label,
        String[] args
    ) {
        List<String> completions = new ArrayList<>();
        if (args.length <= 1) {
            completions.add(new String("balance"));
            completions.add(new String("send"));
            completions.add(new String("deposit"));
            completions.add(new String("withdraw"));
            completions.add(new String("help"));
            completions.add(new String("version"));
        }

        if (args.length > 0 && args[0].length() > 0) {
           completions = completions
               .stream()
               .filter(c -> c.startsWith(args[0]))
               .collect(Collectors.toList());
        }

        if (args.length > 1 && args[0].equals("send")) {
            if (args.length == 2) {
                for(Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().startsWith(args[1])) {
                        completions.add(p.getName());
                    }
                }
            } else if (args.length == 3) {

            } else if (args.length == 4) {
                completions.add(new String("spice"));
                completions.add(new String("honk"));
            }
        }

        return completions;
	}
}
