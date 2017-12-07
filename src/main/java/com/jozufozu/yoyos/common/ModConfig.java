package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Yoyos.MODID)
@Mod.EventBusSubscriber(modid = Yoyos.MODID)
public class ModConfig
{
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (Yoyos.MODID.equals(event.getModID()))
        {
            ConfigManager.sync(Yoyos.MODID, Config.Type.INSTANCE);
        }
    }
    
    @Config.LangKey("yoyos.config.vanilla")
    public static VanillaYoyos vanillaYoyos = new VanillaYoyos();
    
    @Config.LangKey("yoyos.config.tinkers")
    @Config.Comment("If enabled and Tinkers' Construct is installed, adds customizable yoyos")
    public static boolean tinkersYoyos = true;
    
    @Config.LangKey("yoyos.config.swing")
    @Config.Comment("Allows you to swing from yoyos when they get caught")
    public static boolean yoyoSwing = true;
    
    public static class VanillaYoyos
    {
        @Config.Comment("Whether or not the default yoyos are added")
        @Config.RequiresMcRestart
        public boolean enable = true;
        
        @Config.LangKey("item.wooden_yoyo.name")
        public YoyoSettings woodenYoyo = new YoyoSettings(2.2f, 6.0f, 100);
        @Config.LangKey("item.stone_yoyo.name")
        public YoyoSettings stoneYoyo = new YoyoSettings(4.0f, 7.0f, 200);
        @Config.LangKey("item.iron_yoyo.name")
        public YoyoSettings ironYoyo = new YoyoSettings(5.0f, 8.0f, 300);
        @Config.LangKey("item.shear_yoyo.name")
        public YoyoSettings shearYoyo = new YoyoSettings(5.1f, 8.0f, 300);
        @Config.LangKey("item.gold_yoyo.name")
        public YoyoSettings goldYoyo = new YoyoSettings(5.5f, 11.0f, 600);
        @Config.LangKey("item.diamond_yoyo.name")
        public YoyoSettings diamondYoyo = new YoyoSettings(1.7f, 9.0f, 400);
        @Config.LangKey("item.sticky_yoyo.name")
        public YoyoSettings stickyYoyo = new YoyoSettings(1.7f, 9.0f, 400);
        
        public static class YoyoSettings
        {
            @Config.Comment("Effects how fast the yoyo moves")
            @Config.RangeDouble(min = 0)
            public float weight;
    
            @Config.Comment("How far away the yoyo can get (in blocks)")
            @Config.RangeDouble(min = 0)
            public float length;
    
            @Config.Comment("How long the yoyo can stay out (in ticks)")
            @Config.RangeInt(min = 0)
            public int duration;
    
            public YoyoSettings()
            {
            }
    
            public YoyoSettings(float weight, float length, int duration)
            {
                this.weight = weight;
                this.length = length;
                this.duration = duration;
            }
        }
    }
}