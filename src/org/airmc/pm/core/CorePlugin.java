package org.airmc.pm.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {
  /***************************** Command Event ******************************/

  /**
   * @brief On Command
   *
   * Processes all registered commands in plugin.yml with the appropriate task:
   * - pload:   loads a plugin by name from the plugin directory
   * - preload: reloads an already loaded plugin
   * - punload: removes an already loaded plugin
   *
   * @param sender The author of the command
   * @param cmd    The received command
   * @param label  The title of the received command
   * @param args   The provided arguments split by whitespace
   *
   * @return true
   */
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label,
      String[] args) {
    // Ensure the required argument count is present
    if(args.length > 0) {
      // Declare variables for the plugin name and the plugin (if loaded)
      boolean       unload     = label.equalsIgnoreCase("preload") ||
        label.equalsIgnoreCase("punload");
      boolean       load       = label.equalsIgnoreCase("pload") ||
        label.equalsIgnoreCase("preload");
      PluginManager pm         = Bukkit.getServer().getPluginManager();
      String        pluginName = args[0];

      if (unload)
        if (pm.getPlugin(pluginName) != null)
          // Unload the plugin to complete the requested action
          this.unloadPlugin(pm.getPlugin(pluginName));
        else
          this.sendMessage(sender, "NOTE: The provided plugin was not " +
            "loaded - doing nothing.");

      if (load)
        if (pm.getPlugin(pluginName) == null)
          // Load the plugin to complete the requested action
          this.loadPlugin(pluginName);
        else
          this.sendMessage(sender, "NOTE: The provided plugin was already " +
            "loaded - doing nothing.");

      if ((  load && pm.getPlugin(pluginName) != null) ||
          (unload && pm.getPlugin(pluginName) == null))
        this.sendMessage(sender, "The requestion action was completed " +
          "successfully.");
      else
        this.sendMessage(sender, "The requested action could not be " +
          "completed.");
    }
    else {
      sender.sendMessage("Usage: /" + label.toLowerCase() + " <plugin>");
    }
    return true;
  }

  /**************************** Helper Functions ****************************/

  /**
   * @brief Get Field
   *
   * Fetches the requested field from the provided class and instance
   *
   * @remarks
   * Parameter `o` can be null as per Field.get(...) documentation states
   *
   * @param c The class which holds the desired field
   * @param o The instance of c which holds the desired field (null if static)
   * @param f The name of the field to fetch
   *
   * @return The value of the requested field, or null
   */
  private Object getField(Object c, Object o, String f) {
    // Setup storage for the return value
    Object retVal = null;

    // Fetch the field (f) from the provided class (c) and instance object (o)
    Field field;
    try {
      field = c.getClass().getDeclaredField(f);
      // Make sure the field is accessible externally
      field.setAccessible(true);
      retVal = field.get(o);
    } catch (NoSuchFieldException | SecurityException |
        IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }

    // Return the fetched field
    return retVal;
  }

  /**
   * @brief Load Plugin
   *
   * Loads the plugin at the requested path ("plugins/" suffixed with the
   * provided name then suffixed with ".jar")
   *
   * @param pluginName The name of the Plugin to be loaded
   */
  private synchronized void loadPlugin(String pluginName) {
    Plugin pl = null;
    if (pluginName != null && pluginName.length() > 0) {
      // Declare a File object with relative path of plugins/<PluginName>
      File plugin = new File("plugins/" + pluginName + ".jar");
      if (plugin.exists() && plugin.isFile())
        try {
          // Attempt to load and enable the requested plugin
          pl = Bukkit.getPluginManager().loadPlugin(plugin);
          Bukkit.getPluginManager().enablePlugin(pl);
        } catch (UnknownDependencyException | InvalidPluginException
            | InvalidDescriptionException e) {
          e.printStackTrace();
        }
    }
  }

  /**
   * @brief Send Message
   *
   * Sends a formatted message to the provided CommandSender
   *
   * @param sender The CommandSender to recieve the message
   * @param string The string to send to the CommandSender
   */
  private void sendMessage(CommandSender sender, String string) {
    sender.sendMessage("[" + this.getName() + "] " + string);
  }

  /**
   * @brief Unload Plugin
   *
   * Disables the requested plugin and removes it from the PluginManager's
   * internal plugin list (effectively "unloading" the plugin)
   *
   * @remarks
   * Potentially promiscuous probing of the PluginManager provided by Bukkit
   *
   * @param plugin The plugin desired to be unloaded
   */
  private synchronized void unloadPlugin(Plugin plugin) {
    if (plugin != null) {
      String name = plugin.getName();

      // Setup generic containers for mutation
      List<?> plugins = null;
      Map<?, ?> lookupNames = null;

      // Fetch each field's reference
      Object pluginsObj = this.getField(Bukkit.getPluginManager(),
        Bukkit.getPluginManager(), "plugins");
      Object lookupNamesObj = this.getField(Bukkit.getPluginManager(),
        Bukkit.getPluginManager(), "lookupNames");

      // Safely cast into the generic containers
      if (pluginsObj instanceof List<?>)
        plugins = (List<?>)pluginsObj;
      if (lookupNamesObj instanceof Map<?, ?>)
        lookupNames = (Map<?, ?>)lookupNamesObj;

      // Disable the plugin
      Bukkit.getPluginManager().disablePlugin(plugin);

      // Remove matching plugin from the plugins list
      if (plugins != null && plugins.contains(plugin))
        plugins.remove(plugin);

      // Remove matching plugin from the lookupNames map
      if (lookupNames != null && lookupNames.containsKey(
          name.replace(' ', '_')))
        lookupNames.remove(name.replace(' ', '_'));

      // Attempt to close the class loader for the provided plugin
      Object pl = plugin.getClass().getClassLoader();
      try {
        if (pl instanceof URLClassLoader)
          ((URLClassLoader)pl).close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
