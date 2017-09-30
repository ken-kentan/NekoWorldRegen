package jp.kentan.minecraft.neko_world_regen.regen;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import jp.kentan.minecraft.neko_world_regen.NekoWorldRegen;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.Random;

public class WorldRegenerator {

    private final static String WARN_MESSAGE = NekoWorldRegen.TAG + ChatColor.translateAlternateColorCodes('&', "&3ワールド再生成&rのため&b数秒間&r、サーバーが&4高負荷&rになります.");

    private Server mServer;
    private ConsoleCommandSender mCommandSender;

    private MVWorldManager mWorldManager;

    private final Random RANDOM = new Random();

    public WorldRegenerator(Server server, MultiverseCore multiverseCore){
        mServer = server;
        mCommandSender = server.getConsoleSender();

        mWorldManager = multiverseCore.getMVWorldManager();
    }

    public void regen(final RegenParameter param){
        mServer.broadcastMessage(WARN_MESSAGE);

        if(!mWorldManager.unloadWorld(param.mWorldName)){
            Log.warn("failed to unload " + param.mWorldName);
        }

        if(!mWorldManager.deleteWorld(param.mWorldName, true, true)){
            File folder = new File(NekoWorldRegen.sServerPath + File.separator + param.mWorldName);
            deleteFolder(folder);

            if(folder.exists()){
                Log.warn("failed to delete world of '" + param.mWorldName + "'");
            }
        }

        final String seed = String.valueOf(RANDOM.nextLong());

        if(!mWorldManager.addWorld(param.mWorldName, param.mEnvironment, seed, param.mWorldType, true, null, true)){
            Log.warn("failed to regen world of '" + param.mWorldName + "'");
            return;
        }

        MultiverseWorld world = mWorldManager.getMVWorld(param.mWorldName);
        world.setSpawnLocation(new Location(world.getCBWorld(), 0.5D, 63D, 0.5D, 180f, 0f));
        world.setDifficulty(param.mDifficulty);
        world.setAlias(param.mAlias);
        world.setColor(param.mColor);

        final Location spawnLocation = world.getSpawnLocation();
        final World spawnWorld = spawnLocation.getWorld();

        //エンドじゃなかったらスポーン作成
        if(spawnWorld.getEnvironment() != World.Environment.THE_END) {
            final Block spawn = spawnLocation.getBlock();

            for(int y = 3; y >= -1; --y) {
                for (int x = 1; x >= -1; --x) {
                    for (int z = 1; z >= -1; --z) {
                        Location location = new Location(spawnWorld, spawn.getX() + x, spawn.getY() + y, spawn.getZ() + z);

                        location.getBlock().setType((y >= 0) ? Material.AIR : ((x == 0 && z == 0) ? Material.GLOWSTONE : Material.BEDROCK));
                    }
                }
            }
        }

        param.mFinishCommandList.forEach(cmd -> {
            cmd = cmd.replace("{spawnX}", Integer.toString((int) spawnLocation.getX())).replace("{spawnZ}", Integer.toString((int) spawnLocation.getZ()));

            mServer.dispatchCommand(mCommandSender, cmd);
            Log.print(cmd);
        }
        );

        param.updateLastRegenDate();

        mServer.broadcastMessage(NekoWorldRegen.TAG + param.mBroadcastMessage);
        Log.print("Regen sequence finished!");
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
}
