package endrov.flowBasic.logic;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: or
 * @author Johan Henriksson
 *
 */
public class FlowUnitOr extends FlowUnitLogicBinop
	{
	private static final String metaType="or";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Logic","|",metaType,FlowUnitOr.class, null,"Or"));
		}
	
	public FlowUnitOr()
		{
		super("A | B",metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		
		checkNotNull(a,b);
		if(a instanceof Boolean && b instanceof Boolean)
			lastOutput.put("C", (Boolean)a || (Boolean)b);
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpOrImage().exec1Untyped((AnyEvImage)a, (AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());

		}

	
	}