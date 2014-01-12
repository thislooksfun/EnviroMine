package enviromine.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EM_ConfigHandler 
{
	
	/*This File is Not implemented Entirely 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public static String configPath = "config/enviromine/";
	static String dataPath = configPath +"EnviroProperties/";
	static String customPath = configPath +"CustomObjects/";

	//Set Up Hash Map For CustomProperties for Blocks
	public static HashMap<String,Object[]> BlocksCustomProperties = new HashMap<String,Object[]>();	
	
	
		public static void initConfig()
		{
			// Check for Data Directory 
			CheckDir(new File(dataPath));
			CheckDir(new File(customPath));
			
			// Haven't decided on this just yet.. 
			// CheckDir(new File(customPath+"/Armor")); // May Remove
			// CheckDir(new File(customPath+"/Blocks")); // May Remove
			// CheckDir(new File(customPath+"/Entity")); // May Remove
			// CheckDir(new File(customPath+"/Items")); // May Remove7u8
			
			// Check/Make Properties Files
			CheckFile(new File(dataPath + "BlockProperties.dat"));
			CheckFile(new File(dataPath + "ArmorProperties.dat"));
			CheckFile(new File(dataPath + "EntityProperties.dat"));
			
			// Check/Make Help File
			CheckFile(new File(configPath + "Help_File_Custom.txt"));
			
			// Loads Custom Properties for Blocks(Update for all Objects)
			LoadCustomProperties();			
			
			
			GetFileList(); // Testing Remove later
			
			// Load Main Config File And this will go though changes
			File configFile = new File(configPath + "EnviroMine.cfg");
			System.out.println("Attempting to load config: "+configFile.getAbsolutePath());
			EM_Settings.LoadConfig(configFile);
		}
		
		//#######################################
		//#          Get File List              #                 
		//#This Grabs Directory List for Custom #
		//#######################################
		public static File[] GetFileList()
		{
			
			// Will be used Auto Load Custom Objects from ??? Dir 
			File f = new File(dataPath);
			File[] list = f.listFiles();
			
			
				// Load File list into a Dynamic Array
				for(int i = 0; i < list.length; i++)
				{
					String name = list[i].getName();
					System.out.println("Attempting to Load: " + i +" - "+ name);	
				}
				
				return list;
		} // GetFileList
		
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
		
		
		//###############################
		//#           Check Files       #                 
		//#  Checks for, or makes Files #
		//###############################	
		public static void CheckFile(File file)
		{
    		try 
    		{
    		
    			//File CustomFile = 
    			
    			System.out.println("Attempting to load File "+file.getAbsolutePath());
 
    			if (file.createNewFile())
    			{
    				System.out.println("File is created!");
    			}
    			else
    			{
    				System.out.println("File already exists.");
    			}
 
    		} 
    		catch (IOException e) 
    		{
    			e.printStackTrace();
    		}	
		}// CheckFile
		
		
		//###########################
		//#      Read Files         # 
		//# Reads Files and Returns #
		//#   ArrayList of Lines    #
		//###########################	
		public static List ReadFile(String path)
		{
	        List outputArray = new ArrayList();
	        
			try (BufferedReader br = new BufferedReader(new FileReader(path))) 
		    {   
				String output;
	
		        int count = 0;

		        while ( (output = br.readLine()) != null) 
		        {

		          	String test = output.toString();
		           	outputArray.add(test);
		           // System.out.println(outputArray.get(count));

		            count++;
		        }
		        System.out.println("Number of lines: " + count);
		        
				
		    } catch (FileNotFoundException e) 
		    {
		        System.out.println("File not found");
		    } catch (IOException e) 
		    {
		        System.out.println("Unable to read file");
		    }
			return outputArray;
		        
		}
		
		
		
		
		

		//###########################
		//#  Load Custom Properties #
		//# (Loads Blocks only atm) #                              
		//#      Upgrade Later		#
		//###########################	
		public static void LoadCustomProperties()
		{
			try
			{		
				// Grab File For reading
				List File = ReadFile(dataPath + "BlockProperties.txt");
				
				// Make Iterator for lines
				Iterator data = File.iterator( ); // May be changed later due 
				
				// Variables
				String line=null;
				int i=0;
				
				while ( data.hasNext( ) ) 
				{
					// Take Iterator Data and input to Variables.
					line = data.next( ).toString(); 	//System.out.println(line); // Debugging
					String[] LineSplit= line.split(", ");
					
					if(LineSplit.length != 9)
					{
						String name = LineSplit[0].toString();					
						System.out.println("Invalid property format for custom Properties: " + name +" - "+ LineSplit.length +" - ");
						return;
					}
				
					Object[] Props = new Object[9];

					for(int j = 8; j >= 0; j--)
					{
						Props[j] = LineSplit[j];
					}


					Props[1] = Integer.valueOf((String)Props[1]); // drop num

					Props[2] = Float.valueOf((String)Props[2]); // temp
					Props[3] = Float.valueOf((String)Props[3]); // sanity
					Props[4] = Float.valueOf((String)Props[4]); // air
					
					Props[5] = Integer.valueOf((String)Props[5]); 
					Props[6] = Integer.valueOf((String)Props[6]);
					Props[7] = Integer.valueOf((String)Props[7]);

				
					if(Props[8] == "F")
					{
						Props[2] = EM_Settings.convertToCelcius((Float)Props[2]);
					}

					// Save Blocks Custom Properties
					BlocksCustomProperties.put((String)Props[0], Props);

					System.out.println("Loaded Custom Property: " + Props[0]);
					i++;

				}
				
				// DEBUGGING AND TESTING
	    		if(BlocksCustomProperties.containsKey("Rock"))
	    		{
	    			System.out.println("YOU CHOSE ROCK!");
	    		}
	    		else System.out.println("WTF!");
	    		// End Debugging
	    		
			} catch(NumberFormatException e)
				{
					System.out.println("Invalid property format");
				}
			
		}
}
