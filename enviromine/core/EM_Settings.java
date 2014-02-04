package enviromine.core;

import java.util.HashMap;
import java.util.UUID;
import enviromine.trackers.ArmorProperties;
import enviromine.trackers.BlockProperties;
import enviromine.trackers.EntityProperties;
import enviromine.trackers.ItemProperties;
import enviromine.trackers.StabilityType;

public class EM_Settings
{
	public static final UUID FROST1_UUID = UUID.fromString("B0C5F86A-78F3-417C-8B5A-527B90A1E919");
	public static final UUID FROST2_UUID = UUID.fromString("5C4111A7-A66C-40FB-9FAD-1C6ADAEE7E27");
	public static final UUID FROST3_UUID = UUID.fromString("721E793E-2203-4F6F-883F-6F44D7DDCCE1");
	public static final UUID HEAT1_UUID = UUID.fromString("CA6E2CFA-4C53-4CD2-AAD3-3D6177A4F126");
	
	//Mod Data
	public static final String Version = "1.1.2";
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
	public static boolean saddleRecipe = true;
	
	public static boolean spreadIce = false;
	
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
	public static boolean ShowText_actual;
	public static boolean ShowDebug_default = false;
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
	public static HashMap<String,ItemProperties> itemProperties = new HashMap<String,ItemProperties>();
	
	public static HashMap<String,StabilityType> stabilityTypes = new HashMap<String,StabilityType>();
	public static int updateCap = -1;
	public static boolean stoneCracks;
	
	public static float convertToFarenheit(float num)
	{
		return((num * (9 / 5)) + 32F);
	}
	
	public static float convertToCelcius(float num)
	{
		return((num - 32F) * (5 / 9));
	}
}
