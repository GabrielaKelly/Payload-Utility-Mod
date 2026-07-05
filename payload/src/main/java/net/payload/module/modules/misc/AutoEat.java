package net.payload.module.modules.misc;

import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.player.InvUtils;
import net.payload.utils.player.PlayerUtils;
import net.payload.utils.player.SlotUtils;
import net.payload.utils.player.combat.EntityUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AutoEat extends Module implements TickListener {

	private final FloatSetting hungerSetting = FloatSetting.builder()
			.id("autoeat_hunger")
			.displayName("Hunger")
			.description("Determines when AutoEat will trigger.")
			.defaultValue(10f)
			.minValue(1f)
			.maxValue(20f)
			.step(1f)
			.build();

	private final FloatSetting healthSetting = FloatSetting.builder()
			.id("autoeat_health")
			.displayName("Health")
			.description("Determines when AutoEat will trigger based on health.")
			.defaultValue(10f)
			.minValue(1f)
			.maxValue(20f)
			.step(1f)
			.build();

	private final BooleanSetting prioritizeGapples = BooleanSetting.builder()
			.id("autoeat_prioritize_gapples")
			.displayName("Prioritize Gapples")
			.description("Prioritizes enchanted golden apples and golden apples.")
			.defaultValue(true)
			.build();

	private final BooleanSetting blacklistGapples = BooleanSetting.builder()
			.id("autoeat_blacklist_gapples")
			.displayName("Blacklist Gapples")
			.description("Prevents eating golden apples and enchanted golden apples.")
			.defaultValue(false)
			.build();

	private final BooleanSetting useInventory = BooleanSetting.builder()
			.id("autoeat_use_inventory")
			.displayName("Use Inventory")
			.description("Allows using food from the entire inventory, not just hotbar.")
			.defaultValue(false)
			.build();

	private final BooleanSetting returnToSlot = BooleanSetting.builder()
			.id("autoeat_return_to_slot")
			.displayName("Return To Slot")
			.description("Returns to original slot after eating.")
			.defaultValue(true)
			.build();

	private final BooleanSetting auraPause = BooleanSetting.builder()
			.id("autoeat_pauseauras")
			.displayName("Pause Auras")
			.description("Stops attacks when eating")
			.defaultValue(false)
			.build();

	public enum EatSetting {
		Hunger, Health, Both, Any
	}

	private final EnumSetting<EatSetting> eatMode = EnumSetting.<EatSetting>builder()
			.id("autoeat_mode")
			.displayName("Check For")
			.description("Which condition to check for eating")
			.defaultValue(EatSetting.Hunger)
			.build();

	private static final Item[] BLACKLISTED_ITEMS = {
			Items.CHORUS_FRUIT,
			Items.POISONOUS_POTATO,
			Items.PUFFERFISH,
			Items.CHICKEN,
			Items.ROTTEN_FLESH,
			Items.SPIDER_EYE,
			Items.SUSPICIOUS_STEW
	};

	private static final Item[] GAPPLE_ITEMS = {
			Items.ENCHANTED_GOLDEN_APPLE,
			Items.GOLDEN_APPLE
	};

	private final List<Item> blacklist = Arrays.asList(BLACKLISTED_ITEMS);
	private final List<Item> gapples = Arrays.asList(GAPPLE_ITEMS);

	private boolean eating;
	private int slot, prevSlot;
	private int lastInventorySlot = -1;
	private int originalSlot = -1;

	public AutoEat() {
		super("AutoEat");

		this.setCategory(Category.of("Misc"));
		this.setDescription("Automatically eats the best food in your inventory");

		this.addSetting(eatMode);
		this.addSetting(hungerSetting);
		this.addSetting(healthSetting);
		this.addSetting(prioritizeGapples);
		this.addSetting(blacklistGapples);
		this.addSetting(useInventory);
		this.addSetting(returnToSlot);
		this.addSetting(auraPause);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);

		if (eating) stopEating();
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		eating = false;
		lastInventorySlot = -1;
		originalSlot = -1;
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (nullCheck()) return;

		if (eating) {
			// If we are eating check if we should still be eating
			if (shouldEat()) {
				// Check if the item in current slot is still food
				if (isFoodItem(MC.player.getInventory().getStack(slot))) {
					// Continue eating
					eat();
				} else {
					// Find a new food slot
					FoodSlot foodSlot = findBestFoodSlot();

					// If no valid slot was found then stop eating
					if (foodSlot == null) {
						stopEating();
						return;
					}
					// Otherwise change to the new slot
					else {
						changeSlot(foodSlot.slot);
						EntityUtil.syncInventory();
					}
				}
			} else {
				// If we shouldn't be eating anymore then stop
				stopEating();
			}
		} else {
			// If we are not eating check if we should start eating
			if (shouldEat()) {
				// Try to find a valid slot
				FoodSlot foodSlot = findBestFoodSlot();

				// If slot was found then start eating
				if (foodSlot != null) {
					slot = foodSlot.slot;
					startEating();
				}
			}
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {
	}

	private void startEating() {
		// Remember the original slot before we start swapping
		originalSlot = MC.player.getInventory().selectedSlot;
		prevSlot = originalSlot;
		eat();
	}

	private void eat() {
		changeSlot(slot);
		EntityUtil.syncInventory();
		setPressed(true);
		if (!MC.player.isUsingItem()) {
			PlayerUtils.rightClick();
		}
		eating = true;
	}

	private void stopEating() {
		setPressed(false);
		MC.options.useKey.setPressed(false);

		// Only return to original slot if the setting is enabled
		if (returnToSlot.getValue()) {
			// Swap back to the original slot
			if (lastInventorySlot != -1) {
				// If we were using an inventory slot, swap back properly
				InventoryUtil.inventorySwap(lastInventorySlot, originalSlot);
				lastInventorySlot = -1;
			} else {
				// Normal hotbar swap back to original
				InventoryUtil.switchToSlot(originalSlot);
			}

			EntityUtil.syncInventory();
		}

		eating = false;
		originalSlot = -1;
	}

	private void setPressed(boolean pressed) {
		MC.options.useKey.setPressed(pressed);
	}

	private void changeSlot(int newSlot) {
		// Handle inventory slots (non-hotbar)
		if (newSlot >= 9 && newSlot < 36) {
			if (useInventory.getValue()) {
				// Remember this was an inventory swap
				lastInventorySlot = newSlot;
				// Remember the original slot if not already set
				if (originalSlot == -1) {
					originalSlot = MC.player.getInventory().selectedSlot;
				}
				// Swap the inventory item to the hotbar
				InventoryUtil.inventorySwap(newSlot, MC.player.getInventory().selectedSlot);
				slot = MC.player.getInventory().selectedSlot;
			}
		} else if (newSlot == SlotUtils.OFFHAND) {
			// No need to change selected slot for offhand
		} else {
			// Normal hotbar swap
			InventoryUtil.switchToSlot(newSlot);
			slot = newSlot;
		}
	}

	/**
	 * Determines if eating should be triggered based on current conditions
	 * and selected eat mode.
	 *
	 * @return true if eating should be triggered, false otherwise
	 */
	public boolean shouldEat() {
		boolean healthLow = MC.player.getHealth() <= healthSetting.getValue().intValue();
		boolean hungerLow = MC.player.getHungerManager().getFoodLevel() <= hungerSetting.getValue();

		// Only override hunger check with gapples when in Health or Any mode
		boolean hasGapple = false;
		FoodSlot foodSlot = findBestFoodSlot();
		if (foodSlot != null) {
			hasGapple = foodSlot.isGapple;

			// Gapples can be used for healing only in health or any mode
			if (hasGapple && healthLow && (eatMode.getValue() == EatSetting.Health || eatMode.getValue() == EatSetting.Any)) {
				hungerLow = true;
			}
		}

		// Don't eat for health if hunger is full (as eating won't heal)
		// Unless we're using gapples which can heal directly in the appropriate mode
		if (MC.player.getHungerManager().getFoodLevel() >= 20 && healthLow &&
				!(hasGapple && (eatMode.getValue() == EatSetting.Health || eatMode.getValue() == EatSetting.Any))) {
			healthLow = false;
		}

		switch (eatMode.getValue()) {
			case Both:
				return healthLow && hungerLow;
			case Health:
				return healthLow;
			case Hunger:
				return hungerLow;
			case Any:
				return healthLow || hungerLow;
			default:
				return false;
		}
	}

	private static class FoodSlot {
		final int slot;
		final int hunger;
		final boolean isGapple;
		final float saturation;

		FoodSlot(int slot, int hunger, float saturation, boolean isGapple) {
			this.slot = slot;
			this.hunger = hunger;
			this.saturation = saturation;
			this.isGapple = isGapple;
		}
	}

	private boolean isFoodItem(ItemStack stack) {
		return stack.get(DataComponentTypes.FOOD) != null;
	}

	/**
	 * Finds the best food item to eat based on current settings.
	 * Prioritizes gapples if enabled, then higher hunger restoration,
	 * then higher saturation.
	 *
	 * @return The best food slot, or null if no suitable food was found
	 */
	private FoodSlot findBestFoodSlot() {
		Optional<FoodSlot> bestFood = Optional.empty();

		// Check all slots (hotbar + inventory if enabled)
		int maxSlot = useInventory.getValue() ? 36 : 9;

		for (int i = 0; i < maxSlot; i++) {
			ItemStack stack = MC.player.getInventory().getStack(i);
			Item item = stack.getItem();

			// Skip if item isn't food or is blacklisted
			FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
			if (foodComponent == null || blacklist.contains(item)) continue;

			boolean isGapple = gapples.contains(item);

			// Skip gapples if they're blacklisted
			if (isGapple && blacklistGapples.getValue()) continue;

			// Skip gapples in Hunger mode if health is low but hunger is not
			if (isGapple && eatMode.getValue() == EatSetting.Hunger &&
					MC.player.getHealth() <= healthSetting.getValue() &&
					MC.player.getHungerManager().getFoodLevel() > hungerSetting.getValue()) {
				continue;
			}

			FoodSlot foodSlot = new FoodSlot(
					i,
					foodComponent.nutrition(),
					foodComponent.saturation(),
					isGapple
			);

			// Update best food based on priority settings
			if (!bestFood.isPresent()) {
				bestFood = Optional.of(foodSlot);
			} else {
				FoodSlot current = bestFood.get();

				// If we prioritize gapples and either this is a gapple and current isn't,
				// or both/neither are gapples but this one has better hunger
				if (prioritizeGapples.getValue() && !blacklistGapples.getValue() &&
						((isGapple && !current.isGapple) ||
								(isGapple == current.isGapple && foodSlot.hunger > current.hunger))) {
					bestFood = Optional.of(foodSlot);
				}
				// Otherwise just compare hunger values
				else if ((!prioritizeGapples.getValue() || blacklistGapples.getValue()) && foodSlot.hunger > current.hunger) {
					bestFood = Optional.of(foodSlot);
				}
				// If hunger values are equal, prefer the one with higher saturation
				else if (foodSlot.hunger == current.hunger && foodSlot.saturation > current.saturation) {
					bestFood = Optional.of(foodSlot);
				}
			}
		}

		// Check offhand as well
		ItemStack offhandStack = MC.player.getOffHandStack();
		Item offhandItem = offhandStack.getItem();
		FoodComponent offhandFood = offhandStack.get(DataComponentTypes.FOOD);

		if (offhandFood != null && !blacklist.contains(offhandItem)) {
			boolean isGapple = gapples.contains(offhandItem);

			// Skip gapples if they're blacklisted
			if (isGapple && blacklistGapples.getValue()) {
				return bestFood.orElse(null);
			}

			// Skip gapples in Hunger mode if health is low but hunger is not
			if (isGapple && eatMode.getValue() == EatSetting.Hunger &&
					MC.player.getHealth() <= healthSetting.getValue() &&
					MC.player.getHungerManager().getFoodLevel() > hungerSetting.getValue()) {
				return bestFood.orElse(null);
			}

			FoodSlot offhandFoodSlot = new FoodSlot(
					SlotUtils.OFFHAND,
					offhandFood.nutrition(),
					offhandFood.saturation(),
					isGapple
			);

			// Compare with current best
			if (!bestFood.isPresent()) {
				bestFood = Optional.of(offhandFoodSlot);
			} else {
				FoodSlot current = bestFood.get();

				if (prioritizeGapples.getValue() && !blacklistGapples.getValue() &&
						((isGapple && !current.isGapple) ||
								(isGapple == current.isGapple && offhandFoodSlot.hunger > current.hunger))) {
					bestFood = Optional.of(offhandFoodSlot);
				} else if ((!prioritizeGapples.getValue() || blacklistGapples.getValue()) && offhandFoodSlot.hunger > current.hunger) {
					bestFood = Optional.of(offhandFoodSlot);
				} else if (offhandFoodSlot.hunger == current.hunger && offhandFoodSlot.saturation > current.saturation) {
					bestFood = Optional.of(offhandFoodSlot);
				}
			}
		}

		return bestFood.orElse(null);
	}

	public boolean isEating() {
		if (eating && auraPause.getValue()) {
			return eating;
		}

		return false;
	}
}