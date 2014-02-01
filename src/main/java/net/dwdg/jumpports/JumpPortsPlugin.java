package net.dwdg.jumpports;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class JumpPortsPlugin extends JavaPlugin {

	private static JumpPortsPlugin instance;
	
	private static FileConfiguration config;
	
	public void onEnable() {
		log("Enabling " + getDescription().getFullName());
		
		// Register commands
		
		
		// Register Listeners
		
		
		// Load ports

		
		log(getDescription().getFullName() + "Enabled!");
	}
	
	public void onDisable() {
		
	}
	
	
	public static void log(String message) {
		if(config.getBoolean("main.useFancyConsole", true) == true) {
			ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
			console.sendMessage("["+ChatColor.LIGHT_PURPLE+instance.getDescription().getFullName()+"] " + message);
		}
		else {
			Logger.getLogger("Minecraft").log(Level.INFO,"["+ChatColor.LIGHT_PURPLE+instance.getDescription().getFullName()+"] " + message);
		}
	}
	
}
