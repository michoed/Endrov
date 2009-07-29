package endrov.flowAveraging;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;
import endrov.util.Maybe;

/**
 * Flow unit: automatically adjust contrast brightness
 * @author Johan Henriksson
 *
 */
public class FlowUnitAutoContrastBrightness2D extends FlowUnitBasic
	{
	public static final String showName="Auto contrast/brightness 2D";
	private static final String metaType="autoCB2D";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitAutoContrastBrightness2D.class, null,
				"Scale image to be within 0-255"));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.ANYIMAGE);
		types.put("invert", FlowType.TBOOLEAN); //TODO should show default value
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage a=(AnyEvImage)flow.getInputValue(this, exec, "image");
		Maybe<Boolean> invert=flow.getInputValueMaybe(this, exec, "invert", Boolean.class);
		boolean b;
		if(invert.hasValue())
			b=invert.get();
		else
			b=false;
		
		lastOutput.put("out", new EvOpAutoContrastBrightness2D(b).exec1Untyped(a));
		}

	
	}