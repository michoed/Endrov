/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

import endrov.basicWindow.WSTransformer;
import endrov.data.*;
import endrov.ev.SimpleObserver;
import endrov.imageWindow.*;
import endrov.roi.primitive.BoxROI;
import endrov.roi.primitive.EllipseROI;
import endrov.util.EvDecimal;

/**
 * Render ROI in Image Window
 * 
 * @author Johan Henriksson
 */
public class ImageRendererROI implements ImageWindowRenderer
	{
	public static final int HANDLESIZE=3;

	private final ImageWindow w;
	public Map<ROI, Map<String,ROI.Handle>> handleList=new HashMap<ROI, Map<String,ROI.Handle>>();
	
	SimpleObserver.Listener listenSelelection=new SimpleObserver.Listener()
		{public void observerEvent(Object src){w.updateImagePanel();}};
	
	public ROI drawROI=null;
		
	public ImageRendererROI(final ImageWindow w)
		{
		this.w=w;
		
		ROI.selectionChanged.addWeakListener(listenSelelection);
		ROI.roiParamChanged.addWeakListener(listenSelelection);
		ROI.roiStructChanged.addWeakListener(listenSelelection);
		}


	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		EvDecimal frame=w.frameControl.getFrame();
		EvDecimal z=w.frameControl.getZ();
		String channel=w.getCurrentChannelName();
		
		handleList.clear();
		for(EvObject ob:w.getRootObject().metaObject.values())
			if(ob instanceof ROI)
				drawROI(w, g, (ROI)ob, frame, z, channel);
		if(drawROI!=null)
			drawROI(w, g, drawROI, frame, z, channel);
		}
	
	
	public void dataChangedEvent()
		{
		}


	
	
	private void drawROI(WSTransformer w, Graphics g, ROI roiUncast,
			EvDecimal frame, EvDecimal z, String channel)
		{
		if(roiUncast.imageInRange(channel, frame, z))
			{
			if(ROI.isSelected(roiUncast))
				g.setColor(Color.MAGENTA);
			else
				g.setColor(Color.WHITE);
			if(roiUncast instanceof BoxROI)
				{
				BoxROI roi=(BoxROI)roiUncast;
				double x1=-1000,y1=-1000,x2=1000,y2=1000;
				if(!roi.regionX.all)
					{
					x1=roi.regionX.start.doubleValue();
					x2=roi.regionX.end.doubleValue();
					}
				if(!roi.regionY.all)
					{
					y1=roi.regionY.start.doubleValue();
					y2=roi.regionY.end.doubleValue();
					}
				Vector2d ul=w.transformW2S(new Vector2d(x1,y1));
				Vector2d ll=w.transformW2S(new Vector2d(x1,y2));
				Vector2d ur=w.transformW2S(new Vector2d(x2,y1));
				Vector2d lr=w.transformW2S(new Vector2d(x2,y2));
				
				g.drawLine((int)ul.x, (int)ul.y, (int)ll.x, (int)ll.y);
				g.drawLine((int)ur.x, (int)ur.y, (int)lr.x, (int)lr.y);
				g.drawLine((int)ul.x, (int)ul.y, (int)ur.x, (int)ur.y);
				g.drawLine((int)ll.x, (int)ll.y, (int)lr.x, (int)lr.y);
				}
			else if(roiUncast instanceof EllipseROI)
				{
				EllipseROI roi=(EllipseROI)roiUncast;
				
				Vector2d ul=w.transformW2S(new Vector2d(roi.regionX.start.doubleValue(),roi.regionY.start.doubleValue()));
				Vector2d lr=w.transformW2S(new Vector2d(roi.regionX.end.doubleValue(),roi.regionY.end.doubleValue()));
				
				g.drawOval((int)ul.x, (int)ul.y, (int)(lr.x-ul.x), (int)(lr.y-ul.y));
				}
			else if(roiUncast instanceof CompoundROI)
				{
				for(ROI subroi:((CompoundROI)roiUncast).getSubRoi())
					drawROI(w, g,subroi, frame, z, channel);
				}
			}
		
		
		
		//Draw handles
		HashMap<String,ROI.Handle> roimap=new HashMap<String,ROI.Handle>();
		handleList.put(roiUncast,roimap);
		for(ROI.Handle h:roiUncast.getHandles())
			{
			roimap.put(h.getID(),h);
			Vector2d xy=w.transformW2S(new Vector2d(h.getX(), h.getY()));
			g.setColor(Color.CYAN);
			g.drawRect((int)xy.x-HANDLESIZE, (int)xy.y-HANDLESIZE, HANDLESIZE*2, HANDLESIZE*2);
			}
		
		
		}

	
	
	
	}
