package jp.kentan.minecraft.neko_world_regen.regen;

import jp.kentan.minecraft.neko_world_regen.config.Updatable;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.io.File;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;


public class RegenParameter{

    private ZonedDateTime mLastRegenDate;

    //Period
    private int mMonth, mHour;
    private DayOfWeek mDayOfWeek;

    private final String NAME;

    //World
    final String WORLD_NAME;
    final World.Environment ENVIRONMENT;
    final WorldType WORLD_TYPE;
    final Difficulty DIFFICULTY;

    //Alias
    final String ALIAS;
    final AliasColor ALIAS_COLOR;

    //Spawn
    final File SCHEMATIC_FILE;
    final int PROTECT_SIZE;

    final String BROADCAST_MSG;

    final List<String> FINISH_CMD_LIST = new ArrayList<>();

    private final Updatable CALLBACK;


    public RegenParameter(String name, ZonedDateTime lastRegenDate,
                          String worldName, World.Environment environment, WorldType worldType, Difficulty difficulty,
                          String alias, AliasColor aliasColor,
                          int month, DayOfWeek dayOfWeek, int hour,
                          File schematicFile, int protectSize,
                          String broadcastMessage, List<String> finishCmdList, Updatable updatable){

        mLastRegenDate = lastRegenDate;

        NAME = name;

        WORLD_NAME = worldName;
        ENVIRONMENT = environment;
        WORLD_TYPE = worldType;
        DIFFICULTY = difficulty;

        ALIAS = alias;
        ALIAS_COLOR = aliasColor;

        mMonth = month;
        mDayOfWeek = dayOfWeek;
        mHour = hour;

        SCHEMATIC_FILE = schematicFile;
        PROTECT_SIZE = protectSize;

        BROADCAST_MSG = ChatColor.translateAlternateColorCodes('&', broadcastMessage);

        FINISH_CMD_LIST.addAll(finishCmdList);

        CALLBACK = updatable;
    }

    void updateLastRegenDate(){
        mLastRegenDate = ZonedDateTime.now();

        CALLBACK.updateLastRegenDate(NAME, mLastRegenDate);
    }

    public boolean isRegenDate(ZonedDateTime now){
        if(mMonth > 0 && diffMonth(mLastRegenDate, now) < mMonth){
            return false;
        }

        //check same day
        if(mLastRegenDate.truncatedTo(ChronoUnit.DAYS).equals(now.truncatedTo(ChronoUnit.DAYS))){
            return false;
        }

        //check DayOfWeek
        if(mDayOfWeek != null && mDayOfWeek != now.getDayOfWeek()){
            return false;
        }

        return now.get(ChronoField.HOUR_OF_DAY) >= mHour;
    }

    private static long diffMonth(Temporal from, Temporal to){
        return ChronoUnit.MONTHS.between(from, to) * 12 + to.get(ChronoField.MONTH_OF_YEAR) - from.get(ChronoField.MONTH_OF_YEAR);
    }
}
