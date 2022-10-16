package net.glitchifyed.quick_elytra;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.glitchifyed.quick_elytra.config.ConfigHandler;
import net.glitchifyed.quick_elytra.event.KeyInputHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class QuickElytra implements ClientModInitializer {
	public static final String MODID = "quick_elytra";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static MinecraftClient CLIENT;
	public static final List<Integer> cropTicks = new LinkedList<>();
	public static final List<BlockHitResult> cropResults = new LinkedList<>();
	public static final List<Integer> emptyCropTicks = new LinkedList<>();
	public static final List<BlockHitResult> emptyCropResults = new LinkedList<>();
	public static final int emptyAttempts = 2;
	public static int switchTimer = 0;
	public static int switchTime = 40;
	public static ItemStack cropSwitchedItemStack = ItemStack.EMPTY;

	@Override
	public void onInitializeClient() {
		CLIENT = MinecraftClient.getInstance();

		ConfigHandler.register();

		KeyInputHandler.register();

		ClientTickEvents.END_WORLD_TICK.register(client -> {
			Replant(0, cropTicks, cropResults);
			Replant(1, cropTicks, cropResults);

			for (int i = 0; i < emptyCropTicks.size(); i++) {
				Replant(i, emptyCropTicks, emptyCropResults);
			}

			if (switchTimer == 0) {
				switchTimer = -1;

				if (!cropSwitchedItemStack.isOf(Items.AIR)) {
					ItemStack switchStack = cropSwitchedItemStack;
					cropSwitchedItemStack = ItemStack.EMPTY;

					if (ConfigHandler.replantSwaps) {
						switchSelectedSlot(CLIENT.player.getInventory().main.indexOf(switchStack));
					}
				}
			}
			else if (switchTimer > 0) {
				switchTimer--;
			}
		});
	}

	public static void switchSelectedSlot(int slotId) {
		if (slotId == -1) {
			return;
		}

		CLIENT.interactionManager.clickSlot(
				CLIENT.player.playerScreenHandler.syncId,
				45,
				slotId,
				SlotActionType.SWAP,
				CLIENT.player
		);
	}

	private static void Replant(int i, List<Integer> ticksList, List<BlockHitResult> resultsList) {
		if (ticksList.size() > i) {
			int tickAmount = ticksList.get(i);

			if (tickAmount <= 0) {
				BlockHitResult blockHitResult = resultsList.get(i);

				ticksList.remove(i);
				resultsList.remove(i);

				Packet packet = new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, blockHitResult, 1);

				CLIENT.getNetworkHandler().sendPacket(packet);
			} else {
				ticksList.set(i, tickAmount - 1);
			}
		}
	}
}
