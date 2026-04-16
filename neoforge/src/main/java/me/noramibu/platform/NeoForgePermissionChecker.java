package me.noramibu.platform;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class NeoForgePermissionChecker implements PermissionChecker {
    @Override
    public boolean check(Player player, String permission, boolean defaultValue) {
        if (defaultValue) {
            return true;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        }
        return false;
    }
}
