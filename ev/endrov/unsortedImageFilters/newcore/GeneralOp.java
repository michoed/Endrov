package endrov.unsortedImageFilters.newcore;

import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * General image processing operation. Programmers are normally not meant to implement this
 * class directly. Operations have a natural domain, single images or up to entire channels.
 * There are more convenient subclasses that work with each of these.
 * 
 * @author Johan Henriksson
 *
 */
public interface GeneralOp
	{
	public EvPixels exec(EvPixels... p);
	public EvStack exec(EvStack... p);
	public EvChannel exec(EvChannel... ch);
	}
