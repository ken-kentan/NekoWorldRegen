package jp.kentan.minecraft.neko_world_regen.config;

import jp.kentan.minecraft.neko_world_regen.regen.RegenParameter;
import jp.kentan.minecraft.neko_world_regen.utils.Log;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ConfigManager implements Updatable {

    private final static Charset UTF_8 = StandardCharsets.UTF_8;
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private File mConfigFile;
    private String mConfigFilePath;

    private List<RegenParameter> mRegenParamList = new ArrayList<>();

    public ConfigManager(Plugin plugin){
        File dataFolder = plugin.getDataFolder();

        mConfigFile = new File(dataFolder, "config.yml");
        mConfigFilePath = dataFolder + File.separator + "config.yml";

        setupIfNeed(plugin);
    }

    private void setupIfNeed(Plugin plugin){
        File dataFolder = plugin.getDataFolder();

        if(dataFolder.exists()){
            return;
        }

        dataFolder.mkdirs();

        try {
            plugin.saveResource("config.yml", false);
            plugin.saveResource("config_sample.txt", false);
        } catch (Exception e){
            Log.warn(e.getMessage());
        }
    }

    public void load(){
        try (Reader reader = new InputStreamReader(new FileInputStream(mConfigFilePath), UTF_8)) {
            final FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            loadRegenParams(config);

            reader.close();
        }catch (Exception e){
            Log.warn(e.getMessage());
        }
    }

    private void loadRegenParams(FileConfiguration config){
        List<String> regenWorldList = config.getStringList("WorldRegenList");

        mRegenParamList.clear();

        regenWorldList.forEach(world -> {
            final String path = "RegenParams." + world + ".";

            String lastRegenDate = config.getString(path + "lastRegenDate", "2017-01-01 00:00:00");

            int month     = config.getInt(path + "month");
            int dayOfWeek = config.getInt(path + "dayOfWeek");
            int hour      = config.getInt(path + "hour");

            String env   = config.getString(path + "environment");
            String type  = config.getString(path + "worldType");
            String diff  = config.getString(path + "difficulty");
            String alias = config.getString(path + "alias");
            String color = config.getString(path + "color");
            String msg   = config.getString(path + "message");
            List<String> cmdList = config.getStringList(path + "commandList");

            RegenParameter param = compileRegenParam(lastRegenDate, month, dayOfWeek, hour, world, env, type, diff, alias, color, msg, cmdList);

            if(param != null) {
                mRegenParamList.add(param);
            }
        });
    }

    @Override
    public void updateLastRegenDate(String path, Date date) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mConfigFile);

            config.set(path, DATE_FORMAT.format(date));

            config.save(mConfigFile);
        } catch (Exception e) {
            Log.warn(e.getMessage());
        }
    }

    public List<RegenParameter> getRegenParamList(){
        return mRegenParamList;
    }

    private RegenParameter compileRegenParam(String lastDate, int month, int dayOfWeek, int hour, String name, String env, String type, String diff, String alias, String color, String msg, List<String> cmdList){
        try {
            Date date = DATE_FORMAT.parse(lastDate);
            World.Environment environment = World.Environment.valueOf(env);
            WorldType worldType = WorldType.valueOf(type);
            Difficulty difficulty = Difficulty.valueOf(diff);

            return new RegenParameter(this, date, month, dayOfWeek, hour, name, environment, worldType, difficulty, alias, color, msg, cmdList);
        } catch (Exception e){
            Log.warn("failed to compile RegenParam of '" + name + "'.");
            Log.warn(e.toString());

            return null;
        }
    }

}
