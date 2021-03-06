package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class ContainerLogistics extends ContainerPneumaticBase<TileEntityBase> {
    public final EntityLogisticsFrame logistics;
    private final boolean itemContainer;  // true if GUI opened from held item, false if from in-world entity

    public ContainerLogistics(ContainerType<?> containerType, int i, PlayerInventory playerInventory, int entityId) {
        super(containerType, i, playerInventory);

        World world = playerInventory.player.world;
        if (entityId == -1) {
            // opening container from held item; no in-world entity so fake one up from the held item NBT
            this.logistics = EntityLogisticsFrame.fromItemStack(world, playerInventory.player, getHeldLogisticsFrame(playerInventory.player));
            this.itemContainer = true;
        } else {
            Entity e = world.getEntityByID(entityId);
            if (e instanceof EntityLogisticsFrame) {
                this.logistics = (EntityLogisticsFrame) e;
            } else {
                this.logistics = null;
                Log.error("no logistics frame entity for id %d!", entityId);
            }
            this.itemContainer = false;
        }
        if (logistics != null) {
            IItemHandler requests = logistics.getItemFilterHandler();
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 9; x++) {
                    addSlot(logistics.canFilterStack() ?
                            new SlotPhantom(requests, y * 9 + x, x * 18 + 8, y * 18 + 29) :
                            new SlotPhantomUnstackable(requests, y * 9 + x, x * 18 + 8, y * 18 + 29));
                }
            }

            addPlayerSlots(playerInventory, 134);
        }
    }

    private ContainerLogistics(ContainerType logisticsFrameRequester, int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(logisticsFrameRequester, i, playerInventory, buffer.readVarInt());
    }

    private ItemStack getHeldLogisticsFrame(PlayerEntity player) {
        if (player.getHeldItemMainhand().getItem() instanceof ItemLogisticsFrame) {
            return player.getHeldItemMainhand();
        } else if (player.getHeldItem(Hand.OFF_HAND).getItem() instanceof ItemLogisticsFrame) {
            return player.getHeldItem(Hand.OFF_HAND);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public boolean isItemContainer() {
        return itemContainer;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);
        if (logistics != null) {
            logistics.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return logistics != null && logistics.isValid();
    }

    /**
     * Called when the container is closed. If configuring a logistics frame in-hand, update its NBT now.
     */
    @Override
    public void onContainerClosed(PlayerEntity player) {
        if (itemContainer && logistics != null && !player.getEntityWorld().isRemote) {
            updateHeldItem(player, null);
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex) {
        Slot srcSlot = inventorySlots.get(slotIndex);
        if (slotIndex >= playerSlotsStart && srcSlot != null && srcSlot.getHasStack()) {
            // shift-click from player inventory into filter
            ItemStack stackInSlot = srcSlot.getStack();
            for (int i = 0; i < 27; i++) {
                Slot slot = inventorySlots.get(i);
                if (!slot.getHasStack()) {
                    ItemStack s = logistics.canFilterStack() ?
                            stackInSlot.copy() :
                            ItemHandlerHelper.copyStackWithSize(stackInSlot, 1);
                    slot.putStack(s);
                    break;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static ContainerLogistics createProviderContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_PROVIDER.get(), i, playerInventory, buffer);
    }

    public static ContainerLogistics createRequesterContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_REQUESTER.get(), i, playerInventory, buffer);
    }

    public static ContainerLogistics createStorageContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_STORAGE.get(), i, playerInventory, buffer);
    }

//    public static ContainerLogistics createDefaultStorageContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
//        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), i, playerInventory, buffer);
//    }

    public void updateHeldItem(PlayerEntity player, PacketBuffer payload) {
        if (logistics != null) {
            if (payload != null) logistics.readFromBuf(payload);
            ItemStack stack = getHeldLogisticsFrame(player);
            if (!stack.isEmpty()) {
                CompoundNBT subtag = logistics.serializeNBT(new CompoundNBT());
                stack.getOrCreateTag().put("EntityTag", subtag);
            }
        }
    }
}
