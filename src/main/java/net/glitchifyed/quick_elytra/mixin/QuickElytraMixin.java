package net.glitchifyed.quick_elytra.mixin;

import net.glitchifyed.quick_elytra.QuickElytra;
import net.glitchifyed.quick_elytra.config.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.glitchifyed.quick_elytra.QuickElytra.CLIENT;

@Mixin(MiningToolItem.class)
public class QuickElytraMixin {
	@Inject(at = @At("TAIL"), method = "postMine(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)Z")
	private void injected(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> info) {
		if (!ConfigHandler.replantCrops) {
			return;
		}

		Item item = itemStack.getItem();
		if (!(item instanceof HoeItem)) {
			return;
		}

		Block block = blockState.getBlock();
		if (block instanceof CropBlock) {
			if (livingEntity.isPlayer()) {
				PlayerEntity minedPlayer = (PlayerEntity) livingEntity;
				PlayerEntity player = CLIENT.player;

				if (Objects.equals(player.getUuidAsString(), minedPlayer.getUuidAsString())) {
					Item cropItem = block.asItem();

					if (cropItem != Items.AIR) {
						DefaultedList<ItemStack> inventory = player.getInventory().main;
						int cropSlot = -1;

						if (player.getInventory().offHand.get(0).isOf(cropItem)) {
							cropSlot = 45;
						}
						else {
							for (ItemStack inventoryItemStack : inventory) {
								if (inventoryItemStack.getItem() == cropItem) {
									int i = inventory.indexOf(inventoryItemStack);
									cropSlot = i;

									break;
								}
							}
						}

						if (cropSlot == -1) {
							for (int i = 0; i < QuickElytra.emptyAttempts; i++) {
								QuickElytra.emptyCropTicks.add((i + 1) * 20);
								QuickElytra.emptyCropResults.add((BlockHitResult) CLIENT.crosshairTarget);
							}
						}
						else {
							if (cropSlot != 45) {
								ItemStack offhandStack = player.getInventory().offHand.get(0);

								if (offhandStack == ItemStack.EMPTY || !QuickElytra.cropSwitchedItemStack.isOf(offhandStack.getItem())) {
									if (QuickElytra.cropSwitchedItemStack == ItemStack.EMPTY) {
										QuickElytra.cropSwitchedItemStack = offhandStack;
									}

									QuickElytra.switchSelectedSlot(cropSlot);
								}
							}

							QuickElytra.switchTimer = QuickElytra.switchTime;

							for (int i = 0; i < 2; i++) {
								QuickElytra.cropTicks.add(i);
								QuickElytra.cropResults.add((BlockHitResult) CLIENT.crosshairTarget);
							}
						}
					}
				}
			}
		}
	}
}