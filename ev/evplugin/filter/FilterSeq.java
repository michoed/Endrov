package evplugin.filter;

import org.jdom.*;

import java.util.*;
import javax.swing.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.*;
import evplugin.ev.SimpleObserver;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.roi.*;

//Filter = not such a good name? ImageOperators?


/**
 * Filter Sequence - A list of filters to be applied sequentially
 * 
 * @author Johan Henriksson
 */
public class FilterSeq extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="filterseq";
	public static TreeMap<String,FilterInfo> filterInfo=new TreeMap<String,FilterInfo>();
	
	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,new FilterSeqObjectType());
		}

	
	private static ImageIcon iconLabelFS=new ImageIcon(FilterSeq.class.getResource("labelFS.png"));
	public static ImageIcon getIconFilterSeq(){return iconLabelFS;}
	
	public String getMetaTypeDesc()
		{
		return "Filter Sequence";
		}


	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	public static void addFilter(FilterInfo fi)
		{
		FilterSeq.filterInfo.put(fi.getName(),fi);
		}
	
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public static class FilterSeqObjectType implements EvObjectType
		{
		public EvObject extractObjects(Element e)
			{
			FilterSeq seq=new FilterSeq();
			for(Object ec:e.getChildren())
				{
				EvObject o=Filter.extractFilterXML((Element)ec);
				seq.sequence.add((Filter)o);
				}
			return seq;
			}
		}
	
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		for(Filter f:sequence)
			{
			Element e2=new Element("filter");
			f.saveMetadata(e2);
			e.addContent(e2);
			}
		}
	
	/******************************************************************************************************
	 *                               Filter Sequence                                                      *
	 *****************************************************************************************************/

	
	private Vector<Filter> sequence=new Vector<Filter>();

	public final SimpleObserver observer=new SimpleObserver();
	
	//What about copying to a different target?
	
	//updating of window not done here, and should not be done here?
	
	//for entire channel etc, make a temporary roi. right now, might not be the way later
	
	private SimpleObserver.Listener listener=new SimpleObserver.Listener()
		{public void observerEvent(Object o){observer.emit(o);}};
	
	public FilterSeq()
		{
		}
	
	public FilterSeq(Filter[] fl)
		{
		for(Filter f:fl)
			{
			sequence.add(f);
			f.observer.addWeakListener(listener);
			}
		}

	/**
	 * Append a filter
	 */
	public void addFilter(Filter f)
		{
		sequence.add(f);
		f.observer.addWeakListener(listener);
		observer.emit(this);
		}
	/**
	 * Append all filters from a filter sequence
	 */
	public void addFilter(FilterSeq fs)
		{
		for(Filter f:fs.sequence)
			{
			sequence.add(f);
			f.observer.addWeakListener(listener);
			}
		observer.emit(this);
		}
	/**
	 * Move filter at position i up (=earlier in chain)
	 * @param i
	 */
	public void moveUp(int i)
		{
		if(i>0)
			{
			Filter from=sequence.get(i);
			sequence.remove(i);
			sequence.add(i-1, from);
			observer.emit(this);
			}
		}
	
	/**
	 * Move filter at position i down (=later in chain)
	 */
	public void moveDown(int i)
		{
		if(i<sequence.size()-1)
			{
			Filter from=sequence.get(i);
			sequence.remove(i);
			sequence.add(i+1, from);
			observer.emit(this);
			}
		}
	
	/**
	 * Delete filter at position i
	 */
	public void delete(int i)
		{
		if(i>=0 && i<sequence.size())
			{
			sequence.get(i).observer.remove(listener);
			sequence.remove(i);
			observer.emit(this);
			}
		}
	
	/**
	 * Get number of filters
	 */
	public int getNumFilters()
		{
		return sequence.size();
		}
	
	/**
	 * Get filter at position i 
	 */
	public Filter getFilter(int i)
		{
		return sequence.get(i);
		}
	
	
	/**
	 * Check if this filter does nothing
	 */
	public boolean isIdentity()
		{
		return sequence.isEmpty();
		}
	
	
	/////////////////////////////// Apply //////////////////////////////
	/////////////////////////////// Apply //////////////////////////////
	/////////////////////////////// Apply //////////////////////////////
	
	/**
	 * Apply over a ROI
	 */
	public void apply(Imageset rec, ROI roi)
		{
		for(String chan:roi.getChannels(rec))
			for(int frame:roi.getFrames(rec, chan))
				for(int z:roi.getSlice(rec, chan, frame))
					{
					System.out.println("- "+chan+"/"+frame+"/"+z);
					EvImage evim=rec.getChannel(chan).getImageLoader(frame,z);

					for(Filter fi:sequence)
						{
						FilterROI firoi=(FilterROI)fi;
						firoi.applyImage(evim, roi, chan, frame, z);
						}
					}
		}
	
	/**
	 * Apply on an entire imageset
	 */
	public void apply(Imageset rec)
		{
		for(String chan:rec.channelImages.keySet())
			{
			ChannelImages ch=rec.channelImages.get(chan);
			for(int frame:ch.imageLoader.keySet())
				{
				Map<Integer,EvImage> slices=ch.imageLoader.get(frame);
				for(int z:slices.keySet())
					{
					System.out.println("- "+chan+"/"+frame+"/"+z);
					EvImage evim=slices.get(z);

					for(Filter fi:sequence)
						{
						FilterROI firoi=(FilterROI)fi;
						firoi.applyImage(evim);
						}
					}
				}
			}
		BasicWindow.updateWindows();
		}
	
	/**
	 * Apply to a single image
	 */
	public void apply(EvImage evim)
		{
		for(Filter fi:sequence)
			{
			FilterROI firoi=(FilterROI)fi;
			firoi.applyImage(evim);
			}
		BasicWindow.updateWindows();
		}
	
	/**
	 * Apply sequence, but do not modify source; return modified image
	 */
	public EvImage applyReturnImage(EvImage evim)
		{
		evim=evim.getWritableCopy();
		for(Filter fi:sequence)
			{
			FilterROI firoi=(FilterROI)fi;
			firoi.applyImage(evim);
			}
		return evim;
		}
	}
