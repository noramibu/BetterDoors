package me.noramibu.event;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.noramibu.BetterDoors;
import me.noramibu.config.Config;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class KnockingHandler implements AttackBlockCallback {

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (world.isClient || (Config.knockingRequiresEmptyHand && !player.getStackInHand(hand).isEmpty())) {
            return ActionResult.PASS;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean isDoor = block instanceof DoorBlock;
        boolean isTrapdoor = block instanceof TrapdoorBlock;

        if (!isDoor && !isTrapdoor) {
            return ActionResult.PASS;
        }

        boolean isWooden = state.isIn(BlockTags.WOODEN_DOORS) || state.isIn(BlockTags.WOODEN_TRAPDOORS);

        if (isDoor) {
            if (isWooden && !Config.allowKnockingWoodenDoors) return ActionResult.PASS;
            if (!isWooden && !Config.allowKnockingIronDoors) return ActionResult.PASS;
        } else { // isTrapdoor
            if (isWooden && !Config.allowKnockingWoodenTrapdoors) return ActionResult.PASS;
            if (!isWooden && !Config.allowKnockingIronTrapdoors) return ActionResult.PASS;
        }

        if (Config.knockingRequiresShift && !player.isSneaking()) {
            return ActionResult.PASS;
        }

        if (Config.requirePermissionToKnock && !Permissions.check(player, "betterdoors.knock", false)) {
            return ActionResult.PASS;
        }

        SoundEvent knockSound = getSoundEvent(isWooden ? Config.soundKnockWood : Config.soundKnockIron);
        if (knockSound == null) {
            return ActionResult.PASS;
        }

        world.playSound(null, pos, knockSound, net.minecraft.sound.SoundCategory.BLOCKS, (float) Config.soundKnockVolume, (float) Config.soundKnockPitch);
        world.emitGameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
        return ActionResult.PASS;
    }

    private SoundEvent getSoundEvent(String soundId) {
        Identifier id = Identifier.tryParse(soundId);
        if (id == null) {
            BetterDoors.LOGGER.warn("Invalid sound event ID in config: {}", soundId);
            return null;
        }
        return Registries.SOUND_EVENT.get(id);
    }
} 