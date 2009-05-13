package endrov.recording;

import java.util.HashMap;
import java.util.Map;

import endrov.basicWindow.BasicWindow;
import endrov.hardware.Device;
import endrov.hardware.DevicePath;
import endrov.keyBinding.JInputManager;
import endrov.keyBinding.JInputMode;
import endrov.util.Tuple;


/**
 * Use input to control hardware
 * 
 * @author Johan Henriksson
 *
 */
public class JInputModeRecording implements JInputMode
	{
	/**
	 * Axis name TO DevicePath+axis
	 */
	Map<String, Tuple<DevicePath,String>> gpMap=new HashMap<String, Tuple<DevicePath,String>>();
	
	public JInputModeRecording()
		{
		gpMap.put("x", Tuple.make(new DevicePath("ev/demo/stage"),"x"));
		gpMap.put("y", Tuple.make(new DevicePath("ev/demo/stage"),"y"));
		gpMap.put("rz", Tuple.make(new DevicePath("ev/demo/stage"),"z"));
		//rz
		}
	
	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		//System.out.println(status);
		for(Map.Entry<String, Float> e:status.values.entrySet())
			{
			Tuple<DevicePath,String> t=gpMap.get(e.getKey());
			if(t!=null)
				{
				Device dev=t.fst().getDevice();
				if(dev instanceof HWStage)
					{
					HWStage stage=(HWStage)dev;
					String axisName[]=stage.getAxisName();
					double axis[]=new double[stage.getNumAxis()];
					for(int i=0;i<axisName.length;i++)
						if(axisName[i].equals(t.snd()))
							axis[i]+=e.getValue();
					stage.setRelStagePos(axis);
					BasicWindow.updateWindows();
					
					}
				else
					System.out.println("Not stage");
				
				
				
				}
//			else
//				System.out.println("Device for axis found "+e.getKey());
			}
		
		
		}
	
	
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		}

	
	
	}