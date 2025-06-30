package net.payload.module.modules.client;

import net.minecraft.entity.player.PlayerEntity;
import net.payload.Payload;
import net.payload.SoundGenerator;
import net.payload.combatmanager.CombatManager;
import net.payload.event.events.DeathEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.events.TotemPopEvent;
import net.payload.event.listeners.DeathListener;
import net.payload.event.listeners.PlayerDeathListener;
import net.payload.event.events.PlayerDeathEvent;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.payload.event.listeners.TickListener;
import net.payload.event.listeners.TotemPopListener;
import net.payload.gui.GuiStyle;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.combat.AutoTotem;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;

import java.util.Random;

import static net.payload.gui.GuiManager.*;

public class ClientGUI extends Module {
    public ClientGUI() {
        super("ClientGUI");
        this.setCategory(Category.of("Client"));
        this.setDescription("Settings related to the GUI");
        this.addSetting(guistyle);
        this.addSetting(modulesettingsstyle);
        this.addSetting(textrenderformat);
        this.addSetting(matrixscaling);
        this.addSetting(guigirl);
        this.addSetting(minifloat);
        this.addSetting(headerheight);
        this.addSetting(atsubackround2);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        keepEnabled();
    }

    @Override
    public void onToggle() {
    }
}