package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to get the client to spawn a new client-side ring entity
 */
public class PacketSpawnRing extends LocationDoublePacket {

    private int[] colors;
    private int targetEntityId;

    public PacketSpawnRing() {
    }

    public PacketSpawnRing(double x, double y, double z, Entity targetEntity, int... colors) {
        super(x, y, z);
        targetEntityId = targetEntity.getEntityId();
        this.colors = colors;
    }

    public PacketSpawnRing(PacketBuffer buffer) {
        super(buffer);
        targetEntityId = buffer.readInt();
        colors = new int[buffer.readInt()];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = buffer.readInt();
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeInt(targetEntityId);
        buffer.writeInt(colors.length);
        Arrays.stream(colors).forEach(buffer::writeInt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtils.getClientWorld();
            Entity entity = world.getEntityByID(targetEntityId);
            if (entity != null) {
                for (int color : colors) {
                    world.addEntity(new EntityRing(world, x, y, z, entity, color));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
