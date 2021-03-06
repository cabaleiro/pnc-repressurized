package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiDroneDebuggerOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityStart;
import me.desht.pneumaticcraft.common.network.PacketUpdateDebuggingDrone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.List;

public class RenderEntityTarget {
    private static final float STAT_SCALE = 0.02f;

    public final Entity entity;
    private final RenderTargetCircle circle1;
    private final RenderTargetCircle circle2;
    public int ticksExisted = 0;
    private float oldSize;
    private final WidgetAnimatedStat stat;
    private boolean didMakeLockSound;
    public boolean isLookingAtTarget;
    private List<String> textList = new ArrayList<>();
    private final List<IEntityTrackEntry> trackEntries;
    private int hackTime;
    private double distToEntity;

    public RenderEntityTarget(Entity entity) {
        this.entity = entity;
        trackEntries = EntityTrackHandler.getTrackersForEntity(entity);
        circle1 = new RenderTargetCircle(entity);
        circle2 = new RenderTargetCircle(entity);

        stat = new WidgetAnimatedStat(null, entity.getDisplayName().getFormattedText(), StatIcon.NONE,
                20, -20, 0x3000AA00, null, false);
        stat.setMinDimensionsAndReset(0, 0);
    }

    public RenderDroneAI getDroneAIRenderer() {
        for (IEntityTrackEntry tracker : trackEntries) {
            if (tracker instanceof EntityTrackHandler.EntityTrackEntryDrone) {
                return ((EntityTrackHandler.EntityTrackEntryDrone) tracker).getDroneAIRenderer();
            }
        }
        throw new IllegalStateException("[RenderTarget] Drone entity, but no drone AI Renderer?");
    }

    public void update() {
        stat.tickWidget();
        stat.setTitle(entity.getDisplayName().getFormattedText());
        PlayerEntity player = Minecraft.getInstance().player;

        distToEntity = entity.getDistance(ClientUtils.getClientPlayer());

        if (ticksExisted >= 30 && !didMakeLockSound) {
            didMakeLockSound = true;
            player.world.playSound(player.getPosX(), player.getPosY(), player.getPosZ(), ModSounds.HUD_ENTITY_LOCK.get(), SoundCategory.PLAYERS, 0.1F, 1.0F, true);
        }

        boolean tagged = ItemPneumaticArmor.isPlayerDebuggingEntity(player, entity);
        circle1.setRenderingAsTagged(tagged);
        circle2.setRenderingAsTagged(tagged);
        circle1.update();
        circle2.update();
        for (IEntityTrackEntry tracker : trackEntries) {
            tracker.update(entity);
        }

        isLookingAtTarget = isPlayerLookingAtTarget();

        if (hackTime > 0) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(entity, ClientUtils.getClientPlayer());
            if (hackableEntity != null) {
                hackTime++;
            } else {
                hackTime = 0;
            }
        }
    }

    public boolean isInitialized() {
        return ticksExisted > 120;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, boolean justRenderWhenHovering) {

        for (IEntityTrackEntry tracker : trackEntries) {
            tracker.render(matrixStack, buffer, entity, partialTicks);
        }

        double x = MathHelper.lerp(partialTicks, entity.prevPosX, entity.getPosX());
        double y = MathHelper.lerp(partialTicks, entity.prevPosY, entity.getPosY()) + entity.getHeight() / 2D;
        double z = MathHelper.lerp(partialTicks, entity.prevPosZ, entity.getPosZ());

        matrixStack.push();

        matrixStack.translate(x, y, z);
        RenderUtils.rotateToPlayerFacing(matrixStack);

        float size = entity.getHeight() * 0.5F;
        float alpha = 0.5F;
        if (ticksExisted < 60) {
            size += 5 - Math.abs(ticksExisted) * 0.083F;
            alpha = Math.abs(ticksExisted) * 0.005F;
        }
        float renderSize = MathHelper.lerp(partialTicks, oldSize, size);

        circle1.render(matrixStack, buffer, renderSize, partialTicks, alpha);
        circle2.render(matrixStack, buffer, renderSize + 0.2F, partialTicks, alpha);

        float targetAcquireProgress = ((ticksExisted + partialTicks - 50) / 0.7F);
        if (ticksExisted > 50 && ticksExisted <= 120) {
            RenderProgressBar.render3d(matrixStack, buffer, 0D, 0.4D, 1.8D, 0.7D, 0, targetAcquireProgress,  0xD0FFFF00, 0xD000FF00);
        }

        matrixStack.scale(STAT_SCALE, STAT_SCALE, STAT_SCALE);

        if (ticksExisted > 120) {
            if (justRenderWhenHovering && !isLookingAtTarget) {
                stat.closeWindow();
            } else {
                stat.openWindow();
            }
            textList = new ArrayList<>();
            for (IEntityTrackEntry tracker : trackEntries) {
                tracker.addInfo(entity, textList, isLookingAtTarget);
            }
            textList.add(String.format("Dist: %5.1fm", distToEntity));
            stat.setText(textList);
            // a bit of growing or shrinking to keep the stat on screen and/or of legible size
            float mul = getStatSizeMultiplier(distToEntity);
            matrixStack.scale(mul, mul, mul);
            stat.render3d(matrixStack, buffer, partialTicks);
        } else if (ticksExisted > 50) {
            RenderUtils.renderString3d("Acquiring Target...", 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
            RenderUtils.renderString3d((int)targetAcquireProgress + "%", 37, 24, 0xFF002F00, matrixStack, buffer, false, true);
        } else if (ticksExisted < -30) {
            stat.closeWindow();
            stat.render3d(matrixStack, buffer, partialTicks);
            RenderUtils.renderString3d("Lost Target!", 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
        }

        matrixStack.pop();

        oldSize = size;
    }

    private float getStatSizeMultiplier(double dist) {
        if (dist < 4) {
           return (float) (dist / 4);
        } else if (dist < 10) {
            return 1f;
        } else {
            return (float) (dist / 10);
        }
    }

    public List<String> getEntityText() {
        return textList;
    }

    private boolean isPlayerLookingAtTarget() {
        // code used from the Enderman player looking code.
        PlayerEntity player = Minecraft.getInstance().player;
        Vec3d vec3 = player.getLook(1.0F).normalize();
        Vec3d vec31 = new Vec3d(entity.getPosX() - player.getPosX(), entity.getBoundingBox().minY + entity.getHeight() / 2.0F - (player.getPosY() + player.getEyeHeight()), entity.getPosZ() - player.getPosZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 > 1.0D - 0.050D / d0;
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, ClientUtils.getClientPlayer());
            if (hackable != null && (hackTime == 0 || hackTime > hackable.getHackTime(entity, ClientUtils.getClientPlayer())))
                NetworkHandler.sendToServer(new PacketHackingEntityStart(entity));
        }
    }

    public void selectAsDebuggingTarget() {
        if (isInitialized() && isPlayerLookingAtTarget() && entity instanceof EntityDrone) {
            GuiDroneDebuggerOptions.clearAreaShowWidgetId();
            if (ItemPneumaticArmor.isPlayerDebuggingEntity(ClientUtils.getClientPlayer(), entity)) {
                NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(-1));
                Minecraft.getInstance().player.playSound(ModSounds.SCI_FI.get(), 1.0f, 2.0f);
            } else {
                NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(entity.getEntityId()));
                Minecraft.getInstance().player.playSound(ModSounds.HUD_ENTITY_LOCK.get(), 1.0f, 2.0f);
            }
        }
    }

    public void onHackConfirmServer() {
        hackTime = 1;
    }

    public int getHackTime() {
        return hackTime;
    }

    public boolean scroll(InputEvent.MouseScrollEvent event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        return false;
    }
}
