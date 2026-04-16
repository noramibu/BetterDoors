package me.noramibu.platform;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PermissionChecker {
    boolean check(Player player, String permission, boolean defaultValue);
}
