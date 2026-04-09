package me.noramibu.event;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.noramibu.BetterDoors;
import me.noramibu.config.Config;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class KnockingHandler implements AttackBlockCallback {

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (world.isClientSide() || (Config.knockingRequiresEmptyHand && !player.getItemInHand(hand).isEmpty())) {
            return InteractionResult.PASS;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean isDoor = block instanceof DoorBlock;
        boolean isTrapdoor = block instanceof TrapDoorBlock;
        boolean isGate = block instanceof FenceGateBlock;

        if (!isDoor && !isTrapdoor && !isGate) {
            return InteractionResult.PASS;
        }

        boolean isWooden = state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_TRAPDOORS) || state.is(BlockTags.FENCE_GATES);
        boolean isCopper = isCopper(state);

        if (isDoor) {
            if (isWooden && !Config.allowKnockingWoodenDoors) return InteractionResult.PASS;
            if (isWooden && !isWoodenDoorTypeEnabled(state)) return InteractionResult.PASS;
            if (isCopper && !Config.allowKnockingCopperDoors) return InteractionResult.PASS;
            if (!isWooden && !isCopper && !Config.allowKnockingIronDoors) return InteractionResult.PASS;
        } else if (isTrapdoor) {
            if (isWooden && !Config.allowKnockingWoodenTrapdoors) return InteractionResult.PASS;
            if (isCopper && !Config.allowKnockingCopperTrapdoors) return InteractionResult.PASS;
            if (!isWooden && !isCopper && !Config.allowKnockingIronTrapdoors) return InteractionResult.PASS;
        } else {
            if (!Config.allowKnockingFenceGates) return InteractionResult.PASS;
        }

        if (Config.knockingRequiresShift && !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!isGate && Config.requirePermissionToKnock && !Permissions.check(player, "betterdoors.knock", false)) {
            return InteractionResult.PASS;
        }

        if (isGate && Config.requirePermissionToKnockFenceGates && !Permissions.check(player, "betterdoors.knockgates", false)) {
            return InteractionResult.PASS;
        }

        String soundId = isGate ? Config.soundKnockWood : (isWooden ? Config.soundKnockWood : (isCopper ? Config.soundKnockCopper : Config.soundKnockIron));
        SoundEvent knockSound = getSoundEvent(soundId);
        if (knockSound == null) {
            return InteractionResult.PASS;
        }

        world.playSound(null, pos, knockSound, net.minecraft.sounds.SoundSource.BLOCKS, (float) Config.soundKnockVolume, (float) Config.soundKnockPitch);
        world.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
        return InteractionResult.PASS;
    }

    private SoundEvent getSoundEvent(String soundId) {
        Identifier id = Identifier.tryParse(soundId);
        if (id == null) {
            BetterDoors.LOGGER.warn("Invalid sound event ID in config: {}", soundId);
            return null;
        }
        return BuiltInRegistries.SOUND_EVENT.getValue(id);
    }

    private boolean isCopper(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().contains("copper_door") || BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().contains("copper_trapdoor");
    }

    private boolean isWoodenDoorTypeEnabled(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return switch (path) {
            case "oak_door" -> Config.allowKnockingOakDoors;
            case "spruce_door" -> Config.allowKnockingSpruceDoors;
            case "birch_door" -> Config.allowKnockingBirchDoors;
            case "jungle_door" -> Config.allowKnockingJungleDoors;
            case "acacia_door" -> Config.allowKnockingAcaciaDoors;
            case "dark_oak_door" -> Config.allowKnockingDarkOakDoors;
            case "mangrove_door" -> Config.allowKnockingMangroveDoors;
            case "bamboo_door" -> Config.allowKnockingBambooDoors;
            case "cherry_door" -> Config.allowKnockingCherryDoors;
            case "crimson_door" -> Config.allowKnockingCrimsonDoors;
            case "warped_door" -> Config.allowKnockingWarpedDoors;
            case "pale_oak_door" -> Config.allowKnockingPaleOakDoors;
            default -> true;
        };
    }
} 
