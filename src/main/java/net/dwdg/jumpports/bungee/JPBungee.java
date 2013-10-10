/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dwdg.jumpports.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import net.dwdg.jumpports.JumpPortsPlugin;
import net.dwdg.jumpports.util.JPLocation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 *
 * @author Dan
 */
public class JPBungee implements PluginMessageListener {

    private static ByteArrayOutputStream b = new ByteArrayOutputStream();
    private static DataOutputStream out = new DataOutputStream(b);

    public static boolean checkTeleportLoc(JPLocation loc) {
        String server = loc.getServer();

        try {
            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF("JumpPorts");

            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);

            msgout.writeUTF("CHKLOC:" + loc.getWorld() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getY());
            msgout.writeShort(123);

            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());

        } catch (Exception e) {
        }

        return false;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("JumpPorts")) {
            return;
        }

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();
            JumpPortsPlugin.debug("Received Plugin Message: Channel: "+subchannel + ", Message: " + message.toString());
            
        } catch (Exception e) {
        }
    }
}
