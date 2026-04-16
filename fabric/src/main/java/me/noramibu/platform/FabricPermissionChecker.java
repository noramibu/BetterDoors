package me.noramibu.platform;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.world.entity.player.Player;

public final class FabricPermissionChecker implements PermissionChecker {
    @Override
    public boolean check(Player player, String permission, boolean defaultValue) {
        return Permissions.check(player, permission, defaultValue);
    }
}
