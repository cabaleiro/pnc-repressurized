package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.BiFunction;

public class ItemDrone extends ItemPressurizable implements IChargeableContainerProvider, IProgrammable, IUpgradeAcceptor {
    private final BiFunction<World, PlayerEntity, EntityDrone> droneCreator;
    private final boolean programmable;

    public ItemDrone(BiFunction<World, PlayerEntity, EntityDrone> droneCreator, boolean programmable) {
        super((int)(PneumaticValues.DRONE_MAX_PRESSURE * PneumaticValues.DRONE_VOLUME), PneumaticValues.DRONE_VOLUME);
        this.droneCreator = droneCreator;
        this.programmable = programmable;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        if (!world.isRemote) {
            ItemStack iStack = ctx.getPlayer().getHeldItem(ctx.getHand());
            if (iStack.getItem() == ModItems.LOGISTICS_DRONE.get()) {
                AdvancementTriggers.LOGISTICS_DRONE_DEPLOYED.trigger((ServerPlayerEntity) ctx.getPlayer());
            }
            BlockPos placePos = pos.offset(ctx.getFace());
            spawnDrone(ctx.getPlayer(), world, pos, ctx.getFace(), placePos, iStack);
            iStack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    public void spawnDrone(PlayerEntity player, World world, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack){
        EntityDrone drone = droneCreator.apply(world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        drone.readFromItemStack(iStack);
        world.addEntity(drone);

        if (drone.addProgram(clickPos, facing, placePos, iStack, drone.progWidgets)) {
            TileEntityProgrammer.updatePuzzleConnections(drone.progWidgets);
        }

        drone.onInitialSpawn(world, world.getDifficultyForLocation(placePos), SpawnReason.TRIGGERED, new ILivingEntityData() {}, null);
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return programmable;
    }

    @Override
    public boolean usesPieces(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showProgramTooltip() {
        return true;
    }

    @Override
    public Map<EnumUpgrade,Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainers.CHARGING_DRONE.get());
    }
}
