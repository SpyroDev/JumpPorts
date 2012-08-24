package net.roguedraco.jumpports.player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RDPlayers {
	
	// Settings for RDPlayers (Use across all of my plugins)
	public static boolean saveData = false;
	
	public static JavaPlugin plugin = null;
	private static Map<String,RDPlayer> players = new HashMap<String,RDPlayer>();

	public static void loadAll() {
		Collection<RDPlayer> col = RDPlayers.players.values();
		for(RDPlayer rdp : col) {
			rdp.load();
		}
	}

	public static void saveAll() {
		Collection<RDPlayer> col = RDPlayers.players.values();
		for(RDPlayer rdp : col) {
			rdp.save();
		}
	}
	
	public static RDPlayer getPlayer(String name) {
		// Check if player is new
		if((RDPlayers.check(name) || saveData == false) && players.containsKey(name)) {
			// Player is existing, load data
			RDPlayer pd = players.get(name);
			return pd;
		}
		else {
			// Player is new, create data
			RDPlayer pd = new RDPlayer(name);
			//pd.save();
			players.put(name, pd);
			return pd;
		}
	}
	
	public static boolean check(String name) {
		if(saveData == true) {
			File file = new File("plugins/JumpPorts/players/",name+".yml");
			boolean exists = file.exists();
			if (!exists) {
				return false;
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	public static void playerJoin(final PlayerJoinEvent event) {
		
		String name = event.getPlayer().getName();
		// Check if player is new
		addPlayer(name);
		
	}
	
	public static void playerQuit(final PlayerQuitEvent event) {
		String name = event.getPlayer().getName();
		RDPlayers.players.remove(name);
	}
	
	public static Collection<RDPlayer> getPlayers() {
		return RDPlayers.players.values();
	}
	
	public static void addPlayer(String name) {
		if(RDPlayers.check(name) == true) {
			// Player is existing, load data
			RDPlayer pd = new RDPlayer(name);
			pd.load();
			RDPlayers.players.put(name,pd);
		}
		else {
			// Player is new, create data
			RDPlayer pd = new RDPlayer(name);
			pd.save();
			RDPlayers.players.put(name, pd);
		}
	}

}
