/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * A * B
 * @author Johan Henriksson
 *
 */
public class EvOpImageMulImage extends EvOpSlice1
	{
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return EvOpImageMulImage.apply(p[0], p[1]);
		}

	/**
	 * Add images. Assumes same size and position
	 */
	public static EvPixels apply(EvPixels a, EvPixels b)
		{
		if(a.getType()==EvPixelsType.INT && b.getType()==EvPixelsType.INT)
			{
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.INT);
			b=b.getReadOnly(EvPixelsType.INT);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			int[] aPixels=a.getArrayInt();
			int[] bPixels=b.getArrayInt();
			int[] outPixels=out.getArrayInt();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=aPixels[i]*bPixels[i];
			
			return out;
			}
		else
			{
			//Should use the common higher type here
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			b=b.getReadOnly(EvPixelsType.DOUBLE);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
			double[] aPixels=a.getArrayDouble();
			double[] bPixels=b.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				outPixels[i]=aPixels[i]*bPixels[i];
			
			return out;
			}
		}
	}