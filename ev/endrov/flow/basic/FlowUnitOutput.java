package endrov.flow.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import endrov.flow.FlowUnit;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: output variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitOutput extends FlowUnit
	{
	
	public String varName="channel";
	public FlowUnit varUnit;
	
	
	
	public Dimension getBoundingBox()
		{
		int w=fm.stringWidth("Out: "+varName);
		Dimension d=new Dimension(w+15,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		Dimension d=getBoundingBox();

//		g.drawRect(x,y,d.width,d.height);
		
		int arcsize=8;
		
		
		g.setColor(Color.lightGray);
		g.fillRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(Color.black);
		g.drawRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		
		g.drawString("Out: "+varName, x+5, y+fonta);
		
		
		panel.drawConnPointLeft(g,this, "in",x,y+d.height/2);
		
		}

	public boolean mouseHoverMoveRegion(int x, int y)
		{
		Dimension dim=getBoundingBox();
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	
	}