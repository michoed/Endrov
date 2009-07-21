package endrov.flowBasic.images;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Convert pixel type to a given type
 * @author Johan Henriksson
 *
 */
public class EvOpImageConvertPixel extends EvOpSlice1
	{
	private final EvPixelsType type;
	public EvOpImageConvertPixel(EvPixelsType type)
		{
		this.type=type;
		}
	
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageConvertPixel.apply(p[0],type);
		}

	public static EvPixels apply(EvPixels a, EvPixelsType type)
		{
		return a.getReadOnly(type);
		}
	}