package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone.Operation;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToServer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.dyeColorDesc;

public class GuiRedstoneModule extends GuiTubeModule {
    private WidgetComboBox comboBox;
    private WidgetLabel constLabel;
    private WidgetTextFieldNumber textField;
    private WidgetLabel otherColorLabel;
    private WidgetColorSelector otherColorButton;
    private int ourColor;
    private int otherColor;
    private WidgetCheckBox invertCheckBox;
    private boolean upgraded;
    private boolean output;
    private final List<String> lowerText = new ArrayList<>();

    public GuiRedstoneModule(BlockPos modulePos) {
        super(modulePos);

        ySize = ((ModuleRedstone) module).getRedstoneDirection() == ModuleRedstone.EnumRedstoneDirection.OUTPUT ? 202 : 57;
    }

    @Override
    protected ResourceLocation getTexture() {
        return output ? Textures.GUI_WIDGET_OPTIONS : Textures.GUI_MODULE_SIMPLE;
    }

    @Override
    public void init() {
        super.init();

        ModuleRedstone mr = (ModuleRedstone) module;
        upgraded = mr.isUpgraded();
        output = mr.getRedstoneDirection() == ModuleRedstone.EnumRedstoneDirection.OUTPUT;
        ourColor = mr.getColorChannel();
        otherColor = mr.getOtherColor();

        addButton(new WidgetLabel(guiLeft + xSize / 2, guiTop + 5, getTitle().getFormattedText()).setAlignment(WidgetLabel.Alignment.CENTRE));

        WidgetLabel ourColorLabel;
        addButton(ourColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 25, I18n.format("gui.tubeModule.channel")));

        WidgetLabel opLabel;
        addButton(opLabel = new WidgetLabel(guiLeft + 10, guiTop + 45, I18n.format("gui.redstoneModule.operation")));
        opLabel.visible = output;

        otherColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 65, I18n.format("gui.tubeModule.otherChannel"));
        otherColorLabel.visible = output;
        addButton(otherColorLabel);

        constLabel = new WidgetLabel(guiLeft + 15, guiTop + 65, I18n.format("gui.redstoneModule.constant"));
        addButton(constLabel);
        constLabel.visible = output;

        int w = 0;
        for (WidgetLabel label : ImmutableList.of(ourColorLabel, otherColorLabel, opLabel, constLabel)) {
            w = Math.max(label.getWidth(), w);
        }
        int xBase = guiLeft + w + 15;

        addButton(new WidgetColorSelector(xBase, guiTop + 20, b -> ourColor = b.getColor().getId())
                .withInitialColor(DyeColor.byId(ourColor)));

        if (!output) return;

        List<String> ops = new ArrayList<>();
        for (Operation op : Operation.values()) {
            ops.add(I18n.format(op.getTranslationKey()));
        }
        comboBox = new WidgetComboBox(font, xBase, guiTop + 43, xSize - xBase + guiLeft - 10, 12)
                .setFixedOptions().setShouldSort(false).setElements(ops);
        comboBox.selectElement(mr.getOperation().ordinal());
        comboBox.active = upgraded;
        addButton(comboBox);

        otherColorButton = new WidgetColorSelector(xBase, guiTop + 60, b -> otherColor = b.getColor().getId())
                .withInitialColor(DyeColor.byId(otherColor));
        otherColorButton.active = upgraded;
        addButton(otherColorButton);

        textField = new WidgetTextFieldNumber(font, xBase, guiTop + 63, 30, 12);
        textField.minValue = 0;
        textField.setDecimals(0);
        textField.setValue(mr.getConstantVal());
        textField.active = upgraded;
        addButton(textField);

        invertCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 85, 0xFF404040, I18n.format("gui.redstoneModule.invert")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (comboBox.isFocused()) return true;  // it hangs over the button
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        invertCheckBox.checked = mr.isInverted();
        invertCheckBox.setTooltip(I18n.format("gui.redstoneModule.invert.tooltip"));
        addButton(invertCheckBox);

        updateWidgetVisibility();
    }

    @Override
    public void tick() {
        super.tick();

        if (output) {
            if (upgraded) {
                updateWidgetVisibility();
            }
            updateLowerText();
        }
    }

    private void updateWidgetVisibility() {
        Operation op = getSelectedOp();
        constLabel.visible = op.useConst();
        textField.setVisible(op.useConst());
        otherColorLabel.visible = op.useOtherColor();
        otherColorButton.visible = op.useOtherColor();
        otherColorButton.setVisible(op.useOtherColor());
    }

    private void updateLowerText() {
        lowerText.clear();

        Operation op = getSelectedOp();
        String key = op.getTranslationKey() + ".tooltip";
        String s;
        if (op.useConst()) {
            s = I18n.format(key, dyeColorDesc(ourColor), textField.getValue());
        } else if (op.useOtherColor()) {
            s = I18n.format(key, dyeColorDesc(ourColor), dyeColorDesc(otherColor));
        } else {
            s = I18n.format(key, dyeColorDesc(ourColor));
        }
        lowerText.addAll(PneumaticCraftUtils.splitString(s, 30));
        if (!upgraded) {
            List<String> extra = PneumaticCraftUtils.splitString(I18n.format("gui.redstoneModule.addAdvancedPCB"), 30).stream()
                    .map(str -> TextFormatting.DARK_BLUE + str)
                    .collect(Collectors.toList());
            lowerText.addAll(extra);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        int yBase = guiTop + ySize - lowerText.size() * font.FONT_HEIGHT - 10;
        for (int i = 0; i < lowerText.size(); i++) {
            font.drawString(lowerText.get(i), guiLeft + 10, yBase + i * font.FONT_HEIGHT, 0xFF404040);
        }
    }

    private Operation getSelectedOp() {
        return Operation.values()[comboBox.getSelectedElementIndex()];
    }

    @Override
    public void onClose() {
        super.onClose();

        ((ModuleRedstone) module).setColorChannel(ourColor);
        if (output) {
            ((ModuleRedstone) module).setInverted(invertCheckBox.checked);
            ((ModuleRedstone) module).setOperation(getSelectedOp(), otherColor, textField.getValue());
        }
        NetworkHandler.sendToServer(new PacketSyncRedstoneModuleToServer((ModuleRedstone) module));
    }
}
