/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.roguedraco.jumpports.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import net.roguedraco.jumpports.JumpPort;
import net.roguedraco.jumpports.JumpPorts;
import net.roguedraco.jumpports.Lang;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Dan
 */
public class PortCommandsCommands {

    @Command(aliases = {"add", "a"}, usage = "[port] <command>", flags = "", desc = "Adds a command to run on enter", help = "The command the player is forced to execute", min = 2, max = -1)
    @CommandPermissions("jumpports.admin.commands.add")
    @Console
    public static void add(CommandContext args, CommandSender sender) throws CommandException {
        if (JumpPorts.getPort(args.getString(0)) != null) {
            JumpPort port = JumpPorts.getPort(args.getString(0));
            port.addCommand(args.getJoinedStrings(1));
            sender.sendMessage(Lang.get("commands.commands.add")
                    .replaceAll("%N", port.getName())
                    .replaceAll("%C", args.getJoinedStrings(1)));
        } else {
            sender.sendMessage(Lang.get("exceptions.portDoesntExist")
                    .replaceAll("%N", args.getString(0)));
        }
    }

    @Command(aliases = {"remove", "rm", "r"}, usage = "[port] <command>", flags = "", desc = "Removes a command to run on enter", help = "Remove a command you have previously set on the port", min = 2, max = -1)
    @CommandPermissions("jumpports.admin.commands.remove")
    @Console
    public static void remove(CommandContext args, CommandSender sender) throws CommandException {
        if (JumpPorts.getPort(args.getString(0)) != null) {
            JumpPort port = JumpPorts.getPort(args.getString(0));
            port.removeCommand(args.getJoinedStrings(1));
            sender.sendMessage(Lang.get("commands.commands.remove")
                    .replaceAll("%N", port.getName())
                    .replaceAll("%C", args.getJoinedStrings(1)));
        } else {
            sender.sendMessage(Lang.get("exceptions.portDoesntExist")
                    .replaceAll("%N", args.getString(0)));
        }
    }

    @Command(aliases = {"clear", "c"}, usage = "[port]", flags = "", desc = "Clears all commands on the port", help = "Remove all commands on the port", min = 1, max = 1)
    @CommandPermissions("jumpports.admin.commands.add")
    @Console
    public static void clear(CommandContext args, CommandSender sender) throws CommandException {
        if (JumpPorts.getPort(args.getString(0)) != null) {
            JumpPort port = JumpPorts.getPort(args.getString(0));
            sender.sendMessage(Lang.get("commands.commands.clear")
                    .replaceAll("%N", port.getName()));
            port.deleteCommands();
        } else {
            sender.sendMessage(Lang.get("exceptions.portDoesntExist")
                    .replaceAll("%N", args.getString(0)));
        }
    }
}