package net.roguedraco.jumpports;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.roguedraco.lang.Lang;

import org.bukkit.Location;

public class JumpPorts {

	public static Map<String, JumpPort> ports = new HashMap<String, JumpPort>();
	
	public static boolean isInPort(Location loc) {
		Collection<JumpPort> jports = ports.values();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		for (JumpPort port : jports) {
			if (port.hasBlock(x, y, z)) {
				return true;
			}
		}
		return false;
	}

	public static JumpPort getPort(Location loc) {
		Collection<JumpPort> jports = ports.values();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		for (JumpPort port : jports) {
			if (port.hasBlock(x, y, z)) {
				return port;
			}
		}
		return null;
	}

	public static JumpPort getPort(String name) {
		if (ports.containsKey(name)) {
			return ports.get(name);
		}
		return null;
	}

	public static void loadPorts() {
		File[] files = new File("plugins/JumpPorts/ports").listFiles();
		for (File file : files) {
			String name = file.getName();
			int pos = name.lastIndexOf('.');
			String ext = name.substring(pos + 1);
			if (ext.equalsIgnoreCase("yml")) {
				name = name.replaceAll(".yml", "");
				JumpPort port = new JumpPort(name);
				port.load();
				ports.put(name, port);
				JumpPortsPlugin.log(Lang.get("plugin.loaded")+": "+name);
			}
		}
	}

	public static void savePorts() {
		Collection<JumpPort> jports = ports.values();
		for (JumpPort port : jports) {
			port.save();
			JumpPortsPlugin.log(Lang.get("plugin.saved")+": "+port.getName());
		}
	}
	
	public static void addPort(JumpPort port) {
		ports.put(port.getName(), port);
	}
	
	public static void removePort(String name) {
		JumpPort port = ports.get(name);
		port.delete();
		ports.remove(name);
	}
	
	public static List<JumpPort> getList() {
		List<JumpPort> entries = new ArrayList<JumpPort>();
		for(JumpPort port : ports.values()) {
			entries.add(port);
		}
		return entries;
	}
}
