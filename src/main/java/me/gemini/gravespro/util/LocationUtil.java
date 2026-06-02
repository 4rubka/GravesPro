package me.gemini.gravespro.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {

    public static String serializeLocation(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + ":" +
                location.getX() + ":" +
                location.getY() + ":" +
                location.getZ() + ":" +
                location.getYaw() + ":" +
                location.getPitch();
    }

    public static Location deserializeLocation(String data) {
        if (data == null || data.isEmpty()) return null;
        String[] parts = data.split(":");
        if (parts.length < 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

        return new Location(world, x, y, z, yaw, pitch);
    }
}
