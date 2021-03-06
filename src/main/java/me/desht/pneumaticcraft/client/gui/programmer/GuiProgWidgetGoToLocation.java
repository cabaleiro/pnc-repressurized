package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetGoToLocation extends GuiProgWidgetAreaShow<ProgWidgetGoToLocation> {

    public GuiProgWidgetGoToLocation(ProgWidgetGoToLocation progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 8, guiTop + 24, 0xFF404040,
                I18n.format("gui.progWidget.goto.doneWhenDeparting"),b -> progWidget.doneWhenDeparting = false);
        radioButton.checked = !progWidget.doneWhenDeparting;
        radioButton.setTooltip(PneumaticCraftUtils.splitString(I18n.format("gui.progWidget.goto.doneWhenDeparting.tooltip")));
        addButton(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        WidgetRadioButton radioButton2 = new WidgetRadioButton(guiLeft + 8, guiTop + 38, 0xFF404040,
                I18n.format("gui.progWidget.goto.doneWhenArrived"), b -> progWidget.doneWhenDeparting = true);
        radioButton2.checked = progWidget.doneWhenDeparting;
        radioButton2.setTooltip(PneumaticCraftUtils.splitString(I18n.format("gui.progWidget.goto.doneWhenArrived.tooltip")));
        addButton(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
    }

}
