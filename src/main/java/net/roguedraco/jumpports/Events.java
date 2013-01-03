package net.roguedraco.jumpports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.roguedraco.jumpports.player.RDPlayer;
import net.roguedraco.jumpports.player.RDPlayers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Events implements Listener {

	private static JumpPortsPlugin plugin = null;
	public static ArrayList<String> teleportQueue = new ArrayList<String>();
	private static Set<String> ignoredPlayers = new HashSet<String>();
	private static Set<String> afterEffects = new HashSet<String>();
	private static Set<Location> ignoreIgnite = new HashSet<Location>();

	public Events(JumpPortsPlugin plugin) {
		Events.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (JumpPortsPlugin.permission.playerHas(event.getPlayer(),
				"jumpports.update")) {
			JumpPortsPlugin.getUpdater().updateNeeded(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		if (teleportQueue.contains(name)) {
			teleportQueue.remove(name);
		}
		if (ignoredPlayers.contains(name)) {
			ignoredPlayers.remove(name);
		}
		if (afterEffects.contains(name)) {
			afterEffects.remove(name);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// Respect other plugins
		if (event.isCancelled()) {
			return;
		}
		// Are they already being teleported?
		if (!teleportQueue.contains(event.getPlayer().getName())) {
			Player player = event.getPlayer();
			// Moved into a telepad?
			if (JumpPorts.isInPort(event.getTo())) {
				JumpPort port = JumpPorts.getPort(event.getTo());

				if (port.isEnabled()) {
					if (port.canTeleport(player)) {
						Location loc = port.getTarget();
						if (loc != null) {

							if (port.isInstant()) {
								teleportPlayer(player, port, loc);
								return;
							}

							// If Jump is on, check for jump
							if (JumpPortsPlugin.getPlugin().getConfig()
									.getBoolean("triggers.jump")) {
								if (event.getTo().getY() > event.getFrom()
										.getY()) {
									teleportPlayer(player, port, loc);
									return;
								}
							}

							// If fall is on, check for fall
							if (JumpPortsPlugin.getPlugin().getConfig()
									.getBoolean("triggers.fall")) {
								if (event.getTo().getY() < event.getFrom()
										.getY()) {
									teleportPlayer(player, port, loc);
									return;
								}
							}

							// Tell them info about the pad
							if (!ignoredPlayers.contains(player.getName())) {
								player.sendMessage(Lang
										.get("port.triggered")
										.replaceAll("%N", port.getName())
										.replaceAll("%D", port.getDescription()));
								if (port.getPrice() > 0) {
									player.sendMessage(Lang.get("port.price")
											.replaceAll("%P",
													"" + port.getPrice()));
								}
								player.sendMessage(Lang.get("port.triggers"));
								ignoredPlayers.add(player.getName());
							}
						} else {
							if (!ignoredPlayers.contains(player.getName())) {
								JumpPortsPlugin.debug("Action| Player: "
										+ player.getName()
										+ ", Action: No target for port");
								player.sendMessage(Lang.get("port.noTarget"));
								ignoredPlayers.add(player.getName());
							}
						}
					} else {
						if (!ignoredPlayers.contains(player.getName())) {
							JumpPortsPlugin.debug("Action| Player: "
									+ player.getName()
									+ ", Action: No permission to use");
							player.sendMessage(Lang.get("port.noPermission"));
							ignoredPlayers.add(player.getName());
						}
					}
				} else {
					// Set that we've told them, don't show again until they
					// exit the pad/join the pad
					if (!ignoredPlayers.contains(player.getName())) {
						JumpPortsPlugin.debug("Action| Player: "
								+ player.getName() + ", Action: Port Disabled");
						player.sendMessage(Lang.get("port.disabled"));
						ignoredPlayers.add(player.getName());
					}
				}
			} else {
				String playername = player.getName();
				if (ignoredPlayers.contains(playername)) {
					if (!teleportQueue.contains(playername)) {
						JumpPortsPlugin.debug("Action| Player: "
								+ player.getName()
								+ ", Action: Teleport Cancelled");
						ignoredPlayers.remove(playername);
						player.sendMessage(Lang.get("port.cancelled"));
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		// Respect other plugins
		if (event.isCancelled()) {
			return;
		}
		if (plugin.getConfig().getBoolean("triggers.sneak")) {
			if (event.isSneaking()) {
				if (!teleportQueue.contains(event.getPlayer().getName())) {
					Player player = event.getPlayer();
					// Moved into a telepad?
					if (JumpPorts.isInPort(event.getPlayer().getLocation())) {
						JumpPort port = JumpPorts.getPort(event.getPlayer()
								.getLocation());

						if (port.isEnabled()) {
							Location loc = port.getTarget();
							if (loc != null) {
								teleportPlayer(player, port, loc);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		// Respect other plugins
		if (event.isCancelled()) {
			return;
		}
		if (plugin.getConfig().getBoolean("triggers.sprint")) {
			if (event.isSprinting()) {
				if (!teleportQueue.contains(event.getPlayer().getName())) {
					Player player = event.getPlayer();
					// Moved into a telepad?
					if (JumpPorts.isInPort(event.getPlayer().getLocation())) {
						JumpPort port = JumpPorts.getPort(event.getPlayer()
								.getLocation());

						if (port.isEnabled()) {
							Location loc = port.getTarget();
							if (loc != null) {
								teleportPlayer(player, port, loc);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		// Respect other plugins
		if (event.isCancelled()) {
			return;
		}
		if (plugin.getConfig().getBoolean("triggers.fireArrow")) {
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				if (!teleportQueue.contains(player.getName())) {
					// Moved into a telepad?
					if (JumpPorts.isInPort(player.getLocation())) {
						JumpPort port = JumpPorts.getPort(player.getLocation());

						if (port.isEnabled()) {
							Location loc = port.getTarget();
							if (loc != null) {
								teleportPlayer(player, port, loc);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Respect other plugins
		if (event.isCancelled()) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			RDPlayer rdp = RDPlayers.getPlayer(event.getPlayer().getName());
			if (rdp.getBoolean("regionSelection") == true) {
				if (rdp.getInt("regionBlockMode") == 0) {
					Block block = event.getClickedBlock();
					rdp.set("regionBlocks.world", block.getWorld().getName());
					rdp.set("regionBlocks.1.x", block.getX());
					rdp.set("regionBlocks.1.y", block.getY());
					rdp.set("regionBlocks.1.z", block.getZ());
					rdp.set("regionBlockMode", 1);
					event.getPlayer().sendMessage(
							Lang.get("select.point1")
									.replaceAll("%X", "" + block.getX())
									.replaceAll("%Y", "" + block.getY())
									.replaceAll("%Z", "" + block.getZ()));
					JumpPortsPlugin.debug("Action | Player: "
							+ event.getPlayer().getName()
							+ ", Action: Point 1, W:"
							+ block.getWorld().getName() + ", X:"
							+ block.getX() + " Y:" + block.getY() + ", Z:"
							+ block.getZ());
				} else if (rdp.getInt("regionBlockMode") == 1) {
					Block block = event.getClickedBlock();
					rdp.set("regionBlocks.2.x", block.getX());
					rdp.set("regionBlocks.2.y", block.getY());
					rdp.set("regionBlocks.2.z", block.getZ());
					rdp.set("regionBlockMode", 2);
					event.getPlayer().sendMessage(
							Lang.get("select.point2")
									.replaceAll("%X", "" + block.getX())
									.replaceAll("%Y", "" + block.getY())
									.replaceAll("%Z", "" + block.getZ()));
					JumpPortsPlugin.debug("Action | Player: "
							+ event.getPlayer().getName()
							+ ", Action: Point 2, W:"
							+ block.getWorld().getName() + ", X:"
							+ block.getX() + " Y:" + block.getY() + ", Z:"
							+ block.getZ());
				} else {
					rdp.set("regionBlocks.world", null);
					rdp.set("regionBlocks.1.x", null);
					rdp.set("regionBlocks.1.y", null);
					rdp.set("regionBlocks.1.z", null);
					rdp.set("regionBlocks.2.x", null);
					rdp.set("regionBlocks.2.y", null);
					rdp.set("regionBlocks.2.z", null);
					rdp.set("regionBlockMode", 0);
					event.getPlayer().sendMessage(Lang.get("select.reset"));
					JumpPortsPlugin.debug("Action | Player: "
							+ event.getPlayer().getName()
							+ ", Action: Points Reset");
				}
			}
		}
	}

	@EventHandler
	public void onEggThrow(PlayerEggThrowEvent event) {
		if (plugin.getConfig().getBoolean("triggers.eggThrow")) {
			if (!teleportQueue.contains(event.getPlayer().getName())) {
				Player player = event.getPlayer();
				// Moved into a telepad?
				if (JumpPorts.isInPort(event.getPlayer().getLocation())) {
					JumpPort port = JumpPorts.getPort(event.getPlayer()
							.getLocation());

					if (port.isEnabled()) {
						Location loc = port.getTarget();
						if (loc != null) {
							teleportPlayer(player, port, loc);
						}
					}
				}
			}
		}
	}

	public void applyBeginEffects(Player player) {
		if (plugin.getConfig().getBoolean("beginEffect") == true) {
			// Override other teleports is set to true.
			Iterator<String> effects = plugin.getConfig()
					.getConfigurationSection("beginEffects").getKeys(false)
					.iterator();

			while (effects.hasNext()) {
				String key = effects.next();
				PotionEffectType effect = PotionEffectType.getByName(key);

				int duration = plugin.getConfig().getInt(
						"beginEffects." + key + ".duration", 80);
				int amplifier = plugin.getConfig().getInt(
						"beginEffects." + key + ".amplifier", 4);

				PotionEffect potionEffect = new PotionEffect(effect, duration,
						amplifier);
				player.addPotionEffect(potionEffect);
				JumpPortsPlugin.debug("BeginEffect | Player: "
						+ player.getName() + ", Effect: " + effect.getName()
						+ "|" + effect.getId() + ", Duration: " + duration
						+ ", Amplifier: " + amplifier);
			}
		}
	}

	public static void applyAfterEffects() {
		for (String p : afterEffects) {
			Player player = Bukkit.getServer().getPlayer(p);
			if (JumpPortsPlugin.getPlugin().getConfig()
					.getBoolean("harmlessLightningArrive", false) == true) {
				ignoreIgnite.add(player.getWorld().getBlockAt(player.getLocation()).getLocation());
				player.getWorld().strikeLightningEffect(player.getLocation());
			}
			if (JumpPortsPlugin.getPlugin().getConfig()
					.getBoolean("afterEffect") == true) {
				// Override other teleports is set to true.
				Iterator<String> effects = JumpPortsPlugin.getPlugin()
						.getConfig().getConfigurationSection("afterEffects")
						.getKeys(false).iterator();

				while (effects.hasNext()) {
					String key = effects.next();
					PotionEffectType effect = PotionEffectType.getByName(key);
					if(effect == null) {
						JumpPortsPlugin.log("Invalid Potion Effect - "+key);
						break;
					}
					int duration = JumpPortsPlugin.getPlugin().getConfig()
							.getInt("afterEffects." + key + ".duration", 50);
					int amplifier = JumpPortsPlugin.getPlugin().getConfig()
							.getInt("afterEffects." + key + ".amplifier", 4);

					PotionEffect potionEffect = new PotionEffect(effect,
							duration, amplifier);
					player.addPotionEffect(potionEffect);
					JumpPortsPlugin.debug("AfterEffect | Player: "
							+ player.getName() + ", Effect: "
							+ effect.getName() + "|" + effect.getId()
							+ ", Duration: " + duration + ", Amplifier: "
							+ amplifier);
				}
				afterEffects.remove(p);
			}
		}
	}

	public void teleportPlayer(Player player, JumpPort port, Location loc) {
		JumpPortsPlugin.debug("Port: " + port.getName() + ", Desc:"
				+ port.getDescription() + ", Price: " + port.getPrice()
				+ ", Instant: "
				+ ((port.isInstant() == true) ? "true" : "false")
				+ ", Enabled: "
				+ ((port.isEnabled() == true) ? "true" : "false"));
		if (port.getPrice() > 0) {
			JumpPortsPlugin.debug("Price is higher than 0");
			if (JumpPortsPlugin.economy.has(player.getName(), port.getPrice())) {
				JumpPortsPlugin.debug("Has enough funds. Player: "
						+ JumpPortsPlugin.economy.getBalance(player.getName())
						+ ", Port: " + port.getPrice());
				JumpPortsPlugin.economy.withdrawPlayer(player.getName(),
						port.getPrice());
			} else {
				JumpPortsPlugin.debug("Not enough funds. Player: "
						+ JumpPortsPlugin.economy.getBalance(player.getName())
						+ ", Port: " + port.getPrice());
				player.sendMessage(Lang.get("port.notEnoughFunds")
						.replaceAll("%D", port.getDescription())
						.replaceAll("%N", port.getName())
						.replaceAll("%P", "" + port.getPrice()));
				return;
			}
		}
		if (JumpPortsPlugin.getPlugin().getConfig().getInt("teleportDelay") > 0) {
			applyBeginEffects(player);

			teleportQueue.add(player.getName());
			RDPlayer rdp = RDPlayers.getPlayer(player.getName());
			rdp.set("target.world", loc.getWorld().getName());
			rdp.set("target.x", loc.getX());
			rdp.set("target.y", loc.getY());
			rdp.set("target.z", loc.getZ());
			rdp.set("target.yaw", (double) loc.getYaw());
			rdp.set("target.pitch", (double) loc.getPitch());

			// Start timer
			JumpPortsPlugin
					.getPlugin()
					.getServer()
					.getScheduler()
					.scheduleSyncDelayedTask(
							plugin,
							new Runnable() {

								public void run() {
									Events.processQueue();
								}

							},
							JumpPortsPlugin.getPlugin().getConfig()
									.getInt("teleportDelay"));

		} else {
			applyBeginEffects(player);
			player.teleport(loc);
			player.sendMessage(Lang.get("port.onArrival")
					.replaceAll("%D", port.getDescription())
					.replaceAll("%N", port.getName()));
		}
	}

	public static void processQueue() {
		for (String p : teleportQueue) {
			Player player = Bukkit.getServer().getPlayer(p);
			// Lightning?
			if (JumpPortsPlugin.getPlugin().getConfig()
					.getBoolean("harmlessLightningLeave", false) == true) {
				ignoreIgnite.add(player.getWorld().getBlockAt(player.getLocation()).getLocation());
				player.getWorld().strikeLightningEffect(player.getLocation());
				
			}

			RDPlayer rdp = RDPlayers.getPlayer(p);
			Location target = new Location(Bukkit.getWorld(rdp
					.getString("target.world")), rdp.getDouble("target.x"),
					rdp.getDouble("target.y"), rdp.getDouble("target.z"),
					Float.parseFloat(rdp.getString("target.yaw")),
					Float.parseFloat(rdp.getString("target.pitch")));
			player.teleport(target);
			if (JumpPortsPlugin.getPlugin().getConfig()
					.getBoolean("overrideTeleport") == false) {
				afterEffects.add(Bukkit.getServer().getPlayer(p).getName());
				JumpPortsPlugin.getPlugin().getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {

							public void run() {
								Events.applyAfterEffects();
							}

						}, 1L);
			}
			teleportQueue.remove(p);
			return;
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		// If other plugin's have already cancelled this event
		if (event.isCancelled()) {
			return;
		}

		if (plugin.getConfig().getBoolean("overrideTeleport") == true) {
			afterEffects.add(event.getPlayer().getName());
			JumpPortsPlugin.getPlugin().getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {

						public void run() {
							Events.applyAfterEffects();
						}

					}, 1L);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent event)
    {
      if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
    	  if(ignoreIgnite.contains(event.getBlock().getLocation())) {
    		  event.setCancelled(true);
    		  ignoreIgnite.remove(event.getBlock().getLocation());
    		  JumpPortsPlugin.debug("Cancelled Ignite event.");
    	  }
      }
    }

}
