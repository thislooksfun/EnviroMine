package enviromine.core.sounds;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import enviromine.core.EM_Settings;

public class SlowBreathing 
{
	@ForgeSubscribe
	public void onSound(SoundLoadEvent event) 
	{
		// You add them the same way as you add blocks.
		
		event.manager.addSound("enviromine:slowbreathing.ogg");
	}
}
