package games.play4ever.customplaceholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * PlaceholderAPI (PAPI) expansion.
 *
 * @author Marcel Schoen
 */
public class MyCustomPlaceholdersExpansion extends PlaceholderExpansion {

    private final MyCustomPlaceholders plugin;
    private String alias = "custompapi";

    /**
     * Creates the expansion instance.
     *
     * @param plugin The name generator handler.
     */
    public MyCustomPlaceholdersExpansion(MyCustomPlaceholders plugin, String alias) {
        this.alias = alias;
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "Marcel Schoen";
    }

    @Override
    public String getIdentifier() {
        return this.alias;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public List<String> getPlaceholders() {
        return plugin.getPlaceholderNames();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String result = plugin.getPlaceholderValue(params);
        return result == null ? "" : result;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        String result = plugin.getPlaceholderValue(params);
        return result == null ? "" : result;
    }

}
