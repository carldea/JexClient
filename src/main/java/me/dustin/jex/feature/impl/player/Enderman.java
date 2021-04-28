package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

@Feat(name = "Enderman", category = FeatureCategory.PLAYER, description = "Select how to deal with enderman.")
public class Enderman extends Feature {

    @Op(name = "Mode", all = {"Look At", "Look Away"})
    public String mode = "Look At";

    @EventListener(events = {EventPlayerPackets.class}, priority = EventPriority.HIGH)
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            switch (mode.toLowerCase()) {
                case "look at":
                    EndermanEntity lookat = getEnderman();
                    if (lookat != null) {
                        RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), lookat);
                        eventPlayerPackets.setRotation(rotation);
                    }
                    break;
                case "look away":
                    for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                        if (entity instanceof EndermanEntity) {
                            if (isPlayerStaring(Wrapper.INSTANCE.getLocalPlayer(), (EndermanEntity) entity)) {
                                if (PlayerHelper.INSTANCE.getPitch() > 85)
                                    eventPlayerPackets.setPitch(-90);
                                else
                                    eventPlayerPackets.setPitch(90);
                                break;
                            }
                        }
                    }
                    break;
            }
        }
        setSuffix(mode);
    }

    private boolean isPlayerStaring(PlayerEntity player, EndermanEntity endermanEntity) {
        ItemStack itemStack = (ItemStack) player.inventory.armor.get(3);
        if (itemStack.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
            return false;
        } else {
            Vec3d vec3d = player.getRotationVec(1.0F).normalize();
            Vec3d vec3d2 = new Vec3d(endermanEntity.getX() - player.getX(), endermanEntity.getEyeY() - player.getEyeY(), endermanEntity.getZ() - player.getZ());
            double d = vec3d2.length();
            vec3d2 = vec3d2.normalize();
            double e = vec3d.dotProduct(vec3d2);
            return e > 1.0D - 0.025D / d ? player.canSee(endermanEntity) : false;
        }
    }

    private EndermanEntity getEnderman() {
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof EndermanEntity) {
                EndermanEntity endermanEntity1 = (EndermanEntity) entity;
                if (!endermanEntity1.isAngry() && Wrapper.INSTANCE.getLocalPlayer().canSee(endermanEntity1))
                    return endermanEntity1;
            }
        }
        return null;
    }

}
