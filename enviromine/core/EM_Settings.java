package enviromine.core;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import enviromine.trackers.ArmorProperties;
import enviromine.trackers.BlockProperties;
import enviromine.trackers.EntityProperties;
import net.minecraftforge.common.Configuration;

public class EM_Settings
{
	public static final UUID FROST1_UUID = UUID.fromString("B0C5F86A-78F3-417C-8B5A-527B90A1E919");
	public static final UUID FROST2_UUID = UUID.fromString("5C4111A7-A66C-40FB-9FAD-1C6ADAEE7E27");
	public static final UUID FROST3_UUID = UUID.fromString("721E793E-2203-4F6F-883F-6F44D7DDCCE1");
	public static final UUID HEAT1_UUID = UUID.fromString("CA6E2CFA-4C53-4CD2-AAD3-3D6177A4F126");
	
	//Mod Data
	public static final String Version = "1.1.0";
	public static final String ID = "EnviroMine";
	public static final String Channel = "EM_CH";
	public static final String Name = "EnviroMine";
	public static final String Proxy = "enviromine.core.proxies";
	
	
	public static boolean enablePhysics = true;
	public static boolean enableLandslide = true;
	public static boolean enableAirQ = true;
	public static boolean enableHydrate = true;
	public static boolean enableSanity = true;
	public static boolean enableBodyTemp = true;
	public static boolean trackNonPlayer_default = true;
		public static boolean trackNonPlayer_actual;
		
	public static boolean ShowGuiIcons_default = true;
		public static boolean ShowGuiIcons_actual;
	//public static boolean saddleRecipe = true;
	
	
	//Gui settings
	public static boolean sweatParticals_default = true;
		public static boolean sweatParticals_actual;
	public static boolean insaneParticals_default = true;
		public static boolean insaneParticals_actual;
	
	public static boolean useFarenheit = false;
	public static String heatBarPos_default = "Bottom_Left";
		public static String heatBarPos_actual;
	public static String waterBarPos_default = "Bottom_Left";
		public static String waterBarPos_actual;
	public static String sanityBarPos_default = "Bottom_Right";
		public static String sanityBarPos_actual;
	public static String oxygenBarPos_default = "Bottom_Right";
		public static String oxygenBarPos_actual;
		
    public static boolean ShowText_default = false;
    	public static boolean ShowText_actual = false;
    public static boolean ShowDebug_default = true;
    	public static boolean ShowDebug_actual;	
	
	public static int dirtBottleID = 5001;
	public static int saltBottleID = 5002;
	public static int coldBottleID = 5003;
	public static int camelPackID = 5004;

	public static int hypothermiaPotionID = 27;
	public static int heatstrokePotionID = 28;
	public static int frostBitePotionID = 29;
	public static int dehydratePotionID = 30;
	public static int insanityPotionID = 31;
	
	//World Gen
	public static boolean shaftGen_default = true;
		public static boolean shaftGen_actual;
	
	//Properties
	public static HashMap<Integer,ArmorProperties> armorProperties = new HashMap<Integer,ArmorProperties>();
	public static HashMap<String,BlockProperties> blockProperties = new HashMap<String,BlockProperties>();
	public static HashMap<String,EntityProperties> livingProperties = new HashMap<String,EntityProperties>();
    
	public static void loadGeneralConfig(File file)
	{
		Configuration config;
		try
		{
			config = new Configuration(file);
		} catch(NullPointerException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.INFO, "FAILED TO LOAD MAIN CONFIG!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
			return;
		} catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.INFO, "FAILED TO LOAD MAIN CONFIG!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
			return;
		}
		
        config.load();
        
        
        //World Generation
        
        shaftGen_actual =  config.get("Wold Generations", "Enable Village MineShafts", true, "Generates mineshafts in villages").getBoolean(true);
        
        //General Settings
        enablePhysics = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics", true, "Turn physics On/Off").getBoolean(true);
        enableLandslide =  config.get(Configuration.CATEGORY_GENERAL, "Enable Physics Landslide", true).getBoolean(true);
        enableSanity = config.get(Configuration.CATEGORY_GENERAL, "Allow Sanity", true).getBoolean(true);
        enableHydrate = config.get(Configuration.CATEGORY_GENERAL, "Allow Hydration", true).getBoolean(true);
        enableBodyTemp = config.get(Configuration.CATEGORY_GENERAL, "Allow Body Temperature", true).getBoolean(true);
        enableAirQ = config.get(Configuration.CATEGORY_GENERAL, "Allow Air Quality", true, "True/False to turn Enviromine Trackers for Sanity, Air Quality, Hydration, and Body Temperature.").getBoolean(true);
        trackNonPlayer_actual = config.get(Configuration.CATEGORY_GENERAL, "Track NonPlayer entitys", trackNonPlayer_default , "Track enviromine properties on Non-player entites(mobs & animals)").getBoolean(trackNonPlayer_default);
        
        
        // Gui settings
        String GuiSetCat = "GUI Settings";
        sweatParticals_actual = config.get(GuiSetCat, "Show Sweat Particales",  sweatParticals_default).getBoolean(true);
        insaneParticals_actual =  config.get(GuiSetCat, "Show Insanity Particles", insaneParticals_default ,"Show/Hide Particales").getBoolean(true);
        useFarenheit = config.get(GuiSetCat, "Use Farenheit instead of Celsius", false, "Will display either Farenhit or Celcius on GUI").getBoolean(false);
        heatBarPos_actual = config.get(GuiSetCat, "Position Heat Bat", "Bottom_Left").getString();
    	waterBarPos_actual = config.get(GuiSetCat, "Position Thirst Bar", "Bottom_Left").getString();
    	sanityBarPos_actual = config.get(GuiSetCat, "Position Sanity Bar", "Bottom_Right").getString();
    	oxygenBarPos_actual = config.get(GuiSetCat, "Position Air Quality Bar", "Bottom_Right", "Change position of Enviro Bars. \\n Options: Bottom_Left, Bottom_Right, Bottom_Center_Left, Bottom_Center_Right, Top_Left, Top_Right, Top_Center").getString();
    	ShowText_actual = config.get(GuiSetCat, "Show Gui Text",  ShowText_default).getBoolean(ShowText_default);
    	ShowDebug_actual = config.get(GuiSetCat, "Show Debugging Info",  ShowDebug_default).getBoolean(ShowDebug_default);
    	
    	ShowGuiIcons_actual = config.get(GuiSetCat, "Show Gui Icons",  ShowGuiIcons_default, "Show Hide Gui Text Display and Icons").getBoolean(ShowGuiIcons_default);
        
        //removed
        //saddleRecipe = config.get(Configuration.CATEGORY_GENERAL, "Enable Saddle Recipe", true , "True will allow you to build Saddles for horses.").getBoolean(true);
        
        // Config Item ID's
        dirtBottleID = config.get(Configuration.CATEGORY_ITEM, "Dirty Water Bottle", 5001).getInt(5001);
        saltBottleID = config.get(Configuration.CATEGORY_ITEM, "Salt Water Bottle", 5002).getInt(5002);
        coldBottleID = config.get(Configuration.CATEGORY_ITEM, "Cold Water Bottle", 5003).getInt(5003);
        camelPackID= config.get(Configuration.CATEGORY_ITEM, "Camel Pack", 5004).getInt(5004);

        
        // Potion ID's
        frostBitePotionID = config.get("Potions", "Hypothermia", 27).getInt(27);
        frostBitePotionID = config.get("Potions", "Heat Stroke", 28).getInt(28);
        frostBitePotionID = config.get("Potions", "Frostbite", 29).getInt(29);
        dehydratePotionID = config.get("Potions", "Dehydration", 30).getInt(30);
        insanityPotionID = config.get("Potions", "Insanity", 31).getInt(31);

        config.save();
	}
	
	public static float convertToFarenheit(float num)
	{
		return ((num *(9/5))+32F);
	}
	
	public static float convertToCelcius(float num)
	{
		return ((num-32F) * (5/9));
	}
}
