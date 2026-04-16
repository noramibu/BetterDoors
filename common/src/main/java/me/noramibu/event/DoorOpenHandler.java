package me.noramibu.event;

import me.noramibu.config.Config;
import me.noramibu.platform.PermissionsBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;

public final class DoorOpenHandler {
    private DoorOpenHandler() {
    }

    public static InteractionResult onUse(Player player, Level world, InteractionHand hand, BlockPos pos) {
        if (world.isClientSide()) {
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

        if (isDoor) {
            handleDoubleDoor(player, world, pos, state);
        }

        if (isGate) {
            handleDoubleGate(player, world, pos, state);
        }

        return InteractionResult.PASS;
    }

    private static void handleDoubleDoor(Player player, Level world, BlockPos pos, BlockState state) {
        boolean isWooden = isWooden(state);
        boolean isCopper = isCopperDoor(state);

        if (isWooden && !Config.allowDoubleWoodenDoors) {
            return;
        }
        if (isCopper && !Config.allowDoubleCopperDoors) {
            return;
        }
        if (!isWooden && !isCopper && !Config.allowDoubleIronDoors) {
            return;
        }

        if (Config.requirePermissionForDoubleDoors && !PermissionsBridge.check(player, "betterdoors.doubledoors", false)) {
            return;
        }

        BlockPos otherPos = findSecondDoor(world, pos, state);
        if (otherPos == null) {
            return;
        }

        BlockState otherState = world.getBlockState(otherPos);
        if (!otherState.is(state.getBlock())) {
            return;
        }

        toggleDoor(world, otherState, otherPos);
    }

    private static void handleDoubleGate(Player player, Level world, BlockPos pos, BlockState state) {
        if (!Config.allowDoubleFenceGates) {
            return;
        }

        if (Config.requirePermissionForDoubleFenceGates && !PermissionsBridge.check(player, "betterdoors.doublegates", false)) {
            return;
        }

        BlockPos otherPos = findSecondGate(world, pos, state);
        if (otherPos == null) {
            return;
        }

        BlockState otherState = world.getBlockState(otherPos);
        if (!(otherState.getBlock() instanceof FenceGateBlock)) {
            return;
        }

        toggleFenceGate(world, otherState, otherPos);
    }

    private static BlockPos findSecondDoor(Level world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof DoorBlock)) {
            return null;
        }

        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof DoorBlock)) {
                return null;
            }
        }

        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);

        BlockPos secondDoorPos = pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
        BlockState secondDoorState = world.getBlockState(secondDoorPos);

        if (secondDoorState.getBlock() == state.getBlock() && secondDoorState.getValue(DoorBlock.HINGE) != hinge) {
            return secondDoorPos;
        }

        return null;
    }

    private static BlockPos findSecondGate(Level world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof FenceGateBlock)) {
            return null;
        }

        Direction facing = state.getValue(FenceGateBlock.FACING);

        BlockPos leftPos = pos.relative(facing.getClockWise());
        BlockState leftState = world.getBlockState(leftPos);
        if (isMatchingGate(state, leftState)) {
            return leftPos;
        }

        BlockPos rightPos = pos.relative(facing.getCounterClockWise());
        BlockState rightState = world.getBlockState(rightPos);
        if (isMatchingGate(state, rightState)) {
            return rightPos;
        }

        return null;
    }

    private static boolean isMatchingGate(BlockState sourceState, BlockState candidateState) {
        if (!(candidateState.getBlock() instanceof FenceGateBlock)) {
            return false;
        }
        if (candidateState.getBlock() != sourceState.getBlock()) {
            return false;
        }
        return candidateState.getValue(FenceGateBlock.FACING) == sourceState.getValue(FenceGateBlock.FACING);
    }

    public static void toggleDoor(Level world, BlockState state, BlockPos pos) {
        state = state.cycle(DoorBlock.OPEN);
        world.setBlock(pos, state, 10);
        world.gameEvent(null, state.getValue(DoorBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        boolean isWooden = state.is(BlockTags.WOODEN_DOORS);
        world.playSound(
                null,
                pos,
                isWooden
                        ? (state.getValue(DoorBlock.OPEN) ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE)
                        : (state.getValue(DoorBlock.OPEN) ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE),
                SoundSource.BLOCKS,
                1.0f,
                world.getRandom().nextFloat() * 0.1f + 0.9f
        );
    }

    public static void toggleTrapdoor(Level world, BlockState state, BlockPos pos) {
        state = state.cycle(TrapDoorBlock.OPEN);
        world.setBlock(pos, state, 2);
        world.gameEvent(null, state.getValue(TrapDoorBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        boolean isWooden = state.is(BlockTags.WOODEN_TRAPDOORS);
        world.playSound(
                null,
                pos,
                isWooden
                        ? (state.getValue(TrapDoorBlock.OPEN) ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE)
                        : (state.getValue(TrapDoorBlock.OPEN) ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE),
                SoundSource.BLOCKS,
                1.0f,
                world.getRandom().nextFloat() * 0.1f + 0.9f
        );
    }

    public static void toggleFenceGate(Level world, BlockState state, BlockPos pos) {
        state = state.cycle(FenceGateBlock.OPEN);
        world.setBlock(pos, state, 10);
        world.gameEvent(null, state.getValue(FenceGateBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        world.playSound(
                null,
                pos,
                state.getValue(FenceGateBlock.OPEN) ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE,
                SoundSource.BLOCKS,
                1.0f,
                world.getRandom().nextFloat() * 0.1f + 0.9f
        );
    }

    private static boolean isWooden(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof DoorBlock) {
            return state.is(BlockTags.WOODEN_DOORS);
        }
        if (block instanceof TrapDoorBlock) {
            return state.is(BlockTags.WOODEN_TRAPDOORS);
        }
        return false;
    }

    private static boolean isCopperDoor(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().contains("copper_door");
    }
}
