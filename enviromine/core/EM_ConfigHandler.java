package enviromine.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class EM_ConfigHandler 
{
	

	// Dirs for Custom Files
	public static String configPath = "config/enviromine/";
	static String dataPath = configPath +"EnviroProperties/";
	static String customPath = configPath +"CustomObjects/";
	
	// Categories for Custom Objects
	static String armorCat = "armor";
	static String blockCat = "blocks";
	static String entityCat = "entity";
	static String itemsCat = "items";
	//static String foodCat = "food";
	
	//Properties
	public static HashMap<Integer,Object[]> armorProperties = new HashMap<Integer,Object[]>();
	public static HashMap<Integer,Object[]> blockProperties = new HashMap<Integer,Object[]>();
	public static HashMap<String,Object[]> livingProperties = new HashMap<String,Object[]>();
	
	
		public static void initConfig()
		{
			// Check for Data Directory 
			CheckDir(new File(dataPath));
			CheckDir(new File(customPath));


			//CheckFile(new File(configPath + "Help_File_Custom.txt"));
				
			// Now load Files from "Custom Objects"
			File[] customFiles = GetFileList(customPath);
			for(int i = 0; i < customFiles.length; i++)
			{
				LoadCustomObjects(customFiles[i]);
			}
			
			// load defaults
			loadDefaultArmorProperties();
			
			// Load Main Config File And this will go though changes
			File configFile = new File(configPath + "EnviroMine.cfg");
			System.out.println("Attempting to load config: "+configFile.getAbsolutePath());
			EM_Settings.LoadConfig(configFile);

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

		// Load File list into a Dynamic Array

				for(int i = 0; i < list.length; i++)
				{
				    String name = list[i].getName();
					System.out.println("Attempting to Load: " + i +" - "+ name);
				}

				return list;
		} // GetFileList
		
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
			
			try 
			{
			   dirFlag = Dir.mkdirs();
			} 
			catch (SecurityException Se) 
			{
				System.out.println("Error while creating directory in Java:" + Se);
			}

				if (dirFlag)  System.out.println("Directory created successfully");
				else   System.out.println("Directory was not created successfully");
		}// CheckDir
		

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
					System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
					return;
				} catch(StringIndexOutOfBoundsException e)
				{
					e.printStackTrace();
					System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
					return;
				}
						

					config.load();
					
					// Load Default Categories
					config.addCustomCategoryComment(armorCat, "Add Custom Armor");
					config.addCustomCategoryComment(blockCat, "Add Custom Blocks");
					config.addCustomCategoryComment(entityCat, "Custom Entities");
	

					// 	Grab all Categories in File
					List catagory = new ArrayList();
					Set<String> nameList = config.getCategoryNames();
					Iterator nameListData = nameList.iterator ( );
	        
					// add Categories to a List 
					for (int x=0;nameListData.hasNext( );x++) 
					{
						catagory.add((String) nameListData.next());
					
						System.out.println("Searching Config for Found: " +catagory.get(x).toString());
					}
					
					// Now Read/Save Each Category And Add into Proper Hash Maps

					
						for(int x=0; x <= (catagory.size()-1); x++)
						{
							String CurCat = catagory.get(x).toString();
							
							if(!((String) CurCat).isEmpty() && ((String) CurCat).contains(Configuration.CATEGORY_SPLITTER))
							{	
								String[] CatSplit = CurCat.toString().split("\\"+Configuration.CATEGORY_SPLITTER);
								String parent = (String) CatSplit[0];
								String child = CatSplit[1];
	
								if(parent.equals(blockCat))
								{
									System.out.println("This is A Block:" + child);
									
									LoadBlocksToHashMap(config, catagory.get(x));
								}
								
								else if(parent.equals(armorCat))
								{
									
									LoadArmorToHashMap(config, catagory.get(x));
									System.out.println("This is A Armor:" + child);
								}
								
								else if(parent.equals(itemsCat))
								{

									System.out.println("This is A ITEM:" + child);
								}
								
								else if(parent.equals(entityCat))
								{
									LoadLivingToHashMap(config, catagory.get(x));
									System.out.println("This is A entity:" + child);
								}
								else
								{
									System.out.println("Could not load Custom Object:"+ parent + "/" + child);
								}

						
							}
						}
					
					config.save();
				}
				else
				{
					
					System.out.println(customFiles.getName() + " is not a Data Configuration File.");
				}
				
	        
		} // End of Load Custom Objects

		
		private static void LoadBlocksToHashMap(Configuration config, Object catagory)
		{
			Object blockProps[] = new Object[11];
			
			System.out.println("Loading - " + catagory);

			config.addCustomCategoryComment((String) catagory, "");
			int id = config.get((String) catagory, "1.ID", 0).getInt(0);
			int metaData = config.get((String) catagory, "2.MetaID", 0).getInt(0);
			int dropID = config.get((String) catagory, "3.DropID", 0).getInt(0);
			int dropMeta = config.get((String) catagory, "4.DropMetaID", 0).getInt(0);
			double temprature = config.get((String) catagory, "5.Temprature", 0.00).getDouble(0.00);     
			double airQuality = config.get((String) catagory, "6.AirQuality", 0.00).getDouble(0.00);
			String stability = config.get((String) catagory, "7.Stability", "loose").getString();
			
			// Change Temperatures to Floats
			float tempFloat = (float) temprature;
			float airQFloat = (float) airQuality;
			

			// Get Stability Options
		    int minFall = 10;
		    int maxFall = 15;
		    int supportDist = 1;
		    boolean hasPhys = true;
		    
		    switch (stability) {
		        case "loose":  minFall = 10; maxFall = 15; supportDist = 1;
		                break;
		        case "average":  minFall = 15; maxFall = 22; supportDist = 3;
		                break;
		        case "strong":  minFall = 22; maxFall = 25; supportDist = 5;
		                break;
		        case "off":   hasPhys = false;
                break;		           
		    }			
		    
		    // Old has map settings
		    blockProps[0] = Integer.valueOf((int) id);
		    blockProps[1] = Integer.valueOf((int) metaData);
		    blockProps[2] = Integer.valueOf((int) dropID);
		    blockProps[3] = Integer.valueOf((int) dropMeta);
		    blockProps[4] = Integer.valueOf((int) 0); // Drop number
		    blockProps[5] =  Float.valueOf((float) temprature);
		    blockProps[6] =  Float.valueOf((float) -10.0);
		    blockProps[7] =  Float.valueOf((float) airQuality);
		    blockProps[8] = Integer.valueOf((int) minFall);
		    blockProps[9] = Integer.valueOf((int) maxFall);
		    blockProps[10] = (String) "C";
		    // hasPhys blockProps[11] = hasPhys;
		    // support distance blockProps[12] = Interger.valueOf((int) supportDist);
		    
		    // Saves to old hash map
		    EM_Settings.blockProperties.put((Integer)blockProps[0], blockProps);
		    
		    //System.out.print(EM_Settings.blockProperties.get((Integer) id).toString());
		    // unused is hasPhys, support distances

		}
		
		private static void LoadArmorToHashMap(Configuration config, Object catagory)
		{

			Object[] armorProps = new Object[6];
			
			config.addCustomCategoryComment((String) catagory, "");
			int id = config.get((String) catagory, "1.ID", 0).getInt(0);
			double nightTemp = config.get((String) catagory, "2.nightTemp", 0.00).getDouble(0);
			double shadeTemp = config.get((String) catagory, "3.shadeTemp", 0.00).getDouble(0);
			double sunTemp = config.get((String) catagory, "4.sunTemp", -1.00).getDouble(0);
			double nightMult = config.get((String) catagory, "5.nightMult", 1).getDouble(1);     
			double shadeMult = config.get((String) catagory, "6.shadeMult", 1).getDouble(1);
			double sunMult = config.get((String) catagory, "7.sunMult", 1).getDouble(1);
			
					//System.out.println("File:"+ customFiles.getName() +" id = "+ id + "- Type = "+ type + " - Block Options are " + blockOptions);
					
					// SAVE INTO HASH MAPS WITH A CASE:
			float nightFloat = (float) nightTemp;
			float shadeFloat = (float) shadeTemp;
			float sunFloat = (float) sunTemp;		
					
			armorProps[0] = Integer.valueOf((int) id);
			armorProps[1] = Float.valueOf((float) nightFloat);
			armorProps[2] = Float.valueOf((float) shadeFloat);
			armorProps[3] = Float.valueOf((float) sunFloat);
			armorProps[4] = Float.valueOf((float) sunMult); // Drop number
			armorProps[5] = (String) "C";
			// shade multi
			// sunMulti
			
			
			
			EM_Settings.armorProperties.put((Integer)armorProps[0], armorProps);
		
		}
		
		private static void LoadLivingToHashMap(Configuration config, Object catagory)
		{
			
			Object[] entityProps = new Object[4];
			
			config.addCustomCategoryComment((String) catagory, "");
			String name = config.get((String) catagory, "1.Entity Name", "").toString();
			Boolean dehydration = config.get((String) catagory, "2.Enable Dehydration", true).getBoolean(true);
			Boolean bodyTemp = config.get((String) catagory, "3.Enable BodyTemp", true).getBoolean(true);
			Boolean airQ = config.get((String) catagory, "4.Enable Air Quility", true).getBoolean(true);
			Boolean immuneToFrost = config.get((String) catagory, "5.ImmuneToFrost", false).getBoolean(false); 
			
			entityProps[0] = (String) name;
			entityProps[1] = (Boolean) dehydration;
			entityProps[2] = (Boolean) bodyTemp;
			entityProps[3] = (Boolean) airQ;
			//entityProps[4] = (Boolean) immuneToFrost;
			
			EM_Settings.livingProperties.put((String)entityProps[0], entityProps);
			
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
				{e.printStackTrace();System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");	return;	} 
			catch(StringIndexOutOfBoundsException e)	
				{	e.printStackTrace();System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");	return;	}System.out.println("Loading EnviroMine Config: " + customFile.getAbsolutePath());
			custom.load();

			// Load Default Categories
			custom.addCustomCategoryComment(armorCat, "Add Custom Armor");
			custom.addCustomCategoryComment(blockCat, "Add Custom Blocks");
			custom.addCustomCategoryComment(entityCat, "Custom Entities");		
*/
			
			
			Boolean helmetLeather = ArmorDefaultSave( "Leather Helmet",ItemArmor.helmetLeather.itemID, 2.0, 2.0,1.0,1);
			Boolean plateLeather = ArmorDefaultSave("Leather Helmet",ItemArmor.plateLeather.itemID, 2.0, 2.0,1.0,1);
			Boolean legsLeather = ArmorDefaultSave( "Leather Helmet",ItemArmor.legsLeather.itemID, 2.0, 2.0,1.0,1);
			Boolean bootsLeather = ArmorDefaultSave( "Leather Helmet",ItemArmor.bootsLeather.itemID, 2.0, 2.0,1.0,1);
			
			Boolean helmetIron = ArmorDefaultSave( "Leather Helmet",ItemArmor.helmetIron.itemID, -1.0, 0.0, 5.0, 1.1);
			Boolean plateIron = ArmorDefaultSave( "Leather Helmet",ItemArmor.plateIron.itemID,-1.0, 0.0, 5.0, 1.1);
			Boolean legsIron = ArmorDefaultSave( "Leather Helmet",ItemArmor.legsIron.itemID, -1.0, 0.0, 5.0, 1.1);
			Boolean bootsIron = ArmorDefaultSave( "Leather Helmet",ItemArmor.bootsIron.itemID, -1.0, 0.0, 5.0, 1.1);
			
			Boolean helmetGold = ArmorDefaultSave( "Leather Helmet",ItemArmor.helmetGold.itemID, 0.0, 0.0, 3.0, 1.1);
			Boolean plateGold = ArmorDefaultSave("Leather Helmet",ItemArmor.plateGold.itemID,0.0, 0.0, 3.0, 1.1);
			Boolean legsGold = ArmorDefaultSave("Leather Helmet",ItemArmor.legsGold.itemID, 0.0, 0.0, 3.0, 1.1);
			Boolean bootsGold = ArmorDefaultSave("Leather Helmet",ItemArmor.bootsGold.itemID, 0.0, 0.0, 3.0, 1.1);
			
			Boolean helmetDiamond = ArmorDefaultSave( "Leather Helmet,", ItemArmor.helmetDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
			Boolean plateDiamond = ArmorDefaultSave( "Leather Helmet",ItemArmor.plateDiamond.itemID,0.0, 0.0, 3.0, 1.1);
			Boolean legsDiamond = ArmorDefaultSave( "Leather Helmet",ItemArmor.legsDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
			Boolean bootsDiamond = ArmorDefaultSave( "Leather Helmet",ItemArmor.bootsDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
			
			//custom.save();	
		}

		private static Boolean ArmorDefaultSave( String name, int id, double nightFloat, double shadeFloat, double sunFloat, double sunMult)
		{
			Object[] armorProps = new Object[6];
			
			armorProps[0] = Integer.valueOf((int) id);
			armorProps[1] = Float.valueOf((float) nightFloat);
			armorProps[2] = Float.valueOf((float) shadeFloat);
			armorProps[3] = Float.valueOf((float) sunFloat);
			armorProps[4] = Float.valueOf((float) sunMult); // Drop number
			armorProps[5] = (String) "C";

			
			if(!EM_Settings.armorProperties.containsKey(id)) EM_Settings.armorProperties.put((Integer)armorProps[0], armorProps);
			else System.out.println("This Armor was already loaded in Customs:" + id);
			return true;
		}
		
		
		//Currently not used
		private static float convertToFarenheit(float num)
		{
			return ((num *(9/5))+32F);
		}
		
		private static float convertToCelcius(float num)
		{
			return ((num-32F) * (5/9));
		}
		
} // End of Page
