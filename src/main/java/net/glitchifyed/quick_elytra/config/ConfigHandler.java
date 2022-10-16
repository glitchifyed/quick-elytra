package net.glitchifyed.quick_elytra.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Decoder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.glitchifyed.quick_elytra.QuickElytra;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;

public class ConfigHandler {
    public static SimpleConfig CONFIG;
    static ConfigProvider configs;

    public static boolean replantCrops = true;
    public static boolean replantSwaps = true;

    public static Screen createConfigMenu() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(QuickElytra.CLIENT.currentScreen)
                .setTitle(Text.translatable("title.glitchifyed.quick_elytra.config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.glitchifyed.quick_elytra.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.glitchifyed.quick_elytra.replant_crops"), replantCrops)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("If enabled, crops will be replanted after harvesting with a hoe."))
                .setSaveConsumer(newValue -> replantCrops = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.glitchifyed.quick_elytra.replant_swaps"), replantSwaps)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("If enabled when crops are replanted, the offhand will switch back to its original item."))
                .setSaveConsumer(newValue -> replantSwaps = newValue)
                .build());

        builder.setSavingRunnable(() -> {
            QuickElytra.LOGGER.info("SAVE CONFIG");

            SaveToFile();
        });

        Screen screen = builder.build();

        return screen;
    }

    public static void register() {
        configs = new ConfigProvider();

        RetreiveFromFile();
    }

    static void SaveToFile() {
        configs.resetConfig();

        configs.addKeyValuePair(new Pair<>("replant_crops", replantCrops), "replant crops");
        configs.addKeyValuePair(new Pair<>("replant_swaps", replantSwaps), "replant swaps");

        if (CONFIG != null) {
            QuickElytra.LOGGER.info("Attempting to save config to a file");

            try {
                CONFIG.saveConfig();
            } catch (Exception e) {
                QuickElytra.LOGGER.info("Couldn't save config");
            }
        }
    }

    static void RetreiveFromFile() {
        // set data
        SaveToFile();

        CONFIG = SimpleConfig.of(QuickElytra.MODID + ".config").provider(configs).request();

        // get keys
        replantCrops = CONFIG.getOrDefault("replant_crops", true);
        replantCrops = CONFIG.getOrDefault("replant_swaps", true);
    }
}