package endrov.flow.std.constants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: double constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstDouble extends FlowUnitConst
	{
	
	
	private double var=123;
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstDouble.class.getResource("jhNumber.png"));

	private static final String metaType="constDouble";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Double",metaType,FlowUnitConstDouble.class, icon,"Constant double"));
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+getVar());
		return metaType;
		}
	
	public void fromXML(Element e)
		{
		setVar(Double.parseDouble(e.getAttributeValue("value")));
		}

	
	protected String getLabel()
		{
		return "D";
		}

	protected FlowType getConstType()
		{
		return FlowType.TDOUBLE;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", getVar());
		}
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextField field=new JTextField(""+getVar());
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		final Color colorOk=field.getForeground();
		
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			try
				{
				field.setForeground(colorOk);
				setVar(Double.parseDouble(field.getText()));
				}
			catch (NumberFormatException e1)
				{
				field.setForeground(colorBadValue);
				setVar(0);
				}
			p.repaint();
			}
		});
		return field;
		}

	public void setVar(double var)
		{
		this.var = var;
		}

	public double getVar()
		{
		return var;
		}
	

	}
