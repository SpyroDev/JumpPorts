/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dwdg.jumpports.util;

import org.bukkit.Location;

/**
 *
 * @author Dan
 */
public class JPLocation {

    private double x = 0.00;
    private double y = 0.00;
    private double z = 0.00;
    private float yaw;
    private float pitch;
    private String world = "";
    private String server = "";

    public JPLocation(String server, String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.server = server;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public JPLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.server = "local";
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getWorld() {
        return world;
    }

    public String getServer() {
        return server;
    }
}
