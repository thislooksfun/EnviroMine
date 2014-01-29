package enviromine.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import enviromine.trackers.ArmorProperties;
import enviromine.trackers.BlockProperties;
import enviromine.trackers.EntityProperties;
import enviromine.trackers.ItemProperties;
import enviromine.trackers.StabilityType;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.Configuration;

public class EM_ConfigHandler
{
	
	// Dirs for Custom Files
	public static String configPath = "config/enviromine/";
	static String customPath = configPath + "CustomProperties/";
	
	// Categories for Custom Objects
	static String armorCat = "armor";
	static String blockCat = "blocks";
	static String entityCat = "entity";
	static String itemsCat = "items";
	
	public static void initConfig()
	{
		// Check for Data Directory 
		CheckDir(new File(customPath));
		
		//CheckFile(new File(configPath + "Help_File_Custom.txt"));
		
		EnviroMine.logger.log(Level.INFO, "Loading configs");
		
		File stabConfigFile = new File(configPath + "StabilityTypes.cfg");
		loadStabilityTypes(stabConfigFile);
		
		// load defaults
		loadDefaultArmorProperties();
		
		// Now load Files from "Custom Objects"
		File[] customFiles = GetFileList(customPath);
		for(int i = 0; i < customFiles.length; i++)
		{
			LoadCustomObjects(customFiles[i]);
		}
		
		// Load Main Config File And this will go though changes
		File configFile = new File(configPath + "EnviroMine.cfg");
		loadGeneralConfig(configFile);
		
		EnviroMine.logger.log(Level.INFO, "Loaded " + EM_Settings.armorProperties.size() + " armor properties");
		EnviroMine.logger.log(Level.INFO, "Loaded " + EM_Settings.blockProperties.size() + " block properties");
		EnviroMine.logger.log(Level.INFO, "Loaded " + EM_Settings.livingProperties.size() + " entity properties");
		EnviroMine.logger.log(Level.INFO, "Loaded " + EM_Settings.itemProperties.size() + " item properties");
		
		EnviroMine.logger.log(Level.INFO, "Finished loading configs");
	}
	
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
		
		EM_Settings.shaftGen_actual = config.get("Wold Generations", "Enable Village MineShafts", true, "Generates mineshafts in villages").getBoolean(true);
		
		//General Settings
		EM_Settings.enablePhysics = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics", true, "Turn physics On/Off").getBoolean(true);
		EM_Settings.enableLandslide = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics Landslide", true).getBoolean(true);
		EM_Settings.enableSanity = config.get(Configuration.CATEGORY_GENERAL, "Allow Sanity", true).getBoolean(true);
		EM_Settings.enableHydrate = config.get(Configuration.CATEGORY_GENERAL, "Allow Hydration", true).getBoolean(true);
		EM_Settings.enableBodyTemp = config.get(Configuration.CATEGORY_GENERAL, "Allow Body Temperature", true).getBoolean(true);
		EM_Settings.enableAirQ = config.get(Configuration.CATEGORY_GENERAL, "Allow Air Quality", true, "True/False to turn Enviromine Trackers for Sanity, Air Quality, Hydration, and Body Temperature.").getBoolean(true);
		EM_Settings.trackNonPlayer_actual = config.get(Configuration.CATEGORY_GENERAL, "Track NonPlayer entitys", EM_Settings.trackNonPlayer_default, "Track enviromine properties on Non-player entites(mobs & animals)").getBoolean(EM_Settings.trackNonPlayer_default);
		
		EM_Settings.spreadIce = config.get(Configuration.CATEGORY_GENERAL, "Large Ice Cracking", false, "Setting Large Ice Cracking to true can cause Massive Lag").getBoolean(false);
		
		EM_Settings.updateCap = config.get(Configuration.CATEGORY_GENERAL, "Consecutive Physics Update Cap", -1 , "This will change maximum number of blocks that can be updated with physics at a time. - 1 = Unlimited").getInt(-1);
		
		// Gui settings
		String GuiSetCat = "GUI Settings";
		EM_Settings.sweatParticals_actual = config.get(GuiSetCat, "Show Sweat Particales", EM_Settings.sweatParticals_default).getBoolean(true);
		EM_Settings.insaneParticals_actual = config.get(GuiSetCat, "Show Insanity Particles", EM_Settings.insaneParticals_default, "Show/Hide Particales").getBoolean(true);
		EM_Settings.useFarenheit = config.get(GuiSetCat, "Use Farenheit instead of Celsius", false, "Will display either Farenhit or Celcius on GUI").getBoolean(false);
		EM_Settings.heatBarPos_actual = config.get(GuiSetCat, "Position Heat Bat", "Bottom_Left").getString();
		EM_Settings.waterBarPos_actual = config.get(GuiSetCat, "Position Thirst Bar", "Bottom_Left").getString();
		EM_Settings.sanityBarPos_actual = config.get(GuiSetCat, "Position Sanity Bar", "Bottom_Right").getString();
		EM_Settings.oxygenBarPos_actual = config.get(GuiSetCat, "Position Air Quality Bar", "Bottom_Right", "Change position of Enviro Bars. \\n Options: Bottom_Left, Bottom_Right, Bottom_Center_Left, Bottom_Center_Right, Top_Left, Top_Right, Top_Center").getString();
		
		EM_Settings.ShowDebug_actual = config.get(GuiSetCat, "Show Gui Debugging Info", EM_Settings.ShowDebug_default, "Show Hide Gui Text Display and Icons").getBoolean(EM_Settings.ShowDebug_default);
		EM_Settings.ShowText_actual = config.get(GuiSetCat, "Show Gui Status Text", EM_Settings.ShowText_default).getBoolean(EM_Settings.ShowText_default);
		EM_Settings.ShowGuiIcons_actual = config.get(GuiSetCat, "Show Gui Icons", EM_Settings.ShowGuiIcons_default).getBoolean(EM_Settings.ShowGuiIcons_default);
		
		// Config Item ID's
		EM_Settings.dirtBottleID = config.get(Configuration.CATEGORY_ITEM, "Dirty Water Bottle", 5001).getInt(5001);
		EM_Settings.saltBottleID = config.get(Configuration.CATEGORY_ITEM, "Salt Water Bottle", 5002).getInt(5002);
		EM_Settings.coldBottleID = config.get(Configuration.CATEGORY_ITEM, "Cold Water Bottle", 5003).getInt(5003);
		EM_Settings.camelPackID = config.get(Configuration.CATEGORY_ITEM, "Camel Pack", 5004).getInt(5004);
		
		// Potion ID's
		EM_Settings.frostBitePotionID = config.get("Potions", "Hypothermia", 27).getInt(27);
		EM_Settings.frostBitePotionID = config.get("Potions", "Heat Stroke", 28).getInt(28);
		EM_Settings.frostBitePotionID = config.get("Potions", "Frostbite", 29).getInt(29);
		EM_Settings.dehydratePotionID = config.get("Potions", "Dehydration", 30).getInt(30);
		EM_Settings.insanityPotionID = config.get("Potions", "Insanity", 31).getInt(31);
		
		config.save();
	}
	
	//#######################################
	//#          Get File List              #                 
	//#This Grabs Directory List for Custom #
	//#######################################
	public static File[] GetFileList(String path)
	{
		
		// Will be used Auto Load Custom Objects from ??? Dir 
		File f = new File(path);
		File[] list = f.listFiles();
		
		return list;
	}
	
	private static boolean isDatFile(String fileName)
	{
		//Matcher
		String patternString = "(.*\\.dat$)";
		
		Pattern pattern;
		Matcher matcher;
		// Make Sure its a .Dat File
		pattern = Pattern.compile(patternString);
		matcher = pattern.matcher(fileName);
		
		return matcher.matches();
	}
	
	//###################################
	//#           Check Dir             #                 
	//#  Checks for, or makes Directory #
	//###################################	
	public static void CheckDir(File Dir)
	{
		boolean dirFlag = false;
		
		// create File object
		
		if(Dir.exists())
		{
			return;
		}
		
		try
		{
			dirFlag = Dir.mkdirs();
		} catch(SecurityException Se)
		{
			EnviroMine.logger.log(Level.INFO, "Error while creating config directory:\n" + Se);
		}
		
		if(!dirFlag)
		{
			EnviroMine.logger.log(Level.INFO, "Failed to create config directory!");
		}
	}
	
	//####################################
	//#   Load Custom Objects            #
	//# Used to Load Custom Blocks,Armor #                              
	//#   Entitys, & Items from Mods     #
	//####################################
	public static void LoadCustomObjects(File customFiles)
	{
		boolean datFile = isDatFile(customFiles.getName());
		
		// Check to make sure this is a Data File Before Editing
		if(datFile == true)
		{
			Configuration config;
			try
			{
				config = new Configuration(customFiles);
			} catch(NullPointerException e)
			{
				e.printStackTrace();
				EnviroMine.logger.log(Level.INFO, "FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!");
				return;
			} catch(StringIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				EnviroMine.logger.log(Level.INFO, "FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!");
				return;
			}
			
			config.load();
			
			// Load Default Categories
			config.addCustomCategoryComment(armorCat, "Custom armor properties");
			config.addCustomCategoryComment(blockCat, "Custom block properties");
			config.addCustomCategoryComment(entityCat, "Custom entity properties");
			config.addCustomCategoryComment(itemsCat, "Custom item properties");
			
			// 	Grab all Categories in File
			List<String> catagory = new ArrayList<String>();
			Set<String> nameList = config.getCategoryNames();
			Iterator<String> nameListData = nameList.iterator();
			
			// add Categories to a List 
			while(nameListData.hasNext())
			{
				catagory.add(nameListData.next());
			}
			
			// Now Read/Save Each Category And Add into Proper Hash Maps
			
			for(int x = 0; x <= (catagory.size() - 1); x++)
			{
				String CurCat = catagory.get(x);
				
				if(!((String)CurCat).isEmpty() && ((String)CurCat).contains(Configuration.CATEGORY_SPLITTER))
				{
					String parent = CurCat.split("\\" + Configuration.CATEGORY_SPLITTER)[0];
					
					if(parent.equals(blockCat))
					{
						LoadBlockProperty(config, catagory.get(x));
					} else if(parent.equals(armorCat))
					{
						LoadArmorProperty(config, catagory.get(x));
					} else if(parent.equals(itemsCat))
					{
						LoadItemProperty(config, catagory.get(x));
					} else if(parent.equals(entityCat))
					{
						LoadLivingProperty(config, catagory.get(x));
					} else
					{
						EnviroMine.logger.log(Level.INFO, "Failed to load object " + CurCat);
					}
					
				}
			}
			
			config.save();
		}
	}
	
	private static void LoadItemProperty(Configuration config, String category)
	{
		
		config.addCustomCategoryComment(category, "");
		int id = config.get(category, "01.ID", 0).getInt(0);
		int meta = config.get(category, "02.Damage", 0).getInt(0);
		boolean enableTemp = config.get(category, "03.Enable Ambient Temperature", false).getBoolean(false);
		float ambTemp = (float)config.get(category, "04.Ambient Temprature", 0.00).getDouble(0.00);
		float ambAir = (float)config.get(category, "05.Ambient Air Quality", 0.00).getDouble(0.00);
		float ambSanity = (float)config.get(category, "06.Ambient Sanity", 0.00).getDouble(0.00);
		float effTemp = (float)config.get(category, "07.Effect Temprature", 0.00).getDouble(0.00);
		float effAir = (float)config.get(category, "08.Effect Air Quality", 0.00).getDouble(0.00);
		float effSanity = (float)config.get(category, "09.Effect Sanity", 0.00).getDouble(0.00);
		float effHydration = (float)config.get(category, "10.Effect Hydration", 0.00).getDouble(0.00);
		float effTempCap = (float)config.get(category, "11.Effect Temperature Cap", 37.00).getDouble(37.00);
		
		ItemProperties entry = new ItemProperties(id, meta, enableTemp, ambTemp, ambAir, ambSanity, effTemp, effAir, effSanity, effHydration, effTempCap);
		
		if(meta < 0)
		{
			EM_Settings.itemProperties.put("" + id, entry);
		} else
		{
			EM_Settings.itemProperties.put("" + id + "," + meta, entry);
		}
	}
	
	private static void LoadBlockProperty(Configuration config, String category)
	{
		config.addCustomCategoryComment(category, "");
		
		int id = config.get(category, "01.ID", 0).getInt(0);
		boolean hasPhys = config.get(category, "02.Enable Physics", false).getBoolean(false);
		int metaData = config.get(category, "03.MetaID", 0).getInt(0);
		int dropID = config.get(category, "04.DropID", 0).getInt(0);
		int dropMeta = config.get(category, "05.DropMetaID", 0).getInt(0);
		int dropNum = config.get(category, "06.DropNumber", 0).getInt(0);
		boolean enableTemp = config.get(category, "07.Enable Temperature", false).getBoolean(false);
		float temperature = (float)config.get(category, "08.Temprature", 0.00).getDouble(0.00);
		float airQuality = (float)config.get(category, "09.Air Quality", 0.00).getDouble(0.00);
		float sanity = (float)config.get(category, "10.Sanity", 0.00).getDouble(0.00);
		String stability = config.get(category, "11.Stability", "loose").getString();
		boolean slides = config.get(category, "12.Slides", false).getBoolean(false);
		
		// Get Stability Options
		int minFall = 99;
		int maxFall = 99;
		int supportDist = 5;
		boolean holdOther = false;
		
		if(EM_Settings.stabilityTypes.containsKey(stability))
		{
			StabilityType stabType = EM_Settings.stabilityTypes.get(stability);
			
			minFall = stabType.minFall;
			maxFall = stabType.maxFall;
			supportDist = stabType.supportDist;
			hasPhys = stabType.enablePhysics;
			holdOther = stabType.holdOther;
		} else
		{
			minFall = 99;
			maxFall = 99;
			supportDist = 9;
			hasPhys = false;
			holdOther = false;
		}
		
		BlockProperties entry = new BlockProperties(id, metaData, hasPhys, minFall, maxFall, supportDist, dropID, dropMeta, dropNum, enableTemp, temperature, airQuality, sanity, holdOther, slides);
		
		if(metaData < 0)
		{
			EM_Settings.blockProperties.put("" + id, entry);
		} else
		{
			EM_Settings.blockProperties.put("" + id + "," + metaData, entry);
		}
	}
	
	private static void LoadArmorProperty(Configuration config, String catagory)
	{
		config.addCustomCategoryComment(catagory, "");
		int id = config.get(catagory, "01.ID", 0).getInt(0);
		float nightTemp = (float)config.get(catagory, "02.Temp Add - Night", 0).getDouble(0);
		float shadeTemp = (float)config.get(catagory, "03.Temp Add - Shade", 0).getDouble(0);
		float sunTemp = (float)config.get(catagory, "04.Temp Add - Sun", 0).getDouble(0);
		float nightMult = (float)config.get(catagory, "05.Temp Multiplier - Night", 1).getDouble(1);
		float shadeMult = (float)config.get(catagory, "06.Temp Multiplier - Shade", 1).getDouble(1);
		float sunMult = (float)config.get(catagory, "07.Temp Multiplier - Sun", 1).getDouble(1);
		float sanity = (float)config.get(catagory, "08.Sanity", 0).getDouble(0);
		float air = (float)config.get(catagory, "09.Air", 0).getDouble(0);
		
		ArmorProperties entry = new ArmorProperties(id, nightTemp, shadeTemp, sunTemp, nightMult, shadeMult, sunMult, sanity, air);
		EM_Settings.armorProperties.put(id, entry);
	}
	
	private static void LoadLivingProperty(Configuration config, String catagory)
	{
		config.addCustomCategoryComment(catagory, "");
		String name = config.get(catagory, "1.Entity Name", "").getString();
		Boolean track = config.get(catagory, "2.Enable EnviroTracker", true).getBoolean(true);
		Boolean dehydration = config.get(catagory, "3.Enable Dehydration", true).getBoolean(true);
		Boolean bodyTemp = config.get(catagory, "4.Enable BodyTemp", true).getBoolean(true);
		Boolean airQ = config.get(catagory, "5.Enable Air Quility", true).getBoolean(true);
		Boolean immuneToFrost = config.get(catagory, "6.Immune To Frost", false).getBoolean(false);
		Boolean immuneToHeat = config.get(catagory, "7.Immune To Heat", false).getBoolean(false);
		
		EntityProperties entry = new EntityProperties(name, track, dehydration, bodyTemp, airQ, immuneToFrost, immuneToHeat);
		EM_Settings.livingProperties.put(name.toLowerCase(), entry);
	}
	
	// RIGHT NOW I AM JUST LOADING DEFAULT ARMOR INTO HASH MAPS
	// IF SOME CUSTOMIZED ARMOR>> THAN IT OVERIDES THIS FUNCTION
	public static void loadDefaultArmorProperties()
	{
		/*
		File customFile = new File(customPath + "ArmorDefaults.dat");
		
		Configuration custom;
		try	
			{ custom = new Configuration(customFile);} 
		catch(NullPointerException e)
			{e.printStackTrace();EnviroMine.logger.log(Level.INFO, "FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");	return;	} 
		catch(StringIndexOutOfBoundsException e)	
			{	e.printStackTrace();EnviroMine.logger.log(Level.INFO, "FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");	return;	}EnviroMine.logger.log(Level.INFO, "Loading EnviroMine Config: " + customFile.getAbsolutePath());
		custom.load();

		// Load Default Categories
		custom.addCustomCategoryComment(armorCat, "Add Custom Armor");
		custom.addCustomCategoryComment(blockCat, "Add Custom Blocks");
		custom.addCustomCategoryComment(entityCat, "Custom Entities");		
		*/
		
		ArmorDefaultSave(ItemArmor.helmetLeather.itemID, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.plateLeather.itemID, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.legsLeather.itemID, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.bootsLeather.itemID, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
		
		ArmorDefaultSave(ItemArmor.helmetIron.itemID, -0.5F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.plateIron.itemID, -0.5F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.legsIron.itemID, -0.5F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.bootsIron.itemID, -0.5F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F, 0.0F, 0.0F);
		
		ArmorDefaultSave(ItemArmor.helmetGold.itemID, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.plateGold.itemID, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.legsGold.itemID, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.bootsGold.itemID, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F, 0.0F, 0.0F);
		
		ArmorDefaultSave(ItemArmor.helmetDiamond.itemID, 0.0F, 0.0F, 0.0F, 1.1F, 1.0F, 0.9F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.plateDiamond.itemID, 0.0F, 0.0F, 0.0F, 1.1F, 1.0F, 0.9F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.legsDiamond.itemID, 0.0F, 0.0F, 0.0F, 1.1F, 1.0F, 0.9F, 0.0F, 0.0F);
		ArmorDefaultSave(ItemArmor.bootsDiamond.itemID, 0.0F, 0.0F, 0.0F, 1.1F, 1.0F, 0.9F, 0.0F, 0.0F);
		
		//custom.save();	
	}
	
	private static void ArmorDefaultSave(int id, float nightTemp, float shadeTemp, float sunTemp, float nightMult, float shadeMult, float sunMult, float sanity, float air)
	{
		ArmorProperties entry = new ArmorProperties(id, nightTemp, shadeTemp, sunTemp, nightMult, shadeMult, sunMult, sanity, air);
		EM_Settings.armorProperties.put(id, entry);
	}
	
	public static String SaveMyCustom(String type, String name, Object[] data)
	{
		
		// Check to make sure this is a Data File Before Editing
		File configFile = new File(customPath + "MyCustom.dat");
		
		Configuration config;
		try
		{
			config = new Configuration(configFile);
		} catch(NullPointerException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.INFO, "FAILED TO SAVE NEW OBJECT TO MYCUSTOM.DAT");
			return "Failed to Open MyCustom.dat";
		} catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.INFO, "FAILED TO SAVE NEW OBJECT TO MYCUSTOM.DAT");
			return "Failed to Open MyCustom.dat";
		}
		
		config.load();
		
		String returnValue = "";
		// Load Default Categories
		config.addCustomCategoryComment(armorCat, "Add Custom Armor");
		config.addCustomCategoryComment(blockCat, "Add Custom Blocks");
		config.addCustomCategoryComment(entityCat, "Custom Entities");
		
		if(type == "TILE")
		{
			String nameULCat = blockCat + "." + name.toLowerCase() + " " + (Integer)data[1];
			
			if(config.hasCategory(nameULCat) == true)
			{
				config.removeCategory(config.getCategory(nameULCat));
				returnValue = "Removed";
			} else
			{
				config.addCustomCategoryComment(nameULCat, name);
				config.get(nameULCat, "01.ID", (Integer)data[0]).getInt(0);
				config.get(nameULCat, "02.EnablePhysics", true).getBoolean(true);
				config.get(nameULCat, "03.MetaID", (Integer)data[1]).getInt(0);
				config.get(nameULCat, "04.DropID", (Integer)data[0]).getInt(0);
				config.get(nameULCat, "05.DropMetaID", (Integer)data[1]).getInt(0);
				config.get(nameULCat, "06.DropNumber", 0).getInt(0);
				config.get(nameULCat, "07.EnableTemperature", false).getBoolean(false);
				config.get(nameULCat, "08.Temprature", 0.00).getDouble(0.00);
				config.get(nameULCat, "09.AirQuality", 0.00).getDouble(0.00);
				config.get(nameULCat, "10.Sanity", 0.00).getDouble(0.00);
				config.get(nameULCat, "11.Stability", "loose").getString();
				config.get(nameULCat, "12.Holds Other Blocks", false).getBoolean(false);
				returnValue = "Saved";
			}
		} else if(type == "ENTITY")
		{
			
			String nameEntityCat = entityCat + "." + name.toLowerCase();
			
			if(config.hasCategory(nameEntityCat) == true)
			{
				config.removeCategory(config.getCategory(nameEntityCat));
				returnValue = "Removed";
			} else
			{
				config.addCustomCategoryComment(nameEntityCat, "");
				config.get(nameEntityCat, "1.Entity Name", name).toString();
				config.get(nameEntityCat, "2.Enable EnviroTracker", true).getBoolean(true);
				config.get(nameEntityCat, "2.Enable Dehydration", true).getBoolean(true);
				config.get(nameEntityCat, "3.Enable BodyTemp", true).getBoolean(true);
				config.get(nameEntityCat, "4.Enable Air Quility", true).getBoolean(true);
				config.get(nameEntityCat, "5.ImmuneToFrost", false).getBoolean(false);
				returnValue = "Saved";
			}
			
		} else if(type == "ITEM")
		{
			
			String nameItemCat = itemsCat + "." + name.toLowerCase();
			
			if(config.hasCategory(nameItemCat) == true)
			{
				config.removeCategory(config.getCategory(nameItemCat));
				returnValue = "Removed";
			} else
			{
				config.addCustomCategoryComment(nameItemCat, name);
				config.get(nameItemCat, "01.ID", (Integer)data[0]).getInt(0);
				config.get(nameItemCat, "02.MetaID", (Integer)data[1]).getInt(0);
				config.get(nameItemCat, "03.EnableTemperature", false).getBoolean(false);
				config.get(nameItemCat, "04.Ambient Temprature", 0.00).getDouble(0.00);
				config.get(nameItemCat, "05.Ambient Air Quality", 0.00).getDouble(0.00);
				config.get(nameItemCat, "06.Ambient Sanity", 0.00).getDouble(0.00);
				config.get(nameItemCat, "07.Ambient Hydration", 0.00).getDouble(0.00);
				config.get(nameItemCat, "08.Effect Temprature", 0.00).getDouble(0.00);
				config.get(nameItemCat, "09.Effect Air Quality", 0.00).getDouble(0.00);
				config.get(nameItemCat, "10.Effect Sanity", 0.00).getDouble(0.00);
				config.get(nameItemCat, "11.Effect Hydration", 0.00).getDouble(0.00);
				returnValue = "Saved";
			}
			
		} else if(type == "ARMOR")
		{
			String nameArmorCat = armorCat + "." + name.toLowerCase();
			
			if(config.hasCategory(nameArmorCat) == true)
			{
				config.removeCategory(config.getCategory(nameArmorCat));
				returnValue = "Removed";
			} else
			{
				config.addCustomCategoryComment(nameArmorCat, name);
				config.get(nameArmorCat, "1.ID", (Integer)data[0]).getInt(0);
				config.get(nameArmorCat, "2.nightTemp", 0.00).getDouble(0);
				config.get(nameArmorCat, "3.shadeTemp", 0.00).getDouble(0);
				config.get(nameArmorCat, "4.sunTemp", -1.00).getDouble(0);
				config.get(nameArmorCat, "5.nightMult", 1).getDouble(1);
				config.get(nameArmorCat, "6.shadeMult", 1).getDouble(1);
				config.get(nameArmorCat, "7.sunMult", 1).getDouble(1);
				returnValue = "Saved";
			}
		}
		
		config.save();
		return returnValue;
	}
	
	public static void loadStabilityTypes(File file)
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
		
		loadDefaultStabilityTypes(config);
		
		// 	Grab all Categories in File
		List<String> catagory = new ArrayList<String>();
		Set<String> nameList = config.getCategoryNames();
		Iterator<String> nameListData = nameList.iterator();
		
		// add Categories to a List 
		while(nameListData.hasNext())
		{
			catagory.add(nameListData.next());
		}
		
		// Now Read/Save Each Category And Add into Proper Hash Maps
		
		for(int x = 0; x <= (catagory.size() - 1); x++)
		{
			String currentCat = catagory.get(x);
			
			boolean physEnable = config.get(currentCat, "1.Enable Physics", true).getBoolean(true);
			int supportDist = config.get(currentCat, "2.Max Support Distance", 0).getInt(0);
			int minFall = config.get(currentCat, "3.Min Missing Blocks To Fall", -1).getInt(-1);
			int maxFall = config.get(currentCat, "4.Max Missing Blocks To Fall", -1).getInt(-1);
			boolean canHang = config.get(currentCat, "5.Can Hang", false).getBoolean(false);
			boolean holdOther = config.get(currentCat, "6.Holds Up Others", false).getBoolean(false);
			
			EM_Settings.stabilityTypes.put(currentCat, new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		}
		
		config.save();
	}
	
	public static void loadDefaultStabilityTypes(Configuration config)
	{
		boolean physEnable = config.get("sand-like", "1.Enable Physics", true).getBoolean(true);
		int supportDist = config.get("sand-like", "2.Max Support Distance", 0).getInt(0);
		int minFall = config.get("sand-like", "3.Min Missing Blocks To Fall", -1).getInt(-1);
		int maxFall = config.get("sand-like", "4.Max Missing Blocks To Fall", -1).getInt(-1);
		boolean canHang = config.get("sand-like", "5.Can Hang", false).getBoolean(false);
		boolean holdOther = config.get("sand-like", "6.Holds Up Others", false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("sand-like", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		
		physEnable = config.get("loose", "1.Enable Physics", true).getBoolean(true);
		supportDist = config.get("loose", "2.Max Support Distance", 1).getInt(1);
		minFall = config.get("loose", "3.Min Missing Blocks To Fall", 10).getInt(10);
		maxFall = config.get("loose", "4.Max Missing Blocks To Fall", 15).getInt(15);
		canHang = config.get("loose", "5.Can Hang", false).getBoolean(false);
		holdOther = config.get("loose", "6.Holds Up Others", false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("loose", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		
		physEnable = config.get("average", "1.Enable Physics", true).getBoolean(true);
		supportDist = config.get("average", "2.Max Support Distance", 2).getInt(2);
		minFall = config.get("average", "3.Min Missing Blocks To Fall", 15).getInt(15);
		maxFall = config.get("average", "4.Max Missing Blocks To Fall", 22).getInt(22);
		canHang = config.get("average", "5.Can Hang", false).getBoolean(false);
		holdOther = config.get("average", "6.Holds Up Others", false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("average", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		
		physEnable = config.get("strong", "1.Enable Physics", true).getBoolean(true);
		supportDist = config.get("strong", "2.Max Support Distance", 3).getInt(3);
		minFall = config.get("strong", "3.Min Missing Blocks To Fall", 22).getInt(22);
		maxFall = config.get("strong", "4.Max Missing Blocks To Fall", 25).getInt(25);
		canHang = config.get("strong", "5.Can Hang", true).getBoolean(true);
		holdOther = config.get("strong", "6.Holds Up Others", false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("strong", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
	}
} // End of Page
