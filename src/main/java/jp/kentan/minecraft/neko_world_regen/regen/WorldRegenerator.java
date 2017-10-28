package jp.kentan.minecraft.neko_world_regen.regen;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import jp.kentan.minecraft.neko_world_regen.NekoWorldRegen;
import jp.kentan.minecraft.neko_world_regen.regen.provider.SchematicProvider;
import jp.kentan.minecraft.neko_world_regen.regen.provider.WorldGuardProvider;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import jp.kentan.minecraft.neko_world_regen.utils.PluginLoader;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.Random;

public class WorldRegenerator {

    private final static Server SERVER = Bukkit.getServer();
    private final static Random RANDOM = new Random();

    private final static ConsoleCommandSender CMD_SENDER = Bukkit.getConsoleSender();

    private static MVWorldManager sWorldManager;


    public static boolean setup(){
        final MultiverseCore multiverseCore = PluginLoader.detectMultiverseCore();

        if(multiverseCore == null){
            Log.warn("Could not detect MultiverseCore.");
            return false;
        }

        sWorldManager = multiverseCore.getMVWorldManager();


        if(!PluginLoader.enabledWorldGuard()){
            Log.print("Could not detect WorldGuard(Optional).");
        }

        if(!PluginLoader.enabledFastAsyncWorldEdit()){
            Log.print("Could not detect FastAsyncWorldEdit(Optional).");
        }

        return true;
    }

    public static void regen(final RegenParameter param){
        SERVER.broadcastMessage(WARN_MESSAGE);

        removeWorld(param.WORLD_NAME);

        //World regen
        if(!sWorldManager.addWorld(param.WORLD_NAME, param.ENVIRONMENT, String.valueOf(RANDOM.nextLong()), param.WORLD_TYPE, true, null, true)){
            Log.warn("failed to regen world of '" + param.WORLD_NAME + "'");
            return;
        }

        //World setup
        final MultiverseWorld mvWorld = sWorldManager.getMVWorld(param.WORLD_NAME);
        mvWorld.setDifficulty(param.DIFFICULTY);
        mvWorld.setAlias(param.ALIAS);
        mvWorld.setColor(param.ALIAS_COLOR.toString());

        final Location spawn = mvWorld.getSpawnLocation();
        final World world = spawn.getWorld();

        //Spawn Schematic
        if(PluginLoader.enabledFastAsyncWorldEdit() && param.SCHEMATIC_FILE != null) {
            try {
                SchematicProvider.paste(param.SCHEMATIC_FILE, world, spawn);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //Spawn Protect
        if(PluginLoader.enabledWorldGuard() && param.PROTECT_SIZE > 0){
            WorldGuardProvider.protect(spawn, param.PROTECT_SIZE);
        }


        param.FINISH_CMD_LIST.forEach(cmd -> {
            cmd = cmd.replace("{spawnX}", Integer.toString(spawn.getBlockX()));
            cmd = cmd.replace("{spawnY}", Integer.toString(spawn.getBlockY()));
            cmd = cmd.replace("{spawnZ}", Integer.toString(spawn.getBlockZ()));

            SERVER.dispatchCommand(CMD_SENDER, cmd);
        }
        );

        param.updateLastRegenDate();

        SERVER.broadcastMessage(NekoWorldRegen.PREFIX + param.BROADCAST_MSG);
        Log.print("Regen sequence finished!");
    }

    private static void removeWorld(String worldName){
        if(!sWorldManager.unloadWorld(worldName)){
            Log.warn("failed to unload " + worldName);
        }

        if(!sWorldManager.deleteWorld(worldName, true, true)){
            File folder = new File(NekoWorldRegen.sServerPath + File.separator + worldName);
            deleteFolder(folder);

            if(folder.exists()){
                Log.warn("failed to delete world of '" + worldName + "'");
            }
        }
    }

    private static void deleteFolder(File f){
        if(!f.exists()) {
            return;
        }

        if(f.isFile()) {
            f.delete();
        } else if(f.isDirectory()){
            File[] files = f.listFiles();

             if(files != null) {
                 for (File file : files) {
                     deleteFolder(file);
                 }
             }

            f.delete();
        }
    }

    private final static String WARN_MESSAGE = NekoWorldRegen.PREFIX + ChatColor.translateAlternateColorCodes('&', "&3ワールド再生成&rのため&b数秒間&r、サーバーが&4高負荷&rになります.");

}
