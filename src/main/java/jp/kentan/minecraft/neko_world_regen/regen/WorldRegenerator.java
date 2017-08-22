package jp.kentan.minecraft.neko_world_regen.regen;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import jp.kentan.minecraft.neko_world_regen.NekoWorldRegen;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.Random;

public class WorldRegenerator {

    private Server mServer;
    private ConsoleCommandSender mCommandSender;

    private MVWorldManager mWorldManager;

    public WorldRegenerator(Server server, MultiverseCore multiverseCore){
        mServer = server;
        mCommandSender = server.getConsoleSender();

        mWorldManager = multiverseCore.getMVWorldManager();
    }

    public void regen(final RegenParameter param){
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

        final String seed = String.valueOf(new Random().nextInt(100000));

        if(!mWorldManager.addWorld(param.mWorldName, param.mEnvironment, seed, param.mWorldType, false, "")){
            Log.warn("failed to regen world of '" + param.mWorldName + "'");
            return;
        }

        MultiverseWorld world = mWorldManager.getMVWorld(param.mWorldName);
        world.setDifficulty(param.mDifficulty);
        world.setAlias(param.mAlias);
        world.setColor(param.mColor);

        param.mFinishCommandList.forEach(cmd -> mServer.dispatchCommand(mCommandSender, cmd));

        param.updateLastRegenDate();

        broadcast(param.mBroadcastMessage);
        Log.print("Regen sequence finished!");
    }

    private void broadcast(String message){
        final String text = NekoWorldRegen.TAG + message;
        mServer.getOnlinePlayers().forEach(player -> player.sendMessage(text));
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
