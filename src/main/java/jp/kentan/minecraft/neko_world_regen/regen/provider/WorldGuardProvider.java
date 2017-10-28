package jp.kentan.minecraft.neko_world_regen.regen.provider;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import jp.kentan.minecraft.neko_world_regen.utils.PluginLoader;
import org.bukkit.Location;

public class WorldGuardProvider {
    private final static WorldGuardPlugin WORLD_GUARD = PluginLoader.detectWorldGuard();

    public static void protect(Location location, int size){
        if(WORLD_GUARD == null){
            return;
        }

        RegionContainer regionContainer = WORLD_GUARD.getRegionContainer();


        RegionManager manager = regionContainer.get(location.getWorld());

        if(manager == null){
            Log.warn("failed to get RegionManager.");
            return;
        }

        if(manager.hasRegion("spawn")){
            manager.removeRegion("spawn");
        }

        int half = size / 2;

        BlockVector min = new BlockVector(location.getBlockX() - half, 1, location.getBlockZ() - half);
        BlockVector max = new BlockVector(location.getBlockX() + half, 255, location.getBlockZ() + half);
        ProtectedRegion region = new ProtectedCuboidRegion("spawn", min, max);
        region.setFlag(DefaultFlag.USE, StateFlag.State.ALLOW);
        region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.ALLOW);

        manager.addRegion(region);
    }
}
