package enviromine.core.commands;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.core.EM_Settings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;

public class CommandPhysics extends CommandBase
{

	@Override
	public String getCommandName() 
	{
		// TODO Auto-generated method stub
		return "togglephysic";
	}

    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
	@Override
	public String getCommandUsage(ICommandSender icommandsender) 
	{
		// TODO Auto-generated method stub
		return "Enviromine: /togglephysic";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) 
	{

		this.togglePhy();
	}

	public void togglePhy()
	{

		EM_Settings.enablePhysics = !EM_Settings.enablePhysics;
	}

}
