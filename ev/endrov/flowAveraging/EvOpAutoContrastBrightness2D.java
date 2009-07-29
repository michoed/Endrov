package endrov.flowAveraging;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Automatically scale image to be within limits
 * 
 * Complexity O(w*h)
 */
public class EvOpAutoContrastBrightness2D extends EvOpSlice1
	{
	private final boolean invert;
	
	public EvOpAutoContrastBrightness2D(boolean invert)
		{
		this.invert=invert;
		}
	
	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0],invert);
		}
	
	public static EvPixels apply(EvPixels in, boolean invert)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		double[] inPixels=in.getArrayDouble();

		double min=Double.MAX_VALUE;
		double max=Double.MIN_VALUE;
		for(int i=0;i<inPixels.length;i++)
			{
			double p=inPixels[i];
			if(p>max)
				max=p;
			if(p<min)
				min=p;
			}
		double sub,mul;
		if(invert)
			{
			double diff=max-min;
			mul=diff==0 ? 0 : -255/diff;
			sub=max;
			}
		else
			{
			double diff=max-min;
			mul=diff==0 ? 0 : 255/diff;
			sub=min;
			}
		
		for(int i=0;i<inPixels.length;i++)
			outPixels[i]=(inPixels[i]-sub)*mul;
		return out;
		}
	}