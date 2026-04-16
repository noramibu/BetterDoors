package me.noramibu;

import me.noramibu.command.BetterDoorsCommand;
import me.noramibu.event.DoorOpenHandler;
import me.noramibu.event.KnockingHandler;
import me.noramibu.platform.QuiltPermissionChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;

public final class BetterDoorsQuilt implements ModInitializer {
    @Override
    public void onInitialize() {
        BetterDoorsCommon.init(FabricLoader.getInstance().getConfigDir(), new QuiltPermissionChecker());

        AttackBlockCallback.EVENT.register(KnockingHandler::onAttack);
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                DoorOpenHandler.onUse(player, world, hand, hitResult.getBlockPos()));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                BetterDoorsCommand.register(dispatcher));
    }
}
