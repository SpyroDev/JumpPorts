package net.roguedraco.jumpports;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateCheck {
	
	private JavaPlugin plugin;
	private URL filesFeed;
	
	private String version;

	public UpdateCheck(JavaPlugin plugin, String url) {
		this.plugin = plugin;
		
		try {
			this.filesFeed = new URL(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean updateNeeded(Player player) {
		InputStream input;
		try {
			input = this.filesFeed.openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Node latestFile = document.getElementsByTagName("item").item(0);
			
			NodeList children = latestFile.getChildNodes();
			
			this.version = children.item(1).getTextContent();
			
			this.version = this.version.replaceAll("JumpPorts-","").trim();
			if(version.equalsIgnoreCase(plugin.getDescription().getVersion())) {
				return false;
			}
			else {
				// Send to player who requested
				player.sendMessage("["+ChatColor.LIGHT_PURPLE+"JumpPorts"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Plugin Outdated. Current: "+ChatColor.DARK_GREEN+plugin.getDescription().getVersion()+ChatColor.GREEN+" Latest: "+ChatColor.DARK_GREEN+version);
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
