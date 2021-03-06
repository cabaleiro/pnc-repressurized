package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

public class WidgetTemperature extends Widget implements ITooltipProvider {

    private int[] scales;
    protected final LazyOptional<IHeatExchangerLogic> logic;
    private final int minTemp, maxTemp;

    public WidgetTemperature(int x, int y, int minTemp, int maxTemp, LazyOptional<IHeatExchangerLogic> logic, int... scales) {
        super(x, y, 13, 50, "");
        this.scales = scales;
        this.logic = logic;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp - 273;
    }

    public void setScales(int... scales) {
        this.scales = scales;
    }

    public int[] getScales() {
        return scales;
    }

    public void setTemperature(double temp) {
        logic.ifPresent(h -> h.setTemperature(temp));
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            RenderSystem.disableLighting();
            Minecraft.getInstance().getTextureManager().bindTexture(Textures.WIDGET_TEMPERATURE);
            RenderSystem.color4f(1, 1, 1, 1);
            AbstractGui.blit(x + 6, y, 6, 0, 7, 50, 18, 50);

            int h = height - 2;

            int temp = logic.map(IHeatExchangerLogic::getTemperatureAsInt).orElseThrow(RuntimeException::new);
            int barLength = (temp - minTemp) * h / maxTemp;
            barLength = MathHelper.clamp(barLength, 0, h);
            AbstractGui.blit(x + 7, y + 1 + h - barLength, 13, h - barLength, 5, barLength, 18, 50);

            for (int scale : scales) {
                if (scale != 0) {
                    int scaleY = h - (scale - minTemp) * h / maxTemp;
                    int v = scaleY < 0 ? 6 : (scaleY > 48 ? 12 : 0);
                    AbstractGui.blit(x, y - 1 + MathHelper.clamp(scaleY, 0, h), 0, v, 6, 6, 18, 50);
                }
            }

        }
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
        int temp = logic.map(IHeatExchangerLogic::getTemperatureAsInt).orElseThrow(RuntimeException::new);
        curTip.add(HeatUtil.formatHeatString(temp).getFormattedText());
    }
}
