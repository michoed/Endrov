package evplugin.nuc;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.script.*;

/**
 * Show nucleus
 * @author Johan Henriksson
 */
public class CmdNucshow extends Command
	{
	
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		for(NucPair p:NucLineage.selectedNuclei)
			NucLineage.hiddenNuclei.remove(p);
		BasicWindow.updateWindows();
		return null;
		}
	
	}
