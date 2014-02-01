package net.dwdg.jumpports.objects;

public enum PortFlag {

	Unique, // Makes this port unique from the default config, and will use it's own effects/flags rather than the "global" ones.
	
	Instant, // Do not use any triggers, commence teleportation straight away.
	
	Price, // Use economy to pay for this port.
	Permission, // Player must have a certain permission node to use this port.
	
	Lightning_Arrive, // Lightning effect on leave location
	Lightning_Leave, // lightning effect on arrive location
	
	Effects_Arrive, // Previously "after effects"
	Effects_Leave, // Previously "before effects"
	
	PlayerCommand_Arrive, // Run player commands on arrival.
	PlayerCommand_Leave, // Run player commands on leaving.
	
	ConsoleCommand_Arrive, // Run console command on arrival.
	ConsoleCommand_Leave, // Run console command on leaving.
	
	Message_Arrive, // Display a message to the user on arrival
	Message_Leave, // Display a message when the user leaves
	
	Broadcast_Arrive, // Send a message to all players about the arrival
	Broadcast_Leave, // Send a message to all players about leaving.
	
	BungeeBroadcast_Arrive, // Send a message to ALL players connected to the bungeecord on arrival.
	BungeeBroadcast_Leave, // Send a message to ALL players connected to the bungeecord on leaving.
	
	Bungee, // Enable cross-server teleportation (This will just take them to that server, not to an X,Y,Z coord.
	BungeePrecise, // Teleports to a specific location on target server (world, x, y, z, pitch, yaw etc)
	
	TargetRandom, // Choose a random target out of the assigned ones.
	TargetSpecific, // Only go to a single target (specified - good for disabling other targets)
	
	Disable, // Make this port only available to people with jumpports.bypass permission.
	Disable_Default_Messages, // Disables the default JumpPort "You have been teleported" messages.
	
}
