package enviromine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class EM_GuiMenu extends GuiScreen
{

	public EM_GuiMenu()
	{
		System.out.println("tester");
	}
	
	public void initGui()
	{
	    this.buttonList.add(new GuiButton(101, this.width / 2 - 75, this.height / 6 + 74 - 6, 150, 20, "Gui Options"));
        this.buttonList.add(new GuiButton(100, this.width / 2 - 75, this.height / 6 + 98 - 6, 150, 20, "Config Settings"));
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.getString("gui.done")));
   
	}
	
	public boolean doesGuiPauseGame()
	{
		return true;
	}
	
    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.id == 100)
        {
        	this.mc.displayGuiScreen(new EM_GuiGeneral(this));	
        }
        else if (par1GuiButton.id == 101)
        {
        	this.mc.displayGuiScreen(new EM_GuiSettings(this));	
        }
        else if (par1GuiButton.id == 200)
        {
            this.mc.displayGuiScreen(null);
        }
    }

    public void drawScreen(int par1, int par2, float par3)
    {
    	this.drawDefaultBackground();
    	this.drawCenteredString(this.fontRenderer,"Enviromine Menu", this.width / 2, 15, 16777215);
    	super.drawScreen(par1, par2, par3);
    }
}
