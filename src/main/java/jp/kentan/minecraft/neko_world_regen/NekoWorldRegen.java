package jp.kentan.minecraft.neko_world_regen;

import jp.kentan.minecraft.neko_world_regen.config.ConfigManager;
import jp.kentan.minecraft.neko_world_regen.regen.RegenParameter;
import jp.kentan.minecraft.neko_world_regen.regen.WorldRegenerator;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.*;

public class NekoWorldRegen extends JavaPlugin {

    public final static String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Neko" + ChatColor.GRAY + "World" + ChatColor.DARK_AQUA + "Regen" + ChatColor.GRAY + "] " + ChatColor.WHITE;

    public static String sServerPath;

    private ConfigManager mConfig;
    private final Map<String, RegenParameter> mRegenParamMap = Collections.synchronizedMap(new HashMap<String, RegenParameter>());

    @Override
    public void onEnable() {
        new Log(getLogger());

        if(!WorldRegenerator.setup()){
            Log.warn("failed to enable NekoWorldRegen.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        mConfig = new ConfigManager(this);
        mConfig.load();

        mRegenParamMap.putAll(mConfig.getRegenParamMap());

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
                mRegenParamMap.clear();
                mRegenParamMap.putAll(mConfig.getRegenParamMap());

                sender.sendMessage(PREFIX + "パラメータを更新しました.");
                break;
            case "info":
                if(params >= 2){
                    if(mRegenParamMap.containsKey(args[1])){
                        sender.sendMessage(mRegenParamMap.get(args[1]).getInfo());
                    }else{
                        sender.sendMessage(PREFIX + ChatColor.YELLOW + args[1] + "は存在しません.");
                    }
                }else{
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + "パラメータ名を指定して下さい.");
                }
                break;
            case "list":
                StringBuilder builder = new StringBuilder("---パラメータ一覧---\n");

                if(mRegenParamMap.size() > 0) {
                    mRegenParamMap.forEach((key, param) -> {
                        builder.append(key);
                        builder.append(", ");
                    });
                    builder.delete(builder.length()-2, builder.length());
                }

                sender.sendMessage(builder.toString());
                break;
            default:
                if(mRegenParamMap.containsKey(args[0])){
                    WorldRegenerator.regen(mRegenParamMap.get(args[0]));
                }else{
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + args[0] + "は存在しません.");
                }
                break;
        }

        return true;
    }

    private void startTimer(){
        final BukkitScheduler scheduler = getServer().getScheduler();

        scheduler.runTaskTimerAsynchronously(this, () -> {
                    ZonedDateTime now = ZonedDateTime.now();

                    mRegenParamMap.forEach((key, param) -> {
                        if (param.isRegenDate(now)) {
                            scheduler.scheduleSyncDelayedTask(this, () -> WorldRegenerator.regen(param));
                        }
                    });
                }
        , 20*60L, 20*60L); //20ticks = 1sec
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage("---------- NekoWorldRegenコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen <name>      " + ChatColor.WHITE + " -<name>パラメータで再生成.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen info <name> " + ChatColor.WHITE + " -<name>パラメータの情報を表示.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen list        " + ChatColor.WHITE + " -再生成ワールドの一覧を表示.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen reload      " + ChatColor.WHITE + " -設定ファイルをリロード.");
        sender.sendMessage("| " + ChatColor.GOLD + "/regen help        " + ChatColor.WHITE + " -ヘルプを表示.");
        sender.sendMessage("---------------------------------------");
    }
}
