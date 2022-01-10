package games.play4ever.customplaceholders;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Allows to define fixed, constant custom PAPI placeholders.
 *
 * @author Marcel Schoen
 */
public final class MyCustomPlaceholders extends JavaPlugin implements CommandExecutor, TabCompleter {

    private Map<String, String> placeholders = new HashMap<>();
    private LinkedHashSet<String> placeholderNames = new LinkedHashSet<>();
    private List<String> completions = new ArrayList<>();
    private final String CONFIG_FILENAME = "myCustomPlaceholders.properties";

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Small check to make sure that PlaceholderAPI is installed
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getLogger().info("((MyCustomPlaceholders)) Registering PAPI expansion...");
            new MyCustomPlaceholdersExpansion(this, "custompapi").register();
            new MyCustomPlaceholdersExpansion(this, "cpapi").register();
        } else {
            Bukkit.getLogger().info("((MyCustomPlaceholders)) PlaceholderAPI not found.");
        }

        completions = new ArrayList<>(Arrays.asList("reload"));

        // Prefix as defined in "plugin.yml"
        Objects.requireNonNull(getCommand("custompapi")).setExecutor(this);
        Objects.requireNonNull(getCommand("custompapi")).setTabCompleter(this);

        readConfig();
    }

    private void readConfig() {
        File targetDirectory = getDataFolder();
        if(!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        File placeholderConfig = new File(targetDirectory, CONFIG_FILENAME);
        if(!placeholderConfig.exists()) {
            File targetFile = new File(getDataFolder(), CONFIG_FILENAME);
            if(!targetFile.exists()) {
                getLogger().info("((MyCustomPlaceholders)) Creating placeholders configuration file: " + CONFIG_FILENAME);
                saveResource(CONFIG_FILENAME, false);
            }
        }
        if(placeholderConfig.exists()) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(placeholderConfig));
            } catch (IOException e) {
                Bukkit.getLogger().info("((MyCustomPlaceholders)) Failed to read custom placeholders config, reason: " + e.toString());
                e.printStackTrace();
            }
            Set<String> keys = props.stringPropertyNames();
            placeholders.clear();
            for(String key : keys) {
                placeholders.put(key, props.getProperty(key));
                placeholderNames.add(key);
            }
            getLogger().info("Loaded custom placeholders configuration. Placeholders: ");
            for(String customPlaceholder : placeholderNames) {
                getLogger().info("> custom placeholder: " + customPlaceholder + "=" + placeholders.get(customPlaceholder));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return completions;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String cmd = command.getName().toLowerCase();
        if (!cmd.equals("custompapi") && !cmd.equals("cpapi")) {
            return false;
        }
        if(args.length < 1) {
            String msg = "Missing command parameters.";
            sender.sendMessage(ChatColor.RED + msg);
            getLogger().warning(msg);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            getLogger().info("Reloading custom placeholders configuration...");
            readConfig();
            return true;
        } else if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GREEN + "MyCustomPlaceholders commands:");
            sender.sendMessage(ChatColor.GREEN + "help - shows this help");
            sender.sendMessage(ChatColor.GREEN + "reload - reload placeholder configuration");
            sender.sendMessage(ChatColor.GREEN + "set <name> <value> - Sets the configured placeholder <name> to the given <value>");
            return true;
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length != 3) {
                String msg = "Invalid parameters. Syntax: set <name> <value>";
                sender.sendMessage(ChatColor.RED + msg);
                getLogger().warning(msg);
                return false;
            }
            String placeholderName = args[1];
            if(placeholders.get(placeholderName) == null) {
                String msg = "Cannot set unknown placeholder '" + placeholderName + "'; only configured custom placeholders can be changed.";
                sender.sendMessage(ChatColor.RED + msg);
                getLogger().warning(msg);
                return false;
            }
            String placeholderValue = args[2];
            getLogger().info("Changing custom placeholder '" + placeholderName + "' to value '" + placeholderValue + "'");
            placeholders.put(placeholderName, placeholderValue);
            return true;
        }
        return false;
    }

    public List<String> getPlaceholderNames() {
        return new ArrayList(placeholderNames);
    }

    /**
     * @param name The name of the placeholder; either a plain name (which equals
     *             a property key) or a local constant name like ${MyValue}
     * @return
     */
    public String getPlaceholderValue(String name) {
        String value = placeholders.get(name);
        if(value == null) {
            return name;
        }
        while(value.contains("${")) {
            int start = value.indexOf("${");
            int end = value.indexOf("}");
            if(start > -1 && end > start + 1) {
                // TODO - make more efficient
                String prePart = value.substring(0, start);
                String expression = value.substring(start + 2, end);
                String postPart = value.substring(end + 1);
                value = prePart + getExpressionValue(expression) + postPart;
            } else {
                break;
            }
        }

        // Do NOT try to resolve our own placeholders (prefixed 'custompapi_')
        // with PAPI, since this could lead to an infinite loop / stack overflow
        // which would probably crash the entire server.
        if(!value.contains("custompapi")) {
            value = PlaceholderAPI.setPlaceholders(null, value);
        }
        return value;
    }

    /**
     * Resolves an expression like ${somePlaceholder}

     * @param expression
     * @return
     */
    private String getExpressionValue(String expression) {
        if(placeholders.containsKey(expression)) {
            return placeholders.get(expression);
        }
        return expression;
    }

    @Override
    public void onDisable() {
        placeholders.clear();
        placeholderNames.clear();
    }
}
