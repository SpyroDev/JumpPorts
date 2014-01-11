package net.dwdg.jumpports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.dwdg.jumpports.util.JPLocation;
import net.dwdg.jumpports.util.PortCommand;
import net.dwdg.jumpports.util.PortEffect;
import net.dwdg.jumpports.util.PortTrigger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JumpPort {

	private String name;
	private String description;

	private String permissionNode;

	private boolean enabled;
	private boolean instant;
	private boolean cmdPortal;
	private boolean isTeleport;
	private boolean isBungee;
	private boolean useGlobalConfig;

	private boolean harmlessLightningLeave;
	private boolean harmlessLightningArrive;
	private int teleportDelay;

	private double price;
	private Location minLoc;
	private Location maxLoc;
	private Set<JPLocation> locations = new HashSet<>();
	private List<PortTrigger> triggers = new ArrayList<>();

	private List<String> blacklist = new ArrayList<>();
	private List<String> whitelist = new ArrayList<>();
	private List<PortCommand> commands = new ArrayList<>();
	private List<PotionEffect> beforeEffects = new ArrayList<>();
	private List<PotionEffect> afterEffects = new ArrayList<>();

	private File confFile = null;
	private FileConfiguration conf = null;

	public JumpPort(String name) {
		this.name = name;
		ConfigurationSection defaults = JumpPortsPlugin.getPlugin().getConfig().getConfigurationSection("globalPortConfig");
		this.description = defaults.getString("description", "");
		this.enabled = defaults.getBoolean("enabled", true);
		this.instant = defaults.getBoolean("instant", false);
		this.cmdPortal = defaults.getBoolean("cmdPortal", false);
		this.isTeleport = defaults.getBoolean("isTeleport", true);
		this.isBungee = defaults.getBoolean("isBungee", false);
		this.price = defaults.getDouble("price", 0.00);
		load();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		try {
			if (description.length() > 0) {
				return description;
			} else {
				return name;
			}
		} catch (Exception e) {
			return name;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInstant() {
		return instant;
	}

	public boolean isTeleport() {
		return isTeleport;
	}

	public boolean isBungee() {
		return isBungee;
	}

	public boolean isCmdPortal() {
		return cmdPortal;
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
		save();
	}

	public void setDescription(String description) {
		this.description = description;
		save();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		save();
	}

	public void setInstant(boolean instant) {
		this.instant = instant;
		save();
	}

	public void setCmdPortal(boolean cmdPortal) {
		this.cmdPortal = cmdPortal;
		save();
	}

	public void setIsTeleport(boolean isTeleport) {
		this.isTeleport = isTeleport;
		save();
	}

	public void setIsBungee(boolean isBungee) {
		this.isBungee = isBungee;
		save();
	}

	public void setPrice(double price) {
		this.price = price;
		save();
	}

	public boolean hasBlock(int x, int y, int z) {
		// Is this block within the region boundaries?
		if (minLoc != null && maxLoc != null) {
			if (minLoc.getBlockX() <= x && x <= maxLoc.getBlockX() && minLoc.getBlockY() <= y && y <= maxLoc.getBlockY() && minLoc.getBlockZ() <= z && z <= maxLoc.getBlockZ()) {
				return true;
			}
		}
		return false;
	}

	public void setRegion(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
		JumpPortsPlugin.debug("setRegion: " + world + "|" + x1 + "," + y1 + "," + z1 + "|" + x2 + "," + y2 + "," + z2);
		int xMin = (x1 > x2) ? x2 : x1;
		int yMin = (y1 > y2) ? y2 : y1;
		int zMin = (z1 > z2) ? z2 : z1;
		int xMax = (x1 < x2) ? x2 : x1;
		int yMax = (y1 < y2) ? y2 : y1;
		int zMax = (z1 < z2) ? z2 : z1;
		minLoc = new Location(Bukkit.getServer().getWorld(world), xMin, yMin, zMin);
		maxLoc = new Location(Bukkit.getServer().getWorld(world), xMax, yMax, zMax);
		save();
	}

	public boolean canTeleport(Player player) {
		try {
			if (!blacklist.contains(player.getName()) && !blacklist.contains("g:" + JumpPortsPlugin.permission.getPrimaryGroup(player))) {
				if (!whitelist.isEmpty()) {
					if (whitelist.contains(player.getName()) || whitelist.contains("g:" + JumpPortsPlugin.permission.getPrimaryGroup(player))) {
						return true;
					}
				} else {
					return true;
				}
			}
		} catch (UnsupportedOperationException e) {
			if (!blacklist.contains(player.getName())) {
				if (!whitelist.isEmpty()) {
					if (whitelist.contains(player.getName())) {
						return true;
					}
				} else {
					return true;
				}
			}
		}

		return false;
	}

	public String getPermissionNode() {
		return permissionNode;
	}

	public void setPermissionNode(String permissionNode) {
		this.permissionNode = permissionNode;
	}

	public boolean isUseGlobalConfig() {
		return useGlobalConfig;
	}

	public void setUseGlobalConfig(boolean useGlobalConfig) {
		this.useGlobalConfig = useGlobalConfig;
	}

	public boolean isHarmlessLightningLeave() {
		return harmlessLightningLeave;
	}

	public void setHarmlessLightningLeave(boolean harmlessLightningLeave) {
		this.harmlessLightningLeave = harmlessLightningLeave;
	}

	public boolean isHarmlessLightningArrive() {
		return harmlessLightningArrive;
	}

	public void setHarmlessLightningArrive(boolean harmlessLightningArrive) {
		this.harmlessLightningArrive = harmlessLightningArrive;
	}

	public int getTeleportDelay() {
		return teleportDelay;
	}

	public void setTeleportDelay(int teleportDelay) {
		this.teleportDelay = teleportDelay;
	}

	public List<PotionEffect> getBeforeEffects() {
		return beforeEffects;
	}

	public void setBeforeEffects(List<PotionEffect> beforeEffects) {
		this.beforeEffects = beforeEffects;
	}

	public List<PotionEffect> getAfterEffects() {
		return afterEffects;
	}

	public void setAfterEffects(List<PotionEffect> afterEffects) {
		this.afterEffects = afterEffects;
	}

	public List<PortTrigger> getTriggers() {
		return triggers;
	}

	public void setTeleport(boolean isTeleport) {
		this.isTeleport = isTeleport;
	}

	public void setBungee(boolean isBungee) {
		this.isBungee = isBungee;
	}

	public void addToWhitelist(String playername) {
		if (!whitelist.contains(playername)) {
			whitelist.add(playername);
			save();
		}
	}

	public void addToBlacklist(String playername) {
		if (!blacklist.contains(playername)) {
			blacklist.add(playername);
			save();
		}
	}

	public void removeFromWhitelist(String playername) {
		if (whitelist.contains(playername)) {
			whitelist.remove(playername);
			save();
		}
	}

	public void removeFromBlacklist(String playername) {
		if (blacklist.contains(playername)) {
			blacklist.remove(playername);
			save();
		}
	}

	public boolean hasTrigger(PortTrigger trigger) {
		if (JumpPortsPlugin.getPlugin().getConfig().getBoolean("overridePortTriggers", true) == false) {
			if (triggers.contains(trigger)) return true;
			return false;
		}
		return true;
	}

	public void save() {
		confFile = new File("plugins/JumpPorts/ports/", name + ".yml");
		conf = YamlConfiguration.loadConfiguration(confFile);

		conf.set("description", description);
		conf.set("enabled", enabled);
		conf.set("instant", instant);
		conf.set("cmdPortal", cmdPortal);
		conf.set("isTeleport", isTeleport);
		conf.set("isBungee", isBungee);
		conf.set("permissionNode", permissionNode);
		conf.set("useGlobalConfig", useGlobalConfig);
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
		for (JPLocation loc : locations) {
			conf.set("targets." + x + ".server", loc.getServer());
			conf.set("targets." + x + ".world", loc.getWorld());
			conf.set("targets." + x + ".x", loc.getX());
			conf.set("targets." + x + ".y", loc.getY());
			conf.set("targets." + x + ".z", loc.getZ());
			conf.set("targets." + x + ".yaw", (double) loc.getYaw());
			conf.set("targets." + x + ".pitch", (double) loc.getPitch());
			x++;
		}

		x = 0;
		for (PortCommand command : commands) {
			conf.set("commands." + x + ".type", (command.getCommandType() == PortCommand.Type.PLAYER ? "PLAYER" : "CONSOLE"));
			conf.set("commands." + x + ".command", command.getCommand());
			x++;
		}
		
		///////////////////////
		// Global Port Stuff //
		///////////////////////
		
		// After Effects
		for (PotionEffect effect : afterEffects) {
			String effectName = effect.getType().getName();
			conf.set("afterEffects." + effectName + ".duration", effect.getDuration());
			conf.set("afterEffects." + effectName + ".amplifier", effect.getAmplifier());
		}
		
		// Before Effects
		for (PotionEffect effect : beforeEffects) {
			String effectName = effect.getType().getName();
			conf.set("beforeEffects." + effectName + ".duration", effect.getDuration());
			conf.set("beforeEffects." + effectName + ".amplifier", effect.getAmplifier());
		}
		
		// Lightning
		conf.set("harmlessLightningLeave", harmlessLightningLeave);
		conf.set("harmlessLightningArrive", harmlessLightningArrive);
		conf.set("teleportDelay", teleportDelay);
		
		// Triggers
		conf.set("triggers.sneak", (triggers.contains(PortTrigger.SNEAK)));
		conf.set("triggers.sprint", (triggers.contains(PortTrigger.SPRINT)));
		conf.set("triggers.jump", (triggers.contains(PortTrigger.JUMP)));
		conf.set("triggers.fall", (triggers.contains(PortTrigger.FALL)));
		conf.set("triggers.fireArrow", (triggers.contains(PortTrigger.ARROW)));
		conf.set("triggers.eggThrow", (triggers.contains(PortTrigger.EGG)));
		

		conf.set("blacklist", blacklist);
		conf.set("whitelist", whitelist);

		try {
			conf.save(confFile); // Save the file
		} catch (IOException ex) {
			JumpPortsPlugin.log(Level.SEVERE, "Could not save config to " + confFile);
		}
	}

	public final void load() {
		confFile = new File("plugins/JumpPorts/ports/", name + ".yml");
		conf = YamlConfiguration.loadConfiguration(confFile);

		// Set values
		this.description = conf.getString("description", "");
		this.enabled = conf.getBoolean("enabled", true);
		this.permissionNode = conf.getString("permissionNode", "");
		this.instant = conf.getBoolean("instant", false);
		this.price = conf.getDouble("price", 0.00);
		this.isTeleport = conf.getBoolean("isTeleport", true);
		this.isBungee = conf.getBoolean("isBungee", false);
		this.cmdPortal = conf.getBoolean("cmdPortal", false);
		this.useGlobalConfig = conf.getBoolean("useGlobalConfig", true);

		ConfigurationSection extraConfig = conf;

		// /////////////////////
		// Global Port Stuff //
		// /////////////////////

		if (useGlobalConfig) {
			extraConfig = JumpPortsPlugin.getPlugin().getConfig().getConfigurationSection("globalPortConfig");
		}

		// After Effects
		ConfigurationSection effects = extraConfig.getConfigurationSection("afterEffects");
		Iterator<String> effectsIterator = effects.getKeys(false).iterator();

		while (effectsIterator.hasNext()) {
			String key = effectsIterator.next();
			PotionEffectType effect = PotionEffectType.getByName(key);
			if (effect == null) {
				JumpPortsPlugin.log("Invalid Potion Effect for " + name + " - " + key);
				break;
			}

			int duration = effects.getInt(key + ".duration", 40);
			int amplifier = effects.getInt(key + ".amplifier", 1);
			
			PotionEffect potionEffect = new PotionEffect(effect,duration,amplifier);
			afterEffects.add(potionEffect);
		}

		// Before Effects
		effects = extraConfig.getConfigurationSection("beforeEffects");
		effectsIterator = effects.getKeys(false).iterator();

		while (effectsIterator.hasNext()) {
			String key = effectsIterator.next();
			PotionEffectType effect = PotionEffectType.getByName(key);
			if (effect == null) {
				JumpPortsPlugin.log("Invalid Potion Effect for " + name + " - " + key);
				break;
			}

			int duration = effects.getInt(key + ".duration", 40);
			int amplifier = effects.getInt(key + ".amplifier", 1);
			
			PotionEffect potionEffect = new PotionEffect(effect,duration,amplifier);
			beforeEffects.add(potionEffect);
		}
		
		// Lightning & Misc
		this.harmlessLightningLeave = extraConfig.getBoolean("harmlessLightningLeave", false);
		this.harmlessLightningArrive = extraConfig.getBoolean("harmlessLightninArrive", false);
		this.teleportDelay = conf.getInt("teleportDelay", 50);

		// Triggers
		ConfigurationSection triggerSection = extraConfig.getConfigurationSection("triggers");
		if (triggerSection.getBoolean("sneak", false)) triggers.add(PortTrigger.SNEAK);
		if (triggerSection.getBoolean("sprint", false)) triggers.add(PortTrigger.SPRINT);
		if (triggerSection.getBoolean("jump", false)) triggers.add(PortTrigger.JUMP);
		if (triggerSection.getBoolean("fall", false)) triggers.add(PortTrigger.FALL);
		if (triggerSection.getBoolean("fireArrow", false)) triggers.add(PortTrigger.ARROW);
		if (triggerSection.getBoolean("eggThrow", false)) triggers.add(PortTrigger.EGG);

		// /////////////////////
		// Normal Port Stuff //
		// /////////////////////

		// Target Location(s)
		ConfigurationSection confSection = conf.getConfigurationSection("targets");
		if (confSection != null) {
			Iterator<String> locs = confSection.getKeys(false).iterator();
			while (locs.hasNext()) {
				String key = locs.next();
				JPLocation loc = new JPLocation(confSection.getString(key + ".server"), confSection.getString(key + ".world"), confSection.getDouble(key + ".x"), confSection.getDouble(key + ".y"), confSection.getDouble(key + ".z"), Float.parseFloat(confSection.getString(key + ".yaw")), Float.parseFloat(confSection.getString(key + ".pitch")));
				this.locations.add(loc);
			}
		}

		// Commands
		confSection = conf.getConfigurationSection("commands");
		if (confSection != null) {
			Iterator<String> cmds = confSection.getKeys(false).iterator();
			while (cmds.hasNext()) {
				String key = cmds.next();
				PortCommand cmd = new PortCommand(PortCommand.getTypeFromString(confSection.getString(key + ".type")), confSection.getString(key + ".command"));

				this.commands.add(cmd);
			}
		}

		this.blacklist = conf.getStringList("blacklist");
		this.whitelist = conf.getStringList("whitelist");

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
	}

	public void delete() {
		confFile.delete();
	}

	public JPLocation getTarget() {
		int size = locations.size();
		if (isTeleport) {
			if (size > 0) {
				int item = new Random().nextInt(size);
				int i = 0;
				for (JPLocation loc : locations) {
					if (i == item) {
						return loc;
					}
					i++;
				}
			}
		}
		return null;
	}

	public void addTarget(JPLocation loc) {
		locations.add(loc);
		save();
	}

	public void deleteTargets() {
		locations.clear();
		save();
	}

	public List<PortCommand> getCommands() {
		return commands;
	}

	public boolean hasCommand(String cmdStr) {
		for (PortCommand cmd : commands) {
			if (cmd.getCommand().equals(cmdStr)) return true;
		}
		return false;
	}

	public boolean hasCommand(PortCommand cmd) {
		if (commands.contains(cmd)) return true;
		return false;
	}

	public void addCommand(PortCommand cmd) {
		if (!hasCommand(cmd)) {
			commands.add(cmd);
			save();
		}
	}

	public void removeCommand(String cmdStr) {
		for (PortCommand cmd : commands) {
			if (cmd.getCommand().equals(cmdStr)) commands.remove(cmd);
		}
		save();
	}

	public void removeCommand(PortCommand cmd) {
		if (commands.contains(cmd)) commands.remove(cmd);
		save();
	}

	public void deleteCommands() {
		commands.clear();
		save();
	}
}
