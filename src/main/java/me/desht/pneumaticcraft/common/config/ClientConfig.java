package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static class General {
        ForgeConfigSpec.BooleanValue aphorismDrama;
        ForgeConfigSpec.EnumValue<WidgetDifficulty> programmerDifficulty;
        ForgeConfigSpec.BooleanValue topShowsFluids;
        ForgeConfigSpec.BooleanValue logisticsGuiTint;
        ForgeConfigSpec.BooleanValue semiBlockLighting;
        ForgeConfigSpec.BooleanValue guiBevel;
        ForgeConfigSpec.BooleanValue alwaysShowPressureDurabilityBar;
        ForgeConfigSpec.BooleanValue tubeModuleRedstoneParticles;
        ForgeConfigSpec.BooleanValue guiRemoteGridSnap;
    }

    public static class Armor {
        ForgeConfigSpec.IntValue blockTrackerMaxTimePerTick;
        ForgeConfigSpec.DoubleValue leggingsFOVFactor;
        ForgeConfigSpec.BooleanValue fancyArmorModels;
        ForgeConfigSpec.BooleanValue pathEnabled;
        ForgeConfigSpec.BooleanValue wirePath;
        ForgeConfigSpec.BooleanValue xRayEnabled;
        ForgeConfigSpec.EnumValue<PathUpdateSetting> pathUpdateSetting;
    }

    public static class Sound {
        ForgeConfigSpec.DoubleValue elevatorVolumeRunning;
        ForgeConfigSpec.DoubleValue elevatorVolumeStartStop;
    }

    public ClientConfig.General general = new General();
    public ClientConfig.Armor armor = new Armor();
    public ClientConfig.Sound sound = new Sound();

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("general");
        general.aphorismDrama = builder
                .comment("Enable Aphorism Tile Drama!  http://mc-drama.herokuapp.com/")
                .translation("pneumaticcraft.config.client.general.aphorism_drama")
                .define("aphorism_drama", true);
        general.programmerDifficulty = builder
                .comment("Defines which widgets are shown in the Programmer GUI: easy, medium, or advanced")
                .translation("pneumaticcraft.config.client.general.fancy_armor_models")
                .defineEnum("programmer_difficulty", WidgetDifficulty.EASY);
        general.topShowsFluids = builder
                .comment("Show tank fluids with the The One Probe. Note that TOP also has support for showing tanks, which may or may not be enabled.")
                .translation("pneumaticcraft.config.client.general.top_shows_fluids")
                .define("top_shows_fluids", true);
        general.logisticsGuiTint = builder
                .comment("Tint Logistics configuration GUI backgrounds according to the colour of the logistics frame you are configuring.")
                .translation("pneumaticcraft.config.client.general.logistics_gui_tint")
                .define("logistics_gui_tint", true);
        general.guiBevel = builder
                .comment("Should GUI side tabs be shown with a beveled edge? Setting to false uses a plain black edge, as in earlier versions of the mod.")
                .translation("pneumaticcraft.config.client.general.gui_bevel")
                .define("gui_bevel", true);
        general.alwaysShowPressureDurabilityBar = builder
                .comment("Always show the pressure durability bar for pressurizable items, even when full?")
                .translation("pneumaticcraft.config.client.general.always_show_pressure_durability_bar")
                .define("always_show_pressure_durability_bar", true);
        general.tubeModuleRedstoneParticles = builder
                .comment("Should tube modules emit redstone play redstone particle effects when active?")
                .translation("pneumaticcraft.config.client.general.tube_module_redstone_particles")
                .define("tube_module_redstone_particles", true);
        general.guiRemoteGridSnap = builder
                .comment("Should widgets in the GUI Remote Editor be snapped to a 4x4 grid?")
                .translation("pneumaticcraft.config.client.general.gui_remote_grid_snap")
                .define("gui_remote_grid_snap", true);
        builder.pop();

        builder.push("armor");
        armor.fancyArmorModels = builder
                .comment("Use fancy models for Pneumatic Armor (currently unimplemented)")
                .translation("pneumaticcraft.config.client.armor.fancy_armor_models")
                .define("fancy_armor_models", true);
        armor.leggingsFOVFactor = builder
                .comment("Intensity of the FOV modification when using Pneumatic Leggings speed boost: 0.0 for no FOV modification, higher values zoom out more.  Note: non-zero values may cause FOV clashes with other mods.")
                .translation("pneumaticcraft.config.client.armor.leggings_fov_factor")
                .defineInRange("leggings_fov_factor", 0.0, 0.0, 1.0);
        armor.blockTrackerMaxTimePerTick = builder
                .comment("Maximum time, as a percentage of the tick, that the Pneumatic Helmet Block Tracker may take when active and scanning blocks. Larger values mean more rapid update of block information, but potentially greater impact on client FPS.")
                .translation("pneumaticcraft.config.client.armor.block_tracker_max_time_per_tick")
                .defineInRange("block_tracker_max_time_per_tick", 10, 1, 100);
        armor.pathEnabled = builder
                .comment("Enable the Pneumatic Helmet Coordinate Tracker pathfinder")
                .translation("pneumaticcraft.config.client.armor.path_enabled")
                .define("path_enabled", false);
        armor.wirePath = builder
                .comment("True if the Pneumatic Helmet Coordinate Tracker pathfinder should draw lines, false for tiles.")
                .translation("pneumaticcraft.config.client.armor.wire_path")
                .define("wire_path", false);
        armor.xRayEnabled = builder
                .comment("Should the Pneumatic Helmet Coordinate Tracker pathfinder path be visible through blocks?")
                .translation("pneumaticcraft.config.client.armor.xray_enabled")
                .define("xray_enabled", false);
        armor.pathUpdateSetting = builder
                .comment("How frequently should the Pneumatic Helmet Coordinate Tracker pathfinder path be recalculated?")
                .translation("pneumaticcraft.config.client.armor.xray_enabled")
                .defineEnum("path_update_setting", PathUpdateSetting.NORMAL);
        builder.pop();

        builder.push("sound");
        sound.elevatorVolumeRunning = builder
                .comment("Volume level of the Elevator while running")
                .translation("pneumaticcraft.config.client.sound.elevator_volume_running")
                .defineInRange("elevator_volume_running", 1d, 0d, 2d);
        sound.elevatorVolumeStartStop = builder
                .comment("Volume level of the Elevator *clunk* while starting/stopping")
                .translation("pneumaticcraft.config.client.sound.elevator_volume_start_stop")
                .defineInRange("elevator_volume_start_stop", 0.5d, 0d, 2d);
    }

    /**
     * Used by the Pneumatic Helmet coordinate tracker to control path update frequency.
     */
    public enum PathUpdateSetting {
        SLOW(100),
        NORMAL(20),
        FAST(1);

        private final int ticks;

        PathUpdateSetting(int ticks) {
            this.ticks = ticks;
        }

        public int getTicks() {
            return ticks;
        }

        public PathUpdateSetting cycle() {
            return PathUpdateSetting.values()[(ordinal() + 1) % values().length];
        }
    }
}
