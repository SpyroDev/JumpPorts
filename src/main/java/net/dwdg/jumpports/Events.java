package net.dwdg.jumpports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.dwdg.jumpports.bungee.JPBungee;
import net.dwdg.jumpports.player.RDPlayer;
import net.dwdg.jumpports.player.RDPlayers;
import net.dwdg.jumpports.util.JPLocation;
import net.dwdg.jumpports.util.PortCommand;
import net.dwdg.jumpports.util.PortTrigger;

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

public class Events implements Listener {

    private static JumpPortsPlugin plugin = null;
    public static ArrayList<String> teleportQueue = new ArrayList<String>();
    private static Set<String> ignoredPlayers = new HashSet<String>();
    private static Set<String> afterEffects = new HashSet<String>();
    private static Set<String> cmdDonePlayers = new HashSet<String>();
    private static Set<Location> ignoreIgnite = new HashSet<Location>();

    public Events(JumpPortsPlugin plugin) {
        Events.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (JumpPortsPlugin.permission.playerHas(event.getPlayer(), "jumpports.update")) {
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
        if (cmdDonePlayers.contains(name)) {
            cmdDonePlayers.remove(name);
        }
    }

    private boolean preChecks(Player player, Location moveLocation) {
        // Are they already being teleported?
        if (!teleportQueue.contains(player.getName())) {
            // Moved into a telepad?
            if (JumpPorts.isInPort(moveLocation)) {
                JumpPort port = JumpPorts.getPort(moveLocation);

                if (port.isEnabled()) {

                    if (port.canTeleport(player)) {

                        JPLocation loc = port.getTarget();
                        if (loc != null || !port.isTeleport()) {
                            return true;
                        } else {
                            if (!ignoredPlayers.contains(player.getName())) {
                                if (port.isTeleport()) {
                                    JumpPortsPlugin.debug("Action| Player: " + player.getName() + ", Action: No target for port");
                                    player.sendMessage(Lang.get("port.noTarget"));
                                    ignoredPlayers.add(player.getName());
                                }
                            }
                        }
                    } else {
                        if (!ignoredPlayers.contains(player.getName())) {
                            JumpPortsPlugin.debug("Action| Player: " + player.getName() + ", Action: No permission to use");
                            player.sendMessage(Lang.get("port.noPermission"));
                            ignoredPlayers.add(player.getName());
                        }
                    }
                } else {
                    // Set that we've told them, don't show again until they
                    // exit the pad/join the pad
                    if (!ignoredPlayers.contains(player.getName())) {
                        JumpPortsPlugin.debug("Action| Player: " + player.getName() + ", Action: Port Disabled");
                        player.sendMessage(Lang.get("port.disabled"));
                        ignoredPlayers.add(player.getName());
                    }
                }
            } else {
                String playername = player.getName();
                JumpPort port = JumpPorts.getPort(RDPlayers.getPlayer(playername).getString("targetPort"));

                if (port != null) {
                    if (port.isTeleport()) {
                        if (ignoredPlayers.contains(playername)) {
                            if (!teleportQueue.contains(playername)) {
                                JumpPortsPlugin.debug("Action| Player: " + player.getName() + ", Action: Teleport Cancelled");
                                ignoredPlayers.remove(playername);
                                player.sendMessage(Lang.get("port.cancelled"));
                            }
                        }
                    } else {
                        if (cmdDonePlayers.contains(playername)) {
                            cmdDonePlayers.remove(playername);
                            if (ignoredPlayers.contains(playername)) {
                                ignoredPlayers.remove(playername);
                            }
                            JumpPortsPlugin.debug("Action| Player: " + player.getName() + ", Action: CMD Portal Cancelled");
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean checkInstant(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        JumpPort port = JumpPorts.getPort(event.getTo());

        if (port == null) {
            return false;
        }

        if (port.isInstant()) {
            teleportPlayer(player, port);
            return true;
        }
        return false;
    }

    public boolean checkJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        JumpPort port = JumpPorts.getPort(event.getTo());

        if (port.hasTrigger(PortTrigger.JUMP)) {
            if (event.getTo().getY() > event.getFrom().getY()) {
                teleportPlayer(player, port);
                return true;
            }
        }
        return false;
    }

    public boolean checkFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        JumpPort port = JumpPorts.getPort(event.getTo());

        if (port.hasTrigger(PortTrigger.FALL)) {
            if (event.getTo().getY() < event.getFrom().getY()) {
                teleportPlayer(player, port);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Respect other plugins
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        if (preChecks(player, event.getTo())) {
            JumpPort port = JumpPorts.getPort(event.getTo());

            if (checkInstant(event)) {
                return;
            }
            if (checkJump(event)) {
                return;
            }
            if (checkFall(event)) {
                return;
            }

            // Tell them info about the pad
            if (!ignoredPlayers.contains(player.getName())) {
                player.sendMessage(Lang.get("port.triggered").replaceAll("%N", port.getName()).replaceAll("%D", port.getDescription()));
                if (port.getPrice() > 0) {
                    player.sendMessage(Lang.get("port.price").replaceAll("%P", "" + port.getPrice()));
                }
                player.sendMessage(Lang.get("port.triggers"));
                ignoredPlayers.add(player.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        // Respect other plugins
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();

        if (preChecks(player, player.getLocation())) {
            JumpPort port = JumpPorts.getPort(player.getLocation());
            if (port.hasTrigger(PortTrigger.SNEAK)) {
                if (player.isSneaking()) {
                    teleportPlayer(player, port);
                    return;
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
        Player player = event.getPlayer();

        if (preChecks(player, player.getLocation())) {
            JumpPort port = JumpPorts.getPort(player.getLocation());
            if (port.hasTrigger(PortTrigger.SPRINT)) {
                if (player.isSprinting()) {
                    teleportPlayer(player, port);
                    return;
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
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (preChecks(player, player.getLocation())) {
                JumpPort port = JumpPorts.getPort(player.getLocation());
                if (port.hasTrigger(PortTrigger.ARROW)) {
                    teleportPlayer(player, port);
                    return;
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
                    event.getPlayer().sendMessage(Lang.get("select.point1").replaceAll("%X", "" + block.getX()).replaceAll("%Y", "" + block.getY()).replaceAll("%Z", "" + block.getZ()));
                    JumpPortsPlugin.debug("Action | Player: " + event.getPlayer().getName() + ", Action: Point 1, W:" + block.getWorld().getName() + ", X:" + block.getX() + " Y:" + block.getY() + ", Z:" + block.getZ());
                } else if (rdp.getInt("regionBlockMode") == 1) {
                    Block block = event.getClickedBlock();
                    rdp.set("regionBlocks.2.x", block.getX());
                    rdp.set("regionBlocks.2.y", block.getY());
                    rdp.set("regionBlocks.2.z", block.getZ());
                    rdp.set("regionBlockMode", 2);
                    event.getPlayer().sendMessage(Lang.get("select.point2").replaceAll("%X", "" + block.getX()).replaceAll("%Y", "" + block.getY()).replaceAll("%Z", "" + block.getZ()));
                    JumpPortsPlugin.debug("Action | Player: " + event.getPlayer().getName() + ", Action: Point 2, W:" + block.getWorld().getName() + ", X:" + block.getX() + " Y:" + block.getY() + ", Z:" + block.getZ());
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
                    JumpPortsPlugin.debug("Action | Player: " + event.getPlayer().getName() + ", Action: Points Reset");
                }
            }
        }
    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event) {
        Player player = event.getPlayer();

        if (preChecks(player, player.getLocation())) {
            JumpPort port = JumpPorts.getPort(player.getLocation());
            if (port.hasTrigger(PortTrigger.EGG)) {
                teleportPlayer(player, port);
                return;
            }
        }
    }

    public void applyBeginEffects(Player player) {
        JumpPort port = JumpPorts.getPort(RDPlayers.getPlayer(player.getName()).getString("targetPort"));

        if (port.getBeforeEffects().size() > 0) {

            // Override other teleports is set to true.
            List<PotionEffect> effects = port.getBeforeEffects();

            for (PotionEffect effect : effects) {

                player.addPotionEffect(effect);
                JumpPortsPlugin.debug("BeforeEffect | Player: " + player.getName() + ", Effect: " + effect.getType().getName() + ", Duration: " + effect.getDuration() + ", Amplifier: " + effect.getAmplifier());
            }
        }
    }

    public static void applyAfterEffects() {
        for (String p : afterEffects) {
            Player player = Bukkit.getServer().getPlayer(p);
            if (player == null) {
                break;
            }
            JumpPort port = JumpPorts.getPort(RDPlayers.getPlayer(p).getString("targetPort"));

            // Harmless Lightning
            if (port.isHarmlessLightningArrive()) {
                ignoreIgnite.add(player.getWorld().getBlockAt(player.getLocation()).getLocation());
                player.getWorld().strikeLightningEffect(player.getLocation());
            }

            // After Effects
            if (port.getAfterEffects().size() > 0) {

                // Override other teleports is set to true.
                List<PotionEffect> effects = port.getAfterEffects();
                for (PotionEffect effect : effects) {

                    if (effect == null) {
                        JumpPortsPlugin.log("Invalid Potion Effect on " + port.getName());
                        break;
                    }

                    player.addPotionEffect(effect);
                    JumpPortsPlugin.debug("AfterEffect | Player: " + player.getName() + ", Effect: " + effect.getType().getName() + ", Duration: " + effect.getDuration() + ", Amplifier: " + effect.getAmplifier());
                }
                afterEffects.remove(p);
            }
        }
    }

    public void teleportPlayer(Player player, JumpPort port) {
        JumpPortsPlugin.debug("Port: " + port.getName() + ", Desc:" + port.getDescription() + ", Price: " + port.getPrice() + ", Instant: " + ((port.isInstant() == true) ? "true" : "false") + ", Enabled: " + ((port.isEnabled() == true) ? "true" : "false"));

        RDPlayer rdp = RDPlayers.getPlayer(player.getName());

        rdp.set("targetPort", port.getName());

        if (port.getPrice() > 0) {
            JumpPortsPlugin.debug("Price is higher than 0");
            if (JumpPortsPlugin.economy.has(player.getName(), port.getPrice())) {
                JumpPortsPlugin.debug("Has enough funds. Player: " + JumpPortsPlugin.economy.getBalance(player.getName()) + ", Port: " + port.getPrice());
            } else {
                JumpPortsPlugin.debug("Not enough funds. Player: " + JumpPortsPlugin.economy.getBalance(player.getName()) + ", Port: " + port.getPrice());
                player.sendMessage(Lang.get("port.notEnoughFunds").replaceAll("%D", port.getDescription()).replaceAll("%N", port.getName()).replaceAll("%P", "" + port.getPrice()));
                return;
            }
        }

        if (port.isCmdPortal()) {
            if (!cmdDonePlayers.contains(player.getName())) {
                if (port.getCommands().size() > 0) {
                    for (PortCommand cmd : port.getCommands()) {
                        if (cmd.getCommandType().equals(PortCommand.Type.CONSOLE)) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.getCommand().replace("%player%", player.getName()));
                            JumpPortsPlugin.debug("Execute Console Command: " + player.getName() + " - /" + cmd.getCommand());
                        } else {
                            player.chat("/" + cmd.getCommand());
                            JumpPortsPlugin.debug("Execute Player Command: " + player.getName() + " - /" + cmd.getCommand());
                        }
                    }
                    cmdDonePlayers.add(player.getName());
                }
            }
        }

        boolean ableToTeleport = false;

        // Is this a Bungee teleport?
        JPLocation target = port.getTarget();
        if (port.isTeleport()) {
            if (!target.getServer().equals("local")) {
                // This portal goes to another server within Bungee, so check with
                // that server if we are able to teleport this player
                // to that server.
                JPBungee.checkTeleportLoc(target);
            }
        }

        if (ableToTeleport) {
            JumpPortsPlugin.economy.withdrawPlayer(player.getName(), port.getPrice());
        }

        if (!port.isInstant()) {
            applyBeginEffects(player);

            if (port.isTeleport()) {
                teleportQueue.add(player.getName());
                rdp.set("target.world", target.getWorld());
                rdp.set("target.x", target.getX());
                rdp.set("target.y", target.getY());
                rdp.set("target.z", target.getZ());
                rdp.set("target.yaw", (double) target.getYaw());
                rdp.set("target.pitch", (double) target.getPitch());

                // Start timer
                JumpPortsPlugin.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        Events.processQueue();
                    }
                }, port.getTeleportDelay());
            }

        } else {
            applyBeginEffects(player);
            if (port.isTeleport()) {
                // Lightning?
                if (port.isHarmlessLightningLeave()) {
                    ignoreIgnite.add(player.getWorld().getBlockAt(player.getLocation()).getLocation());
                    player.getWorld().strikeLightningEffect(player.getLocation());

                }
                
                JumpPortsPlugin.debug("JPTarget: s="+target.getServer()+", w="+target.getWorld()+", x="+target.getX()+", y="+target.getY()+", z="+target.getZ()+", yaw="+target.getYaw()+", pitch="+target.getPitch());
                
                player.teleport(new Location(Bukkit.getWorld(target.getWorld()),target.getX(),target.getY(),target.getZ(),target.getYaw(),target.getPitch()));

                if (JumpPortsPlugin.getPlugin().getConfig().getBoolean("overrideTeleport") == false) {
                    afterEffects.add(player.getName());
                    JumpPortsPlugin.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            Events.applyAfterEffects();
                        }
                    }, 1L);
                }
                teleportQueue.remove(player.getName());
                ignoredPlayers.remove(player.getName());
                cmdDonePlayers.remove(player.getName());
                player.sendMessage(Lang.get("port.onArrival").replaceAll("%D", port.getDescription()).replaceAll("%N", port.getName()));
            }
        }
    }

    public static void processQueue() {
        for (String p : teleportQueue) {
            Player player = Bukkit.getServer().getPlayer(p);
            if (player == null) {
                break;
            }
            JumpPort port = JumpPorts.getPort(RDPlayers.getPlayer(p).getString("targetPort"));

            // Lightning?
            if (port.isHarmlessLightningLeave()) {
                ignoreIgnite.add(player.getWorld().getBlockAt(player.getLocation()).getLocation());
                player.getWorld().strikeLightningEffect(player.getLocation());

            }

            RDPlayer rdp = RDPlayers.getPlayer(p);
            Location target = new Location(Bukkit.getWorld(rdp.getString("target.world")), rdp.getDouble("target.x"), rdp.getDouble("target.y"), rdp.getDouble("target.z"), Float.parseFloat(rdp.getString("target.yaw")), Float.parseFloat(rdp.getString("target.pitch")));

            player.teleport(target);

            if (JumpPortsPlugin.getPlugin().getConfig().getBoolean("overrideTeleport") == false) {
                afterEffects.add(Bukkit.getServer().getPlayer(p).getName());
                JumpPortsPlugin.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        Events.applyAfterEffects();
                    }
                }, 1L);
            }
            teleportQueue.remove(p);
            ignoredPlayers.remove(p);
            cmdDonePlayers.remove(p);
            return;
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // If other plugin's have already cancelled this event
        if (event.isCancelled()) {
            return;
        }

        if (!ignoredPlayers.contains(event.getPlayer().getName())) {
            // Moved into a telepad?
            if (JumpPorts.isInPort(event.getPlayer().getLocation())) {
                ignoredPlayers.add(event.getPlayer().getName());
            }
        }

        if (plugin.getConfig().getBoolean("overrideTeleport") == true) {
            afterEffects.add(event.getPlayer().getName());
            JumpPortsPlugin.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    Events.applyAfterEffects();
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            if (ignoreIgnite.contains(event.getBlock().getLocation())) {
                event.setCancelled(true);
                ignoreIgnite.remove(event.getBlock().getLocation());
                JumpPortsPlugin.debug("Cancelled Ignite event.");
            }
        }
    }
}
