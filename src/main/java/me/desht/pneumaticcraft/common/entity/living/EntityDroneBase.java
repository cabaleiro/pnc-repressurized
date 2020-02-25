package me.desht.pneumaticcraft.common.entity.living;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class EntityDroneBase extends CreatureEntity {
    public float oldPropRotation;
    public float propRotation;
    public float laserExtension; // How far the laser comes out of the drone. 1F is fully extended
    public float oldLaserExtension;

    public EntityDroneBase(EntityType<? extends CreatureEntity> type, World world) {
        super(type, world);
    }

    public double getLaserOffsetY() {
        return 0.05;
    }

    public int getLaserColor() {
        return 0xFF0000;
    }

    public int getDroneColor() {
        return DyeColor.BLACK.getId();
    }

    public boolean isAccelerating() {
        return true;
    }

    public abstract BlockPos getDugBlock();

    @Nonnull
    public abstract ItemStack getDroneHeldItem();
}
