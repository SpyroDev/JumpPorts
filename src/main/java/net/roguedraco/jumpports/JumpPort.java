package net.roguedraco.jumpports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class JumpPort {

	private String name;
	private String description;

	private boolean enabled;
	private boolean instant;
	private double price;

	private Location minLoc;
	private Location maxLoc;

	private Set<Location> locations = new HashSet<Location>();
	private List<String> blacklist = new ArrayList<String>();
	private List<String> whitelist = new ArrayList<String>();
	

	private File confFile = null;
	private FileConfiguration conf = null;

	public JumpPort(String name) {
		this.name = name;
		ConfigurationSection defaults = JumpPortsPlugin.getPlugin().getConfig()
				.getConfigurationSection("portDefaults");
		this.description = defaults.getString("description", "");
		this.enabled = defaults.getBoolean("enabled", false);
		this.instant = defaults.getBoolean("instant", false);
		this.price = defaults.getDouble("price", 0.00);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		if (description.length() > 0) {
			return description;
		} else {
			return name;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInstant() {
		return instant;
	}

	public double getPrice() {
		return price;
	}

	public Location getMinLoc() {
		return minLoc;
	}

	public Location getMaxLoc() {
		return maxLoc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setInstant(boolean instant) {
		this.instant = instant;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean hasBlock(int x, int y, int z) {
		// Is thie block within the region boundaries?
		if (minLoc != null && maxLoc != null) {
			if (minLoc.getBlockX() <= x && x <= maxLoc.getBlockX()
					&& minLoc.getBlockY() <= y && y <= maxLoc.getBlockY()
					&& minLoc.getBlockZ() <= z && z <= maxLoc.getBlockZ()) {
				return true;
			}
		}
		return false;
	}

	public void setRegion(String world, int x1, int y1, int z1, int x2, int y2,
			int z2) {
		JumpPortsPlugin.debug("setRegion: "+world+"|"+x1+","+y1+","+z1+"|"+x2+","+y2+","+z2);
		int xMin = (x1 > x2) ? x2 : x1;
		int yMin = (y1 > y2) ? y2 : y1;
		int zMin = (z1 > z2) ? z2 : z1;
		int xMax = (x1 < x2) ? x2 : x1;
		int yMax = (y1 < y2) ? y2 : y1;
		int zMax = (z1 < z2) ? z2 : z1;
		minLoc = new Location(Bukkit.getServer().getWorld(world), xMin, yMin, zMin);
		maxLoc = new Location(Bukkit.getServer().getWorld(world), xMax, yMax, zMax);
	}
	
	public boolean canTeleport(Player player) {
		if(!blacklist.contains(player.getName()) && !blacklist.contains("g:"+JumpPortsPlugin.permission.getPrimaryGroup(player))) {
			if(!whitelist.isEmpty()) {
				if(whitelist.contains(player.getName()) || whitelist.contains("g:"+JumpPortsPlugin.permission.getPrimaryGroup(player))) {
					return true;
				}
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	public void addToWhitelist(String playername) {
		if(!whitelist.contains(playername)) {
			whitelist.add(playername);
		}
	}
	
	public void addToBlacklist(String playername) {
		if(!blacklist.contains(playername)) {
			blacklist.add(playername);
		}
	}
	
	public void removeFromWhitelist(String playername) {
		if(whitelist.contains(playername)) {
			whitelist.remove(playername);
		}
	}
	
	public void removeFromBlacklist(String playername) {
		if(blacklist.contains(playername)) {
			blacklist.remove(playername);
		}
	}

	public void save() {
		confFile = new File("plugins/JumpPorts/ports/", name + ".yml");
		conf = YamlConfiguration.loadConfiguration(confFile);

		conf.set("description", description);
		conf.set("enabled", enabled);
		conf.set("instant", instant);
		conf.set("price", price);

		Location min = this.getMinLoc();
		Location max = this.getMaxLoc();

		if (min != null && max != null) {
			conf.set("region.world", min.getWorld().getName());
			conf.set("region.min.x", min.getBlockX());
			conf.set("region.min.y", min.getBlockY());
			conf.set("region.min.z", min.getBlockZ());
			conf.set("region.max.x", max.getBlockX());
			conf.set("region.max.y", max.getBlockY());
			conf.set("region.max.z", max.getBlockZ());
		}

		int x = 0;
		for (Location loc : locations) {
			conf.set("targets." + x + ".world", loc.getWorld().getName());
			conf.set("targets." + x + ".x", loc.getX());
			conf.set("targets." + x + ".y", loc.getY());
			conf.set("targets." + x + ".z", loc.getZ());
			conf.set("targets." + x + ".yaw", (double) loc.getYaw());
			conf.set("targets." + x + ".pitch", (double) loc.getPitch());
			x++;
		}		
		
		conf.set("blacklist", blacklist);
		conf.set("whitelist", whitelist);

		try {
			conf.save(confFile); // Save the file
		} catch (IOException ex) {
			JumpPortsPlugin.log(Level.SEVERE, "Could not save config to "
					+ confFile);
		}
	}

	public void load() {
		confFile = new File("plugins/JumpPorts/ports/", name + ".yml");
		conf = YamlConfiguration.loadConfiguration(confFile);

		// Set values
		this.description = conf.getString("description");
		this.enabled = conf.getBoolean("enabled");
		this.instant = conf.getBoolean("instant");
		this.price = conf.getDouble("price");

		String world = conf.getString("region.world");
		int xMin = conf.getInt("region.min.x");
		int yMin = conf.getInt("region.min.y");
		int zMin = conf.getInt("region.min.z");
		int xMax = conf.getInt("region.max.x");
		int yMax = conf.getInt("region.max.y");
		int zMax = conf.getInt("region.max.z");
		if (world != null) {
			this.setRegion(world, xMin, yMin, zMin, xMax, yMax, zMax);
		}

		// Target Location(s)
		ConfigurationSection confSection = conf.getConfigurationSection("targets");
		if (confSection != null) {
			Iterator<String> locs = confSection.getKeys(false).iterator();
			while (locs.hasNext()) {
				String key = locs.next();
				Location loc = new Location(Bukkit.getWorld(confSection
						.getString(key + ".world")), confSection.getDouble(key
						+ ".x"), confSection.getDouble(key + ".y"),
						confSection.getDouble(key + ".z"),
						Float.parseFloat(confSection.getString(key + ".yaw")),
						Float.parseFloat(confSection.getString(key + ".pitch")));
				this.locations.add(loc);
			}
		}
		
		this.blacklist = conf.getStringList("blacklist");
		this.whitelist = conf.getStringList("whitelist");
	}

	public void delete() {
		confFile.delete();
	}

	public Location getTarget() {
		int size = locations.size();
		if (size > 0) {
			int item = new Random().nextInt(size);
			int i = 0;
			for (Location loc : locations) {
				if (i == item)
					return loc;
				i++;
			}
		}
		return null;
	}

	public void addTarget(Location loc) {
		locations.add(loc);
	}

	public void deleteTargets() {
		locations.clear();
	}
}
