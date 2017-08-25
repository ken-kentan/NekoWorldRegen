package jp.kentan.minecraft.neko_world_regen;

import com.onarandombox.MultiverseCore.MultiverseCore;
import jp.kentan.minecraft.neko_world_regen.config.ConfigManager;
import jp.kentan.minecraft.neko_world_regen.regen.RegenParameter;
import jp.kentan.minecraft.neko_world_regen.regen.WorldRegenerator;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NekoWorldRegen extends JavaPlugin {

    public final static String TAG = ChatColor.GRAY + "[" + ChatColor.GOLD + "Neko" + ChatColor.GRAY + "World" + ChatColor.DARK_AQUA + "Regen" + ChatColor.GRAY + "] " + ChatColor.WHITE;

    public static String sServerPath;

    private ConfigManager mConfig;
    private WorldRegenerator mWorldRegenerator;
    private List<RegenParameter> mRegenParamList = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onEnable() {
        new Log(getLogger());

        try {
            MultiverseCore multiverseCore = loadMultiverseCore();
            mWorldRegenerator = new WorldRegenerator(getServer(), multiverseCore);
        } catch (Exception e){
            Log.warn("failed to enable NekoWorldRegen.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        mConfig = new ConfigManager(this);
        mConfig.load();
        mRegenParamList.addAll(mConfig.getRegenParamList());

        sServerPath = new File(getDataFolder().getAbsolutePath()).getParentFile().getParent();

        startTimer();

        Log.print("Enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        Log.print("Disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final int params = args.length;

        if(params <= 0 || args[0].equals("help")){
            printHelp(sender);
            return true;
        }

        switch (args[0]){
            case "reload":
                mConfig.load();
                mRegenParamList.clear();
                mRegenParamList.addAll(mConfig.getRegenParamList());

                sender.sendMessage(TAG + "reloaded config file.");
                break;
            case "list":
                StringBuilder builder = new StringBuilder(TAG + " ---Regen Parameter List---\n");

                if(mRegenParamList.size() > 0) {
                    mRegenParamList.forEach(param -> {
                        builder.append(param.getInfo());
                        builder.append(", ");
                    });
                    builder.delete(builder.length()-2, builder.length());
                }else{
                    builder.append("Empty.");
                }

                sender.sendMessage(builder.toString());
                break;
            default:
                mRegenParamList.forEach(param -> {
                    if(param.equals(args[0])){
                        mWorldRegenerator.regen(param);
                    }
                });
                break;
        }

        return true;
    }

    private void startTimer(){
        final BukkitScheduler scheduler = getServer().getScheduler();

        scheduler.runTaskTimerAsynchronously(this, () -> mRegenParamList.forEach(param -> {
            if(param.isRegenDate()){
                scheduler.scheduleSyncDelayedTask(this, () -> mWorldRegenerator.regen(param));
            }
        }), 20*60L, 20*60L); //20ticks = 1sec
    }

    private MultiverseCore loadMultiverseCore() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin instanceof MultiverseCore) {
            Log.print("Hooked Multiverse-Core.");
            return (MultiverseCore) plugin;
        }

        throw new RuntimeException("Multiverse-Core not found!");
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage("---------- NekoWorldRegenコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen <world>" + ChatColor.WHITE + " -<world>を再生成します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen list   " + ChatColor.WHITE + " -再生成ワールドの一覧を表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen reload " + ChatColor.WHITE + " -設定ファイルをリロードします.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen help   " + ChatColor.WHITE + " -ヘルプを表示します.");
        sender.sendMessage("---------------------------------------");
    }
}
