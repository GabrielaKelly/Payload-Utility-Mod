package net.payload.module.modules.render;

import net.minecraft.client.particle.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.payload.Payload;
import net.payload.event.events.ParticleAddEvent;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.ParticleAddListener;
import net.payload.event.listeners.PlayerHealthListener;
import net.payload.event.listeners.Render3DListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class NoRender extends Module implements Render3DListener, ParticleAddListener {

	private BooleanSetting noFog = BooleanSetting.builder().id("norender_no_fog")
			.displayName("Fog").description("Does not render fog").defaultValue(true)
			.build();

	private BooleanSetting noWeather = BooleanSetting.builder().id("norender_no_weather")
			.displayName("Weather").description("Does not render weather").defaultValue(true)
			.build();

	private BooleanSetting noEatParticles = BooleanSetting.builder().id("norender_eat_particles")
			.displayName("Eating Particles").description("Does not render eating particles.").defaultValue(true)
			.build();

	private BooleanSetting noTotemAnimation = BooleanSetting.builder().id("norender_totem_anim")
			.displayName("Totem Animation").description("Does not render the totem floating animation.")
			.defaultValue(true).build();

	private BooleanSetting noVignette = BooleanSetting.builder().id("norender_vignette").displayName("Vignette")
			.description("Does not render the Minecraft vignette.").defaultValue(true).build();

	private BooleanSetting noPumpkinOverlay = BooleanSetting.builder().id("norender_pumpkin")
			.displayName("Pumpkin Overlay").description("Does not render the pumpkin overlay when you are wearing one.")
			.defaultValue(true).build();

	private BooleanSetting noFireOverlay = BooleanSetting.builder().id("norender_fire").displayName("Fire Overlay")
			.description("Does not render the overlay when the player is on fire.").defaultValue(false).build();

	private BooleanSetting noBlockOverlay = BooleanSetting.builder().id("norender_block").displayName("Block Overlay")
			.description("Does not render the overlay when the player is in block.").defaultValue(true).build();

	private BooleanSetting noWaterOverlay = BooleanSetting.builder().id("norender_water").displayName("Water Overlay")
			.description("Does not render the overlay when the player is on water.").defaultValue(true).build();

	private BooleanSetting noPortalOverlay = BooleanSetting.builder().id("norender_portal")
			.displayName("Portal Overlay").description("Does not render the overlay when travelling through a portal.")
			.defaultValue(true).build();

	private BooleanSetting noPowderSnowOverlay = BooleanSetting.builder().id("norender_powder_snow")
			.displayName("Powder Snow Overlay").description("Does not render the overlay when in powder snow.")
			.defaultValue(true).build();

	private BooleanSetting noPotions = BooleanSetting.builder().id("norender_potions").displayName("Potions")
			.description("Does not render the crosshair.").defaultValue(false).build();

	private BooleanSetting noXpBottles = BooleanSetting.builder().id("norender_xp").displayName("XP Bottles")
			.description("Does not render the crosshair.").defaultValue(true).build();

	private BooleanSetting noArrows = BooleanSetting.builder().id("norender_arrows").displayName("Arrows")
			.description("Does not render the crosshair.").defaultValue(false).build();

	private BooleanSetting noEggs = BooleanSetting.builder().id("norender_eggs").displayName("Eggs")
			.description("Does not render the crosshair.").defaultValue(false).build();

	private BooleanSetting noCrosshair = BooleanSetting.builder().id("norender_crosshair").displayName("Crosshair")
			.description("Does not render the crosshair.").defaultValue(false).build();

	private BooleanSetting noFireworks = BooleanSetting.builder().id("norender_firework").displayName("Fireworks")
			.description("Does not render the crosshair.").defaultValue(false).build();

	private BooleanSetting noExplosions = BooleanSetting.builder().id("norender_explosion").displayName("Explosions")
			.description("Does not render the crosshair.").defaultValue(true).build();

	private BooleanSetting noPotionParticles = BooleanSetting.builder().id("norender_potionparticles").displayName("Potion Particles")
			.description("Does not render the crosshair.").defaultValue(false).build();

	public NoRender() {
		super("NoRender");
		this.setCategory(Category.of("Render"));
		this.setDescription("Stops certain things from rendering");

		this.addSetting(noFog);
		this.addSetting(noWeather);
		this.addSetting(noEatParticles);
		this.addSetting(noTotemAnimation);
		this.addSetting(noVignette);
		this.addSetting(noPumpkinOverlay);
		this.addSetting(noFireOverlay);
		this.addSetting(noBlockOverlay);
		this.addSetting(noWaterOverlay);
		this.addSetting(noPortalOverlay);
		this.addSetting(noPowderSnowOverlay);
		this.addSetting(noPotions);
		this.addSetting(noXpBottles);
		this.addSetting(noArrows);
		this.addSetting(noEggs);
		this.addSetting(noFireworks);
		this.addSetting(noExplosions);
		this.addSetting(noPotionParticles);

		this.addSetting(noCrosshair);
	}
	

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(ParticleAddListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.AddListener(ParticleAddListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	public boolean getNoFog() {
		return this.noFog.getValue();
	}

	public boolean getNoWeather() {
		return this.noWeather.getValue();
	}

	public boolean getNoEatParticles() {
		return this.noEatParticles.getValue();
	}

	public boolean getNoTotemAnimation() {
		return this.noTotemAnimation.getValue();
	}

	public boolean getNoVignette() {
		return this.noVignette.getValue();
	}

	public boolean getNoPumpkinOverlay() {
		return this.noPumpkinOverlay.getValue();
	}

	public boolean getNoFireOverlay() {
		return this.noFireOverlay.getValue();
	}

	public boolean getNoWaterOverlay() {
		return this.noWaterOverlay.getValue();
	}

	public boolean getNoBlockOverlay() {
		return this.noBlockOverlay.getValue();
	}

	public boolean getNoPortalOverlay() {
		return this.noPumpkinOverlay.getValue();
	}

	public boolean getNoPowderSnowOverlay() {
		return this.noPowderSnowOverlay.getValue();
	}

	public boolean getNoCrosshair() {
		return this.noCrosshair.getValue();
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (nullCheck()) return;

		for (Entity ent : MC.world.getEntities()){
			if(ent instanceof PotionEntity){
				if(noPotions.getValue())
					MC.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
			if(ent instanceof ExperienceBottleEntity){
				if(noXpBottles.getValue())
					MC.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
			if(ent instanceof ArrowEntity){
				if(noArrows.getValue())
					MC.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
			if(ent instanceof EggEntity){
				if(noEggs.getValue())
					MC.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
			}
		}
	}

	@Override
	public void onParticleSpawn(ParticleAddEvent event) {
		if (nullCheck()) return;

		if (noExplosions.getValue() && event.particle instanceof ExplosionLargeParticle) {
			event.cancel();
		} else if (noFireworks.getValue() && (event.particle instanceof FireworksSparkParticle.FireworkParticle || event.particle instanceof FireworksSparkParticle.Flash)) {
			event.cancel();
		} else if (noPotionParticles.getValue() && event.particle instanceof SpellParticle) {
			event.cancel();
		}
	}
}
