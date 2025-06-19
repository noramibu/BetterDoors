package me.noramibu.mixin;

import me.noramibu.config.Config;
import me.noramibu.event.DoorOpenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {

    @Redirect(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean onNeighborUpdateSetBlockState(World world, BlockPos pos, BlockState newState, int flags) {
        BlockState oldState = world.getBlockState(pos);
        boolean wasOpen = oldState.getBlock() instanceof DoorBlock && oldState.get(DoorBlock.OPEN);
        boolean willBeOpen = newState.getBlock() instanceof DoorBlock && newState.get(DoorBlock.OPEN);

        boolean result = world.setBlockState(pos, newState, flags);

        if (result && wasOpen != willBeOpen && Config.checkForRedstone) {
            boolean isWooden = newState.isIn(net.minecraft.registry.tag.BlockTags.WOODEN_DOORS);

            if ((isWooden && Config.allowDoubleWoodenDoors) || (!isWooden && Config.allowDoubleIronDoors)) {
                 findAndToggleSecondDoor(world, pos, newState);
            }
        }
        return result;
    }

    private void findAndToggleSecondDoor(World world, BlockPos pos, BlockState state) {
        BlockPos otherPos = findSecondDoor(world, pos, state);
        if (otherPos != null) {
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.getBlock() == state.getBlock() && otherState.get(DoorBlock.OPEN) != state.get(DoorBlock.OPEN)) {
                DoorOpenHandler.toggleDoor(world, otherState, otherPos);
            }
        }
    }

    private BlockPos findSecondDoor(World world, BlockPos pos, BlockState state) {
        Direction facing = state.get(DoorBlock.FACING);
        DoorHinge hinge = state.get(DoorBlock.HINGE);
        BlockPos secondDoorPos = pos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
        BlockState secondDoorState = world.getBlockState(secondDoorPos);

        if (secondDoorState.getBlock() == state.getBlock() && secondDoorState.get(DoorBlock.HINGE) != hinge) {
            return secondDoorPos;
        }
        return null;
    }
} 