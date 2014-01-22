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
	
	public static boolean useFarenheit = false;
	public static boolean enablePhysics = true;
	public static boolean enableAirQ = true;
	public static boolean enableHydrate = true;
	public static boolean enableSanity = true;
	public static boolean enableBodyTemp = true;
	public static boolean saddleRecipe = true;
	
	public static int dirtBottleID = 5001;
	public static int saltBottleID = 5002;
	public static int coldBottleID = 5003;
	public static int camelPackID = 5004;

	public static int hypothermiaPotionID = 27;
	public static int heatstrokePotionID = 28;
	public static int frostBitePotionID = 29;
	public static int dehydratePotionID = 30;
	public static int insanityPotionID = 31;
	
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
        
        
        //General Settings
        useFarenheit = config.get(Configuration.CATEGORY_GENERAL, "Use Farenheit instead of Celsius", false, "Will display either Farenhit or Celcius on GUI").getBoolean(false);
        enablePhysics = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics", true, "Turn physics On/Off").getBoolean(true);
        enableSanity = config.get(Configuration.CATEGORY_GENERAL, "Allow Sanity", true).getBoolean(true);
        enableHydrate = config.get(Configuration.CATEGORY_GENERAL, "Allow Hydration", true).getBoolean(true);
        enableBodyTemp = config.get(Configuration.CATEGORY_GENERAL, "Allow Body Temperature", true).getBoolean(true);
        enableAirQ = config.get(Configuration.CATEGORY_GENERAL, "Allow Air Quality", true, "True/False to turn Enviromine Trackers for Sanity, Air Quality, Hydration, and Body Temperature.").getBoolean(true);
        saddleRecipe = config.get(Configuration.CATEGORY_GENERAL, "Enable Saddle Recipe", true , "True will allow you to build Saddles for horses.").getBoolean(true);
        
        // Config Item ID's
        dirtBottleID = config.get(Configuration.CATEGORY_ITEM, "Dirty Water Bottle", 5001).getInt(5001);
        saltBottleID = config.get(Configuration.CATEGORY_ITEM, "Salt Water Bottle", 5002).getInt(5002);
        coldBottleID = config.get(Configuration.CATEGORY_ITEM, "Cold Water Bottle", 5003).getInt(5003);
        camelPackID = config.get(Configuration.CATEGORY_ITEM, "Camel Pack", 5004).getInt(5004);
        
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
