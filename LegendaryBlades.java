package com.legendaryblades;

import com.legendaryblades.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegendaryBlades implements ModInitializer {
    public static final String MOD_ID = "legendary_blades";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();
        LOGGER.info("Legendary Blades loaded - Yagura no Tachi ready.");
    }
}
