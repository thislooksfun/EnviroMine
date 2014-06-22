package enviromine.handlers;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import enviromine.core.EM_Settings;

public class SoundHandler
{
	@ForgeSubscribe
	public void onSound(SoundLoadEvent event)
	{
		// You add them the same way as you add blocks.
		
		event.manager.addSound("enviromine:gasmask.ogg");
		
		event.manager.addSound("enviromine:gruedistant.ogg");
		event.manager.addSound("enviromine:gruekill.ogg");
		
		event.manager.addSound("enviromine:CaveIn.ogg");
		event.manager.addSound("enviromine:sizzle.ogg");
		
		//Random Breathing
		event.manager.addSound("enviromine:breathing1.ogg");
		event.manager.addSound("enviromine:breathing2.ogg");
		event.manager.addSound("enviromine:breathing3.ogg");
	}
}
