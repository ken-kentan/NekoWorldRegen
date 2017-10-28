package jp.kentan.minecraft.neko_world_regen.regen;

public enum AliasColor {
    BLACK("BLACK"),
    DARK_BLUE("DARKBLUE"),
    DARK_GREEN("DARKGREEN"),
    DARK_AQUA("DARKAQUA"),
    DARK_RED("DARKRED"),
    DARK_PURPLE("DARKPURPLE"),
    GOLD("GOLD"),
    GRAY("GRAY"),
    DARK_GRAY("DARKGRAY"),
    BLUE("BLUE"),
    GREEN("GREEN"),
    AQUA("AQUA"),
    RED("RED"),
    LIGHT_PURPLE("LIGHTPURPLE"),
    YELLOW("YELLOW"),
    WHITE("WHITE");

    private final String toString;

    AliasColor(String string){
        toString = string;
    }

    @Override
    public String toString() {
        return toString;
    }
}
