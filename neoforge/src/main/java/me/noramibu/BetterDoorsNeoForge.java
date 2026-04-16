package me.noramibu;

import me.noramibu.command.BetterDoorsCommand;
import me.noramibu.event.DoorOpenHandler;
import me.noramibu.event.KnockingHandler;
import me.noramibu.platform.NeoForgePermissionChecker;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.SubscribeEvent;

@Mod(BetterDoorsCommon.MOD_ID)
public final class BetterDoorsNeoForge {
    public BetterDoorsNeoForge() {
        BetterDoorsCommon.init(FMLPaths.CONFIGDIR.get(), new NeoForgePermissionChecker());
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        DoorOpenHandler.onUse(event.getEntity(), event.getLevel(), event.getHand(), event.getPos());
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Direction direction = event.getFace() == null ? Direction.UP : event.getFace();
        KnockingHandler.onAttack(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), direction);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        BetterDoorsCommand.register(event.getDispatcher());
    }
}
