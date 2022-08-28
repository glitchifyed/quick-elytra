package net.glitchifyed.quick_elytra;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.glitchifyed.quick_elytra.event.KeyInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickElytraClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyInputHandler.register();
	}
}
