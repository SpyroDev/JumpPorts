package net.roguedraco.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RDEvents implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		RDPlayers.playerJoin(event);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		RDPlayers.playerQuit(event);
	}
	
}
