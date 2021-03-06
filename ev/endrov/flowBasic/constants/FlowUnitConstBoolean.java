/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.constants;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: boolean constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstBoolean extends FlowUnitConst
	{
	
	private boolean var=true;
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstBoolean.class.getResource("jhBoolean.png"));

	private static final String metaType="constBoolean";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Boolean",metaType,FlowUnitConstBoolean.class, icon,"Constant boolean");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+isVar());
		return metaType;
		}

	public void fromXML(Element e)
		{
		setVar(Boolean.parseBoolean(e.getAttributeValue("value")));
		}

	
	
	@Override
	public String getBasicShowName()
		{
		return "B";
		}

	protected FlowType getConstType()
		{
		return FlowType.TBOOLEAN;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", isVar());
		}
	
	
	public Component getGUIcomponent(final FlowView p)
		{
		final JCheckBox comp=new JCheckBox("",isVar());
		comp.setOpaque(false);
		comp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//Should maybe be change listener
				//should emit an update
				setVar(comp.isSelected());
				p.repaint();
				}});
		return comp;
		}

	public void setVar(boolean var)
		{
		this.var = var;
		}

	public boolean isVar()
		{
		return var;
		}
	
	public String getHelpArticle()
		{
		return "Misc flow operations";
		}

	}
