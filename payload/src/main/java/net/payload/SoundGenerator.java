package net.payload;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import static net.payload.PayloadClient.MC;

public class SoundGenerator {
    private static SoundEvent registerSound(String soundName) {
        return null;
    }

    private static float Volume = 1f;

    private static final SoundEvent MY_CUSTOM_SOUND_ON = registerSound("my_custom_sound_on");
    private static final SoundEvent MY_CUSTOM_SOUND_OFF = registerSound("my_custom_sound_off");
    public static final Identifier HALOSPRINT_ID = Identifier.of("payload:halosprint");
    public static final Identifier HALOSHOOT_ID = Identifier.of("payload:haloshoot");
    public static final Identifier HALOSHOOT2_ID = Identifier.of("payload:haloshoot2");
    public static final Identifier HALOHURT_ID = Identifier.of("payload:halohurt");

    public static final Identifier MY_SOUND_ID = Identifier.of("payload:my_sound");
    public static SoundEvent MY_SOUND_EVENT = SoundEvent.of(MY_SOUND_ID);
    public static final Identifier MY_SOUND_ID1 = Identifier.of("payload:anime");
    public static SoundEvent MY_SOUND_EVENT1 = SoundEvent.of(MY_SOUND_ID1);
    public static final Identifier NEWVEGAS1_ID = Identifier.of("payload:newvegas1");
    public static SoundEvent NEWVEGAS1_EVENT = SoundEvent.of(NEWVEGAS1_ID);
    public static final Identifier ARMOR_ID = Identifier.of("payload:armor");
    public static final Identifier ARMOR2_ID = Identifier.of("payload:armor2");
    public static final Identifier ARMOR3_ID = Identifier.of("payload:armor3");
    public static final Identifier ARMOR4_ID = Identifier.of("payload:armor4");
    public static final Identifier ARMOR5_ID = Identifier.of("payload:armor5");
    public static final Identifier BAKA_YOUDIDIT_ID = Identifier.of("payload:baka_youdidit");
    public static final Identifier BETTERLUCK_ID = Identifier.of("payload:betterluck");
    public static final Identifier BETTERLUCK2_ID = Identifier.of("payload:betterluck2");
    public static final Identifier BETTERLUCK3_ID = Identifier.of("payload:betterluck3");
    public static final Identifier BETTERLUCK4_ID = Identifier.of("payload:betterluck4");
    public static final Identifier BETTERLUCK5_ID = Identifier.of("payload:betterluck5");
    public static final Identifier BFF_ID = Identifier.of("payload:bff");
    public static final Identifier BFF2_ID = Identifier.of("payload:bff2");
    public static final Identifier BOO1_ID = Identifier.of("payload:boo1");
    public static final Identifier BOO2_ID = Identifier.of("payload:boo2");
    public static final Identifier BOO3_ID = Identifier.of("payload:boo3");
    public static final Identifier BUZZ_ID = Identifier.of("payload:buzz");
    public static final Identifier BUZZ2_ID = Identifier.of("payload:buzz2");
    public static final Identifier CANIHAVEYOURSTASH_ID = Identifier.of("payload:canihaveyourstash");
    public static final Identifier CANIHAVEYOURSTASH2_ID = Identifier.of("payload:canihaveyourstash2");
    public static final Identifier CANIHAVEYOURSTASH3_ID = Identifier.of("payload:canihaveyourstash3");
    public static final Identifier DIAMOND2_ID = Identifier.of("payload:diamond2");
    public static final Identifier DIAMONDHELMET_ID = Identifier.of("payload:diamondhelmet");
    public static final Identifier DONKEYDUPE1_ID = Identifier.of("payload:donkeydupe1");
    public static final Identifier DONKEYDUPE2_ID = Identifier.of("payload:donkeydupe2");
    public static final Identifier DOYOULOVEMEBACK_ID = Identifier.of("payload:doyoulovemeback");
    public static final Identifier DOYOULOVEMEBACK2_ID = Identifier.of("payload:doyoulovemeback2");
    public static final Identifier DOYOULOVEMEBACK3_ID = Identifier.of("payload:doyoulovemeback3");
    public static final Identifier DOYOULOVEMEBACK4_ID = Identifier.of("payload:doyoulovemeback4");
    public static final Identifier EGG_ID = Identifier.of("payload:egg");
    public static final Identifier EGG2_ID = Identifier.of("payload:egg2");
    public static final Identifier EGG3_ID = Identifier.of("payload:egg3");
    public static final Identifier HAUHHHH_ID = Identifier.of("payload:hauhhhh");
    public static final Identifier HEADSHOT_ID = Identifier.of("payload:headshot");
    public static final Identifier HEADSHOT2_ID = Identifier.of("payload:headshot2");
    public static final Identifier HEADSHOT3_ID = Identifier.of("payload:headshot3");
    public static final Identifier HEADSHOT5_ID = Identifier.of("payload:headshot5");
    public static final Identifier HEADSHOT6_ID = Identifier.of("payload:headshot6");
    public static final Identifier HOIIIIIY_ID = Identifier.of("payload:hoiiiiiy");
    public static final Identifier HOYY_ID = Identifier.of("payload:hoyy");
    public static final Identifier HOYYYY_ID = Identifier.of("payload:hoyyyy");
    public static final Identifier HOYYYYYY_ID = Identifier.of("payload:hoyyyyyy");
    public static final Identifier IKNEWYOUCOULDDOIT_ID = Identifier.of("payload:iknewyoucoulddoit");
    public static final Identifier IKNEWYOUCOULDDOIT2_ID = Identifier.of("payload:iknewyoucoulddoit2");
    public static final Identifier IKNEWYOUCOULDDOIT3_ID = Identifier.of("payload:iknewyoucoulddoit3");
    public static final Identifier IKNEWYOUCOULDDOIT4_ID = Identifier.of("payload:iknewyoucoulddoit4");
    public static final Identifier LONGSMUG_LAUGH_ID = Identifier.of("payload:longsmug_laugh");
    public static final Identifier MEANLAUGH_LONGEST_ID = Identifier.of("payload:meanlaugh_longest");
    public static final Identifier OHYOUDIDIT_BEST_ID = Identifier.of("payload:ohyoudidit_best");
    public static final Identifier OOPS_ID = Identifier.of("payload:oops");
    public static final Identifier OOPS2_ID = Identifier.of("payload:oops2");
    public static final Identifier OOPS3_ID = Identifier.of("payload:oops3");
    public static final Identifier OOPS4_ID = Identifier.of("payload:oops4");
    public static final Identifier OUCH_ID = Identifier.of("payload:ouch");
    public static final Identifier OUCH2_ID = Identifier.of("payload:ouch2");
    public static final Identifier OUCH3_ID = Identifier.of("payload:ouch3");
    public static final Identifier OUCH4_ID = Identifier.of("payload:ouch4");
    public static final Identifier OUCH5_ID = Identifier.of("payload:ouch5");
    public static final Identifier PAYLOAD_ID = Identifier.of("payload:payload");
    public static final Identifier PAYLOAD2_ID = Identifier.of("payload:payload2");
    public static final Identifier PAYLOAD3_ID = Identifier.of("payload:payload3");
    public static final Identifier PAYLOAD5_ID = Identifier.of("payload:payload5");
    public static final Identifier PAYLOAD6_ID = Identifier.of("payload:payload6");
    public static final Identifier PAYLOAD7_ID = Identifier.of("payload:payload7");
    public static final Identifier PINGCARRIED_ID = Identifier.of("payload:pingcarried");
    public static final Identifier PLANB_ID = Identifier.of("payload:planb");
    public static final Identifier PLANB2_ID = Identifier.of("payload:planb2");
    public static final Identifier PLANB3_ID = Identifier.of("payload:planb3");
    public static final Identifier SCREAM1_ID = Identifier.of("payload:scream1");
    public static final Identifier SCREAM2_ID = Identifier.of("payload:scream2");
    public static final Identifier SCREAM3_ID = Identifier.of("payload:scream3");
    public static final Identifier SHH_ID = Identifier.of("payload:shh");
    public static final Identifier SHH2_ID = Identifier.of("payload:shh2");
    public static final Identifier SKEETSKET_ID = Identifier.of("payload:skeetskeet");
    public static final Identifier SKEETSKET2_ID = Identifier.of("payload:skeetskeet2");
    public static final Identifier SKYRIM1_ID = Identifier.of("payload:skyrim1");
    public static final Identifier SMUG_LAUGH_ID = Identifier.of("payload:smug_laugh");
    public static final Identifier SMUG_LAUGH2_ID = Identifier.of("payload:smug_laugh2");
    public static final Identifier THESEMODULESUSEDTO_ID = Identifier.of("payload:thesemodulesusedto");
    public static final Identifier TIMETORUN_ID = Identifier.of("payload:timetorun");
    public static final Identifier TIMETORUN2_ID = Identifier.of("payload:timetorun2");
    public static final Identifier TIMETORUN3_ID = Identifier.of("payload:timetorun3");
    public static final Identifier TOTEM_ID = Identifier.of("payload:totem");
    public static final Identifier WHIMPER_ID = Identifier.of("payload:whimper");
    public static final Identifier YOUDIDIT_ID = Identifier.of("payload:youdidit");
    public static final Identifier YUMMY_ID = Identifier.of("payload:yummy");
    public static final Identifier YUMMY2_ID = Identifier.of("payload:yummy2");
    public static final Identifier YUMMY3_ID = Identifier.of("payload:yummy3");

    // Sound events
    public static SoundEvent HALOSPRINT_EVENT = SoundEvent.of(HALOSPRINT_ID);
    public static final SoundEvent HALOSHOOT_EVENT = SoundEvent.of(HALOSHOOT_ID);
    public static final SoundEvent HALOSHOOT2_EVENT = SoundEvent.of(HALOSHOOT2_ID);
    public static final SoundEvent HALOHURT_EVENT = SoundEvent.of(HALOHURT_ID);

    public static SoundEvent ARMOR_EVENT = SoundEvent.of(ARMOR_ID);
    public static SoundEvent ARMOR2_EVENT = SoundEvent.of(ARMOR2_ID);
    public static SoundEvent ARMOR3_EVENT = SoundEvent.of(ARMOR3_ID);
    public static SoundEvent ARMOR4_EVENT = SoundEvent.of(ARMOR4_ID);
    public static SoundEvent ARMOR5_EVENT = SoundEvent.of(ARMOR5_ID);
    public static SoundEvent BAKA_YOUDIDIT_EVENT = SoundEvent.of(BAKA_YOUDIDIT_ID);
    public static SoundEvent BETTERLUCK_EVENT = SoundEvent.of(BETTERLUCK_ID);
    public static SoundEvent BETTERLUCK2_EVENT = SoundEvent.of(BETTERLUCK2_ID);
    public static SoundEvent BETTERLUCK3_EVENT = SoundEvent.of(BETTERLUCK3_ID);
    public static SoundEvent BETTERLUCK4_EVENT = SoundEvent.of(BETTERLUCK4_ID);
    public static SoundEvent BETTERLUCK5_EVENT = SoundEvent.of(BETTERLUCK5_ID);
    public static SoundEvent BFF_EVENT = SoundEvent.of(BFF_ID);
    public static SoundEvent BFF2_EVENT = SoundEvent.of(BFF2_ID);
    public static SoundEvent BOO1_EVENT = SoundEvent.of(BOO1_ID);
    public static SoundEvent BOO2_EVENT = SoundEvent.of(BOO2_ID);
    public static SoundEvent BOO3_EVENT = SoundEvent.of(BOO3_ID);
    public static SoundEvent BUZZ_EVENT = SoundEvent.of(BUZZ_ID);
    public static SoundEvent BUZZ2_EVENT = SoundEvent.of(BUZZ2_ID);
    public static SoundEvent CANIHAVEYOURSTASH_EVENT = SoundEvent.of(CANIHAVEYOURSTASH_ID);
    public static SoundEvent CANIHAVEYOURSTASH2_EVENT = SoundEvent.of(CANIHAVEYOURSTASH2_ID);
    public static SoundEvent CANIHAVEYOURSTASH3_EVENT = SoundEvent.of(CANIHAVEYOURSTASH3_ID);
    public static SoundEvent DIAMOND2_EVENT = SoundEvent.of(DIAMOND2_ID);
    public static SoundEvent DIAMONDHELMET_EVENT = SoundEvent.of(DIAMONDHELMET_ID);
    public static SoundEvent DONKEYDUPE1_EVENT = SoundEvent.of(DONKEYDUPE1_ID);
    public static SoundEvent DONKEYDUPE2_EVENT = SoundEvent.of(DONKEYDUPE2_ID);
    public static SoundEvent DOYOULOVEMEBACK_EVENT = SoundEvent.of(DOYOULOVEMEBACK_ID);
    public static SoundEvent DOYOULOVEMEBACK2_EVENT = SoundEvent.of(DOYOULOVEMEBACK2_ID);
    public static SoundEvent DOYOULOVEMEBACK3_EVENT = SoundEvent.of(DOYOULOVEMEBACK3_ID);
    public static SoundEvent DOYOULOVEMEBACK4_EVENT = SoundEvent.of(DOYOULOVEMEBACK4_ID);
    public static SoundEvent EGG_EVENT = SoundEvent.of(EGG_ID);
    public static SoundEvent EGG2_EVENT = SoundEvent.of(EGG2_ID);
    public static SoundEvent EGG3_EVENT = SoundEvent.of(EGG3_ID);
    public static SoundEvent HAUHHHH_EVENT = SoundEvent.of(HAUHHHH_ID);
    public static SoundEvent HEADSHOT_EVENT = SoundEvent.of(HEADSHOT_ID);
    public static SoundEvent HEADSHOT2_EVENT = SoundEvent.of(HEADSHOT2_ID);
    public static SoundEvent HEADSHOT3_EVENT = SoundEvent.of(HEADSHOT3_ID);
    public static SoundEvent HEADSHOT5_EVENT = SoundEvent.of(HEADSHOT5_ID);
    public static SoundEvent HEADSHOT6_EVENT = SoundEvent.of(HEADSHOT6_ID);
    public static SoundEvent HOIIIIIY_EVENT = SoundEvent.of(HOIIIIIY_ID);
    public static SoundEvent HOYY_EVENT = SoundEvent.of(HOYY_ID);
    public static SoundEvent HOYYYY_EVENT = SoundEvent.of(HOYYYY_ID);
    public static SoundEvent HOYYYYYY_EVENT = SoundEvent.of(HOYYYYYY_ID);
    public static SoundEvent IKNEWYOUCOULDDOIT_EVENT = SoundEvent.of(IKNEWYOUCOULDDOIT_ID);
    public static SoundEvent IKNEWYOUCOULDDOIT2_EVENT = SoundEvent.of(IKNEWYOUCOULDDOIT2_ID);
    public static SoundEvent IKNEWYOUCOULDDOIT3_EVENT = SoundEvent.of(IKNEWYOUCOULDDOIT3_ID);
    public static SoundEvent IKNEWYOUCOULDDOIT4_EVENT = SoundEvent.of(IKNEWYOUCOULDDOIT4_ID);
    public static SoundEvent LONGSMUG_LAUGH_EVENT = SoundEvent.of(LONGSMUG_LAUGH_ID);
    public static SoundEvent MEANLAUGH_LONGEST_EVENT = SoundEvent.of(MEANLAUGH_LONGEST_ID);
    public static SoundEvent OHYOUDIDIT_BEST_EVENT = SoundEvent.of(OHYOUDIDIT_BEST_ID);
    public static SoundEvent OOPS_EVENT = SoundEvent.of(OOPS_ID);
    public static SoundEvent OOPS2_EVENT = SoundEvent.of(OOPS2_ID);
    public static SoundEvent OOPS3_EVENT = SoundEvent.of(OOPS3_ID);
    public static SoundEvent OOPS4_EVENT = SoundEvent.of(OOPS4_ID);
    public static SoundEvent OUCH_EVENT = SoundEvent.of(OUCH_ID);
    public static SoundEvent OUCH2_EVENT = SoundEvent.of(OUCH2_ID);
    public static SoundEvent OUCH3_EVENT = SoundEvent.of(OUCH3_ID);
    public static SoundEvent OUCH4_EVENT = SoundEvent.of(OUCH4_ID);
    public static SoundEvent OUCH5_EVENT = SoundEvent.of(OUCH5_ID);
    public static SoundEvent PAYLOAD_EVENT = SoundEvent.of(PAYLOAD_ID);
    public static SoundEvent PAYLOAD2_EVENT = SoundEvent.of(PAYLOAD2_ID);
    public static SoundEvent PAYLOAD3_EVENT = SoundEvent.of(PAYLOAD3_ID);
    public static SoundEvent PAYLOAD5_EVENT = SoundEvent.of(PAYLOAD5_ID);
    public static SoundEvent PAYLOAD6_EVENT = SoundEvent.of(PAYLOAD6_ID);
    public static SoundEvent PAYLOAD7_EVENT = SoundEvent.of(PAYLOAD7_ID);
    public static SoundEvent PINGCARRIED_EVENT = SoundEvent.of(PINGCARRIED_ID);
    public static SoundEvent PLANB_EVENT = SoundEvent.of(PLANB_ID);
    public static SoundEvent PLANB2_EVENT = SoundEvent.of(PLANB2_ID);
    public static SoundEvent PLANB3_EVENT = SoundEvent.of(PLANB3_ID);
    public static SoundEvent SCREAM1_EVENT = SoundEvent.of(SCREAM1_ID);
    public static SoundEvent SCREAM2_EVENT = SoundEvent.of(SCREAM2_ID);
    public static SoundEvent SCREAM3_EVENT = SoundEvent.of(SCREAM3_ID);
    public static SoundEvent SHH_EVENT = SoundEvent.of(SHH_ID);
    public static SoundEvent SHH2_EVENT = SoundEvent.of(SHH2_ID);
    public static SoundEvent SKEETSKET_EVENT = SoundEvent.of(SKEETSKET_ID);
    public static SoundEvent SKEETSKET2_EVENT = SoundEvent.of(SKEETSKET2_ID);
    public static SoundEvent SKYRIM1_EVENT = SoundEvent.of(SKYRIM1_ID);
    public static SoundEvent SMUG_LAUGH_EVENT = SoundEvent.of(SMUG_LAUGH_ID);
    public static SoundEvent SMUG_LAUGH2_EVENT = SoundEvent.of(SMUG_LAUGH2_ID);
    public static SoundEvent THESEMODULESUSEDTO_EVENT = SoundEvent.of(THESEMODULESUSEDTO_ID);
    public static SoundEvent TIMETORUN_EVENT = SoundEvent.of(TIMETORUN_ID);
    public static SoundEvent TIMETORUN2_EVENT = SoundEvent.of(TIMETORUN2_ID);
    public static SoundEvent TIMETORUN3_EVENT = SoundEvent.of(TIMETORUN3_ID);
    public static SoundEvent TOTEM_EVENT = SoundEvent.of(TOTEM_ID);
    public static SoundEvent WHIMPER_EVENT = SoundEvent.of(WHIMPER_ID);
    public static SoundEvent YOUDIDIT_EVENT = SoundEvent.of(YOUDIDIT_ID);
    public static SoundEvent YUMMY_EVENT = SoundEvent.of(YUMMY_ID);
    public static SoundEvent YUMMY2_EVENT = SoundEvent.of(YUMMY2_ID);
    public static SoundEvent YUMMY3_EVENT = SoundEvent.of(YUMMY3_ID);

    public static boolean nullcheck() {
        return MC.player == null || MC.world == null;
    }
    
    public static void turnOn() {
        if (nullcheck()) return;

        if (Payload.getInstance().moduleManager.says.toggleSounds.getValue() && Payload.getInstance().moduleManager.says.state.getValue()) {
            MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_LEVER_CLICK, 0.5f, 1f);
        }
    }

    public static void turnOff() {
        if (nullcheck()) return;

        if (Payload.getInstance().moduleManager.says.toggleSounds.getValue() && Payload.getInstance().moduleManager.says.state.getValue()) {
            MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_LEVER_CLICK, 0.33f,0.5f);
        }
    }
    public static void accessettings() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_LEVER_CLICK, 1.33f,2.5f);
    }
    public static void custom() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(MY_SOUND_EVENT, 0.33f,0.5f);
    }

    public static void saveConfig() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_ENDER_PEARL_THROW, 0.5f,1f);
    }

    public static void loadConfig() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_DEEPSLATE_PLACE, 0.5f,0.5f);
    }

    public static void halosprint() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HALOSPRINT_EVENT, Volume, 1.0f);
    }

    public static void haloshoot() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HALOSHOOT_EVENT, Volume, 1.0f);
    }

    public static void haloshoot2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HALOSHOOT2_EVENT, Volume, 1.0f);
    }

    public static void halohurt() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HALOHURT_EVENT, 0.66f, 1.0f);
    }

    public static void armor() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(ARMOR_EVENT, Volume, 1.0f);
    }

    public static void armor2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(ARMOR2_EVENT, Volume, 1.0f);
    }

    public static void armor3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(ARMOR3_EVENT, Volume, 1.0f);
    }

    public static void armor4() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(ARMOR4_EVENT, Volume, 1.0f);
    }

    public static void armor5() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(ARMOR5_EVENT, Volume, 1.0f);
    }

    public static void baka_youdidit() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BAKA_YOUDIDIT_EVENT, Volume, 1.0f);
    }

    public static void betterluck() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BETTERLUCK_EVENT, Volume, 1.0f);
    }

    public static void betterluck2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BETTERLUCK2_EVENT, Volume, 1.0f);
    }
    public static void whimper() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(WHIMPER_EVENT, Volume, 1.0f);
    }
    public static void smuglaugh() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(SMUG_LAUGH_EVENT, Volume, 1.0f);
    }
    public static void smuglaugh2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(SMUG_LAUGH2_EVENT, Volume, 1.0f);
    }

    public static void betterluck3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BETTERLUCK3_EVENT, Volume, 1.0f);
    }

    public static void betterluck4() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BETTERLUCK4_EVENT, Volume, 1.0f);
    }

    public static void betterluck5() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BETTERLUCK5_EVENT, Volume, 1.0f);
    }

    public static void bff() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BFF_EVENT, Volume, 1.0f);
    }

    public static void bff2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BFF2_EVENT, Volume, 1.0f);
    }

    public static void boo1() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BOO1_EVENT, Volume, 1.0f);
    }

    public static void boo2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BOO2_EVENT, Volume, 1.0f);
    }

    public static void boo3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BOO3_EVENT, Volume, 1.0f);
    }

    public static void buzz() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BUZZ_EVENT, Volume, 1.0f);
    }

    public static void buzz2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(BUZZ2_EVENT, Volume, 1.0f);
    }

    public static void canihaveyourstash() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(CANIHAVEYOURSTASH_EVENT, Volume, 1.0f);
    }

    public static void canihaveyourstash2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(CANIHAVEYOURSTASH2_EVENT, Volume, 1.0f);
    }

    public static void canihaveyourstash3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(CANIHAVEYOURSTASH3_EVENT, Volume, 1.0f);
    }

    public static void diamond2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DIAMOND2_EVENT, Volume, 1.0f);
    }

    public static void diamondhelmet() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DIAMONDHELMET_EVENT, Volume, 1.0f);
    }

    public static void donkeydupe1() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DONKEYDUPE1_EVENT, Volume, 1.0f);
    }

    public static void donkeydupe2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DONKEYDUPE2_EVENT, Volume, 1.0f);
    }

    public static void doyoulovemeback() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DOYOULOVEMEBACK_EVENT, Volume, 1.0f);
    }

    public static void doyoulovemeback2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DOYOULOVEMEBACK2_EVENT, Volume, 1.0f);
    }

    public static void doyoulovemeback3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DOYOULOVEMEBACK3_EVENT, Volume, 1.0f);
    }

    public static void doyoulovemeback4() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(DOYOULOVEMEBACK4_EVENT, Volume, 1.0f);
    }

    public static void egg() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(EGG_EVENT, Volume, 1.0f);
    }

    public static void egg2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(EGG2_EVENT, Volume, 1.0f);
    }

    public static void egg3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(EGG3_EVENT, Volume, 1.0f);
    }

    public static void hauhhhh() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HAUHHHH_EVENT, Volume, 1.0f);
    }

    public static void headshot() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HEADSHOT_EVENT, Volume, 1.0f);
    }

    public static void headshot2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HEADSHOT2_EVENT, Volume, 1.0f);
    }

    public static void headshot3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HEADSHOT3_EVENT, Volume, 1.0f);
    }

    public static void headshot5() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HEADSHOT5_EVENT, Volume, 1.0f);
    }

    public static void headshot6() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HEADSHOT6_EVENT, Volume, 1.0f);
    }

    public static void hoiiy() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HOIIIIIY_EVENT, Volume, 1.0f);
    }

    public static void hoyy() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HOYY_EVENT, Volume, 1.0f);
    }

    public static void hoyyy() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HOYYYY_EVENT, Volume, 1.0f);
    }

    public static void hoyyyyyy() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(HOYYYYYY_EVENT, Volume, 1.0f);
    }

    public static void iknewyoucoulddoit() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(IKNEWYOUCOULDDOIT_EVENT, Volume, 1.0f);
    }

    public static void iknewyoucoulddoit2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(IKNEWYOUCOULDDOIT2_EVENT, Volume, 1.0f);
    }

    public static void iknewyoucoulddoit3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(IKNEWYOUCOULDDOIT3_EVENT, Volume, 1.0f);
    }

    public static void iknewyoucoulddoit4() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(IKNEWYOUCOULDDOIT4_EVENT, Volume, 1.0f);
    }

    public static void longsmug_laugh() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(LONGSMUG_LAUGH_EVENT, Volume, 1.0f);
    }

    public static void meanlaugh_longest() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(MEANLAUGH_LONGEST_EVENT, Volume, 1.0f);
    }

    public static void newvegas1() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(NEWVEGAS1_EVENT, Volume, 1.0f);
    }

    public static void anime_moan() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(MY_SOUND_EVENT1, 0.33f,1.0f);
    }

    public static void ohyoudidit_best() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OHYOUDIDIT_BEST_EVENT, Volume, 1.0f);
    }

    public static void oops() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OOPS_EVENT, Volume, 1.0f);
    }

    public static void oops2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OOPS2_EVENT, Volume, 1.0f);
    }

    public static void oops3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OOPS3_EVENT, Volume, 1.0f);
    }

    public static void oops4() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OOPS4_EVENT, Volume, 1.0f);
    }

    public static void ouch() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OUCH_EVENT, Volume, 1.0f);
    }

    public static void ouch2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OUCH2_EVENT, Volume, 1.0f);
    }

    public static void ouch3() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OUCH3_EVENT, Volume, 1.0f);
    }

    public static void ouch4() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OUCH4_EVENT, Volume, 1.0f);
    }

    public static void ouch5() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(OUCH5_EVENT, Volume, 1.0f);
    }

    public static void payload() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(PAYLOAD_EVENT, Volume, 1.0f);
    }

    public static void payload2() {
        if (nullcheck()) return;
        MinecraftClient.getInstance().player.playSound(PAYLOAD2_EVENT, Volume, 1.0f);
    }
}