package jp.kentan.minecraft.neko_world_regen.regen.provider;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldGuardProvider {
    private final static WorldGuard WORLD_GUARD = WorldGuard.getInstance();

    public static void protect(Location location, int size){
        World world = location.getWorld();
        if(WORLD_GUARD == null || world == null){
            return;
        }

        RegionContainer regionContainer = WORLD_GUARD.getPlatform().getRegionContainer();


        RegionManager manager = regionContainer.get(BukkitAdapter.adapt(world));

        if(manager == null){
            Log.warn("failed to get RegionManager.");
            return;
        }

        if(manager.hasRegion("spawn")){
            manager.removeRegion("spawn");
        }

        int half = size / 2;

        BlockVector3 min = BlockVector3.at(location.getBlockX() - half, 1, location.getBlockZ() - half);
        BlockVector3 max = BlockVector3.at(location.getBlockX() + half, 255, location.getBlockZ() + half);
        ProtectedRegion region = new ProtectedCuboidRegion("spawn", min, max);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
        region.setFlag(Flags.LAVA_FLOW, StateFlag.State.DENY);

        manager.addRegion(region);
    }
}
