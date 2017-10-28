package jp.kentan.minecraft.neko_world_regen.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginLoader {

    public static MultiverseCore detectMultiverseCore() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin == null || !(plugin instanceof MultiverseCore)) {
            Log.warn("failed to detect Multiverse-Core");
            return null;
        }

        return (MultiverseCore) plugin;
    }

    public static WorldGuardPlugin detectWorldGuard(){
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            Log.warn("failed to detect WorldGuard");
            return null;
        }

        Log.print("WorldGuard detected.");

        return (WorldGuardPlugin) plugin;
    }

    public static boolean enabledWorldGuard() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard");
    }

    public static boolean enabledFastAsyncWorldEdit() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }
}
