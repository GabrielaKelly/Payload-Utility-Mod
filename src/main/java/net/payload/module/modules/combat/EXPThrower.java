package net.payload.module.modules.combat;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.payload.Payload;
import net.payload.event.events.RotateEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.RotateListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.client.AntiCheat;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.FindItemResult;
import net.minecraft.item.Items;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.combat.EntityUtil;

import java.util.Map;

import static net.payload.PayloadClient.MC;

public class EXPThrower extends Module implements TickListener, RotateListener {
	private FloatSetting pitchSetting = FloatSetting.builder().id("expthrower_pitch").displayName("Pitch")
			.description("The pitch angle for throwing XP bottles.").defaultValue(88.0f).minValue(0f).maxValue(90f)
			.step(1f).build();

	private BooleanSetting invSwap = BooleanSetting.builder().id("expthrower_invswap")
			.displayName("InvSwap").description("Automatically swap to XP bottles if not in hand.").defaultValue(true)
			.build();

	private FloatSetting delay = FloatSetting.builder().id("expthrower_throw_delay")
			.displayName("Throw Delay").description("Delay between throws in ticks.").defaultValue(0f).minValue(0f)
			.maxValue(100f).step(5f).build();

	private BooleanSetting autoToggle = BooleanSetting.builder().id("expthrower_auto_toggle")
			.displayName("Auto Toggle").description("Automatically toggle off when no XP bottles are found.")
			.defaultValue(true).build();

	private BooleanSetting brokenOnly = BooleanSetting.builder().id("expthrower_broken")
			.displayName("Damage Check").description("Only throw bottles when broken armor")
			.defaultValue(true).build();

	private BooleanSetting onlyGround = BooleanSetting.builder().id("expthrower_groundcheck")
			.displayName("Ground Check").description("Only throws when on ground.")
			.defaultValue(true).build();

	private BooleanSetting usingPause = BooleanSetting.builder().id("expthrower_itempause")
			.displayName("Item Use Pause").description("Only throws when on ground.")
			.defaultValue(false).build();

	public EXPThrower() {
		super("EXPThrower");

		this.setCategory(Category.of("misc"));
		this.setDescription("Automatically uses XP bottles");

		this.addSetting(pitchSetting);
		this.addSetting(invSwap);
		this.addSetting(delay);
		this.addSetting(brokenOnly);
		this.addSetting(autoToggle);
		this.addSetting(onlyGround);
		this.addSetting(usingPause);
	}

	private boolean throwing = false;
	boolean rotation = false;
	int exp = 0;

	private final CacheTimer delayTimer = new CacheTimer();

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(RotateListener.class, this);

		throwing = false;
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(RotateListener.class, this);

		rotation = true;
		if (nullCheck() && this.state.getValue()) {
			this.toggle();
			return;
		}
		exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		throwing = checkThrow();
		if (rotation && isThrow() && delayTimer.passedMs(delay.getValue().intValue() * 20L) && (!onlyGround.getValue() || MC.player.isOnGround())) {
			exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE) - 1;
			throwExp();
		}
	}

	@Override
	public void onTick(Post event) {

	}

	public boolean isThrow() {
		return throwing;
	}

	@Override
	public void onLastRotate(RotateEvent event) {
		if (pitchSetting.getValue() == 0) return;

		if (isThrow()) {
			event.setPitch(pitchSetting.get());
			rotation = true;
		}
	}

	public void throwExp() {
		int oldSlot = MC.player.getInventory().selectedSlot;
		int newSlot;

		if (invSwap.getValue() && (InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE)) == -1 && autoToggle.getValue()) {
			this.toggle();
		}
		else if ((InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE)) == -1 && autoToggle.getValue()) {
			this.toggle();
		}

		if (invSwap.getValue() && (newSlot = InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE)) != -1) {
			InventoryUtil.inventorySwap(newSlot, MC.player.getInventory().selectedSlot);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, MC.player.getYaw(), pitchSetting.getValue()));
			EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.swingMode.getValue());
			InventoryUtil.inventorySwap(newSlot, MC.player.getInventory().selectedSlot);
			EntityUtil.syncInventory();
			delayTimer.reset();
		} else if ((newSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE)) != -1) {
			InventoryUtil.switchToSlot(newSlot);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, MC.player.getYaw(), pitchSetting.getValue()));
			EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.swingMode.getValue());
			InventoryUtil.switchToSlot(oldSlot);
			delayTimer.reset();
		}
	}

	public boolean checkThrow() {
		if (!this.state.getValue()) return false;
		if (MC.currentScreen != null) return false;
		if (usingPause.getValue() && MC.player.isUsingItem()) {
			return false;
		}
		if (InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) == -1 && (!invSwap.getValue() || InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE) == -1))
			return false;

		if (brokenOnly.getValue()) {
			DefaultedList<ItemStack> armors = MC.player.getInventory().armor;
			for (ItemStack armor : armors) {
				if (armor.isEmpty()) continue;
				if (EntityUtil.getDamagePercent(armor) >= 100) continue;

				if (EnchantmentHelper.getLevel(MC.world.getRegistryManager().getOrThrow(Enchantments.MENDING.getRegistryRef()).getEntry(Enchantments.MENDING.getValue()).orElse(null), armor) == 1) {
					return true;
				}
				return false;
			}
		} else {
			return true;
		}
		if (autoToggle.getValue() && this.state.getValue()) {
			this.toggle();
		}
		return false;
	}
}