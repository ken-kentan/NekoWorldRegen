package jp.kentan.minecraft.neko_world_regen.regen;

import jp.kentan.minecraft.neko_world_regen.config.Updatable;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.util.*;

import static java.util.Calendar.*;

public class RegenParameter{
    //schedule
    private Updatable mUpdatable;

    private Calendar mLastRegenDate;
    private int mLastRegenMonth;
    private int mLastRegenDay;

    private int mMonth;
    private int mDayOfWeek; //if 0 everyday
    private int mHour;

    //param
    String mWorldName;
    World.Environment mEnvironment;
    WorldType mWorldType;
    Difficulty mDifficulty;
    String mAlias, mColor;
    List<String> mFinishCommandList = new ArrayList<>();
    String mBroadcastMessage;

    public RegenParameter(Updatable updatable, Date lastRegenDate, int month, int dayOfWeek, int hour, String worldName, World.Environment environment, WorldType worldType,
                          Difficulty difficulty, String alias, String color, String broadcastMessage, List<String> finishCmdList){
        mUpdatable = updatable;

        mLastRegenDate = Calendar.getInstance(Locale.JAPAN);
        mLastRegenDate.setTime(lastRegenDate);

        mLastRegenMonth = mLastRegenDate.get(MONTH);
        mLastRegenDay   = mLastRegenDate.get(DAY_OF_MONTH);

        mMonth = month;
        mDayOfWeek = dayOfWeek;
        mHour = hour;

        mWorldName = worldName;
        mEnvironment = environment;
        mWorldType = worldType;
        mDifficulty = difficulty;
        mAlias = alias;
        mColor = color;
        mBroadcastMessage = broadcastMessage;

        mFinishCommandList.addAll(finishCmdList);
    }

    void updateLastRegenDate(){
        Date date = new Date();

        mLastRegenDate.setTime(date);

        mLastRegenMonth = mLastRegenDate.get(MONTH);
        mLastRegenDay   = mLastRegenDate.get(DAY_OF_MONTH);

        mUpdatable.updateLastRegenDate("RegenParams." + mWorldName + ".lastRegenDate", date);
    }

    public String getInfo(){
        return mWorldName;
    }

    public boolean isRegenDate(){
        Calendar now = Calendar.getInstance(Locale.JAPAN);

        if(mMonth > 0 && diffMonth(mLastRegenDate, now) < mMonth){
            return false;
        }

        if(mLastRegenDay == now.get(DAY_OF_MONTH)){
            return false;
        }

        if(mDayOfWeek > 0 && mDayOfWeek != now.get(DAY_OF_WEEK)){
            return false;
        }

        return now.get(HOUR_OF_DAY) >= mHour;
    }

    public boolean equals(String worldName){
        return mWorldName.equals(worldName);
    }

    private static int diffMonth(Calendar from, Calendar to){
        int diff = 0;
        while (from.before(to)) {
            from.add(Calendar.MONTH, 1);
            ++diff;
        }

        return diff;
    }
}
