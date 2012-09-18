package net.roguedraco.jumpports.commands;

import java.util.Collections;
import java.util.List;

import net.roguedraco.jumpports.JumpPort;
import net.roguedraco.jumpports.JumpPorts;
import net.roguedraco.jumpports.JumpPortsComparator;
import net.roguedraco.jumpports.JumpPortsPlugin;
import net.roguedraco.jumpports.lang.Lang;
import net.roguedraco.jumpports.player.RDPlayer;
import net.roguedraco.jumpports.player.RDPlayers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;

public class JumpPortCommands {

	@Command(aliases = { "select", "sel", "s" }, usage = "", flags = "", desc = "Toggle region selection mode", help = "Toggles your region selection mode.", min = 0, max = 0)
	@CommandPermissions("jumpports.admin.select")
	public static void select(CommandContext args, CommandSender sender)
			throws CommandException {
		RDPlayer rdp = RDPlayers.getPlayer(sender.getName());
		if (rdp.getBoolean("regionSelection") == true) {
			rdp.set("regionSelection", false);
			sender.sendMessage(Lang.get("select.disabled"));
		} else {
			rdp.set("regionSelection", true);
			sender.sendMessage(Lang.get("select.enabled"));
		}
	}

	@Command(aliases = { "create", "c" }, usage = "[portname]", flags = "", desc = "Creates a Port", help = "Create a JumpPort", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.create")
	public static void create(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) == null) {
			RDPlayer rdp = RDPlayers.getPlayer(sender.getName());
			if (rdp.getInt("regionBlocks.1.y") > -1
					&& rdp.getInt("regionBlocks.2.y") > -1) {
				if (JumpPortsPlugin.getPlugin().getConfig()
						.getBoolean("quitSelection", true) == true) {
					rdp.set("regionSelection", false);
				}
				JumpPort newPort = new JumpPort(args.getString(0));
				newPort.setRegion(rdp.getString("regionBlocks.world"),
						rdp.getInt("regionBlocks.1.x"),
						rdp.getInt("regionBlocks.1.y"),
						rdp.getInt("regionBlocks.1.z"),
						rdp.getInt("regionBlocks.2.x"),
						rdp.getInt("regionBlocks.2.y"),
						rdp.getInt("regionBlocks.2.z"));
				newPort.save();
				JumpPorts.addPort(newPort);
				sender.sendMessage(Lang.get("createdRegion").replaceAll("%N",
						args.getString(0)));
			} else {
				sender.sendMessage(Lang.get("exceptions.noRegionSelected"));
			}
		} else {
			sender.sendMessage(Lang.get("exceptions.portAlreadyExists")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "list", "l" }, usage = "[page]", flags = "", desc = "Lists all ports", help = "Lists all JumpPorts", min = 0, max = 1)
	@CommandPermissions("jumpports.list")
	@Console
	public static void list(CommandContext args, CommandSender sender)
			throws CommandException {
		List<JumpPort> ports = JumpPorts.getList();
		Collections.sort(ports, new JumpPortsComparator());

		final int totalSize = ports.size();
		final int pageSize = 6;
		final int pages = (int) Math.ceil(totalSize / (float) pageSize);
		int page = args.getInteger(0, 1) - 1;

		if (page < 0) {
			page = 0;
		}

		if (page < pages) {
			sender.sendMessage(Lang.get("list.ports")
					.replaceAll("%P", "" + (page + 1))
					.replaceAll("%T", "" + pages));
			for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
				if (i >= totalSize) {
					break;
				}
				JumpPort port = ports.get(i);
				String price = "" + port.getPrice();
				if (port.getPrice() == 0.00) {
					price = "Free";
				}
				if (port.isEnabled()) {
					sender.sendMessage(Lang.get("list.entryEnabled")
							.replaceAll("%N", port.getName())
							.replaceAll("%D", port.getDescription())
							.replaceAll("%P", "" + price));
				} else {
					sender.sendMessage(Lang.get("list.entryDisabled")
							.replaceAll("%N", port.getName())
							.replaceAll("%D", port.getDescription())
							.replaceAll("%P", "" + price));
				}
			}
		} else {
			sender.sendMessage(Lang.get("exceptions.invalidPage"));
		}

	}

	@Command(aliases = { "setprice", "price", "p" }, usage = "[port] [price]", flags = "", desc = "Sets the price for a port", help = "Sets the price of the port", min = 2, max = 2)
	@CommandPermissions("jumpports.admin.price")
	@Console
	public static void setprice(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPort port = JumpPorts.getPort(args.getString(0));
			port.setPrice(args.getDouble(1));
			sender.sendMessage(Lang.get("commands.setPrice")
					.replaceAll("%P", "" + args.getDouble(1))
					.replaceAll("%N", port.getName()));
			port.save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "redefine", "red", "r" }, usage = "[port]", flags = "", desc = "Redefines the region of a port", help = "Re defines the region of a port", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.redefine")
	public static void redefine(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			RDPlayer rdp = RDPlayers.getPlayer(sender.getName());
			if (JumpPortsPlugin.getPlugin().getConfig()
					.getBoolean("quitSelection", true) == true) {
				rdp.set("regionSelection", false);
			}
			JumpPort port = JumpPorts.getPort(args.getString(0));
			port.setRegion(rdp.getString("regionBlocks.world"),
					rdp.getInt("regionBlocks.1.x"),
					rdp.getInt("regionBlocks.1.y"),
					rdp.getInt("regionBlocks.1.z"),
					rdp.getInt("regionBlocks.2.x"),
					rdp.getInt("regionBlocks.2.y"),
					rdp.getInt("regionBlocks.2.z"));
			port.save();
			sender.sendMessage(Lang.get("commands.setRegion").replaceAll("%N",
					args.getString(0)));
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "delete", "del", "d" }, usage = "[port]", flags = "", desc = "Delete a port", help = "Deletes the port", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.delete")
	@Console
	public static void delete(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPorts.removePort(args.getString(0));
			sender.sendMessage(Lang.get("commands.portDeleted").replaceAll(
					"%N", args.getString(0)));
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "target", "tar", "t" }, usage = "[port]", flags = "", desc = "Add a target for a port", help = "Adds a target for port at your exact position", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.target")
	public static void target(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			Player player = (Player) sender;
			JumpPorts.getPort(args.getString(0))
					.addTarget(player.getLocation());
			sender.sendMessage(Lang.get("commands.targetAdded").replaceAll(
					"%N", args.getString(0)));
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "cleartargets", "ctar", "ct" }, usage = "[port]", flags = "", desc = "Removes all targets for a port", help = "Clears all targets for a port", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.cleartargets")
	@Console
	public static void cleartargets(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPorts.getPort(args.getString(0)).deleteTargets();
			sender.sendMessage(Lang.get("commands.targetsDeleted").replaceAll(
					"%N", args.getString(0)));
			JumpPorts.getPort(args.getString(0)).save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "enable", "en" }, usage = "[port]", flags = "", desc = "Enables a port", help = "Enables a port for use", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.enable")
	@Console
	public static void enable(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPorts.getPort(args.getString(0)).setEnabled(true);
			sender.sendMessage(Lang.get("commands.setEnabled").replaceAll("%N",
					args.getString(0)));
			JumpPorts.getPort(args.getString(0)).save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "disable", "di" }, usage = "[port]", flags = "", desc = "Disables a port", help = "Disables a port so it cannot be used", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.disable")
	@Console
	public static void disable(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPorts.getPort(args.getString(0)).setEnabled(false);
			sender.sendMessage(Lang.get("commands.setDisabled").replaceAll(
					"%N", args.getString(0)));
			JumpPorts.getPort(args.getString(0)).save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "instant", "in" }, usage = "[port]", flags = "", desc = "Toggle whether this port is instant", help = "Toggles a port for instant Teleport.", min = 1, max = 1)
	@CommandPermissions("jumpports.admin.instant")
	@Console
	public static void instant(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPort port = JumpPorts.getPort(args.getString(0));
			if (port.isInstant()) {
				port.setInstant(false);
				sender.sendMessage(Lang.get("commands.noLongerInstant")
						.replaceAll("%N", args.getString(0)));
				JumpPorts.getPort(args.getString(0)).save();

			} else {
				port.setInstant(true);
				sender.sendMessage(Lang.get("commands.nowInstant").replaceAll(
						"%N", args.getString(0)));
				JumpPorts.getPort(args.getString(0)).save();
			}
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}

	@Command(aliases = { "desc", "d" }, usage = "[port] [description]", flags = "", desc = "Sets port description", help = "Sets the description of a port", min = 1, max = -1)
	@CommandPermissions("jumpports.admin.desc")
	@Console
	public static void desc(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPort port = JumpPorts.getPort(args.getString(0));
			port.setDescription(args.getJoinedStrings(1));
			sender.sendMessage(Lang.get("commands.setDescription")
					.replaceAll("%N", args.getString(0))
					.replaceAll("%D", port.getDescription()));
			JumpPorts.getPort(args.getString(0)).save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}
	
	@Command(aliases = { "whitelist", "w" }, usage = "[port] [player1] [player2] ...", flags = "", desc = "Adds/removes players to the whitelist", help = "Choose what players can use this teleport", min = 2, max = -1)
	@CommandPermissions("jumpports.admin.whitelist")
	@Console
	public static void whitelist(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPort port = JumpPorts.getPort(args.getString(0));
			
			String addedPlayers = "";
			String removedPlayers = "";
			
			for(String player : args.getSlice(1)) {
				if(player.startsWith("-")) {
					port.removeFromWhitelist(player);
					removedPlayers += player+" ";
				}
				else {
					port.addToWhitelist(player);
					addedPlayers += player+" ";
				}
			}
			
			sender.sendMessage(Lang.get("commands.addedToWhitelist")
					.replaceAll("%N", args.getString(0))
					.replaceAll("%A", addedPlayers)
					.replaceAll("%R", removedPlayers));
			JumpPorts.getPort(args.getString(0)).save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}
	
	@Command(aliases = { "blacklist", "bl" }, usage = "[port] [player1] [player2] ...", flags = "", desc = "Adds/removes players to the blacklist", help = "Choose what players cannot use this teleport", min = 2, max = -1)
	@CommandPermissions("jumpports.admin.blacklist")
	@Console
	public static void blacklist(CommandContext args, CommandSender sender)
			throws CommandException {
		if (JumpPorts.getPort(args.getString(0)) != null) {
			JumpPort port = JumpPorts.getPort(args.getString(0));
			
			String addedPlayers = "";
			String removedPlayers = "";
			
			for(String player : args.getSlice(1)) {
				if(player.startsWith("-")) {
					port.removeFromBlacklist(player);
					removedPlayers += player+" ";
				}
				else {
					port.addToBlacklist(player);
					addedPlayers += player+" ";
				}
			}
			
			sender.sendMessage(Lang.get("commands.addedToBlacklist")
					.replaceAll("%N", args.getString(0))
					.replaceAll("%A", addedPlayers)
					.replaceAll("%R", removedPlayers));
			JumpPorts.getPort(args.getString(0)).save();
		} else {
			sender.sendMessage(Lang.get("exceptions.portDoesntExist")
					.replaceAll("%N", args.getString(0)));
		}
	}
}
