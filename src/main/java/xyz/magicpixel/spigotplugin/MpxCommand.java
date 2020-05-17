package xyz.magicpixel.spigotplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class MpxCommand implements CommandExecutor {
    SpigotPlugin plugin;
    String apiKey;
    String apiHost;


    public MpxCommand(SpigotPlugin plugin) throws InvalidConfigurationException {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.apiKey = config.getString("apikey");
        this.apiHost = config.getString("apiurl").replaceAll("/$", "");

        if (this.apiKey.equals("setme")) {
            plugin.getLogger().info("You must set apikey in plugins/MagicPixel/config.yml");
            throw new InvalidConfigurationException("You must set apikey in plugins/MagicPixel/config.yml");
       }
    }

    // converts non-dashed uuids to dashed
    // for some dumb reason java requires uuids to have dashes, uuid spec doesnt
    private UUID compressedUuidToUuid(String m) {
        return UUID.fromString(m.replaceFirst(
            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5" 
        ));
    }

    @Override
    public boolean onCommand(
        final CommandSender sender,
        final Command cmd,
        final String label,
        final String[] args
    ) {
        // TODO is this needed?
        if (! cmd.getName().toLowerCase().equals("mpx")) {
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                String q = null;
                try {
                    q = URLEncoder.encode(String.join(" ", args), StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    sender.sendMessage("UnsupportedEncodingException");
                    return;
                }

                URL url = null;
                try {
                    String senderUuid = ((Player) sender).getUniqueId().toString();
                    url = new URL(apiHost+"/command/minecraft?uuid="+senderUuid+"&q="+q);
                } catch (MalformedURLException e) {
                    sender.sendMessage("MalformedURLException");
                    return;
                }

                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("X-Auth-Token", apiKey);
                    conn.setRequestMethod("POST");

                    int respCode = conn.getResponseCode(); 
                    if (respCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("response code not OK");
                    }
                } catch (IOException e) {
                    sender.sendMessage("Could not connect to server");
                    return;
                }

                StringBuffer buffer = new StringBuffer();
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                )) {
                    int read;
                    char[] chars = new char[1024];
                    while ((read = reader.read(chars)) != -1) {
                        buffer.append(chars, 0, read); 
                    }
                } catch (IOException e) {
                    sender.sendMessage("IOException");
                    return;
                }

                try {
                    JSONParser jsonParser = new JSONParser();
                    Object parsed = jsonParser.parse(buffer.toString());
                    JSONObject jsonObj = (JSONObject) parsed;

                    String senderMessage = (String) jsonObj.get("msg");
                    if (senderMessage != null) {
                        sender.sendMessage(senderMessage);
                    }

                    JSONArray receiverJsonMessages = (JSONArray) jsonObj.get("msgs");
                    if (receiverJsonMessages != null) {
                        for (Object jsonMessage : receiverJsonMessages) {
                            JSONObject jsonMessageObj = (JSONObject) jsonMessage;

                            String receiverUuidStr = (String) jsonMessageObj.get("uuid");
                            if (receiverUuidStr == null) {
                                continue;
                            }

                            UUID receiverUuid = compressedUuidToUuid(receiverUuidStr);

                            String receiverMessage = (String) jsonMessageObj.get("msg");
                            if (receiverMessage != null) {
                                Player receiver = Bukkit.getServer().getPlayer(receiverUuid);

                                if (receiver != null) {
                                    receiver.sendMessage(receiverMessage);
                                }
                            }
                        }
                    }
                } catch (ParseException e) {
                    sender.sendMessage("ParseException");
                    return;
                }
            }
        });

        return true;
    }
}
