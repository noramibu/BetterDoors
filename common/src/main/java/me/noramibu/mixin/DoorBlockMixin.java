package me.noramibu.mixin;

import me.noramibu.config.Config;
import me.noramibu.event.DoorOpenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {

    @Redirect(
            method = "neighborChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private boolean onNeighborUpdateSetBlockState(Level world, BlockPos pos, BlockState newState, int flags) {
        BlockState oldState = world.getBlockState(pos);
        boolean wasOpen = oldState.getBlock() instanceof DoorBlock && oldState.getValue(DoorBlock.OPEN);
        boolean willBeOpen = newState.getBlock() instanceof DoorBlock && newState.getValue(DoorBlock.OPEN);

        boolean result = world.setBlock(pos, newState, flags);

        if (result && wasOpen != willBeOpen && Config.checkForRedstone) {
            boolean isWooden = newState.is(BlockTags.WOODEN_DOORS);
            boolean isCopper = isCopperDoor(newState);

            if ((isWooden && Config.allowDoubleWoodenDoors)
                    || (isCopper && Config.allowDoubleCopperDoors)
                    || (!isWooden && !isCopper && Config.allowDoubleIronDoors)) {
                findAndToggleSecondDoor(world, pos, newState);
            }
        }

        return result;
    }

    private void findAndToggleSecondDoor(Level world, BlockPos pos, BlockState state) {
        BlockPos otherPos = findSecondDoor(world, pos, state);
        if (otherPos != null) {
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.getBlock() == state.getBlock() && otherState.getValue(DoorBlock.OPEN) != state.getValue(DoorBlock.OPEN)) {
                DoorOpenHandler.toggleDoor(world, otherState, otherPos);
            }
        }
    }

    private BlockPos findSecondDoor(Level world, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        BlockPos secondDoorPos = pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
        BlockState secondDoorState = world.getBlockState(secondDoorPos);

        if (secondDoorState.getBlock() == state.getBlock() && secondDoorState.getValue(DoorBlock.HINGE) != hinge) {
            return secondDoorPos;
        }

        return null;
    }

    private boolean isCopperDoor(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().contains("copper_door");
    }
}
