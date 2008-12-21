package endrov.imagesetBasic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import endrov.imageset.EvIOImage;

/**
 * Read single-slice image into memory using JAI 
 * @author Johan Henriksson
 *
 */
public class SliceIOJAI implements EvIOImage
	{
	private File file;
	private Integer z=null;
	
	public SliceIOJAI(File f)
		{
		file=f;
		}
	public SliceIOJAI(File f, int z)
		{
		file=f;
		this.z=z;
		//TODO use z
		}
	
	
	public BufferedImage loadJavaImage()
		{
		try
			{
			return ImageIO.read(file);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			return null;
			}
		}

	
	
	}
