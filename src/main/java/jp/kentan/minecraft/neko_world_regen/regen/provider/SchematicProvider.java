package jp.kentan.minecraft.neko_world_regen.regen.provider;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;

public class SchematicProvider {
    public static void paste(File file, World world, Location position) throws Exception {
        EditSession editSession = ClipboardFormat.SCHEMATIC.load(file).paste(BukkitUtil.getLocalWorld(world), new Vector(position.getX(), position.getY(), position.getZ()));
        editSession.flushQueue();
    }
}
