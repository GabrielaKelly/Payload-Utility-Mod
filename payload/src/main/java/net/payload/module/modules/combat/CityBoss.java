
package net.payload.module.modules.combat;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.misc.PacketMine;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.BlockPosX;
import net.payload.utils.block.OtherBlockUtils;
import net.payload.utils.player.combat.CombatUtil;
import net.payload.utils.player.combat.EntityUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.payload.utils.block.OtherBlockUtils.getBlock;

public class CityBoss extends Module implements TickListener {

	private final BooleanSetting burrow = BooleanSetting.builder()
			.id("cityboss_burrow")
			.displayName("Burrow")
			.description("Enable burrow functionality")
			.defaultValue(true)
			.build();

	private final BooleanSetting face = BooleanSetting.builder()
			.id("cityboss_face")
			.displayName("Face")
			.description("Enable face placement")
			.defaultValue(true)
			.build();

	private final BooleanSetting down = BooleanSetting.builder()
			.id("cityboss_down")
			.displayName("Down")
			.description("Enable downward placement")
			.defaultValue(false)
			.build();

	private final BooleanSetting surround = BooleanSetting.builder()
			.id("cityboss_surround")
			.displayName("Surround")
			.description("Enable surround functionality")
			.defaultValue(true)
			.build();

	private final BooleanSetting lowVersion = BooleanSetting.builder()
			.id("cityboss_low_version")
			.displayName("1.12")
			.description("Enable 1.12 compatibility mode")
			.defaultValue(false)
			.build();

	public final FloatSetting targetRange = FloatSetting.builder()
			.id("cityboss_target_range")
			.displayName("TargetRange")
			.description("Maximum range to target blocks")
			.defaultValue(6.0f)
			.minValue(0.0f)
			.maxValue(8.0f)
			.step(0.1f)
			.build();

	public final FloatSetting range = FloatSetting.builder()
			.id("cityboss_range")
			.displayName("Range")
			.description("General operation range")
			.defaultValue(6.0f)
			.minValue(0.0f)
			.maxValue(8.0f)
			.step(0.1f)
			.build();

	public CityBoss() {
		super("CityBoss");

		this.setCategory(Category.of("Combat"));
		this.setDescription("Automatically break opponents surround in CPVP, dependent on PacketMine so config that too");

		this.addSetting(targetRange);
		this.addSetting(range);
		this.addSetting(burrow);
		this.addSetting(face);
		this.addSetting(down);
		this.addSetting(surround);
		this.addSetting(lowVersion);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (Payload.getInstance().moduleManager.antiCrawl.work) return;
		PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
		if (player == null) return;

		if (!Payload.getInstance().moduleManager.packetMine.state.getValue()) {
			sendChatMessage("Enabled PacketMine for CityBoss");
			Payload.getInstance().moduleManager.packetMine.toggle();
		}

		doBreak(player);
	}

	private void doBreak(PlayerEntity player) {
		BlockPos pos = EntityUtil.getEntityPos(player, true);
		{
			double[] yOffset = new double[]{-0.8, 0.5, 1.1};
			double[] xzOffset = new double[]{0.3, -0.3};
			for (PlayerEntity entity : CombatUtil.getEnemies(targetRange.getValue())) {
				for (double y : yOffset) {
					for (double x : xzOffset) {
						for (double z : xzOffset) {
							BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
							if (canBreak(offsetPos) && offsetPos.equals(PacketMine.getBreakPos())) {
								return;
							}
						}
					}
				}
			}
			List<Float> yList = new ArrayList<>();
			if (down.getValue()) {
				yList.add(-0.8f);
			}
			if (burrow.getValue()) {
				yList.add(0.5f);
			}
			if (face.getValue()) {
				yList.add(1.1f);
			}
			for (double y : yList) {
				for (double offset : xzOffset) {
					BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
					if (canBreak(offsetPos)) {
						PacketMine.INSTANCE.mine(offsetPos);
						return;
					}
				}
			}
			for (double y : yList) {
				for (double offset : xzOffset) {
					for (double offset2 : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
						if (canBreak(offsetPos)) {
							PacketMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
			}
		}
		if (surround.getValue()) {
			if (!lowVersion.getValue()) {
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if ((MC.world.isAir(pos.offset(i)) || pos.offset(i).equals(PacketMine.getBreakPos())) && canPlaceCrystal(pos.offset(i), false)) {
						return;
					}
				}
				ArrayList<BlockPos> list = new ArrayList<>();
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), true)) {
						list.add(pos.offset(i));
					}
				}
				if (!list.isEmpty()) {
					//System.out.println("found");
					PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(MC.player.getEyePos()))).get());
				} else {
					for (Direction i : Direction.values()) {
						if (i == Direction.UP || i == Direction.DOWN) continue;
						if (Math.sqrt(MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
							continue;
						}
						if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), false)) {
							list.add(pos.offset(i));
						}
					}
					if (!list.isEmpty()) {
						//System.out.println("found");
						PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(MC.player.getEyePos()))).get());
					}
				}

			} else {

				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (MC.player.getEyePos().distanceTo(pos.offset(i).toCenterPos()) > range.getValue()) {
						continue;
					}
					if ((MC.world.isAir(pos.offset(i)) && MC.world.isAir(pos.offset(i).up())) && canPlaceCrystal(pos.offset(i), false)) {
						return;
					}
				}

				ArrayList<BlockPos> list = new ArrayList<>();
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(MC.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if (canCrystal(pos.offset(i))) {
						list.add(pos.offset(i));
					}
				}

				int max = 0;
				BlockPos minePos = null;
				for (BlockPos cPos : list) {
					if (getAir(cPos) >= max) {
						max = getAir(cPos);
						minePos = cPos;
					}
				}
				if (minePos != null) {
					doMine(minePos);
				}
			}
		}
		if (PacketMine.getBreakPos() == null) {
			if (burrow.getValue()) {
				double[] yOffset;
				double[] xzOffset = new double[]{0, 0.3, -0.3};

				yOffset = new double[]{0.5, 1.1};
				for (double y : yOffset) {
					for (double offset : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
						if (isObsidian(offsetPos)) {
							PacketMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
				for (double y : yOffset) {
					for (double offset : xzOffset) {
						for (double offset2 : xzOffset) {
							BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
							if (isObsidian(offsetPos)) {
								PacketMine.INSTANCE.mine(offsetPos);
								return;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}


	private void doMine(BlockPos pos) {
		if (canBreak(pos)) {
			PacketMine.INSTANCE.mine(pos);
		} else if (canBreak(pos.up())) {
			PacketMine.INSTANCE.mine(pos.up());
		}
	}
	private boolean canCrystal(BlockPos pos) {
		if (PacketMine.godBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock || getBlock(pos) instanceof CobwebBlock || !canPlaceCrystal(pos, true) || OtherBlockUtils.getClickSideStrict(pos) == null) {
			return false;
		}
		if (PacketMine.godBlocks.contains(getBlock(pos.up())) || getBlock(pos.up()) instanceof BedBlock || getBlock(pos.up()) instanceof CobwebBlock || OtherBlockUtils.getClickSideStrict(pos.up()) == null) {
			return false;
		}
		return true;
	}
	private int getAir(BlockPos pos) {
		int value = 0;
		if (!canBreak(pos)) {
			value++;
		}
		if (!canBreak(pos.up())) {
			value++;
		}

		return value;
	}
	public boolean canPlaceCrystal(BlockPos pos, boolean block) {
		BlockPos obsPos = pos.down();
		BlockPos boost = obsPos.up();
		return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || !block)
				&& !OtherBlockUtils.hasEntityBlockCrystal(boost, true, true)
				&& !OtherBlockUtils.hasEntityBlockCrystal(boost.up(), true, true)
				&& (!lowVersion.getValue() || MC.world.isAir(boost.up()));
	}

	private boolean isObsidian(BlockPos pos) {
		return MC.player.getEyePos().distanceTo(pos.toCenterPos()) <= range.getValue() && (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.NETHERITE_BLOCK || getBlock(pos) == Blocks.RESPAWN_ANCHOR) && OtherBlockUtils.getClickSideStrict(pos) != null;
	}
	private boolean canBreak(BlockPos pos) {
		return isObsidian(pos) && (OtherBlockUtils.getClickSideStrict(pos) != null || PacketMine.getBreakPos().equals(pos)) && (!pos.equals(PacketMine.secondPos) || !(MC.player.getMainHandStack().getItem() instanceof PickaxeItem));
	}
}
