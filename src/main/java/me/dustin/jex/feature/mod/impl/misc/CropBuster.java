package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Feature.Manifest(name = "CropBuster", category = Feature.Category.MISC, description = "Destroy any fully grown crops nearby")
public class CropBuster extends Feature {

    @Op(name = "Break Delay (MS)", max = 1000, inc = 10)
    public int breakDelay = 100;

    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (!timer.hasPassed(breakDelay))
                    return;
                timer.reset();
                BlockPos crop = getCrop();
                if (crop != null) {
                    RotationVector rot = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(crop.getX(), crop.getY(), crop.getZ()));
                    eventPlayerPackets.setRotation(rot);
                    rot.normalize();
                    Direction facing = Direction.fromRotation(-rot.getYaw());
                    Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(crop, facing);
                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                }
            }
        } else if (event instanceof EventRender3D) {
            for (int x = -4; x < 4; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -4; z < 4; z++) {
                        BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                        if (isCrop(blockPos)) {
                            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
                            Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                            Render3DHelper.INSTANCE.drawBoxOutline(((EventRender3D) event).getMatrixStack(), box, 0xffff0000);
                        }
                    }
                }
            }
        }
    }

    public BlockPos getCrop() {
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    if (isCrop(blockPos))
                        return blockPos;
                }
            }
        }
        return null;
    }

    public boolean isCrop(BlockPos blockPos) {
        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
        if (block instanceof CropBlock cropBlock) {
            int age = Wrapper.INSTANCE.getWorld().getBlockState(blockPos).get(cropBlock.getAgeProperty());
            if (age == cropBlock.getMaxAge()) {
                return true;
            }
        } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true;
        } else if (block == Blocks.SUGAR_CANE) {
            Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.down());
            if (belowBlock == Blocks.SUGAR_CANE)
                return true;
        } else if (block == Blocks.BAMBOO) {
            Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.down());
            if (belowBlock == Blocks.BAMBOO)
                return true;
        }
        return false;
    }

}
