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
import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
	
	static String[] APName;
	static String[] BPName;
	static String[] EPName;
	static String[] IPName;
	static String[] SPName;
	
	public static int initConfig()
	{
		setPropertyNames();
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
		
		int Total = EM_Settings.armorProperties.size() + EM_Settings.blockProperties.size() + EM_Settings.livingProperties.size()+ EM_Settings.itemProperties.size();
		
		return Total;
	}
	
	private static void setPropertyNames()
	{
		APName = new String[9];
		APName[0] = "01.ID";
		APName[1] = "02.Temp Add - Night";
		APName[2] = "03.Temp Add - Shade";
		APName[3] = "04.Temp Add - Sun";
		APName[4] = "05.Temp Multiplier - Night";
		APName[5] = "06.Temp Multiplier - Shade";
		APName[6] = "07.Temp Multiplier - Sun";
		APName[7] = "08.Sanity";
		APName[8] = "09.Air";
		
		BPName = new String[12];
		BPName[0] = "01.ID";
		BPName[1] = "02.Enable Physics";
		BPName[2] = "03.MetaID";
		BPName[3] = "04.DropID";
		BPName[4] = "05.DropMetaID";
		BPName[5] = "06.DropNumber";
		BPName[6] = "07.Enable Temperature";
		BPName[7] = "08.Temperature";
		BPName[8] = "09.Air Quality";
		BPName[9] = "10.Sanity";
		BPName[10] = "11.Stability";
		BPName[11] = "12.Slides";
		
		EPName = new String[7];
		EPName[0] = "01.Entity Name";
		EPName[1] = "02.Enable EnviroTracker";
		EPName[2] = "03.Enable Dehydration";
		EPName[3] = "04.Enable BodyTemp";
		EPName[4] = "05.Enable Air Quality";
		EPName[5] = "06.Immune To Frost";
		EPName[6] = "07.Immune To Heat";
		
		IPName = new String[11];
		IPName[0] = "01.ID";
		IPName[1] = "02.Damage";
		IPName[2] = "03.Enable Ambient Temperature";
		IPName[3] = "04.Ambient Temperature";
		IPName[4] = "05.Ambient Air Quality";
		IPName[5] = "06.Ambient Santity";
		IPName[6] = "07.Effect Temperature";
		IPName[7] = "08.Effect Air Quality";
		IPName[8] = "09.Effect Sanity";
		IPName[9] = "10.Effect Hydration";
		IPName[10] = "11.Effect Temperature Cap";
		
		SPName = new String[6];
		SPName[0] = "01.Enable Physics";
		SPName[1] = "02.Max Support Distance";
		SPName[2] = "03.Min Missing Blocks To Fall";
		SPName[3] = "04.Max Missing Blocks To Fall";
		SPName[4] = "05.Can Hang";
		SPName[5] = "06.Holds Others Up";
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
			EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD MAIN CONFIG!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
			return;
		} catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD MAIN CONFIG!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
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
		EM_Settings.stoneCracks = config.get(Configuration.CATEGORY_GENERAL, "Stone Cracks Before Falling", true).getBoolean(true);
		
		// Gui settings
		String GuiSetCat = "GUI Settings";
		EM_Settings.sweatParticals_actual = config.get(GuiSetCat, "Show Sweat Particales", EM_Settings.sweatParticals_default).getBoolean(true);
		EM_Settings.insaneParticals_actual = config.get(GuiSetCat, "Show Insanity Particles", EM_Settings.insaneParticals_default, "Show/Hide Particales").getBoolean(true);
		EM_Settings.useFarenheit = config.get(GuiSetCat, "Use Farenheit instead of Celsius", false, "Will display either Farenhit or Celcius on GUI").getBoolean(false);
		EM_Settings.heatBarPos_actual = config.get(GuiSetCat, "Position Heat Bat", "Bottom_Left").getString();
		EM_Settings.waterBarPos_actual = config.get(GuiSetCat, "Position Thirst Bar", "Bottom_Left").getString();
		EM_Settings.sanityBarPos_actual = config.get(GuiSetCat, "Position Sanity Bar", "Bottom_Right").getString();
		EM_Settings.oxygenBarPos_actual = config.get(GuiSetCat, "Position Air Quality Bar", "Bottom_Right", "Change position of Enviro Bars. Options: Bottom_Left, Bottom_Right, Bottom_Center_Left, Bottom_Center_Right, Top_Left, Top_Right, Top_Center").getString();
		
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
			EnviroMine.logger.log(Level.WARNING, "Error while creating config directory:\n" + Se);
		}
		
		if(!dirFlag)
		{
			EnviroMine.logger.log(Level.WARNING, "Failed to create config directory!");
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
				EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!");
				return;
			} catch(StringIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!");
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
						EnviroMine.logger.log(Level.WARNING, "Failed to load object " + CurCat);
					}
					
				}
			}
			
			config.save();
		}
	}
	
	private static void LoadItemProperty(Configuration config, String category)
	{
		
		config.addCustomCategoryComment(category, "");
		int id = 					config.get(category, IPName[0], 0).getInt(0);
		int meta = 					config.get(category, IPName[1], 0).getInt(0);
		boolean enableTemp = 		config.get(category, IPName[2], false).getBoolean(false);
		float ambTemp = (float)		config.get(category, IPName[3], 0.00).getDouble(0.00);
		float ambAir = (float)		config.get(category, IPName[4], 0.00).getDouble(0.00);
		float ambSanity = (float)	config.get(category, IPName[5], 0.00).getDouble(0.00);
		float effTemp = (float)		config.get(category, IPName[6], 0.00).getDouble(0.00);
		float effAir = (float)		config.get(category, IPName[7], 0.00).getDouble(0.00);
		float effSanity = (float)	config.get(category, IPName[8], 0.00).getDouble(0.00);
		float effHydration = (float)config.get(category, IPName[9], 0.00).getDouble(0.00);
		float effTempCap = (float)	config.get(category, IPName[10], 37.00).getDouble(37.00);
		
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
		
		int id = 					config.get(category, BPName[0], 0).getInt(0);
		boolean hasPhys = 			config.get(category, BPName[1], false).getBoolean(false);
		int metaData = 				config.get(category, BPName[2], 0).getInt(0);
		int dropID = 				config.get(category, BPName[3], 0).getInt(0);
		int dropMeta = 				config.get(category, BPName[4], 0).getInt(0);
		int dropNum = 				config.get(category, BPName[5], 0).getInt(0);
		boolean enableTemp = 		config.get(category, BPName[6], false).getBoolean(false);
		float temperature = (float)	config.get(category, BPName[7], 0.00).getDouble(0.00);
		float airQuality = (float)	config.get(category, BPName[8], 0.00).getDouble(0.00);
		float sanity = (float)		config.get(category, BPName[9], 0.00).getDouble(0.00);
		String stability = 			config.get(category, BPName[10], "loose").getString();
		boolean slides = 			config.get(category, BPName[11], false).getBoolean(false);
		
		// Get Stability Options
		int minFall = 99;
		int maxFall = 99;
		int supportDist = 5;
		boolean holdOther = false;
		boolean canHang = true;
		
		if(EM_Settings.stabilityTypes.containsKey(stability))
		{
			StabilityType stabType = EM_Settings.stabilityTypes.get(stability);
			
			minFall = stabType.minFall;
			maxFall = stabType.maxFall;
			supportDist = stabType.supportDist;
			hasPhys = stabType.enablePhysics;
			holdOther = stabType.holdOther;
			canHang = stabType.canHang;
		} else
		{
			EnviroMine.logger.log(Level.WARNING,"Stability type '" + stability + "' not found.");
			minFall = 99;
			maxFall = 99;
			supportDist = 9;
			hasPhys = false;
			holdOther = false;
			canHang = true;
		}
		
		BlockProperties entry = new BlockProperties(id, metaData, hasPhys, minFall, maxFall, supportDist, dropID, dropMeta, dropNum, enableTemp, temperature, airQuality, sanity, holdOther, slides, canHang);
		
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
		int id = 					config.get(catagory, APName[0], 0).getInt(0);
		float nightTemp = (float)	config.get(catagory, APName[1], 0.00).getDouble(0.00);
		float shadeTemp = (float)	config.get(catagory, APName[2], 0.00).getDouble(0.00);
		float sunTemp = (float)		config.get(catagory, APName[3], 0.00).getDouble(0.00);
		float nightMult = (float)	config.get(catagory, APName[4], 1.00).getDouble(1.00);
		float shadeMult = (float)	config.get(catagory, APName[5], 1.00).getDouble(1.00);
		float sunMult = (float)		config.get(catagory, APName[6], 1.00).getDouble(1.00);
		float sanity = (float)		config.get(catagory, APName[7], 0.00).getDouble(0.00);
		float air = (float)			config.get(catagory, APName[8], 0.00).getDouble(0.00);
		
		ArmorProperties entry = new ArmorProperties(id, nightTemp, shadeTemp, sunTemp, nightMult, shadeMult, sunMult, sanity, air);
		EM_Settings.armorProperties.put(id, entry);
	}
	
	private static void LoadLivingProperty(Configuration config, String catagory)
	{
		config.addCustomCategoryComment(catagory, "");
		String name = 			config.get(catagory, EPName[0], "").getString();
		Boolean track = 		config.get(catagory, EPName[1], true).getBoolean(true);
		Boolean dehydration = 	config.get(catagory, EPName[2], true).getBoolean(true);
		Boolean bodyTemp = 		config.get(catagory, EPName[3], true).getBoolean(true);
		Boolean airQ = 			config.get(catagory, EPName[4], true).getBoolean(true);
		Boolean immuneToFrost = config.get(catagory, EPName[5], false).getBoolean(false);
		Boolean immuneToHeat = 	config.get(catagory, EPName[6], false).getBoolean(false);
		
		EntityProperties entry = new EntityProperties(name, track, dehydration, bodyTemp, airQ, immuneToFrost, immuneToHeat);
		EM_Settings.livingProperties.put(name.toLowerCase(), entry);
	}
	
	// RIGHT NOW I AM JUST LOADING DEFAULT ARMOR INTO HASH MAPS
	// IF SOME CUSTOMIZED ARMOR>> THAN IT OVERIDES THIS FUNCTION
	public static void loadDefaultArmorProperties()
	{
		File customFile = new File(customPath + "Defaults.dat");
		
		Configuration custom;
		try	
		{
			custom = new Configuration(customFile);
		} 
		catch(NullPointerException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD DEFAULTS!");
			return;
		} 
		catch(StringIndexOutOfBoundsException e)	
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD DEFAULTS!");
			return;
		}
		EnviroMine.logger.log(Level.INFO, "Loading Default Config: " + customFile.getAbsolutePath());
		
		custom.load();
		
		// Load Default Categories
		custom.addCustomCategoryComment(armorCat, "Custom armor properties");
		custom.addCustomCategoryComment(blockCat, "Custom block properties");
		custom.addCustomCategoryComment(entityCat, "Custom entity properties");
		custom.addCustomCategoryComment(itemsCat, "Custom item properties");
		
		ArmorDefaultSave(custom, armorCat + ".helmetLeather", 	ItemArmor.helmetLeather.itemID, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".plateLeather", 	ItemArmor.plateLeather.itemID, 	1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".legsLeather", 	ItemArmor.legsLeather.itemID, 	1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".bootsLeather", 	ItemArmor.bootsLeather.itemID, 	1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0);
		
		ArmorDefaultSave(custom, armorCat + ".helmetIron", 		ItemArmor.helmetIron.itemID, 	-0.5, 0.0, 2.5, 1.0, 1.0, 1.1, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".plateIron", 		ItemArmor.plateIron.itemID, 	-0.5, 0.0, 2.5, 1.0, 1.0, 1.1, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".legsIron", 		ItemArmor.legsIron.itemID, 		-0.5, 0.0, 2.5, 1.0, 1.0, 1.1, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".bootsIron", 		ItemArmor.bootsIron.itemID, 	-0.5, 0.0, 2.5, 1.0, 1.0, 1.1, 0.0, 0.0);
		
		ArmorDefaultSave(custom, armorCat + ".helmetGold", 		ItemArmor.helmetGold.itemID, 	0.0, 0.0, 0.0, 1.0, 1.0, 1.2, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".plateGold", 		ItemArmor.plateGold.itemID, 	0.0, 0.0, 0.0, 1.0, 1.0, 1.2, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".legsGold", 		ItemArmor.legsGold.itemID, 		0.0, 0.0, 0.0, 1.0, 1.0, 1.2, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".bootsGold", 		ItemArmor.bootsGold.itemID, 	0.0, 0.0, 0.0, 1.0, 1.0, 1.2, 0.0, 0.0);
		
		ArmorDefaultSave(custom, armorCat + ".helmetDiamond", 	ItemArmor.helmetDiamond.itemID, 0.0, 0.0, 0.0, 1.1, 1.0, 0.9, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".plateDiamond", 	ItemArmor.plateDiamond.itemID, 	0.0, 0.0, 0.0, 1.1, 1.0, 0.9, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".legsDiamond", 	ItemArmor.legsDiamond.itemID, 	0.0, 0.0, 0.0, 1.1, 1.0, 0.9, 0.0, 0.0);
		ArmorDefaultSave(custom, armorCat + ".bootsDiamond", 	ItemArmor.bootsDiamond.itemID, 	0.0, 0.0, 0.0, 1.1, 1.0, 0.9, 0.0, 0.0);
		
		ItemDefaultSave(custom, itemsCat + ".potions", 		Item.potion.itemID, 		-1, false, 0.0, 0.0, 0.0, -0.05, 0.0, 0.0, 25.0, 37.05);
		ItemDefaultSave(custom, itemsCat + ".melon", 		Item.melon.itemID, 			-1, false, 0.0, 0.0, 0.0, -0.01, 0.0, 0.0, 5.0, 37.01);
		ItemDefaultSave(custom, itemsCat + ".carrot", 		Item.carrot.itemID, 		-1, false, 0.0, 0.0, 0.0, -0.01, 0.0, 0.0, 5.0, 37.01);
		ItemDefaultSave(custom, itemsCat + ".goldCarrot", 	Item.goldenCarrot.itemID, 	-1, false, 0.0, 0.0, 0.0, -0.01, 0.0, 0.0, 5.0, 37.01);
		ItemDefaultSave(custom, itemsCat + ".redApple", 	Item.appleRed.itemID, 		-1, false, 0.0, 0.0, 0.0, -0.01, 0.0, 0.0, 5.0, 37.01);
		
		ItemDefaultSave(custom, itemsCat + ".bucketLava", 	Item.bucketLava.itemID, 	-1, true, 100.0, -0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".redFlower", 	Block.plantRed.blockID, 	-1, true, 0.0, 0.1, 0.1, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".yellowFlower", Block.plantYellow.blockID, 	-1, true, 0.0, 0.1, 0.1, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".leaves", 		Block.leaves.blockID, 		-1, true, 0.0, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".snowBlock", 	Block.blockSnow.blockID, 	-1, true, -0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".ice", 			Block.ice.blockID, 			-1, true, -0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".snowLayer",	Block.snow.blockID, 		-1, true, -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".netherrack", 	Block.netherrack.blockID, 	-1, true, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0);
		ItemDefaultSave(custom, itemsCat + ".soulSand", 	Block.slowSand.blockID, 	-1, true, 0.0, 0.0, -0.5, 0.0, 0.0, 0.0, 0.0, 37.0);
		
		custom.save();	
	}
	
	private static void ArmorDefaultSave(Configuration config, String catName, int id, double nightTemp, double shadeTemp, double sunTemp, double nightMult, double shadeMult, double sunMult, double sanity, double air)
	{
		config.get(catName, APName[0], id).getInt(id);
		config.get(catName, APName[1], nightTemp).getDouble(nightTemp);
		config.get(catName, APName[2], shadeTemp).getDouble(shadeTemp);
		config.get(catName, APName[3], sunTemp).getDouble(sunTemp);
		config.get(catName, APName[4], nightMult).getDouble(nightMult);
		config.get(catName, APName[5], shadeMult).getDouble(shadeMult);
		config.get(catName, APName[6], sunMult).getDouble(sunMult);
		config.get(catName, APName[7], sanity).getDouble(sanity);
		config.get(catName, APName[8], air).getDouble(air);
	}
	
	private static void ItemDefaultSave(Configuration config, String catName, int id, int meta, boolean enableAmbTemp, double ambTemp, double ambAir, double ambSanity, double effTemp, double effAir, double effSanity, double effHydration, double tempCap)
	{
		config.get(catName, IPName[0], id).getInt(id);
		config.get(catName, IPName[1], meta).getInt(meta);
		config.get(catName, IPName[2], enableAmbTemp).getBoolean(enableAmbTemp);
		config.get(catName, IPName[3], ambTemp).getDouble(ambTemp);
		config.get(catName, IPName[4], ambAir).getDouble(ambAir);
		config.get(catName, IPName[5], ambSanity).getDouble(ambSanity);
		config.get(catName, IPName[6], effTemp).getDouble(effTemp);
		config.get(catName, IPName[7], effTemp).getDouble(effTemp);
		config.get(catName, IPName[8], effSanity).getDouble(effSanity);
		config.get(catName, IPName[9], effHydration).getDouble(effHydration);
		config.get(catName, IPName[10], tempCap).getDouble(tempCap);
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
			EnviroMine.logger.log(Level.WARNING, "FAILED TO SAVE NEW OBJECT TO MYCUSTOM.DAT");
			return "Failed to Open MyCustom.dat";
		} catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.WARNING, "FAILED TO SAVE NEW OBJECT TO MYCUSTOM.DAT");
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
				config.get(nameULCat, BPName[0], (Integer)data[0]).getInt(0);
				config.get(nameULCat, BPName[1], true).getBoolean(true);
				config.get(nameULCat, BPName[2], (Integer)data[1]).getInt(0);
				config.get(nameULCat, BPName[3], (Integer)data[0]).getInt(0);
				config.get(nameULCat, BPName[4], (Integer)data[1]).getInt(0);
				config.get(nameULCat, BPName[5], 0).getInt(0);
				config.get(nameULCat, BPName[6], false).getBoolean(false);
				config.get(nameULCat, BPName[7], 0.00).getDouble(0.00);
				config.get(nameULCat, BPName[8], 0.00).getDouble(0.00);
				config.get(nameULCat, BPName[9], 0.00).getDouble(0.00);
				config.get(nameULCat, BPName[10], "loose").getString();
				config.get(nameULCat, BPName[11], false).getBoolean(false);
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
				config.get(nameEntityCat, EPName[0], name).getString();
				config.get(nameEntityCat, EPName[1], true).getBoolean(true);
				config.get(nameEntityCat, EPName[2], true).getBoolean(true);
				config.get(nameEntityCat, EPName[3], true).getBoolean(true);
				config.get(nameEntityCat, EPName[4], true).getBoolean(true);
				config.get(nameEntityCat, EPName[5], false).getBoolean(false);
				config.get(nameEntityCat, EPName[6], false).getBoolean(false);
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
				config.get(nameItemCat, IPName[0], (Integer)data[0]).getInt(0);
				config.get(nameItemCat, IPName[1], (Integer)data[1]).getInt(0);
				config.get(nameItemCat, IPName[2], false).getBoolean(false);
				config.get(nameItemCat, IPName[3], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[4], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[5], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[6], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[7], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[8], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[9], 0.00).getDouble(0.00);
				config.get(nameItemCat, IPName[10], 37.00).getDouble(37.00);
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
				config.get(nameArmorCat, APName[0], (Integer)data[0]).getInt(0);
				config.get(nameArmorCat, APName[1], 0.00).getDouble(0.00);
				config.get(nameArmorCat, APName[2], 0.00).getDouble(0.00);
				config.get(nameArmorCat, APName[3], 0.00).getDouble(0.00);
				config.get(nameArmorCat, APName[4], 1.00).getDouble(1.00);
				config.get(nameArmorCat, APName[5], 1.00).getDouble(1.00);
				config.get(nameArmorCat, APName[6], 1.00).getDouble(1.00);
				config.get(nameArmorCat, APName[7], 0.00).getDouble(0.00);
				config.get(nameArmorCat, APName[8], 0.00).getDouble(0.00);
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
			EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD MAIN CONFIG!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
			return;
		} catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			EnviroMine.logger.log(Level.WARNING, "FAILED TO LOAD MAIN CONFIG!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
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
			
			boolean physEnable = 	config.get(currentCat, SPName[0], true).getBoolean(true);
			int supportDist = 		config.get(currentCat, SPName[1], 0).getInt(0);
			int minFall = 			config.get(currentCat, SPName[2], -1).getInt(-1);
			int maxFall = 			config.get(currentCat, SPName[3], -1).getInt(-1);
			boolean canHang = 		config.get(currentCat, SPName[4], false).getBoolean(false);
			boolean holdOther = 	config.get(currentCat, SPName[5], false).getBoolean(false);
			
			EM_Settings.stabilityTypes.put(currentCat, new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		}
		
		config.save();
	}
	
	public static void loadDefaultStabilityTypes(Configuration config)
	{
		boolean physEnable = 	config.get("sand-like", SPName[0], true).getBoolean(true);
		int supportDist = 		config.get("sand-like", SPName[1], 0).getInt(0);
		int minFall = 			config.get("sand-like", SPName[2], -1).getInt(-1);
		int maxFall = 			config.get("sand-like", SPName[3], -1).getInt(-1);
		boolean canHang = 		config.get("sand-like", SPName[4], false).getBoolean(false);
		boolean holdOther = 	config.get("sand-like", SPName[5], false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("sand-like", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		
		physEnable = 	config.get("loose", SPName[0], true).getBoolean(true);
		supportDist = 	config.get("loose", SPName[1], 1).getInt(1);
		minFall = 		config.get("loose", SPName[2], 10).getInt(10);
		maxFall = 		config.get("loose", SPName[3], 15).getInt(15);
		canHang = 		config.get("loose", SPName[4], false).getBoolean(false);
		holdOther = 	config.get("loose", SPName[5], false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("loose", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		
		physEnable = 	config.get("average", SPName[0], true).getBoolean(true);
		supportDist = 	config.get("average", SPName[1], 2).getInt(2);
		minFall = 		config.get("average", SPName[2], 15).getInt(15);
		maxFall = 		config.get("average", SPName[3], 22).getInt(22);
		canHang = 		config.get("average", SPName[4], false).getBoolean(false);
		holdOther = 	config.get("average", SPName[5], false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("average", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
		
		physEnable = 	config.get("strong", SPName[0], true).getBoolean(true);
		supportDist = 	config.get("strong", SPName[1], 3).getInt(3);
		minFall = 		config.get("strong", SPName[2], 22).getInt(22);
		maxFall = 		config.get("strong", SPName[3], 25).getInt(25);
		canHang = 		config.get("strong", SPName[4], true).getBoolean(true);
		holdOther = 	config.get("strong", SPName[5], false).getBoolean(false);
		
		EM_Settings.stabilityTypes.put("strong", new StabilityType(physEnable, supportDist, minFall, maxFall, canHang, holdOther));
	}
} // End of Page
