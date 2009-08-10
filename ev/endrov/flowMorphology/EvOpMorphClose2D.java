package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Close: Erode, then dilate
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphClose2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	
	public EvOpMorphClose2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.close(p[0]);
		}
	}