/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.expression;

import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.lineage.Lineage;
import endrov.lineage.util.LineageMergeUtil;
import endrov.util.EvDecimal;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;

/**
 * Integrate expression along DV-axis
 * @author Johan Henriksson
 *
 */
public class IntegratorSliceDV extends IntegratorSlice 
	{

	Vector3d axisDV;
	
	public IntegratorSliceDV(IntegrateExp integrator, int numSubDiv, Map<EvDecimal, Double> bg)
		{
		super(integrator, numSubDiv, bg);
		}
	
	//Normalized with inverse length of axis 
	public ImVector3d getDirVec()
		{
		return new ImVector3d(axisDV.x,axisDV.y,axisDV.z);
		}
	
	public ImVector3d getMidPos()
		{
		return new ImVector3d(shell.midx, shell.midy, shell.midz); 
		}
	
	
	/**
	 * Set up coordinate system, return if successful
	 */
	public boolean setupCS(Lineage refLin)
		{
		Lineage.Particle nucEMS = refLin.particle.get("EMS");

		Vector3d posABp = LineageMergeUtil.getLastPosABp(refLin);

		if (posABp==null || nucEMS==null || nucEMS.pos.isEmpty())
			{
			System.out.println("Does not have enough cells marked, will not produce DV");
			return false;
			}
		else
			System.out.println("Will do DV");

		//Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
		Vector3d posEMS = nucEMS.pos.get(nucEMS.pos.lastKey()).getPosCopy();

		ImVector2 dirvec=ImVector2.polar(shell.major, shell.angle);
		Vector3d axisAP=new Vector3d(dirvec.x, dirvec.y, 0);

		//Approximate axis - but I want it perpendicular to the AP-axis defined by the shell
		Vector3d approxDV = new Vector3d();
		approxDV.sub(posEMS, posABp);
		
		Vector3d axisLR = new Vector3d();
		axisLR.cross(approxDV, axisAP);
		
		Vector3d axisDV = new Vector3d();
		axisDV.cross(axisLR, axisAP);
		
		axisDV.normalize();
		double axisLength=2*shell.minor;
		axisDV.scale(1.0/axisLength);  
		this.axisDV=axisDV;
		
		
		return true;
		}
	
	}
