package net.payload.module.modules.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.friends.Friend;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class MiddleMouse extends Module implements MouseClickListener, TickListener {

	public enum Mode {
		Friend, Pearl, Inventory
	}

	public enum ContainerType {
		PLAYER_INVENTORY,
		CHEST,
		SHULKER,
		UNSUPPORTED
	}

	private final BooleanSetting pearlinvswap = BooleanSetting.builder()
			.id("mca_invswap")
			.displayName("InvSwap Pearl")
			.defaultValue(true)
			.build();

	public FloatSetting delay = FloatSetting.builder()
			.id("mca_delay")
			.displayName("InvSort Delay")
			.defaultValue(0.2f)
			.minValue(0.1f)
			.maxValue(5.0f)
			.step(0.1f)
			.build();

	public FloatSetting operationsPerTick = FloatSetting.builder()
			.id("mca_ops_per_tick")
			.displayName("InvSort Ticks")
			.description("Number of sorting operations to perform each tick")
			.defaultValue(5.0f)
			.minValue(1.0f)
			.maxValue(10.0f)
			.step(1.0f)
			.build();

	private final BooleanSetting dragStealing = BooleanSetting.builder()
			.id("mca_dragstealing")
			.displayName("Drag Stealing")
			.defaultValue(true)
			.build();

	private final BooleanSetting sortChests = BooleanSetting.builder()
			.id("mca_sort_chests")
			.displayName("Sort Chests")
			.defaultValue(true)
			.build();

	private final BooleanSetting sortShulkers = BooleanSetting.builder()
			.id("mca_sort_shulkers")
			.displayName("Sort Shulkers")
			.defaultValue(true)
			.build();

	private final EnumSetting<Mode> mode = EnumSetting.<Mode>builder().id("mca_mode").displayName("Mode")
			.description("The mode for the action to run when the middle mouse button is clicked.")
			.defaultValue(Mode.Friend).build();

	private final CacheTimer timer = new CacheTimer();

	private int previousSlot = -1;
	private boolean sortTrigger = false;
	private boolean throwing = false;

	public MiddleMouse() {
		super("MiddleMouse");

		this.setCategory(Category.of("misc"));
		this.setDescription("Middle Click Function");

		this.addSetting(mode);
		this.addSetting(pearlinvswap);
		this.addSetting(operationsPerTick);
		this.addSetting(delay);
		this.addSetting(dragStealing);
		this.addSetting(sortChests);
		this.addSetting(sortShulkers);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (!timer.passed(delay.getValue() * 1000) || !sortTrigger) return;

		ContainerType containerType = getCurrentContainerType();
		if (containerType == ContainerType.UNSUPPORTED) {
			return;
		}

		// Get container dimensions based on type
		int[] dimensions = getContainerDimensions(containerType);
		int startSlot = dimensions[0];
		int endSlot = dimensions[1];
		int syncId = MC.player.playerScreenHandler.syncId;

		// For chests and shulkers, use the current screen's sync ID
		if (containerType != ContainerType.PLAYER_INVENTORY) {
			syncId = MC.player.currentScreenHandler.syncId;
		}

		// Get the number of operations to perform this tick
		int maxOperations = Math.round(operationsPerTick.getValue());
		int operationsPerformed = 0;

		// First pass: merge stackable items
		for (int slot1 = startSlot; slot1 < endSlot && operationsPerformed < maxOperations; ++slot1) {
			ItemStack stack = getStackFromContainer(containerType, slot1);
			if (stack.isEmpty()) continue;
			if (!stack.isStackable()) continue;
			if (stack.getCount() == stack.getMaxCount()) continue;

			for (int slot2 = endSlot - 1; slot2 >= startSlot; --slot2) {
				if (slot1 == slot2) continue;
				ItemStack stack2 = getStackFromContainer(containerType, slot2);
				if (stack2.isEmpty()) continue;
				if (stack2.getCount() == stack2.getMaxCount()) continue;

				if (canMerge(stack, stack2)) {
					MC.interactionManager.clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, MC.player);
					MC.interactionManager.clickSlot(syncId, slot2, 0, SlotActionType.PICKUP, MC.player);
					MC.interactionManager.clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, MC.player);
					operationsPerformed++;
					if (operationsPerformed >= maxOperations) {
						timer.reset();
						return; // Reached max operations for this tick
					}
				}
			}
		}

		// Second pass: sort by item ID
		for (int slot1 = startSlot; slot1 < endSlot && operationsPerformed < maxOperations; ++slot1) {
			ItemStack stack = getStackFromContainer(containerType, slot1);
			int id = stack.isEmpty() ? Integer.MAX_VALUE : Item.getRawId(stack.getItem());

			int minId = getMinId(containerType, slot1, id, endSlot);

			if (minId < id) {
				for (int slot2 = endSlot - 1; slot2 > slot1; --slot2) {
					ItemStack checkStack = getStackFromContainer(containerType, slot2);
					if (checkStack.isEmpty()) continue;

					int itemID = Item.getRawId(checkStack.getItem());
					if (itemID == minId) {
						MC.interactionManager.clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, MC.player);
						MC.interactionManager.clickSlot(syncId, slot2, 0, SlotActionType.PICKUP, MC.player);
						MC.interactionManager.clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, MC.player);
						operationsPerformed++;
						if (operationsPerformed >= maxOperations) {
							timer.reset();
							return; // Reached max operations for this tick
						}
					}
				}
			}
		}

		// If we reached here, sorting is complete (no operations were performed)
		if (operationsPerformed == 0) {
			String containerName = getContainerName(containerType);
			sendMCAMessage(containerName + " Sorted");
			sortTrigger = false;
		} else {
			timer.reset(); // Reset timer for next tick's operations
		}
	}

	private ContainerType getCurrentContainerType() {
		if (MC.currentScreen == null || MC.currentScreen instanceof ChatScreen || MC.currentScreen instanceof GameMenuScreen) {
			return ContainerType.PLAYER_INVENTORY;
		} else if (MC.currentScreen instanceof InventoryScreen) {
			return ContainerType.PLAYER_INVENTORY;
		} else if (MC.currentScreen instanceof GenericContainerScreen && sortChests.getValue()) {
			return ContainerType.CHEST;
		} else if (MC.currentScreen instanceof ShulkerBoxScreen && sortShulkers.getValue()) {
			return ContainerType.SHULKER;
		}
		return ContainerType.UNSUPPORTED;
	}

	private String getContainerName(ContainerType type) {
		switch (type) {
			case PLAYER_INVENTORY: return "Inventory";
			case CHEST: return "Chest";
			case SHULKER: return "Shulker Box";
			default: return "Container";
		}
	}

	private int[] getContainerDimensions(ContainerType type) {
		switch (type) {
			case PLAYER_INVENTORY:
				return new int[] {9, 36}; // Start at slot 9, end at 36 (main inventory)
			case CHEST:
				int chestSize = ((GenericContainerScreenHandler)MC.player.currentScreenHandler).getRows() * 9;
				return new int[] {0, chestSize}; // Start at slot 0, end at size (depends on chest type)
			case SHULKER:
				return new int[] {0, 27}; // Start at slot 0, end at 27 (shulker box size)
			default:
				return new int[] {0, 0};
		}
	}

	private ItemStack getStackFromContainer(ContainerType type, int slot) {
		switch (type) {
			case PLAYER_INVENTORY:
				return MC.player.getInventory().getStack(slot);
			case CHEST:
			case SHULKER:
				return MC.player.currentScreenHandler.getSlot(slot).getStack();
			default:
				return ItemStack.EMPTY;
		}
	}

	private int getMinId(ContainerType type, int startSlot, int currentId, int endSlot) {
		int id = currentId;
		for (int slot = startSlot + 1; slot < endSlot; ++slot) {
			ItemStack stack = getStackFromContainer(type, slot);
			if (stack.isEmpty()) continue;
			int itemID = Item.getRawId(stack.getItem());
			if (itemID < id) {
				id = itemID;
			}
		}
		return id;
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	@Override
	public void onMouseClick(MouseClickEvent mouseClickEvent) {
		if (mouseClickEvent.button == MouseButton.MIDDLE) {
			if (mouseClickEvent.action == MouseAction.DOWN) {
				switch (mode.getValue()) {
					case Mode.Friend:
						if (MC.targetedEntity == null || !(MC.targetedEntity instanceof PlayerEntity player)) {

							StringBuilder friends = new StringBuilder("Friends: ");

							for (Friend friend : Payload.getInstance().friendsList.getFriends()) {
								friends.append(friend.getUsername()).append(", ");
							}

							if (friends.length() > 10) {
								friends.setLength(friends.length() - 2);
							}

							sendMCAMessage(friends.toString());

							return;
						}

						if (Payload.getInstance().friendsList.contains(player)) {
							Payload.getInstance().friendsList.removeFriend(player);
							mouseClickEvent.cancel();
							sendMCAMessage("Removed " + player.getName().getString() + " from friends list.");
						} else {
							Payload.getInstance().friendsList.addFriend(player);
							mouseClickEvent.cancel();
							sendMCAMessage("Added " + player.getName().getString() + " to friends list.");
						}
						break;

					case Mode.Inventory:
						sortTrigger = true;
						break;

					case Mode.Pearl:
						if (!throwing) {
							throwPearl();
						}

						break;
				}
			} else if (mouseClickEvent.action == MouseAction.UP) {
				if (mode.getValue() == Mode.Pearl && previousSlot != -1) {
					swap(previousSlot, false);
					previousSlot = -1;
					mouseClickEvent.cancel();
				}
			}
		}
	}

	public void throwPearl() {
		throwing = true;
		int pearl;
		if (MC.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, MC.player.getYaw(), MC.player.getPitch()));
		} else if (pearlinvswap.getValue() && (pearl = InventoryUtil.findItemInventorySlot(Items.ENDER_PEARL)) != -1) {
			InventoryUtil.inventorySwap(pearl, MC.player.getInventory().selectedSlot);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, MC.player.getYaw(), MC.player.getPitch()));
			InventoryUtil.inventorySwap(pearl, MC.player.getInventory().selectedSlot);
			EntityUtil.syncInventory();
		} else if ((pearl = InventoryUtil.findItem(Items.ENDER_PEARL)) != -1) {
			int old = MC.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(pearl);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, MC.player.getYaw(), MC.player.getPitch()));
			InventoryUtil.switchToSlot(old);
		}
		else if (pearl == -1) {
			sendMCAMessage("Missing EPearl");
		}
		throwing = false;
	}

	private boolean canMerge(ItemStack source, ItemStack stack) {
		return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
	}

	public static void sendMCAMessage(String message) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.inGameHud != null) {
			mc.inGameHud.getChatHud().addMessage(Text.of(Formatting.DARK_AQUA + "[" + Formatting.AQUA + "MiddleMouse"
					+ Formatting.DARK_AQUA + "] " + Formatting.RESET + message));
		}
	}

	public boolean isDrag() {
		return dragStealing.getValue();
	}
}
