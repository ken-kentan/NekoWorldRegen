package jp.kentan.minecraft.neko_world_regen.config;

import jp.kentan.minecraft.neko_world_regen.regen.AliasColor;
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
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class ConfigManager implements Updatable {

    private final static Charset UTF_8 = StandardCharsets.UTF_8;
    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final File mConfigFile;
    private File mSchematicFolder;
    private final String mConfigFilePath;

    private final Map<String, RegenParameter> mRegenParamMap = new HashMap<>();

    public ConfigManager(Plugin plugin){
        File dataFolder = plugin.getDataFolder();

        mConfigFile = new File(dataFolder, "config.yml");
        mConfigFilePath = dataFolder + File.separator + "config.yml";

        setupIfNeed(plugin);
    }

    private void setupIfNeed(Plugin plugin){
        File dataFolder = plugin.getDataFolder();
        mSchematicFolder = new File(dataFolder, "schematics");

        if(!dataFolder.exists()){
            dataFolder.mkdir();

            plugin.saveResource("config.yml", false);
        }

        plugin.saveResource("config_sample.txt", true);

        if(!mSchematicFolder.exists()){
            mSchematicFolder.mkdir();
        }
    }

    public void load(){
        try (Reader reader = new InputStreamReader(new FileInputStream(mConfigFilePath), UTF_8)) {
            final FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            loadRegenParams(config);

            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadRegenParams(FileConfiguration config){
        Set<String> paramNameSet = config.getConfigurationSection("RegenParams").getKeys(false);

        mRegenParamMap.clear();

        paramNameSet.forEach(name -> {
            final String path = "RegenParams." + name + ".";

            String lastRegenDate = config.getString(path + "lastRegenDate", "2017-01-01 00:00:00");

            //World
            String worldName = config.getString(path + "World.name");
            String type = config.getString(path + "World.type");
            String env   = config.getString(path + "World.environment");
            String diff  = config.getString(path + "World.difficulty");

            //World Alias
            String alias = config.getString(path + "World.Alias.text");
            String color = config.getString(path + "World.Alias.color");

            //Period
            int month     = config.getInt(path + "Period.month");
            String dayOfWeek = config.getString(path + "Period.dayOfWeek");
            int hour      = config.getInt(path + "Period.hour");

            //Spawn
            String schematic = config.getString(path + "Spawn.schematic");
            int protectSize = config.getInt(path + "Spawn.protectSize");
            boolean isAdjust = config.getBoolean(path + "Spawn.isAdjust");

            //Other
            String broadcastMsg = config.getString(path + "broadcast");
            List<String> cmds = config.getStringList(path + "finishCommands");


            RegenParameter param = compileRegenParam(
                    name,
                    lastRegenDate,
                    worldName, type, env, diff,
                    alias, color,
                    month, dayOfWeek, hour,
                    schematic, protectSize, isAdjust,
                    broadcastMsg, cmds
            );

            if(param != null) {
                mRegenParamMap.put(name, param);
            }
        });
    }

    @Override
    public void updateLastRegenDate(String paramName, ZonedDateTime date) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mConfigFile);

            config.set("RegenParams." + paramName + ".lastRegenDate", DATE_FORMAT.format(date));

            config.save(mConfigFile);
        } catch (Exception e) {
            Log.warn(e.getMessage());
        }
    }

    public Map<String, RegenParameter> getRegenParamMap(){
        return mRegenParamMap;
    }

    private RegenParameter compileRegenParam(String name, String strLastDate,
                                             String worldName, String strWorldType, String strWorldEnv, String strWorldDiff,
                                             String alias, String strAliasColor,
                                             int month, String strDayOfWeek, int hour,
                                             String strSchematic, int protectSize, boolean isAdjust,
                                             String broadcastMsg, List<String> finishCmds) {
        try {
            ZonedDateTime lastDate = ZonedDateTime.parse(strLastDate, DATE_FORMAT);

            //World
            WorldType type = WorldType.valueOf(strWorldType);
            World.Environment env = World.Environment.valueOf(strWorldEnv);
            Difficulty diff = Difficulty.valueOf(strWorldDiff);

            //Alias
            AliasColor aliasColor = AliasColor.valueOf(strAliasColor);

            //Period
            if(month < 0){
                throw new IllegalArgumentException("Period.month must greater than 0.");
            }

            if(hour < 0 || hour > 23){
                throw new IllegalArgumentException("Period.hour must be 0~23.");
            }

            DayOfWeek dayOfWeek = null;
            if(!strDayOfWeek.equals("EVERYDAY")){
                dayOfWeek = DayOfWeek.valueOf(strDayOfWeek);
            }

            //Spawn
            File schematicFile = null;
            if (strSchematic != null && strSchematic.length() > 0) {
                if(!strSchematic.endsWith(".schematic")){
                    strSchematic += ".schematic";
                }

                schematicFile = new File(mSchematicFolder, strSchematic);
                if (!schematicFile.exists()) {
                    throw new IllegalArgumentException(schematicFile + " not found.");
                }
            }

            if(protectSize != 0) {
                if (protectSize < 3) {
                    throw new IllegalArgumentException("Spawn.protectSize must greater than 3.");
                }
                if (protectSize % 2 == 0) {
                    throw new IllegalArgumentException("Spawn.protectSize must be odd number.");
                }
            }


            return new RegenParameter(
                    name, lastDate,
                    worldName, env, type, diff,
                    alias, aliasColor,
                    month, dayOfWeek, hour,
                    schematicFile, protectSize, isAdjust,
                    broadcastMsg, finishCmds, this
            );
        } catch (Exception e) {
            Log.warn("failed to compile RegenParam of '" + name + "'.");
            e.printStackTrace();

            return null;
        }
    }

}
