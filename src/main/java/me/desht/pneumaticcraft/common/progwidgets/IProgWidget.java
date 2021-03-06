package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.IProgWidgetBase;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public interface IProgWidget extends IProgWidgetBase {
    int getX();

    int getY();

    void setX(int x);

    void setY(int y);

    int getWidth();

    int getHeight();

//    void render();

    ResourceLocation getTexture();

    Pair<Float,Float> getMaxUV();

    int getTextureSize();

    void getTooltip(List<ITextComponent> curTooltip);

    void addWarnings(List<ITextComponent> curInfo, List<IProgWidget> widgets);

    void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets);

    void renderExtraInfo();

    boolean hasStepInput();

    boolean hasStepOutput();

    default boolean freeToUse() { return false; }

    /**
     * Get the AI for this progwidget
     * @param drone the drone
     * @param widget will be 'this' most of the time, but not when controlled externally (e.g. ComputerCraft)
     * @return widget AI
     */
    Goal getWidgetAI(IDroneBase drone, IProgWidget widget);

    /**
     * Get the targeting AI for this progwidget
     * @param drone the drone
     * @param widget Will be 'this' most of the time, but not when controlled externally (e.g. ComputerCraft)
     * @return widget targeting AI
     */
    Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget);

    /**
     * Set the output widget for this widget, i.e. next in the program.  Called when building the program.
     * @param widget the next widget in the program
     */
    void setOutputWidget(IProgWidget widget);

    /**
     * Get the next widget in the program.
     * @return the next widget to run
     */
    IProgWidget getOutputWidget();

    /**
     * This one will be called when running in an actual program.
     *
     * @param drone the drone
     * @param allWidgets a list of widgets
     * @return the next widget to run
     */
    IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets);

    /**
     * For "auxiliary" widgets that are added onto the left or right of another widget, get the type of the widget
     * being connected to.
     *
     * @return the widget being connected to, or null if this widget isn't an "auxiliary" widget.
     */
    ProgWidgetType returnType();

    /**
     * Get the types of the widgets which connect to this widget on the right, in order (top to bottom). Note that the
     * length of the widget array returned by {@link #getConnectedParameters()} will be double the length of this list,
     * since these widgets can be added on both the right (whitelist) and left (blacklist) sides.
     *
     * @return a list of widget types, or an empty list if no widgets can be added to the side of this widget
     */
    @Nonnull
    List<ProgWidgetType<?>> getParameters();

    void setParameter(int index, IProgWidget parm);

    boolean canSetParameter(int index);

    IProgWidget[] getConnectedParameters();

    void setParent(IProgWidget widget);

    IProgWidget getParent();

    ResourceLocation getTypeID();

    default String getTranslationKey() {
        String s = getTypeID().toString().replace(':', '.');
        return "programmingPuzzle." + s + ".name";
    }

    DyeColor getColor();

    /**
     * At least do <code>tag.putString("name", getTypeID().toString());</code>
     * <p>Note that the base implementation {@link ProgWidget} does this.</p>
     *
     * @param tag NBT tag to write to
     */
    void writeToNBT(CompoundNBT tag);

    void readFromNBT(CompoundNBT tag);

    IProgWidget copy();

    boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget);

    WidgetDifficulty getDifficulty();

    ProgWidgetType getType();

    void readFromPacket(PacketBuffer buf);

    void writeToPacket(PacketBuffer buf);

    String getExtraStringInfo();

    enum WidgetDifficulty {
        EASY("easy"), MEDIUM("medium"), ADVANCED("advanced");

        private final String name;

        WidgetDifficulty(String name) {
            this.name = name;
        }

        public String getTranslationKey() {
            return I18n.format("gui.progWidget.difficulty." + name);
        }
    }

    /**
     * Cast from the API interface to our internal interface.  Should always succeed!
     *
     * @param type type of the progwidget
     * @return the internal non-API progwidget type
     */
    static IProgWidget create(ProgWidgetType type) {
        IProgWidgetBase base = type.create();
        Validate.isTrue(base instanceof IProgWidget);
        return (IProgWidget) base;
    }
}
