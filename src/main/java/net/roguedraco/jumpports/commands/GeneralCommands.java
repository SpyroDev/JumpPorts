package net.roguedraco.jumpports.commands;

import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;

public class GeneralCommands {
	
	@Command(aliases = { "jp", "jumpports" },usage = "", flags = "", desc = "JumpPort commands.",help = "Toggles your region selection mode.", min = 0, max = -1)
	@NestedCommand(value = { JumpPortCommands.class })
	public static void jp(CommandContext args, CommandSender sender) throws CommandException {
	}

}
