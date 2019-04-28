package jp.kentan.minecraft.neko_world_regen.regen.provider;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;

public class SchematicProvider {
    public static void paste(File file, World world, Location position) throws Exception {
        ClipboardFormats.findByFile(file)
                .load(file)
                .paste(BukkitAdapter.adapt(world), BlockVector3.at(position.getX(), position.getY(), position.getZ()));
    }
}
