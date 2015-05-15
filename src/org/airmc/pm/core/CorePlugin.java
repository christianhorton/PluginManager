package org.airmc.pm.core;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {
	
	public void onEnable() {
		getLogger().log(Level.INFO, "Enabled");
	}
	
	public void onDisable() {
		getLogger().log(Level.INFO, "Disabled");
	}


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(label.equalsIgnoreCase("pload")) {
				if(args.length < 1) {
					sender.sendMessage("Usage: /pload <pluginName>");
					return true;
				}
				
				String pluginName = args[0];
				Plugin pl = this.getServer().getPluginManager().getPlugin(pluginName);
				if(!pl.isEnabled()) {
					getServer().getPluginManager().enablePlugin(pl);
					sender.sendMessage("Plugin '"+ pluginName+ "' was loaded successfully.");
				} else {
					sender.sendMessage("Plugin '"+ pluginName+ "' is already loaded.");
				}
				return true;
			}
			
			if(label.equalsIgnoreCase("punload")) {
				if(args.length < 1) {
					sender.sendMessage("Usage: /punload <pluginName>");
					return true;
				}
				
				String pluginName = args[0];
				Plugin pl = this.getServer().getPluginManager().getPlugin(pluginName);
				if(pl.isEnabled()) {
					getServer().getPluginManager().disablePlugin(pl);
					sender.sendMessage("Plugin '"+ pluginName+ "' was unloaded successfully.");
				} else {
					sender.sendMessage("Plugin '"+ pluginName+ "' is not loaded.");
				}
				return true;
			}
			if(label.equalsIgnoreCase("preload")) {
				if(args.length < 1) {
					sender.sendMessage("Usage: /preload <pluginName>");
					return true;
				}
				
				String pluginName = args[0];
				Plugin pl = this.getServer().getPluginManager().getPlugin(pluginName);
				if(pl.isEnabled()) {
					getServer().getPluginManager().disablePlugin(pl);
					getServer().getPluginManager().enablePlugin(pl);
					sender.sendMessage("Plugin '"+ pluginName+ "' was reloaded successfully.");
				} else {
					sender.sendMessage("Plugin '"+ pluginName+ "' is not loaded.");
				}
				return true;
			}
	        return true;
	}
	

}
