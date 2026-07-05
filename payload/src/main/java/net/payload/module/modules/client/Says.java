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
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

import java.util.Random;

public class Says extends Module implements DeathListener, PlayerDeathListener, TotemPopListener {
    private Runnable lastSoundMethod = null;

    public Says() {
        super("Atsu Sounds");
        this.setCategory(Category.of("Client"));
        this.setDescription("Atsu commentary for your mistakes");
        this.addSetting(toggleSounds);
        this.addSetting(deathsay);
        ///below just need to added with the proper mixin event ^_^
      //  this.addSetting(killsay);
         this.addSetting(totempopsay);
     //   this.addSetting(eventssay);
    }

    public BooleanSetting toggleSounds = BooleanSetting.builder()
            .id("atsu_togglesounds")
            .displayName("Toggle Noise")
            .description("")
            .defaultValue(false)
            .build();
    public BooleanSetting deathsay = BooleanSetting.builder()
            .id("atsu_deathsay")
            .displayName("Death Chats")
            .description("")
            .defaultValue(true)
            .build();
    public BooleanSetting killsay = BooleanSetting.builder()
            .id("atsu_killsay")
            .displayName("Kill Chats")
            .description("")
            .defaultValue(true)
            .build();
    public BooleanSetting totempopsay = BooleanSetting.builder()
            .id("atsu_totempopsay")
            .displayName("TotemPop Chats")
            .description("")
            .defaultValue(true)
            .build();

    public BooleanSetting eventssay = BooleanSetting.builder()
            .id("atsu_eventssay")
            .displayName("Events Chats")
            .description("")
            .defaultValue(true)
            .build();

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TotemPopListener.class, this);
        Payload.getInstance().eventManager.AddListener(DeathListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerDeathListener.class, this);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TotemPopListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(DeathListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerDeathListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent readPacketEvent) {
        if (deathsay.getValue()) {
            Runnable[] oursounds = {

                    /*
                    SoundGenerator::longsmug_laugh,
                    SoundGenerator::meanlaugh_longest,
                    SoundGenerator::oops,
                    SoundGenerator::ouch,
                    SoundGenerator::ouch2,
                    SoundGenerator::ouch3,
                    SoundGenerator::ouch4,
                    SoundGenerator::oops2,
                    SoundGenerator::oops3,
                    SoundGenerator::oops4,
                    SoundGenerator::ouch5,
                    SoundGenerator::meanlaugh_longest,

                     */
                    SoundGenerator::betterluck,
                    SoundGenerator::betterluck3,
                    SoundGenerator::betterluck2,
                    SoundGenerator::betterluck4,
                    SoundGenerator::betterluck5
                    /*
                    SoundGenerator::hauhhhh,
                    SoundGenerator::whimper,
                    SoundGenerator::smuglaugh,
                    SoundGenerator::smuglaugh2,

                     */
            };
            Random random = new Random();
            Runnable currentsound;
            do {
                int randomIndex = random.nextInt(oursounds.length);
                currentsound = oursounds[randomIndex];
            } while (currentsound == lastSoundMethod);

            currentsound.run();
            lastSoundMethod = currentsound;
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        //sendChatMessage("Atsu say death null event");

        if (event.getPlayer() != MC.player && event.getPlayer().distanceTo(MC.player) < 25) {
            sendChatMessage("Atsu say death event" + event.getPlayer().toString());

            Runnable[] killsounds = {
                    SoundGenerator::hoiiy,
                    SoundGenerator::hoyy,
                    SoundGenerator::hoyyyyyy,
                    SoundGenerator::hoyyy,
                    SoundGenerator::iknewyoucoulddoit,
                    SoundGenerator::iknewyoucoulddoit2,
                    SoundGenerator::iknewyoucoulddoit3,
                    SoundGenerator::iknewyoucoulddoit4
            };
            Random random = new Random();
            Runnable currentsound;
            do {
                int randomIndex = random.nextInt(killsounds.length);
                currentsound = killsounds[randomIndex];
            } while (currentsound == lastSoundMethod);

            currentsound.run();
            lastSoundMethod = currentsound;
        }
    }

    @Override
    public void onTotemPop(TotemPopEvent event) {
        sendChatMessage("Atsu say totem pop event");

        if (event.getEntity() == MC.player) {
            Runnable[] totemsounds = {
                    SoundGenerator::oops,
                    SoundGenerator::meanlaugh_longest,
                    SoundGenerator::boo1,
                    SoundGenerator::boo2,
                    SoundGenerator::boo3,
                    SoundGenerator::anime_moan,
                    SoundGenerator::longsmug_laugh,
                    SoundGenerator::ouch,
                    SoundGenerator::ouch2,
                    SoundGenerator::ouch3,
                    SoundGenerator::ouch4,
                    SoundGenerator::oops2,
                    SoundGenerator::oops3,
                    SoundGenerator::oops4,
                    SoundGenerator::ouch5,
                    SoundGenerator::hauhhhh,
                    SoundGenerator::whimper
            };
            Random random = new Random();
            Runnable currentsound;
            do {
                int randomIndex = random.nextInt(totemsounds.length);
                currentsound = totemsounds[randomIndex];
            } while (currentsound == lastSoundMethod);

            currentsound.run();
            lastSoundMethod = currentsound;
        }
    }
}
