package jp.kentan.minecraft.neko_world_regen.config;

import java.util.Date;

public interface Updatable {
    void updateLastRegenDate(String path, Date date);
}
