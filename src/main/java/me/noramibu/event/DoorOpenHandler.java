package me.noramibu.event;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.noramibu.config.Config;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DoorOpenHandler implements UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient) return ActionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean isDoor = block instanceof DoorBlock;
        boolean isTrapdoor = block instanceof TrapdoorBlock;

        if (!isDoor && !isTrapdoor) return ActionResult.PASS;

        boolean isWooden = isWooden(state);

        if (isDoor) {
            // Handle wooden doors, which vanilla opens. We just add the double door part.
            if (isWooden) {
                handleDoubleDoor(player, world, pos, state);
            }
        }

        return ActionResult.PASS;
    }

    private void handleDoubleDoor(PlayerEntity player, World world, BlockPos pos, BlockState state) {
        boolean isWooden = isWooden(state);
        
        if (isWooden && !Config.allowDoubleWoodenDoors) return;
        if (!isWooden && !Config.allowDoubleIronDoors) return;

        if (Config.requirePermissionForDoubleDoors && !Permissions.check(player, "betterdoors.doubledoors", false)) return;

        BlockPos otherPos = findSecondDoor(world, pos, state);
        if (otherPos == null) return;
        
        BlockState otherState = world.getBlockState(otherPos);
        if (!otherState.isOf(state.getBlock())) return;

        // If it's a wooden door, its state is pre-toggle. The other door must have the same open state to be toggled.
        // If it's an iron door, its state is post-toggle. The other door must have the opposite open state to be toggled.
        boolean shouldToggle = isWooden;
        
        if (shouldToggle) {
            toggleDoor(world, otherState, otherPos);
        }
    }

    private BlockPos findSecondDoor(World world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof DoorBlock)) return null;

        if (state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.down();
            state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof DoorBlock)) return null;
        }

        Direction facing = state.get(DoorBlock.FACING);
        DoorHinge hinge = state.get(DoorBlock.HINGE);

        BlockPos secondDoorPos = pos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
        BlockState secondDoorState = world.getBlockState(secondDoorPos);

        if (secondDoorState.getBlock() == state.getBlock() && secondDoorState.get(DoorBlock.HINGE) != hinge) {
            return secondDoorPos;
        }

        return null;
    }

    public static void toggleDoor(World world, BlockState state, BlockPos pos) {
        state = state.cycle(DoorBlock.OPEN);
        world.setBlockState(pos, state, 10);
        world.emitGameEvent(null, state.get(DoorBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        boolean isWooden = state.isIn(BlockTags.WOODEN_DOORS);
        world.playSound(null, pos, 
            isWooden ? 
                (state.get(DoorBlock.OPEN) ? SoundEvents.BLOCK_WOODEN_DOOR_OPEN : SoundEvents.BLOCK_WOODEN_DOOR_CLOSE) :
                (state.get(DoorBlock.OPEN) ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE),
            SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }
    
    public static void toggleTrapdoor(World world, BlockState state, BlockPos pos) {
        state = state.cycle(TrapdoorBlock.OPEN);
        world.setBlockState(pos, state, 2);
        world.emitGameEvent(null, state.get(TrapdoorBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        boolean isWooden = state.isIn(BlockTags.WOODEN_TRAPDOORS);
        world.playSound(null, pos,
            isWooden ?
                (state.get(TrapdoorBlock.OPEN) ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE) :
                (state.get(TrapdoorBlock.OPEN) ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE),
            SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    private boolean isWooden(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof DoorBlock) return state.isIn(BlockTags.WOODEN_DOORS);
        if (block instanceof TrapdoorBlock) return state.isIn(BlockTags.WOODEN_TRAPDOORS);
        return false;
    }
} 