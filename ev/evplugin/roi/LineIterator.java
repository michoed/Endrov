package evplugin.roi;

//or should one at least supply scanlines? much less overhead. channel,frame,z,y -> [x]
public abstract class LineIterator
	{
	public int frame;
	public int startX, endX;
	public int y,z;
	public String channel;
	
	public abstract boolean next();
	
	
	}
