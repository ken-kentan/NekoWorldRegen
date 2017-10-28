package jp.kentan.minecraft.neko_world_regen.config;

import java.time.ZonedDateTime;

public interface Updatable {
    void updateLastRegenDate(String worldName, ZonedDateTime date);
}
