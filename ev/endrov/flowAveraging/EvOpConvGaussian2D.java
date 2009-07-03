package endrov.flowAveraging;

import endrov.flow.EvOpSlice1;
import endrov.flow.std.math.EvOpImageComplexMulImage;
import endrov.flowFourier.EvOpFourierComplexInverse2D;
import endrov.flowFourier.EvOpFourierRealForwardFull2D;
import endrov.flowFourier.EvOpRotateImage2D;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.imageset.EvPixels;

/**
 * Convolve by gaussian
 * 
 * Complexity, same as FourierTransform
 */
public class EvOpConvGaussian2D extends EvOpSlice1
	{
	private Number sigmaX, sigmaY;

	public EvOpConvGaussian2D(Number sigmaX, Number sigmaY)
		{
		this.sigmaX = sigmaX;
		this.sigmaY = sigmaY;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0],sigmaX.doubleValue(),sigmaY.doubleValue());
		}
	
	public static EvPixels apply(EvPixels in, double sigmaX, double sigmaY)
		{
		int w=in.getWidth();
		int h=in.getHeight();
		
		EvPixels kernel=GenerateSpecialImage.genGaussian2D(sigmaX, sigmaY, w, h);
		kernel=new EvOpRotateImage2D(null,null).exec1(kernel);
		
		EvPixels[] ckernel=new EvOpFourierRealForwardFull2D().exec(kernel);
//		EvPixels[] ckernel=new EvOpFourierComplexForward2D().exec(kernel,GenerateSpecialImage.genConstant(w, h, 0));
		EvPixels[] cin=new EvOpFourierRealForwardFull2D().exec(in);
		EvPixels[] mul=new EvOpImageComplexMulImage().exec(ckernel[0],ckernel[1],cin[0],cin[1]);
		
		return EvOpFourierComplexInverse2D.transform(mul[0], mul[1],true).fst();
		}
	}