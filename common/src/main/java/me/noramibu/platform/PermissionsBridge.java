package me.noramibu.platform;

import net.minecraft.world.entity.player.Player;

public final class PermissionsBridge {
    private static PermissionChecker checker = (player, permission, defaultValue) -> defaultValue;

    private PermissionsBridge() {
    }

    public static void setChecker(PermissionChecker newChecker) {
        if (newChecker == null) {
            checker = (player, permission, defaultValue) -> defaultValue;
            return;
        }
        checker = newChecker;
    }

    public static boolean check(Player player, String permission, boolean defaultValue) {
        return checker.check(player, permission, defaultValue);
    }
}
