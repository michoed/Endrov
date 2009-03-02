package util2.integrateExpression;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.nuc.*;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.EvParallel;
import endrov.util.Tuple;
import endrov.util.Vector2D;

//TODO all warnings in some file

/**
 * All integrations are done at the same time to reduce disk I/O. Images not needed are discarded through lazy evaluation.
 * @author Johan Henriksson
 *
 */
public class NewIntExp
	{

	public static void main2(String arg[])
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();


		EvParallel.map_(Arrays.asList(new File("/Volumes2/TBU_main01/ost4dgood").listFiles()), new EvParallel.FuncAB<File, Object>(){
			public Object func(File f)
				{
				if(f.getName().endsWith(".ost"))
					{
					EvData data=EvData.loadFile(f);
					if(!data.getObjects(Imageset.class).isEmpty())
						doOne(f);
					}
				return null;
				}
		});
		
		System.exit(0);
		}
	
	public static void main(String arg[])
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		doOne(new File("/home/tbudev3/TB2141_070621_b.ost"));
//		doOne(new File("/Volumes2/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		
		System.exit(0);
		}
	
	
	public static void doOne(File f)
		{
		EvData data=EvData.loadFile(f);
		
		int numSubDiv=20;
		String channelName="GFP";
		String expName="exp"; //Neutral name
		
		
		//Fixing time is not done now. (what about gnuplot?). 
		//makes checking difficult if applied
		//=> forced to apply when merging, and searching if searching in original
		//=> output both frames and model time in gnuplot files?
		
		
		String newLinNameT=linFor(1,channelName);
		String newLinNameAP=linFor(numSubDiv,channelName);
		
		
		//Not the optimal way of finding the lineage
		NucLineage lin=null;
		Map<EvPath, NucLineage> lins=data.getIdObjectsRecursive(NucLineage.class);
		for(Map.Entry<EvPath, NucLineage> e:lins.entrySet())
			//if(!e.getKey().getLeafName().equals(newLinNameT) && !e.getKey().getLeafName().equals(newLinNameAP))
			if(!e.getKey().getLeafName().startsWith("AP"))
				{
				System.out.println("found lineage "+e.getKey());
				lin=e.getValue();
				}
		lin=null;
		
		//Decide on integrators
		LinkedList<Integrator> ints=new LinkedList<Integrator>();
		
		NewIntExp integrator=new NewIntExp(data, expName,channelName);
		IntegratorAP intAP=new IntegratorAP(integrator,newLinNameAP,numSubDiv);
		IntegratorAP intT=new IntegratorAP(integrator,newLinNameT,1);
		
		ints.add(intAP);
		ints.add(intT);
		
		IntegratorCell intC=null;
		if(lin!=null)
			{
			intC=new IntegratorCell(integrator, lin);
			ints.add(intC);
			}
		
		//todo check which lin to use, add to list if one exists
		
		//Run integrators
		integrator.doProfile(intAP,intT);
		
		//Wrap up, store in OST
		//Use common correction factors for exposure
		intAP.done(integrator,null);
		intT.done(integrator,intAP.correctedExposure);
		
		if(lin!=null)
			intC.done(integrator,intAP.correctedExposure);
		
		//Put integral in file for use by Gnuplot
		intAP.profileForGnuplot(integrator,fileFor(data,numSubDiv,channelName));

		//TODO
		//compression?
		
		data.saveData(); 

		
		}
		
	
	public static String linFor(int numSubDiv, String channelName)
		{
		return "AP"+numSubDiv+"-"+channelName;
		}
	
	public static File fileFor(EvData data, int numSubDiv, String channelName)
		{
		//TODO: later, use blobs or similar?
		File datadir=data.io.datadir();
//		return new File(datadir,"AP"+numSubDiv+"-"+channelName);
		return new File(datadir,"AP"+numSubDiv+"-"+channelName+"c"); //TODO temp
		}
	
	
	
	
	
	
	
	
	public interface Integrator
		{
		public void integrateStackStart(NewIntExp images);
		public void integrateImage(NewIntExp images);
		public void integrateStackDone(NewIntExp images);
		}
	
	
	public EvDecimal frame;
	public EvDecimal curZ;
	public EvImage im;
	public EvPixels pixels;
	public int[] pixelsLine;
	public double expTime=1; //For missing frames, use last frame
	public EvData data;
	//public String newLinName;
	public String expName;
	public String channelName;
	public EvChannel ch;
	public Imageset imset;

	
	
	


	/**
	 * Lazily load images
	 */
	public void ensureImageLoaded()
		{
		if(pixels==null)
			{
			BufferedImage b=im.getJavaImage();
			pixels=new EvPixels(b);
			pixels=pixels.getReadOnly(EvPixels.TYPE_INT);
			pixelsLine=pixels.getArrayInt();
			}
		}
	
	
	public NewIntExp(EvData data, String expName, String channelName)
		{
		this.data=data;
		//this.newLinName=newLineName;
		this.expName=expName;
		this.channelName=channelName;
		imset=data.getObjects(Imageset.class).get(0);
		ch=imset.getChannel(channelName);

		}

	
	/**
	 * Run all integrators
	 */
	public void doProfile(Collection<Integrator> ints)
		{
		doProfile(ints.toArray(new Integrator[]{}));
		}

	/**
	 * Run all integrators
	 */
	public void doProfile(Integrator... ints)
		{
		//For all frames
		System.out.println("num frames: "+imset.getChannel(channelName).imageLoader.size());
		EvDecimal firstframe=ch.imageLoader.firstKey();
		EvDecimal lastFrame=ch.imageLoader.lastKey();
		for(EvDecimal frame:ch.imageLoader.keySet())
//			if(frame.less(new EvDecimal("30000")) && frame.greater(new EvDecimal("29000")))
			{
			System.out.println();
			System.out.println(data+"    frame "+frame+" / "+firstframe+" - "+lastFrame);

			//Get exposure time
			String sExpTime=imset.getMetaFrame(frame).get("exposuretime");
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			else
				System.out.println("No exposure time");
			

			for(Integrator i:ints)
				i.integrateStackStart(this);

			//For all z
			for(Map.Entry<EvDecimal, EvImage> eim:ch.imageLoader.get(frame).entrySet())
				{
				curZ=eim.getKey();
				this.frame=frame;
				//Load images lazily (for AP not really needed)
				im=eim.getValue();
				//EvPixels pixels=null;
				//int[] pixelsLine=null;

				pixels=null;
				
				for(Integrator i:ints)
					i.integrateImage(this);

				}

			for(Integrator i:ints)
				i.integrateStackDone(this);

			
			}

	

		

	

		
		}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class IntegratorAP implements Integrator
		{
		int numSubDiv;
		HashMap<EvDecimal, EvPixels> distanceMap=new HashMap<EvDecimal, EvPixels>();
		Shell shell;
		int[] sliceExp;
		int[] sliceVol;
		NucLineage lin;
		String newLinName;
		
		public IntegratorAP(NewIntExp integrator, String newLinName, int numSubDiv)
			{
			this.numSubDiv=numSubDiv;
			this.newLinName=newLinName;
			
			sliceExp=new int[numSubDiv];
			sliceVol=new int[numSubDiv];
			
			//TODO need to group lineage and shell. introduce a new object?
			lin=new NucLineage();
			//imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
			shell=integrator.imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
			//ExpUtil.clearExp(lin, expName);

			integrator.imset.metaObject.put(newLinName, lin);
			
			//Virtual nuc for AP
			for(int i=0;i<numSubDiv;i++)
				lin.getNucCreate("_slice"+i);
			}
		
		
		public void integrateStackStart(NewIntExp integrator)
			{
			}
		
		
		public void integrateImage(NewIntExp integrator)
			{
			integrator.ensureImageLoaded();
			
			//Calculate distance mask lazily
			EvPixels lenMap;
			double[] lenMapArr;
			if(distanceMap.containsKey(integrator.curZ))
				{
				lenMap=distanceMap.get(integrator.curZ);
				lenMapArr=lenMap.getArrayDouble();
				}
			else
				{
				lenMap=new EvPixels(EvPixels.TYPE_DOUBLE, integrator.pixels.getWidth(), integrator.pixels.getHeight());
				lenMapArr=lenMap.getArrayDouble();

				Vector2D dirvec=Vector2D.polar(shell.major, shell.angle);
				Vector2D startpos=dirvec.add(new Vector2D(shell.midx,shell.midy));
				dirvec=dirvec.normalize().mul(-1);

				//Calculate distances
				for(int ay=0;ay<integrator.pixels.getHeight();ay++)
					{
					int lineIndex=lenMap.getRowIndex(ay);
					for(int ax=0;ax<integrator.pixels.getWidth();ax++)
						{
						//Convert to world coordinates
						Vector2D pos=new Vector2D(integrator.im.transformImageWorldX(ax),integrator.im.transformImageWorldY(ay));

						//Check if this is within ellipse boundary
						Vector2D elip=pos.sub(new Vector2D(shell.midx, shell.midy)).rotate(shell.angle); //TODO angle? what?
						double len;
						if(1 >= elip.y*elip.y/(shell.minor*shell.minor) + elip.x*elip.x/(shell.major*shell.major) )
							len=pos.sub(startpos).dot(dirvec)/(2*shell.major);	//xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
						else
							len=-1;
						lenMapArr[lineIndex+ax]=len;
						}
					}
				}
			
			//Integrate this area
			for(int y=0;y<integrator.pixels.getHeight();y++)
				{
				int lineIndex=integrator.pixels.getRowIndex(y);
				for(int x=0;x<integrator.pixels.getWidth();x++)
					{
					int i=lineIndex+x;
					double len=lenMapArr[i];
					if(len>-1)
						{
						int sliceNum=(int)(len*numSubDiv); //may need to bound in addition
						sliceExp[sliceNum]+=integrator.pixelsLine[i];
						sliceVol[sliceNum]++;
						}
					}
				}
			}
	
		
		public void integrateStackDone(NewIntExp images)
			{
			/**
			 * Store pattern in lineage
			 */
			for(int i=0;i<numSubDiv;i++)
				{
				double avg=(double)sliceExp[i]/(double)sliceVol[i];
				avg/=images.expTime;
		
				NucLineage.Nuc nuc=lin.getNucCreate("_slice"+i);
				NucExp exp=nuc.getExpCreate(images.expName);
				exp.level.put(images.frame, avg);
				
				}
			}
		
		
		TreeMap<EvDecimal, Tuple<Double,Double>> correctedExposure;
		
		public void done(NewIntExp integrator, TreeMap<EvDecimal, Tuple<Double,Double>> correctedExposure)
			{
			//Set override start and end times
			for(int i=0;i<numSubDiv;i++)
				{
				NucLineage.Nuc nuc=lin.getNucCreate("_slice"+i);
				nuc.overrideStart=integrator.ch.imageLoader.firstKey();
				nuc.overrideEnd=integrator.ch.imageLoader.lastKey();
				}
			
			//Normalization is needed before exposure correction to make sure the threshold for
			//detecting jumps always works
			ExpUtil.normalizeSignal(lin, integrator.expName, ExpUtil.getSignalMax(lin, integrator.expName),0); 
			
			if(correctedExposure!=null)
				{
				ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);
				}
			else
				{
				this.correctedExposure=
				ExpUtil.correctExposureChange(integrator.imset, lin, integrator.expName, new TreeSet<EvDecimal>(integrator.ch.imageLoader.keySet()));
				}
			
			//This is only for the eye
			double sigMax=ExpUtil.getSignalMax(lin, integrator.expName);
			double sigMin=ExpUtil.getSignalMin(lin, integrator.expName);
			ExpUtil.normalizeSignal(lin, integrator.expName,sigMax,sigMin); 
			}
		
		
		
		/**
		 * Store profile as array on disk
		 */
		public void profileForGnuplot(NewIntExp integrator, File file)
			{
			Imageset imset=integrator.data.getObjects(Imageset.class).get(0);
			EvChannel ch=imset.getChannel(integrator.channelName);
			NucLineage lin=(NucLineage)imset.metaObject.get(newLinName);
			try
				{
				StringBuffer outf=new StringBuffer();
				
				here: for(EvDecimal frame:ch.imageLoader.keySet())
					{
					outf.append(""+frame+"\t");
					for(int i=0;i<numSubDiv;i++)
						{
						NucLineage.Nuc nuc=lin.nuc.get("_slice"+i);
						NucExp nexp=nuc.exp.get(integrator.expName);
						Double level=nexp.level.get(frame);
						if(level==null)
							continue here;
						outf.append(level);
						outf.append("\t");
						}
					outf.append("\n");
					}
				EvFileUtil.writeFile(file, outf.toString());
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			}
		
		
		}

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * Project sphere onto plane. Assumes resx=resy
	 * @param nucRw Radius
	 * @param nucZw Relative z
	 */
	public static Double projectSphere(double nucRw, double nucZw, double imageZw)
		{
		double dz=nucZw-imageZw;
		double tf=nucRw*nucRw-dz*dz;
		if(tf>0)
			return Math.sqrt(tf);
		else
			return null;
		}
	
	
	private static int min2(int a, int b)
		{
		return a<b? a:b;
		}

	private static int max2(int a, int b)
		{
		return a>b? a:b;
		}

	
	
	public static class IntegratorCell implements Integrator
		{
		NucLineage lin;
		
		Map<String, Double> expLevel;
		Map<String, Integer> nucVol;
		Map<NucPair,NucLineage.NucInterp> inter;
	
		
		public IntegratorCell(NewIntExp integrator, NucLineage lin)
			{
			this.lin=lin;
			ExpUtil.clearExp(lin, integrator.expName);
			}

		public void integrateStackStart(NewIntExp integrator)
			{
			expLevel=new HashMap<String, Double>();
			nucVol=new HashMap<String, Integer>();
			inter=lin.getInterpNuc(integrator.frame);
			}
		
		public void integrateImage(NewIntExp integrator)
			{
			double imageZw=integrator.curZ.doubleValue();


			//For all nuc
			for(Map.Entry<NucPair,NucLineage.NucInterp> e:inter.entrySet())
				//					if(e.getKey().getRight().equals("ABarappaa"))
				//	if(e.getKey().getRight().equals("AB"))
				{
				String nucName=e.getKey().snd();
				NucLineage.NucPos pos=e.getValue().pos;

				Double pr=projectSphere(pos.r, pos.z, imageZw);
				if(pr!=null)
					{
					int midSx=(int)integrator.im.transformWorldImageX(pos.x);
					int midSy=(int)integrator.im.transformWorldImageY(pos.y);
					int rS=(int)integrator.im.scaleWorldImageX(pr);
					if(rS>0)
						{
						if(!expLevel.containsKey(nucName))
							{
							expLevel.put(nucName, 0.0);
							nucVol.put(nucName, 0);
							}

						integrator.ensureImageLoaded();

						//Integrate this area
						int sy=max2(midSy-rS,0);
						int ey=min2(midSy+rS,integrator.pixels.getHeight());
						int sx=max2(midSx-rS,0);
						int ex=min2(midSx+rS,integrator.pixels.getWidth());
						int area=0;
						double exp=0;
						for(int y=sy;y<ey;y++)
							{
							int lineIndex=integrator.pixels.getRowIndex(y);
							for(int x=sx;x<ex;x++)
								{
								int dx=x-midSx;
								int dy=y-midSy;
								if(dx*dx+dy*dy<rS*rS)
									{
									int v=integrator.pixelsLine[lineIndex+x];
									area++;
									exp+=v;
									}
								}
							}

						//Sum up volume and area
						nucVol.put(nucName,nucVol.get(nucName)+area);
						expLevel.put(nucName,expLevel.get(nucName)+exp);
						}
					}
				}




			}

	
		public void integrateStackDone(NewIntExp integrator)
			{
			//Store value in XML
			for(String nucName:expLevel.keySet())
				{
				double avg=expLevel.get(nucName)/nucVol.get(nucName);
				avg/=integrator.expTime;
//				System.out.println(nucName+" "+avg);
				NucExp exp=lin.nuc.get(nucName).getExpCreate(integrator.expName);
				if(lin.nuc.get(nucName).pos.lastKey().greaterEqual(integrator.frame) && 
						lin.nuc.get(nucName).pos.firstKey().lessEqual(integrator.frame)) 
					exp.level.put(integrator.frame,avg);
				}
			}
		
		
		public void done(NewIntExp integrator, TreeMap<EvDecimal, Tuple<Double,Double>> correctedExposure)
			{
			
			//Use prior correction on this expression as well
			Double max1=ExpUtil.getSignalMax(lin, integrator.expName);
			if(max1==null)
				{
				System.out.println("max==null, there is no signal!");
				}
			else
				{
				ExpUtil.normalizeSignal(lin, integrator.expName, max1,0); 
				ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);
				}
			
			
			/*
			TreeSet<EvDecimal> framesSorted=new TreeSet<EvDecimal>(bgLevel.keySet());
			ExpUtil.correctExposureChange(imset, lin, expName, framesSorted);
			ExpUtil.normalizeSignal(lin, expName);*/

			//TODO TODO TODO get corrections from AP calc
		
			
			}
		
		}

	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	
	
	}