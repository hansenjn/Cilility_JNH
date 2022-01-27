package cilility_jnh;
/** ===============================================================================
* FreQ.java Version 0.3.0
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*  
* See the GNU General Public License for more details.
*  
* Copyright (C) Jan Niklas Hansen
* Date: June 13, 2019 (This Version: January 27, 2022)
*   
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
* =============================================================================== */

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.text.*;
import javax.swing.UIManager;
import cilility_jnh.edu.emory.mathcs.jtransforms.fft.*;
import cilility_jnh.support.*;
import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;
import ij.process.ImageConverter;
import ij.process.LUT;
import ij.text.*;

public class CililityMain implements PlugIn, Measurements {
	//Name variables
	static final String PLUGINNAME = "FreQ";
	static final String PLUGINVERSION = "0.3.0";
	static final String PLUGINHTML = "https://github.com/hansenjn/FreQ";
	//Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 9);
	static final Color INSTRCOLOR = new java.awt.Color(0,0,200);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 12);
	
	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static DecimalFormat df1US = new DecimalFormat("#0.0");
	
	//Progress Dialog
	ProgressDialog progress;	
	boolean processingDone = false;	
	boolean continueProcessing = true;
	
	static double jetLUTNumbers [][] = new double [][] {{0,0,0.562500000000000},{0,0,0.625000000000000},{0,0,0.687500000000000},
		{0,0,0.750000000000000},{0,0,0.812500000000000},{0,0,0.875000000000000},
		{0,0,0.937500000000000},{0,0,1},{0,0.0625000000000000,1},
		{0,0.125000000000000,1},{0,0.187500000000000,1},{0,0.250000000000000,1},
		{0,0.312500000000000,1},{0,0.375000000000000,1},{0,0.437500000000000,1},
		{0,0.500000000000000,1},{0,0.562500000000000,1},{0,0.625000000000000,1},
		{0,0.687500000000000,1},{0,0.750000000000000,1},{0,0.812500000000000,1},
		{0,0.875000000000000,1},{0,0.937500000000000,1},{0,1,1},
		{0.0625000000000000,1,0.937500000000000},{0.125000000000000,1,0.875000000000000},{0.187500000000000,1,0.812500000000000},
		{0.250000000000000,1,0.750000000000000},{0.312500000000000,1,0.687500000000000},{0.375000000000000,1,0.625000000000000},
		{0.437500000000000,1,0.562500000000000},{0.500000000000000,1,0.500000000000000},{0.562500000000000,1,0.437500000000000},
		{0.625000000000000,1,0.375000000000000},{0.687500000000000,1,0.312500000000000},{0.750000000000000,1,0.250000000000000},
		{0.812500000000000,1,0.187500000000000},{0.875000000000000,1,0.125000000000000},{0.937500000000000,1,0.0625000000000000},
		{1,1,0},{1,0.937500000000000,0},{1,0.875000000000000,0},
		{1,0.812500000000000,0},{1,0.750000000000000,0},{1,0.687500000000000,0},
		{1,0.625000000000000,0},{1,0.562500000000000,0},{1,0.500000000000000,0},
		{1,0.437500000000000,0},{1,0.375000000000000,0},{1,0.312500000000000,0},
		{1,0.250000000000000,0},{1,0.187500000000000,0},{1,0.125000000000000,0},
		{1,0.0625000000000000,0},{1,0,0},{0.937500000000000,0,0},
		{0.875000000000000,0,0},{0.812500000000000,0,0},{0.750000000000000,0,0},
		{0.687500000000000,0,0},{0.625000000000000,0,0},{0.562500000000000,0,0},
		{0.500000000000000,0,0}};
		
	LUT jetLUT;
	
	//-----------------define params for Dialog-----------------
	static final String[] taskVariant = {"active image in FIJI","multiple images (open multi-task manager)", "all images open in FIJI"};
	String selectedTaskVariant = taskVariant[1];
	int tasks = 1;
	double sampleRate = 2000.0;
	
	boolean extractSignalsUsingSD = true;
	boolean removeBackgroundInSpectra = true;
	
	double SDlimitForSpectrumBG = 1.5;	
	double percentLowest = 20;
	
	int kernelWidth = 3;
	double kernelSDThreshold = 3.0;
	boolean extendRange = true;
	
	double upperLimit = 90.0, lowerLimit = 5.0;
	double histogramBins = 5.0;
	
	boolean manualROI = false;
	
	double smoothFreq = 5.0;
	int smoothWindowSize = 5;
	
	boolean enhancedOutput = false;
	
	static final String[] outputVariant = {"save as filename + suffix 'Cil'", "save as filename + suffix 'Cil' + date"};
	String selectedOutputVariant = outputVariant[0];
		
	//-----------------define params for Dialog-----------------
	
	//Global variables for use
	TextPanel OutputPanel;
	
public void run(String arg) {
	df1US.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));;
	
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//-------------------------GenericDialog--------------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	
	GenericDialog gd = new GenericDialog(PLUGINNAME + " - set parameters");	
	//show Dialog-----------------------------------------------------------------
	//.setInsets(top, left, bottom)
	gd.setInsets(0,0,0);		gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2019-2022 JN Hansen", HeadingFont);	
	gd.setInsets(5,0,0);		gd.addChoice("Process ", taskVariant, selectedTaskVariant);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Recording frequency (Hz)", sampleRate, 2);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Sliding window to smooth power spectrum (Hz)", smoothFreq, 2);
	gd.setInsets(0,30,0);		gd.addMessage("(Set sliding window to 0.0 to disable smoothing)", InstructionsFont, INSTRCOLOR);

	gd.setInsets(10,0,0);		gd.addMessage("Enhance peak detection", SubHeadingFont);
	gd.setInsets(0,5,0);		gd.addCheckbox("Remove background from power spectra (determined in spectra with low overall power)", removeBackgroundInSpectra);
	gd.setInsets(0,5,0);		gd.addMessage("INFO: The next two parameters below are only relevant if you select to auto-remove the background. If option is activated,",
			InstructionsFont, INSTRCOLOR);
	gd.setInsets(0,5,0);		gd.addMessage("the percentage (parameter defined below) of pixels yielding the power spectra with the lowest overall power are extracted. Next the ",
			InstructionsFont, INSTRCOLOR);
	gd.setInsets(0,5,0);		gd.addMessage("mean and SD of the spectra from those pixels is determined to calculate a background spectrum as mean + (fold parameter below) * SD.",
			InstructionsFont, INSTRCOLOR);
	gd.setInsets(0, 5, 0);	gd.addNumericField("Percent of lowest-power pixels", percentLowest, 1);
	gd.setInsets(0, 5, 0);	gd.addNumericField("Fold SD used to calculate power threshold", SDlimitForSpectrumBG, 1);
	gd.setInsets(0,30,0);		gd.addMessage("(Example False Discovery Rates (FDRs) for set SDs: 1.5 -> FDR=4.4%, 2 -> FDR=1.7%, 2.5 -> FDR=0.5%, 3 -> FDR=0.1%)", 
			InstructionsFont, INSTRCOLOR);
	
	gd.setInsets(10,0,0);		gd.addMessage("Extract signal regions", SubHeadingFont);
	gd.setInsets(0,5,0);		gd.addCheckbox("Extract signal regions using local SD filter", extractSignalsUsingSD);
	gd.setInsets(5,5,0);		gd.addMessage("INFO: The next two parameters below are only relevant if you select local SD filtering. The SD threshold defines the maximum allowed SD",
			InstructionsFont, INSTRCOLOR);
	gd.setInsets(0,5,0);		gd.addMessage("of a 3x3 pixel region to consider all pixels in that region as signal. If you activate the increase range function, not only pixels",
			InstructionsFont, INSTRCOLOR);
	gd.setInsets(0,5,0);		gd.addMessage("in the 3x3 region are considered for SD calculation but also the central four pixels above, below, left, and right of the 3x3 region.",
			InstructionsFont, INSTRCOLOR);
	gd.setInsets(5,5,0);		gd.addNumericField("Local SD filter - SD threshold: ", kernelSDThreshold, 2);
	gd.setInsets(0,5,0);		gd.addCheckbox("Local SD filter - Increase range:", extendRange);
		
	gd.setInsets(10,0,0);		gd.addMessage("Limit frequency range for quantifications and plots", SubHeadingFont);
	gd.setInsets(0, 5, 0);	gd.addNumericField("Min accepted frequency (Hz)", lowerLimit, 2);
	gd.setInsets(0, 5, 0);	gd.addNumericField("Max accepted frequency (Hz)", upperLimit, 2);
	
	gd.setInsets(10,0,0);		gd.addMessage("Report a specific region of interest (ROI)?", SubHeadingFont);
	gd.setInsets(0, 0, 0);	gd.addCheckbox("Report results for ROI set by user", manualROI);
	gd.setInsets(0,30,0);		gd.addMessage("(If activated, the user is requested to draw a ROI for each image after launching the analysis)",
			InstructionsFont, INSTRCOLOR);
	
	gd.setInsets(10,0,0);		gd.addMessage("Output settings", SubHeadingFont);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Bin size in histograms (Hz)", histogramBins, 2);
	gd.setInsets(0, 0, 0);	gd.addCheckbox("Output complex maps (phase, com, ...)", enhancedOutput);
	gd.setInsets(0,0,0);	gd.addChoice("Output file names ", outputVariant, selectedOutputVariant);
	
	gd.addHelp(PLUGINHTML);
	gd.showDialog();
	//show Dialog-----------------------------------------------------------------

	//read and process variables--------------------------------------------------	
	selectedTaskVariant = gd.getNextChoice();
	sampleRate = gd.getNextNumber();
	smoothFreq = gd.getNextNumber();

	removeBackgroundInSpectra = gd.getNextBoolean();
	percentLowest = gd.getNextNumber();
	SDlimitForSpectrumBG = gd.getNextNumber();
	
	extractSignalsUsingSD = gd.getNextBoolean();
	kernelSDThreshold = gd.getNextNumber();
	extendRange = gd.getNextBoolean();
	
	lowerLimit = gd.getNextNumber();
	upperLimit = gd.getNextNumber();
	
	manualROI = gd.getNextBoolean();

	histogramBins = gd.getNextNumber();
	enhancedOutput = gd.getNextBoolean();
	selectedOutputVariant = gd.getNextChoice();	
	
	//read and process variables--------------------------------------------------
	if (gd.wasCanceled()) return;
	
	//Verify
	if(lowerLimit < 0.0) {
		lowerLimit = 0.0;
		new WaitForUserDialog("<Min accepted frequency> must be >= 0.0. The setting <Min accepted frequency> was automatically corrected to 0.0!").show();
	}
	
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//---------------------end-GenericDialog-end----------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

	jetLUT = this.getLUT(jetLUTNumbers, true);
	
	String name [] = {"",""};
	String dir [] = {"",""};
	ImagePlus allImps [] = new ImagePlus [2];
	RoiEncoder re;
	{
		//Improved file selector
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
		if(selectedTaskVariant.equals(taskVariant[1])){
			OpenFilesDialog od = new OpenFilesDialog ();
			od.setLocation(0,0);
			od.setVisible(true);
			
			od.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
		        	return;
		        }
		    });
		
			//Waiting for od to be done
			while(od.done==false){
				try{
					Thread.currentThread().sleep(50);
			    }catch(Exception e){
			    }
			}
			
			tasks = od.filesToOpen.size();
			name = new String [tasks];
			dir = new String [tasks];
			for(int task = 0; task < tasks; task++){
				name[task] = od.filesToOpen.get(task).getName();
				dir[task] = od.filesToOpen.get(task).getParent() + System.getProperty("file.separator");
			}		
		}else if(selectedTaskVariant.equals(taskVariant[0])){
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
			name [0] = info.fileName;	//get name
			dir [0] = info.directory;	//get directory
			tasks = 1;
		}else if(selectedTaskVariant.equals(taskVariant[2])){	// all open images
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			int IDlist [] = WindowManager.getIDList();
			tasks = IDlist.length;	
			if(tasks == 1){
				selectedTaskVariant=taskVariant[0];
				FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
				name [0] = info.fileName;	//get name
				dir [0] = info.directory;	//get directory
			}else{
				name = new String [tasks];
				dir = new String [tasks];
				allImps = new ImagePlus [tasks];
				for(int i = 0; i < tasks; i++){
					allImps[i] = WindowManager.getImage(IDlist[i]); 
					FileInfo info = allImps[i].getOriginalFileInfo();
					name [i] = info.fileName;	//get name
					dir [i] = info.directory;	//get directory
				}		
			}
					
		}
	}
	 	
	//add progressDialog
	progress = new ProgressDialog(name, tasks);
	progress.setLocation(0,0);
	progress.setVisible(true);
	progress.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(WindowEvent winEvt) {
        	if(processingDone==false){
        		IJ.error("Script stopped...");
        	}
        	continueProcessing = false;	        	
        	return;
        }
	});

	//Request ROIs
	Roi [] selections = new Roi [tasks];
   	ImagePlus imp; 	  	
   	if(manualROI){
		IJ.setTool("polygon");
		{
			for(int task = 0; task < tasks; task++){
				if(selectedTaskVariant.equals(taskVariant[1])){
					imp = IJ.openVirtual(dir [task] + name [task]);
				}else if(selectedTaskVariant.equals(taskVariant[0])){
		   			imp = WindowManager.getCurrentImage();
		   			imp.deleteRoi();
		   		}else{
		   			imp = allImps[task];
		   			imp.deleteRoi();
		   		}
//				WindowManager.setCurrentWindow(imp.getWindow());
				if(!selectedTaskVariant.equals(taskVariant[1])){
					imp.getWindow().setVisible(true);
					imp.getWindow().maximize();
					imp.getWindow().toFront();	
				}							
				imp.show();
				
				while(true){
					progress.replaceBarText("user interaction required... [task " + (task+1) + "/" + tasks + "]");
					new WaitForUserDialog("Set a Roi for the region you want to measure [task " + (task+1) + "/" + tasks + "]").show();
					if(imp.getRoi()!=null) break;
				}		
				selections [task] = imp.getRoi();
				imp.getWindow().minimize();
				if(selectedTaskVariant.equals(taskVariant[1])){
					imp.changes = false;
					imp.close();
				}
				
			}
		}
		System.gc();
	}	
	
   	//Initialize
   	double [][][] phaseMapRoiFreqs;
	double [][][] phaseMapWholeImpFreqs;
	double [] locFreqs;
	double rawFreqRes [][][]; 
   	double valueColumn [];
   	double lowerPowerAvgSD [];
   	boolean signal [][];
   	double [][] powerSpectrumBackground;
	double corrFreqRes [][][]; 
   	boolean signalPostCorr [][];
	double [] assembledPowerSpectrumFromRoi;
   	double [] assembledPowerSpectrumFromRoiCorr;
   	int [] roiFreqs;
   	double [] roiAmpl;
   	double [] roiCorrAmpl;
	double [] roiCorrSharp;  
	double roiCom;
	double ctr;
	double [] assembledPowerSpectrumFromWholeImp;
 	double [] assembledPowerSpectrumFromWholeImpCorr;
	int [] wholeImpFreqs;
   	double [] wholeImpAmpl;
   	double [] wholeImpCorrAmpl;
   	double [] wholeImpCorrSharp;
    double wholeImpCom;

	TextPanel tp1, tp2, tp3;

	double [] xValues;
	double [][] yValues;

	String filePrefix;
	
	for(int task = 0; task < tasks; task++){
		running: while(continueProcessing){
			Date startDate = new Date();
			progress.updateBarText("in progress...");
			//Check for problems
			if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".txt")){
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
			if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".zip")){	
				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
			//Check for problems

			//open Image
		   	try{
		   		if(selectedTaskVariant.equals(taskVariant[1])){
		   			imp = IJ.openImage(""+dir[task]+name[task]+"");			   			
					imp.deleteRoi();
		   		}else if(selectedTaskVariant.equals(taskVariant[0])){
		   			imp = WindowManager.getCurrentImage();
		   			imp.deleteRoi();
		   		}else{
		   			imp = allImps[task];
		   			imp.deleteRoi();
		   		}
		   	}catch (Exception e) {
		   		progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": file is no image - could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
		   	//open Image
		   	
		   	// Prepare saving file name
		   	if(name[task].contains(".")){
				filePrefix = name[task].substring(0,name[task].lastIndexOf(".")) + "_Cil";
			}else{
				filePrefix = name[task] + "_Cil";
			}
			
			if(selectedOutputVariant.equals(outputVariant[1])){
				//saveDate
				filePrefix += "_" + NameDateFormatter.format(startDate);
			}
			
			filePrefix = dir[task] + filePrefix;
			try {   	
			/******************************************************************
			*** 						Processing							***	
			*******************************************************************/
			   	//Initialize Sliding Window
			   	if(smoothFreq != 0.0){
			   		smoothWindowSize = (int)(smoothFreq / (sampleRate/(double)imp.getStackSize()));
				   	if(smoothWindowSize%2==0)	smoothWindowSize++;
				   	progress.notifyMessage("smoothWindowSize = " + smoothWindowSize, ProgressDialog.LOG);
			   	}else{
			   		smoothWindowSize = 0;
			   	}
			   		
			   	//Obtain Raw Frequency Results
			   	
			   	rawFreqRes = new double [imp.getWidth()][imp.getHeight()][7]; 
			   	valueColumn = new double [imp.getStackSize()];
			   	for(int x = 0; x < imp.getWidth(); x++){
			   		progress.addToBar(0.3/(double)imp.getWidth());
			   		for(int y = 0; y < imp.getHeight(); y++){
			   			for(int i = 0; i < imp.getStackSize(); i++){
			   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
			   			}
			   			rawFreqRes[x][y] = getFrequenciesWithPowerAndPhase(valueColumn, sampleRate, false, true, smoothWindowSize, lowerLimit, upperLimit);
				   	}	
			   	}
			   	
			  //Determine Background
			   	if(removeBackgroundInSpectra) {
//				   	boolean signal [][] = getSignalRegionsBySD(output, 3.0);
				   	lowerPowerAvgSD = getAvgSDOfLowerPowers(rawFreqRes, percentLowest);
				   	signal = getSignalRegionsByPowerThreshold(rawFreqRes, lowerPowerAvgSD[0] + SDlimitForSpectrumBG * lowerPowerAvgSD[1]);
				   	
				   	powerSpectrumBackground = getPowerSpectraBackground(imp, signal, smoothWindowSize);
		//		   	double [] powerSpectrumMinimum = getPowerSpectraMinima(imp, smoothWindowSize);
				   
				   	//get corrected Frequencies
					corrFreqRes = new double [imp.getWidth()][imp.getHeight()][7]; 
				   	for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.3/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			for(int i = 0; i < imp.getStackSize(); i++){
				   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
				   			}
				   			corrFreqRes[x][y] = getSignificantFrequenciesWithPowersAndPhases(valueColumn, sampleRate, false, true, powerSpectrumBackground, SDlimitForSpectrumBG, lowerLimit, upperLimit, smoothWindowSize);
		//		   			outputCorr[x][y] = getAboveMinimumFrequenciesWithPowers(valueColumn, sampleRate, false, true, powerSpectrumMinimum, lowerLimit, upperLimit);
					   	}	
				   	}
			   	}else {
			   		lowerPowerAvgSD = null;
			   		signal = null;
			   		powerSpectrumBackground = null;
			   		
			   		corrFreqRes = new double [imp.getWidth()][imp.getHeight()][7]; 
			   		for(int x = 0; x < imp.getWidth(); x++){
				   		for(int y = 0; y < imp.getHeight(); y++){
					   		for(int z = 0; z < rawFreqRes[x][y].length; z++){
					   			corrFreqRes[x][y][z] = rawFreqRes[x][y][z];
					   		}
				   		}
			   		}
			   		
			   		
			   	}
	
			   	if(extractSignalsUsingSD) {
			   		signalPostCorr = getSignalRegionsBySD(corrFreqRes, kernelSDThreshold, extendRange);
			   		
			   		//REMOVE non-signal pixels from corrected maps
			   		for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.01/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			if(!signalPostCorr[x][y]){
				   				for(int i = 0; i < corrFreqRes[x][y].length; i++){
				   					corrFreqRes[x][y][i] = 0.0;
				   				}		   				
				   			}
					   	}	
				   	}
			   	}else {
			   		signalPostCorr = new boolean [corrFreqRes.length][corrFreqRes[0].length];
			   		for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.01/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			signalPostCorr [x][y] = true;
				   		}
			   		}
			   	}
			   	
			   	
			   	int signalPixels;
			   	int valueCounter;
			   	
			   	/**
			   	 * Get region results &| number of signal pixels
			   	 * */
			   	double roiMin = Double.POSITIVE_INFINITY, roiMax = Double.NEGATIVE_INFINITY, 
			   			roiAverage = Double.NaN, roiMedian = Double.NaN, roiSD = Double.NaN;
	   			if(manualROI) {
	   				//Get Number of signal pixels
//	   				boolean signalPostCorr [][] = signal;
	   				signalPixels = 0;
				   	for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.04/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			if(selections[task].contains(x, y) && signalPostCorr [x][y]){
				   				if(corrFreqRes[x][y][0]>=lowerLimit && corrFreqRes[x][y][0]<=upperLimit){
				   					signalPixels++;
				   				}		   				
				   			}
					   	}	
				   	}
				   	
				  //DETERMINE PARAMETERS FOR SELECTION
				   	valueColumn = new double [signalPixels];
				   	signalPixels = 0;
				   	valueCounter = 0;
				   	for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.05/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			if(signalPostCorr[x][y]){
				   				if(selections[task].contains(x, y)){
					   				if(corrFreqRes[x][y][0]>=lowerLimit && corrFreqRes[x][y][0]<=upperLimit){
					   					if(roiMin > corrFreqRes[x][y][0] && corrFreqRes[x][y][0] != -1){
						   					roiMin = corrFreqRes[x][y][0];
						   				}
						   				if(roiMax < corrFreqRes[x][y][0]){
						   					roiMax = corrFreqRes[x][y][0];
						   				}
					   					
					   					valueColumn [valueCounter] = corrFreqRes[x][y][0];
						   				valueCounter++;
						   				signalPixels++;
					   				}		   				
					   			}else{
					   				if(corrFreqRes[x][y][0]>=lowerLimit && corrFreqRes[x][y][0]<=upperLimit){
					   					signalPixels++;
					   				}
					   			}
				   			}
					   	}	
				   	}		   	
				   	if(valueCounter != valueColumn.length){
				   		IJ.log("ERROR IN CODE!!!");
				   	}
				   	roiAverage = tools.getAverage(valueColumn);
				   	roiMedian = tools.getMedian(valueColumn);
				   	roiSD = tools.getSD(valueColumn);


				   	saveAsHistogram(valueColumn, lowerLimit, upperLimit, histogramBins, false, true, "Frequency Histogram (ROI)",
				   			"frequency 1 of signal pixels in ROI","Hz", filePrefix + "_f1_hist_r", new Date());
	   			}else {
	   				signalPixels = 0;
	   				for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.04/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			if(signalPostCorr [x][y]){
				   				if(corrFreqRes[x][y][0]>=lowerLimit && corrFreqRes[x][y][0]<=upperLimit){
				   					signalPixels++;
				   				}		   				
				   			}
					   	}	
				   	}
	   			}

			   	
			   	
			  //DETERMINE PARAMETERS FOR ENTIRE IMAGE
			   	double wholeImpMin = Double.POSITIVE_INFINITY, wholeImpMax = Double.NEGATIVE_INFINITY, wholeImpAverage, wholeImpMedian, wholeImpSD;
			   	valueColumn = new double [signalPixels];
			   	valueCounter = 0;
			   	for(int x = 0; x < imp.getWidth(); x++){
			   		progress.addToBar(0.05/(double)imp.getWidth());
			   		for(int y = 0; y < imp.getHeight(); y++){
			   			if(signalPostCorr[x][y]){
			   				if(corrFreqRes[x][y][0]>=lowerLimit && corrFreqRes[x][y][0]<=upperLimit){
			   					if(wholeImpMin > corrFreqRes[x][y][0] && corrFreqRes[x][y][0] != -1){
				   					wholeImpMin = corrFreqRes[x][y][0];
				   				}
				   				if(wholeImpMax < corrFreqRes[x][y][0]){
				   					wholeImpMax = corrFreqRes[x][y][0];
				   				}
				   				valueColumn [valueCounter] = corrFreqRes[x][y][0];
				   				valueCounter++;
			   				}		   				
			   			}		   			
				   	}	
			   	}		   	
			   	if(valueCounter != valueColumn.length){
			   		IJ.log("ERROR IN CODE - 2!!!");
			   	}
			   	wholeImpAverage = tools.getAverage(valueColumn);
			   	wholeImpMedian = tools.getMedian(valueColumn);
			   	wholeImpSD = tools.getSD(valueColumn);
			   	saveAsHistogram(valueColumn, lowerLimit, upperLimit, histogramBins, false, true, "Frequency Histogram",
			   			"frequency 1 of signal pixels", "Hz", filePrefix + "_f1_hist", new Date());
			   	
			   	//DETERMINE POWER SPECTRUM IN ROI
			   	assembledPowerSpectrumFromRoi = new double [0];
			   	assembledPowerSpectrumFromRoiCorr = new double [0];
			   	roiFreqs = new int [2];
			   	roiAmpl = new double [2];
			   	roiCorrAmpl = new double [2];
				roiCorrSharp = new double [2];  
				roiCom = 0.0;
				ctr = 0;
			   	
				if(manualROI) {
					assembledPowerSpectrumFromRoi = getAssembledPowerSpectrumInRoi(imp, signalPostCorr, selections[task], smoothWindowSize);					
					if(assembledPowerSpectrumFromRoi != null){
			   			if(removeBackgroundInSpectra) {
			   				assembledPowerSpectrumFromRoiCorr = new double [assembledPowerSpectrumFromRoi.length];
			   			}
				   		for(int i = 0; i < assembledPowerSpectrumFromRoi.length; i++){
				   			if(removeBackgroundInSpectra) {
				   				assembledPowerSpectrumFromRoiCorr [i] = assembledPowerSpectrumFromRoi [i] - powerSpectrumBackground [0][i] - SDlimitForSpectrumBG * powerSpectrumBackground [1][i];
			//			   		assembledPowerSpectrumFromRoiCorr [i] = assembledPowerSpectrumFromRoi [i] - powerSpectrumMinimum [i];
						   		if(assembledPowerSpectrumFromRoiCorr [i] < 0.0){
						   			assembledPowerSpectrumFromRoiCorr [i] = 0.0;
						   		}
				   			}else {
				   				assembledPowerSpectrumFromRoiCorr [i] = assembledPowerSpectrumFromRoi [i];
				   			}
					   		
					   	}
					   	
					   	roiFreqs = tools.get2HighestMaximaIndicesWithinRange(assembledPowerSpectrumFromRoiCorr, 
					   			(int)Math.round(lowerLimit*(assembledPowerSpectrumFromRoiCorr.length/sampleRate)), 
					   			(int)Math.round(upperLimit*(assembledPowerSpectrumFromRoiCorr.length/sampleRate)));
					    
					    roiCom = tools.getCenterOfMassOfRange(assembledPowerSpectrumFromRoiCorr, 0, 
					    		(assembledPowerSpectrumFromRoiCorr.length/2)-1);
					    if(roiFreqs[0] >= 0 && roiFreqs [0] < assembledPowerSpectrumFromRoiCorr.length){
					    	if(removeBackgroundInSpectra) {
					    		roiAmpl [0] = assembledPowerSpectrumFromRoiCorr [roiFreqs[0]] + powerSpectrumBackground [0][roiFreqs[0]] + SDlimitForSpectrumBG * powerSpectrumBackground [1][roiFreqs[0]];
						    	roiCorrAmpl [0] = assembledPowerSpectrumFromRoiCorr [roiFreqs[0]];
			//			    	roiAmpl [0] = assembledPowerSpectrumFromRoiCorr [roiFreqs[0]] + powerSpectrumMinimum [roiFreqs[0]];
						    	roiCorrSharp [0] = 0.0;
						    	ctr = 0;
						    	if(assembledPowerSpectrumFromRoiCorr [roiFreqs[0]-1] != 0.0){
						    		roiCorrSharp [0] += Math.abs(assembledPowerSpectrumFromRoiCorr [roiFreqs[0]] - assembledPowerSpectrumFromRoiCorr [roiFreqs[0]-1]);
						    		ctr++;
						    	}
						    	if(assembledPowerSpectrumFromRoiCorr [roiFreqs[0]+1] != 0.0){
						    		roiCorrSharp [0] += Math.abs(assembledPowerSpectrumFromRoiCorr [roiFreqs[0]+1] - assembledPowerSpectrumFromRoiCorr [roiFreqs[0]]);
						    		ctr++;
						    	}
						    	if(ctr != 0) roiCorrSharp [0] /= (double) ctr;	
					    	}else {
					    		roiAmpl [0] = assembledPowerSpectrumFromRoiCorr [roiFreqs[0]];
					    	}
					    }else{
					    	roiAmpl [0] = 0.0;
					    	if(removeBackgroundInSpectra) {
					    		roiCorrAmpl [0] = 0.0;
						    	roiCorrSharp [0] = 0.0;
					    	}				    	
					    }
					    if(roiFreqs[1] >= 0 && roiFreqs [1] < assembledPowerSpectrumFromRoiCorr.length){
					    	if(removeBackgroundInSpectra) {
					    		roiAmpl [1] = assembledPowerSpectrumFromRoiCorr [roiFreqs[1]] + powerSpectrumBackground [0][roiFreqs[1]] + SDlimitForSpectrumBG * powerSpectrumBackground [1][roiFreqs[1]];
						    	roiCorrAmpl [1] = assembledPowerSpectrumFromRoiCorr [roiFreqs[1]];
			//			    	roiAmpl [1] = assembledPowerSpectrumFromRoiCorr [roiFreqs[1]] + powerSpectrumMinimum [roiFreqs[1]];
						    	roiCorrSharp [1] = 0.0;
						    	ctr = 0;
						    	if(assembledPowerSpectrumFromRoiCorr [roiFreqs[1]-1] != 0.0){
						    		roiCorrSharp [1] += Math.abs(assembledPowerSpectrumFromRoiCorr [roiFreqs[1]] - assembledPowerSpectrumFromRoiCorr [roiFreqs[1]-1]);
						    		ctr++;
						    	}
						    	if(assembledPowerSpectrumFromRoiCorr [roiFreqs[1]+1] != 0.0){
						    		roiCorrSharp [1] += Math.abs(assembledPowerSpectrumFromRoiCorr [roiFreqs[1]+1] - assembledPowerSpectrumFromRoiCorr [roiFreqs[1]]);
						    		ctr++;
						    	}
						    	if(ctr != 0) roiCorrSharp [1] /= (double) ctr;
					    	}else {
					    		roiAmpl [1] = assembledPowerSpectrumFromRoiCorr [roiFreqs[1]];					    	
					    	}				    	
					    }else{
					    	roiAmpl [1] = 0.0;
					    	if(removeBackgroundInSpectra) {
					    		roiCorrAmpl [1] = 0.0;
						    	roiCorrSharp [1] = 0.0;
					    	}
					    } 	
				   	}
				}
			    
			  //DETERMINE POWER SPECTRUM IN ENTIRE IMAGE
			   	assembledPowerSpectrumFromWholeImp = getAssembledPowerSpectrum(imp, signalPostCorr, smoothWindowSize);
			 	assembledPowerSpectrumFromWholeImpCorr = new double [0];
				wholeImpFreqs = new int [2];
			   	wholeImpAmpl = new double [2];
			   	wholeImpCorrAmpl = new double [2];
			   	wholeImpCorrSharp = new double [2];
			    wholeImpCom = 0.0;
				if(assembledPowerSpectrumFromWholeImp != null){
					if(removeBackgroundInSpectra) {
						assembledPowerSpectrumFromWholeImpCorr = new double [assembledPowerSpectrumFromWholeImp.length];						
					}
					for(int i = 0; i < assembledPowerSpectrumFromWholeImp.length; i++){
						if(removeBackgroundInSpectra) {
							assembledPowerSpectrumFromWholeImpCorr [i] = assembledPowerSpectrumFromWholeImp [i] - powerSpectrumBackground [0][i] - SDlimitForSpectrumBG * powerSpectrumBackground [1][i];
							//			   		assembledPowerSpectrumFromWholeImpCorr [i] = assembledPowerSpectrumFromWholeImp [i] - powerSpectrumMinimum [i];
										   		if(assembledPowerSpectrumFromWholeImpCorr [i] < 0.0){
										   			assembledPowerSpectrumFromWholeImpCorr [i] = 0.0;
										   		}
						}else {
							assembledPowerSpectrumFromWholeImpCorr [i] = assembledPowerSpectrumFromWholeImp [i];
						}
				   		
				   	}
				   	
				   	wholeImpFreqs = tools.get2HighestMaximaIndicesWithinRange(assembledPowerSpectrumFromWholeImpCorr, 
				   			(int)Math.round(lowerLimit*(assembledPowerSpectrumFromWholeImpCorr.length/sampleRate)), 
				   			(int)Math.round(upperLimit*(assembledPowerSpectrumFromWholeImpCorr.length/sampleRate)));
				    wholeImpAmpl = new double [2];				    
				    wholeImpCorrAmpl = new double [2];
				    wholeImpCorrSharp = new double [2];
				    wholeImpCom = tools.getCenterOfMassOfRange(assembledPowerSpectrumFromWholeImpCorr, 0, 
				    		(assembledPowerSpectrumFromWholeImpCorr.length/2)-1);
				    if(wholeImpFreqs[0] >= 0 && wholeImpFreqs [0] < assembledPowerSpectrumFromWholeImpCorr.length){
				    	if(removeBackgroundInSpectra) {
				    		wholeImpAmpl [0] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]] + powerSpectrumBackground [0][wholeImpFreqs[0]] + SDlimitForSpectrumBG * powerSpectrumBackground [1][wholeImpFreqs[0]];
					    	wholeImpCorrAmpl [0] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]];
		//			    	wholeImpAmpl [0] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]] + powerSpectrumMinimum [wholeImpFreqs[0]];
					    	wholeImpCorrSharp [0] = 0.0;
					    	ctr = 0;
					    	if(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]-1] != 0.0){
					    		wholeImpCorrSharp [0] += Math.abs(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]] - assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]-1]);
					    		ctr++;
					    	}
					    	if(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]+1] != 0.0){
					    		wholeImpCorrSharp [0] += Math.abs(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]+1] - assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]]);
					    		ctr++;
					    	}
					    	if(ctr != 0) wholeImpCorrSharp [0] /= (double) ctr;	
				    	}else {
				    		wholeImpAmpl [0] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[0]];
				    	}
				    	
				    }else{
				    	wholeImpAmpl [0] = 0.0;
				    	if(removeBackgroundInSpectra) {
					    	wholeImpCorrAmpl [0] = 0.0;
					    	wholeImpCorrSharp [0] = 0.0;				    		
				    	}
				    }
				    if(wholeImpFreqs[1] >= 0 && wholeImpFreqs [1] < assembledPowerSpectrumFromWholeImpCorr.length){
				    	if(removeBackgroundInSpectra) {
					    	wholeImpAmpl [1] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]] + powerSpectrumBackground [0][wholeImpFreqs[1]] + SDlimitForSpectrumBG * powerSpectrumBackground [1][wholeImpFreqs[1]];
					    	wholeImpCorrAmpl [1] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]];
		//			    	wholeImpAmpl [1] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]] + powerSpectrumMinimum [wholeImpFreqs[1]];
					    	wholeImpCorrSharp [1] = 0.0;
					    	ctr = 0;
					    	if(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]-1] != 0.0){
					    		wholeImpCorrSharp [1] += Math.abs(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]] - assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]-1]);
					    		ctr++;
					    	}
					    	if(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]+1] != 0.0){
					    		wholeImpCorrSharp [1] += Math.abs(assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]+1] - assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]]);
					    		ctr++;
					    	}
					    	if(ctr != 0) wholeImpCorrSharp [1] /= (double) ctr;					    		
				    	}else {
				    		wholeImpAmpl [1] = assembledPowerSpectrumFromWholeImpCorr [wholeImpFreqs[1]];
				    	}
				    }else{
				    	wholeImpAmpl [1] = 0.0;
				    	if(removeBackgroundInSpectra) {
					    	wholeImpCorrAmpl [1] = 0.0;
					    	wholeImpCorrSharp [1] = 0.0;				    		
				    	}
				    }		   	
				}
			   	
				//GET Phases for specific frequency
				locFreqs = new double [4];
				if(manualROI) {
		   			locFreqs [0] = roiFreqs [0] * sampleRate / assembledPowerSpectrumFromRoi.length;
		   			locFreqs [1] = roiFreqs [1] * sampleRate / assembledPowerSpectrumFromRoi.length;					
				}else {
					locFreqs [0] = 0.0;
					locFreqs [1] = 0.0;
				}
	   			
	   			locFreqs [2] = wholeImpFreqs [0] * sampleRate / assembledPowerSpectrumFromWholeImp.length;
	   			locFreqs [3] = wholeImpFreqs [1] * sampleRate / assembledPowerSpectrumFromWholeImp.length;
	   			
   				phaseMapRoiFreqs = new double [0][0][0];
   				phaseMapWholeImpFreqs = new double [0][0][0];
	   			if(enhancedOutput) {
					if(manualROI) {
						phaseMapRoiFreqs = new double [imp.getWidth()][imp.getHeight()][2];
					}
					phaseMapWholeImpFreqs = new double [imp.getWidth()][imp.getHeight()][2];		   			
				   	valueColumn = new double [imp.getStackSize()];
		   			double [] phases;
					for(int x = 0; x < imp.getWidth(); x++){
				   		progress.addToBar(0.25/(double)imp.getWidth());
				   		for(int y = 0; y < imp.getHeight(); y++){
				   			if(!signalPostCorr[x][y]) continue;
				   			
				   			for(int i = 0; i < imp.getStackSize(); i++){
				   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
				   			}
				   			phases = getPhaseOfSpecificFrequencies(valueColumn, sampleRate, false, true, locFreqs);
				   			
				   			if(manualROI) {
					   			phaseMapRoiFreqs [x][y][0] = phases [0];
					   			phaseMapRoiFreqs [x][y][1] = phases [1];			   				
				   			}
				   			phaseMapWholeImpFreqs [x][y][0] = phases [2];
				   			phaseMapWholeImpFreqs [x][y][1] = phases [3];
					   	}	
				   	}
	   			}else {
	   				progress.addToBar(0.25);
	   			}
				
		   	/******************************************************************
			*** 							Output							***	
			*******************************************************************/
				
				Date endDate = new Date();			
				
				//save metadata
				tp1 = new TextPanel("results");
				tp2 = new TextPanel("results w");
				tp3 = new TextPanel("results r");
				
				tp1.append("Saving date:	" + FullDateFormatter.format(endDate)
							+ "	Starting date:	" + FullDateFormatter.format(startDate));
				tp1.append("Image name:	" + name[task]);
				tp1.append("");
				tp1.append("Parameters");
				tp1.append("	sampling rate (Hz)	" + constants.df6US.format(sampleRate));
				tp1.append("	sliding window size for smoothing power spectrum (Hz)	" + constants.df6US.format(smoothFreq)	+	"	=	" + constants.df0.format(smoothWindowSize)	+ "	(if = 0 -> no smoothing)");
				tp1.append("	max accepted frequency for filtering (Hz)	" + constants.df6US.format(upperLimit));
				tp1.append("	min accepted frequency for filtering (Hz)	" + constants.df6US.format(lowerLimit));
				tp1.append("	histogram bin size (Hz)	" + constants.df6US.format(histogramBins));
				tp1.append("	output complex maps	" + enhancedOutput);
				
				if(extractSignalsUsingSD) {
					tp1.append("	Extract signal regions using local SD filter");
					tp1.append("		Local SD filter - SD threshold:	" + constants.df3US.format(kernelSDThreshold));
					tp1.append("		Local SD filter - Increase range:	" + extendRange);
				}else {
					tp1.append("");
					tp1.append("");
					tp1.append("");
				}
				
				tp1.append("");
				if(removeBackgroundInSpectra) {
					tp1.append("	Remove background in power spectra - parameters");
					tp1.append("		Percent lower powers used for thresholding power spectrum	" + percentLowest);
					tp1.append("		SD fold used for power thresholding	" + SDlimitForSpectrumBG);
					tp1.append("	Remove background in power spectra - estimations");
					tp1.append("		average of lower powers	" + constants.df6US.format(lowerPowerAvgSD[0]));
					tp1.append("		standard deviation of lower powers	" + constants.df6US.format(lowerPowerAvgSD[1]));
				}else {
					tp1.append("	No background-removal performed!");
					tp1.append("");
					tp1.append("");
					tp1.append("");
					tp1.append("");
					tp1.append("");
				}
				
				tp1.append("");
				tp1.append("Result (for entire image)");
				String appText = "";
				appText = ("	average freq (Hz)"
						+ "	median freq (Hz)"
						+ "	SD of freq (Hz)"
						+ "	min freq (Hz)"
						+ "	max freq (Hz)"
						+ "	lower freq in APS (Hz)"
						+ "	power of lower freq. in APS (dB)"
						+ "	higher freq in APS (Hz)"
						+ "	power of higher freq. in APS (dB)"
						+ "	COM of APS"
						+ "	primary freq in APS (Hz)"
						+ "	power of prim. freq. in APS (dB)");
				if(removeBackgroundInSpectra) {
					appText +="	corrected power of prim. freq. in APS (dB)";					
				}else {
					appText += "	";
				}
				appText +="	secondary freq in APS (Hz)"
						+ "	power of sec. freq. in APS (dB)";
				if(removeBackgroundInSpectra) {
					appText +="	corrected power of sec. freq. in APS (dB)"				
						+ "	corr. sharpness of prim. freq. in APS (dB)"
						+ "	corr. sharpness of sec. freq. in APS (dB)";
				}else {
					appText += "			";
				}
				tp1.append(appText);
				if(assembledPowerSpectrumFromWholeImp != null){
						appText = "	" + constants.df6US.format(wholeImpAverage)
						+ "	" + constants.df6US.format(wholeImpMedian)
						+ "	" + constants.df6US.format(wholeImpSD)
						+ "	" + constants.df6US.format(wholeImpMin)
						+ "	" + constants.df6US.format(wholeImpMax);
					if(wholeImpFreqs [0] < wholeImpFreqs [1]){
						appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(wholeImpFreqs [0] * sampleRate / assembledPowerSpectrumFromWholeImp.length);
						appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpAmpl [0])*10);
						appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(wholeImpFreqs [1] * sampleRate / assembledPowerSpectrumFromWholeImp.length);
						appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpAmpl [1])*10);
						appText += "	"; appText += constants.df6US.format(wholeImpCom * sampleRate / assembledPowerSpectrumFromWholeImp.length);			
					}else{
						appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(wholeImpFreqs [1] * sampleRate / assembledPowerSpectrumFromWholeImp.length);
						appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpAmpl [1])*10);
						appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(wholeImpFreqs [0] * sampleRate / assembledPowerSpectrumFromWholeImp.length);
						appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpAmpl [0])*10);
						appText += "	"; appText += constants.df6US.format(wholeImpCom * sampleRate / assembledPowerSpectrumFromWholeImp.length);			
					}
					appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(wholeImpFreqs [0] * sampleRate / assembledPowerSpectrumFromWholeImp.length);
					appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpAmpl [0])*10);
					if(removeBackgroundInSpectra) {
						appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpCorrAmpl [0])*10);
					}else {
						appText += "	";
					}
					appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(wholeImpFreqs [1] * sampleRate / assembledPowerSpectrumFromWholeImp.length);
					appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpAmpl [1])*10);
					if(removeBackgroundInSpectra) {
						appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpCorrAmpl [1])*10);
						appText += "	"; if(wholeImpFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpCorrSharp [0])*10);
						appText += "	"; if(wholeImpFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(wholeImpCorrSharp [1])*10);						
					}else {
						appText += "			";
					}
					
				}			
				tp1.append(appText);
				tp2.append(name[task] + appText);
							
				tp1.append("");
				tp1.append("	Assembled power spectrum (APS) in entire image");
				if(assembledPowerSpectrumFromWholeImp != null){
					appText = "frequency (Hz)";
					for(int i = 0; i < assembledPowerSpectrumFromWholeImp.length; i++){
						appText += "	" + constants.df6US.format((double)i * sampleRate / (double) assembledPowerSpectrumFromWholeImp.length);
				 	}
					tp1.append(appText);
					appText = "raw power (dB)";
					for(int i = 0; i < assembledPowerSpectrumFromWholeImp.length; i++){
						appText += "	";	if(assembledPowerSpectrumFromWholeImp[i]!=0.0)	appText += constants.df6US.format(Math.log10(assembledPowerSpectrumFromWholeImp[i])*10);
				 	}
					tp1.append(appText);
					if(removeBackgroundInSpectra) {
						appText = "corrected power (dB)";
						for(int i = 0; i < assembledPowerSpectrumFromWholeImpCorr.length; i++){
							appText += "	";	if(assembledPowerSpectrumFromWholeImpCorr[i]!=0.0)	appText += constants.df6US.format(Math.log10(assembledPowerSpectrumFromWholeImpCorr[i])*10);
					 	}
						tp1.append(appText);
					}else {
						tp1.append("");
					}					
				}else{
					tp1.append("");
					tp1.append("");
					tp1.append("");
				}
				
				if(manualROI) {
					tp1.append("");
					tp1.append("Result (for selected ROI)");
					appText = ("	average freq (Hz)"
							+ "	median freq (Hz)"
							+ "	SD of freq (Hz)"
							+ "	min freq (Hz)"
							+ "	max freq (Hz)"
							+ "	lower freq in APS (Hz)"
							+ "	power of lower freq. in APS (dB)"
							+ "	higher freq in APS (Hz)"
							+ "	power of higher freq. in APS (dB)"
							+ "	COM of APS"
							+ "	primary freq in APS (Hz)"
							+ "	power of prim. freq. in APS (dB)");
					if(removeBackgroundInSpectra) {
						appText +="	corrected power of prim. freq. in APS (dB)";					
					}else {
						appText += "	";
					}
					appText +="	secondary freq in APS (Hz)"
							+ "	power of sec. freq. in APS (dB)";
					if(removeBackgroundInSpectra) {
						appText +="	corrected power of sec. freq. in APS (dB)"
								+ "	corr. sharpness of prim. freq. in APS (dB)"
								+ "	corr. sharpness of sec. freq. in APS (dB)";
					}		else {
						appText += "			";
					}		
					tp1.append(appText);
					
					if(!assembledPowerSpectrumFromRoi.equals(null) && assembledPowerSpectrumFromRoi.length != 0){
						appText = "	" + constants.df6US.format(roiAverage)
						+ "	" + constants.df6US.format(roiMedian)
						+ "	" + constants.df6US.format(roiSD)
						+ "	" + constants.df6US.format(roiMin)
						+ "	" + constants.df6US.format(roiMax);
						if(roiFreqs [0] < roiFreqs [1]){
							appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(roiFreqs [0] * sampleRate / assembledPowerSpectrumFromRoi.length);
							appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(roiAmpl [0])*10);
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(roiFreqs [1] * sampleRate / assembledPowerSpectrumFromRoi.length);
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(roiAmpl [1])*10);
							appText += "	"; appText += constants.df6US.format(roiCom * sampleRate / assembledPowerSpectrumFromRoi.length);
			
						}else{
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(roiFreqs [1] * sampleRate / assembledPowerSpectrumFromRoi.length);
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(roiAmpl [1])*10);
							appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(roiFreqs [0] * sampleRate / assembledPowerSpectrumFromRoi.length);
							appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(roiAmpl [0])*10);
							appText += "	"; appText += constants.df6US.format(roiCom * sampleRate / assembledPowerSpectrumFromRoi.length);			
						}
						appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(roiFreqs [0] * sampleRate / assembledPowerSpectrumFromRoi.length);
						appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(roiAmpl [0])*10);
						if(removeBackgroundInSpectra) {
							appText += "	"; if(roiFreqs[0]!=-1)	appText += constants.df6US.format(Math.log10(roiCorrAmpl [0])*10);
						}else {
							appText += "	";
						}
						appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(roiFreqs [1] * sampleRate / assembledPowerSpectrumFromRoi.length);
						appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(roiAmpl [1])*10);
						if(removeBackgroundInSpectra) {
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(roiCorrAmpl [1])*10);
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(roiCorrSharp [0])*10);
							appText += "	"; if(roiFreqs[1]!=-1)	appText += constants.df6US.format(Math.log10(roiCorrSharp [1])*10);						
						}else {
							appText += "			";
						}
					}		
					tp1.append(appText);
					tp3.append(name[task] + appText);
					
					tp1.append("");
					tp1.append("	Assembled power spectrum (APS) in Roi");
					if(assembledPowerSpectrumFromRoi != null){
						appText = "frequency (Hz)";
						for(int i = 0; i < assembledPowerSpectrumFromRoi.length; i++){
							appText += "	" + constants.df6US.format((double)i * sampleRate / (double) assembledPowerSpectrumFromRoi.length);
					 	}
						tp1.append(appText);
						appText = "raw power (dB)";
						for(int i = 0; i < assembledPowerSpectrumFromRoi.length; i++){
							appText += "	";	if(assembledPowerSpectrumFromRoi[i]!=0.0)	appText += constants.df6US.format(Math.log10(assembledPowerSpectrumFromRoi[i])*10);
					 	}
						tp1.append(appText);
						if(removeBackgroundInSpectra) {
							appText = "corrected power (dB)";
							for(int i = 0; i < assembledPowerSpectrumFromRoiCorr.length; i++){
								appText += "	";	if(assembledPowerSpectrumFromRoiCorr[i]!=0.0)	appText += constants.df6US.format(Math.log10(assembledPowerSpectrumFromRoiCorr[i])*10);
						 	}
							tp1.append(appText);
						}else {
							tp1.append("");
						}					
					}else{
						tp1.append("");
						tp1.append("");
						tp1.append("");
					}							
				}
						
				/**
				 * No longer add detailed results to main file from v0.3.0
				 * */
//				addDetailedFrequencyMaps(tp1, imp, signalPostCorr, corrFreqRes);
				
				if(removeBackgroundInSpectra) {
					tp1.append("");
					tp1.append("	Background Power Spectrum");
					appText = "	frequency (Hz)";
					for(int i = 0; i < powerSpectrumBackground[0].length; i++){
						appText += "	" + constants.df6US.format((double)i * sampleRate / (double) powerSpectrumBackground[0].length);
				 	}
					tp1.append(appText);
					appText = "	average power (dB)";
					for(int i = 0; i < powerSpectrumBackground[0].length; i++){
						appText += "	" + constants.df6US.format(Math.log10(powerSpectrumBackground [0][i])*10);
				 	}
					tp1.append(appText);
					appText = "	SD of power (dB)";
					for(int i = 0; i < powerSpectrumBackground[0].length; i++){
						appText += "	" + constants.df6US.format(Math.log10(powerSpectrumBackground [1][i])*10);
				 	}
					tp1.append(appText);
				}
				
				
	//			tp1.append("	Minimum Power Spectrum");
	//			appText = "	frequency (Hz)";
	//			for(int i = 0; i < assembledPowerSpectrumFromWholeImp.length; i++){
	//				appText += "	" + constants.df6US.format((double)i * sampleRate / (double) powerSpectrumMinimum.length);
	//		 	}
	//			tp1.append(appText);
	//			appText = "	minimum power (dB)";
	//			for(int i = 0; i < assembledPowerSpectrumFromWholeImp.length; i++){
	//				appText += "	" + constants.df6US.format(Math.log10(powerSpectrumMinimum [i])*10);
	//		 	}
	//			tp1.append(appText);
				
				addFooter(tp1, endDate);				
				tp1.saveAs(filePrefix + ".txt");
				tp2.saveAs(filePrefix + "_1w.txt");
				if(manualROI) {
					tp3.saveAs(filePrefix + "_1r.txt");
				}
	//			IJ.saveAsTiff(imp, filePrefix + ".tif");
				
				
				/**
				 * Generate output maps
				 * */
				{
					save2DPlot(rawFreqRes, 0,0, "1st freq (Hz)", filePrefix + "_f1", true, lowerLimit, upperLimit, "Jet");
					save2DPlot(rawFreqRes, 2,2, "2nd freq (Hz)", filePrefix + "_f2", true, lowerLimit, upperLimit, "Jet");
					save2DPlot(rawFreqRes, 1,1, "power 1", filePrefix + "_f1p", true, 0.0, Double.POSITIVE_INFINITY, "Jet");
					save2DPlot(rawFreqRes, 3,3, "power 2", filePrefix + "_f2p", true, 0.0, Double.POSITIVE_INFINITY, "Jet");
					if(enhancedOutput) {
						save2DPlot(rawFreqRes, 4,4, "com freq (Hz)", filePrefix + "_com", true, 0.0, sampleRate / 2.0, "Jet");
						save2DPlot(rawFreqRes, 5,0, "phase of 1st freq (rad)", filePrefix + "_ph1", true, -Math.PI, Math.PI, "Spectrum");
						save2DPlot(rawFreqRes, 6,2, "phase of 2nd freq (rad)", filePrefix + "_ph2",true, -Math.PI, Math.PI, "Spectrum");
		//				save2DPlot(evMapRaw, "eigenvec ph1 (rad)", filePrefix + "_ev1", true, -Math.PI, Math.PI, "Spectrum");
					}
					if(removeBackgroundInSpectra) {
						saveBooleanAsPlot(signal, "signal region", filePrefix + "_signal");
					}
					save2DPlot(corrFreqRes, 0,0, "1st freq (Hz)", filePrefix + "_f1_c", true, lowerLimit, upperLimit, "Jet");
					save2DPlot(corrFreqRes, 2,2, "2nd freq (Hz)", filePrefix + "_f2_c", true, lowerLimit, upperLimit, "Jet");
					save2DPlot(corrFreqRes, 1,1, "power 1", filePrefix + "_f1p_c", true, 0.0, Double.POSITIVE_INFINITY, "Jet");
					save2DPlot(corrFreqRes, 3,3, "power 2", filePrefix + "_f2p_c", true, 0.0, Double.POSITIVE_INFINITY, "Jet");
					if(enhancedOutput) {
						save2DPlot(corrFreqRes, 4,4, "com freq (Hz)", filePrefix + "_com_c", true, lowerLimit, upperLimit, "Jet");
						save2DPlot(corrFreqRes, 5,0, "phase of 1st freq (rad)", filePrefix + "_ph1_c", true, -Math.PI, Math.PI, "Spectrum");
						save2DPlot(corrFreqRes, 6,2, "phase of 2nd freq (rad)", filePrefix + "_ph2_c",true, -Math.PI, Math.PI, "Spectrum");
					}
		//			save2DPlot(evMapCorr, "eigenvec ph1 (rad)", filePrefix + "_ev1_c", true, -Math.PI, Math.PI, "Spectrum");
					saveBooleanAsPlot(signalPostCorr, "signal region (corr)", filePrefix + "_signal_c");
					
					if(enhancedOutput) {
						if(manualROI) {
							save2DPlotMasked(phaseMapRoiFreqs,corrFreqRes, 0,0, "phase at " + locFreqs [0] + " Hz (rad)", filePrefix + "_ph_freq1_r", -Math.PI, Math.PI, "Spectrum");
							save2DPlotMasked(phaseMapRoiFreqs,corrFreqRes, 1,2, "phase at " + locFreqs [1] + " Hz (rad)", filePrefix + "_ph_freq2_r", -Math.PI, Math.PI, "Spectrum");
						}
						save2DPlotMasked(phaseMapWholeImpFreqs,corrFreqRes, 0,0, "phase at " + locFreqs [2] + " Hz (rad)", filePrefix + "_ph_freq1_w", -Math.PI, Math.PI, "Spectrum");
						save2DPlotMasked(phaseMapWholeImpFreqs,corrFreqRes, 1,2, "phase at " + locFreqs [3] + " Hz (rad)", filePrefix + "_ph_freq2_w", -Math.PI, Math.PI, "Spectrum");
					}
					
					//save these plots also as text files
					save2DPlotAsText(rawFreqRes, 0,0, "1st freq (Hz)", filePrefix + "_f1", true, 0.0, sampleRate / 2.0, endDate);
					save2DPlotAsText(rawFreqRes, 2,2, "2nd freq (Hz)", filePrefix + "_f2", true, 0.0, sampleRate / 2.0, endDate);
					save2DPlotAsText(rawFreqRes, 1,1, "power 1", filePrefix + "_f1p", true, 0.0, Double.POSITIVE_INFINITY, endDate);
					save2DPlotAsText(rawFreqRes, 3,3, "power 2", filePrefix + "_f2p", true, 0.0, Double.POSITIVE_INFINITY, endDate);
					if(enhancedOutput) {
						save2DPlotAsText(rawFreqRes, 4,4, "com freq (Hz)", filePrefix + "_com", true, 0.0, sampleRate / 2.0, endDate);
						save2DPlotAsText(rawFreqRes, 5,5, "phase of 1st freq (rad)", filePrefix + "_ph1", true, -Math.PI, Math.PI, endDate);
						save2DPlotAsText(rawFreqRes, 6,6, "phase of 2nd freq (rad)", filePrefix + "_ph2", true, -Math.PI, Math.PI, endDate);
					}
					
					save2DPlotAsText(corrFreqRes, 0,0, "1st freq (Hz)", filePrefix + "_f1_c", true, 0.0, sampleRate / 2.0, endDate);
					save2DPlotAsText(corrFreqRes, 2,2, "2nd freq (Hz)", filePrefix + "_f2_c", true, 0.0, sampleRate / 2.0, endDate);
					save2DPlotAsText(corrFreqRes, 1,1, "power 1", filePrefix + "_f1p_c", true, 0.0, Double.POSITIVE_INFINITY, endDate);
					save2DPlotAsText(corrFreqRes, 3,3, "power 2", filePrefix + "_f2p_c", true, 0.0, Double.POSITIVE_INFINITY, endDate);
					if(enhancedOutput) {
						save2DPlotAsText(corrFreqRes, 4,4, "com freq (Hz)", filePrefix + "_com_c", true, 0.0, sampleRate / 2.0, endDate);
						save2DPlotAsText(corrFreqRes, 5,0, "phase of 1st freq (rad)", filePrefix + "_ph1_c", true, -Math.PI, Math.PI, endDate);
						save2DPlotAsText(corrFreqRes, 6,2, "phase of 2nd freq (rad)", filePrefix + "_ph2_c",true, -Math.PI, Math.PI, endDate);
					}
					
					if(enhancedOutput) {
						if(manualROI) {
							save2DPlotAsTextMasked(phaseMapRoiFreqs,corrFreqRes, 0,0, "phase at " + locFreqs [0] + " Hz (rad)", filePrefix + "_ph_freq1_r", -Math.PI, Math.PI, endDate);
							save2DPlotAsTextMasked(phaseMapRoiFreqs,corrFreqRes, 1,2, "phase at " + locFreqs [1] + " Hz (rad)", filePrefix + "_ph_freq2_r", -Math.PI, Math.PI, endDate);
						}
					save2DPlotAsTextMasked(phaseMapWholeImpFreqs,corrFreqRes, 0,0, "phase at " + locFreqs [2] + " Hz (rad)", filePrefix + "_ph_freq1_w", -Math.PI, Math.PI, endDate);
					save2DPlotAsTextMasked(phaseMapWholeImpFreqs,corrFreqRes, 1,2, "phase at " + locFreqs [3] + " Hz (rad)", filePrefix + "_ph_freq2_w", -Math.PI, Math.PI, endDate);
					}
				}
				
				/**
				 * Plot power spectra
				 * */
				xValues = new double [(int)(assembledPowerSpectrumFromWholeImp.length/2.0)];
				yValues = new double [1][(int)(assembledPowerSpectrumFromWholeImp.length/2.0)];
				for(int i = 0; i < xValues.length; i++){
					xValues [i] = (double)i * sampleRate / (double) assembledPowerSpectrumFromWholeImp.length;
				}
				
				if(removeBackgroundInSpectra) {
					for(int i = 0; i < xValues.length; i++){
						yValues [0][i] = Math.log10(powerSpectrumBackground[0][i])*10;
					}
					plot2DArray(xValues, yValues, "Power Spectra Background (Mean)", "frequency (Hz)", "mean background power (dB)", filePrefix + "_bg_avg", true, new String[]{""});
						
					for(int i = 0; i < xValues.length; i++){
						yValues [0][i] = Math.log10(powerSpectrumBackground[1][i])*10;
					}
					plot2DArray(xValues, yValues, "Power Spectra Background (SD)", "frequency (Hz)", "SD of background power (dB)", filePrefix + "_bg_sd", true, new String[]{""});
				}
				
				double addLines []; String addLinesLabels [];
				if(assembledPowerSpectrumFromWholeImp != null){
					for(int i = 0; i < xValues.length; i++){
						if(assembledPowerSpectrumFromWholeImp[i] != 0.0) {
							yValues [0][i] = Math.log10(assembledPowerSpectrumFromWholeImp[i])*10;
						}else {
							yValues [0][i] = 0.0;
						}
				 	}
					addLines = new double [] {wholeImpFreqs [0] * sampleRate / assembledPowerSpectrumFromWholeImp.length, wholeImpFreqs [1] * sampleRate / assembledPowerSpectrumFromWholeImp.length};
					addLinesLabels = new String [] {"prim. freq. (APS)","sec. freq. (APS)"};
										
					if(removeBackgroundInSpectra) {				
						plot2DArray(xValues, yValues, "Power Spectrum - entire image", "frequency (Hz)", "power (dB)", filePrefix + "_psw",
								true, new String[]{"APS"});
						
						for(int i = 0; i < xValues.length; i++){
							yValues [0][i] = Math.log10(assembledPowerSpectrumFromWholeImpCorr[i])*10;
					 	}
						plot2DArrayWithAdditionalLines(xValues, yValues, "Power Spectrum - entire image", "frequency (Hz)", "corrected power (dB)", filePrefix + "_psw_c",
								true, new String[]{"corr. APS"}, addLines, addLinesLabels);
					}else {
						plot2DArrayWithAdditionalLines(xValues, yValues, "Power Spectrum - entire image", "frequency (Hz)", "power (dB)", filePrefix + "_psw",
								true, new String[]{"APS"}, addLines, addLinesLabels);
					}
				}			
				
				if(manualROI & !assembledPowerSpectrumFromRoi.equals(null)){
					for(int i = 0; i < xValues.length; i++){
						yValues [0][i] = Math.log10(assembledPowerSpectrumFromRoi[i])*10;
				 	}
					addLines = new double [] {roiFreqs [0] * sampleRate / assembledPowerSpectrumFromRoi.length, roiFreqs [1] * sampleRate / assembledPowerSpectrumFromRoi.length};
					addLinesLabels = new String [] {"prim. freq. (Roi PS)","sec. freq. (Roi PS)"};
					
					if(removeBackgroundInSpectra) {	
						plot2DArray(xValues, yValues, "Power Spectrum - entire roi", "frequency (Hz)", "power (dB)", filePrefix + "_psr",
								true, new String[]{""});	
						
						for(int i = 0; i < xValues.length; i++){
							yValues [0][i] = Math.log10(assembledPowerSpectrumFromRoiCorr[i])*10;
					 	}
						plot2DArrayWithAdditionalLines(xValues, yValues, "Power Spectrum - entire roi", "frequency (Hz)", "corrected power (dB)", filePrefix + "_psr_c",
								true, new String[]{"Roi PS"}, addLines, addLinesLabels);
					}else {
						plot2DArrayWithAdditionalLines(xValues, yValues, "Power Spectrum - entire roi", "frequency (Hz)", "power (dB)", filePrefix + "_psr",
								true, new String[]{"Roi PS"}, addLines, addLinesLabels);	
						
					}
				}
				
				re = new RoiEncoder(filePrefix + "_roi");
				if(manualROI) {
					try {
						re.write(selections [task]);
					} catch (IOException e) {
						IJ.error("ROI Manager", e.getMessage());
					}
				}
			
			/******************************************************************
			*** 							Finish							***	
			*******************************************************************/			
				{
					imp.unlock();	
					if(selectedTaskVariant.equals(taskVariant[1])){
						imp.changes = false;
						imp.close();
					}
					break running;
				}
			}catch(Exception e){
				String out = "" + e.getMessage() + "\n";
				out += "" + e.getLocalizedMessage() + "\n";
				for(int err = 0; err < e.getStackTrace().length; err++){
					out += " \n " + e.getStackTrace()[err].toString();
				}			
				progress.notifyMessage("Analysis of image " + name[task] + " failed - error message: \n"
					+ "" + out,ProgressDialog.ERROR);

				imp.unlock();	
				if(selectedTaskVariant.equals(taskVariant[1])){
					imp.changes = false;
					imp.close();
				}
				break running;
			}
		}
		processingDone = true;
		progress.updateBarText("finished!");
		progress.setBar(1.0);
		progress.moveTask(task);
		System.gc();
	}
}

/**
 * No longer needed to be added to output main text file from version v0.3.0 on.
 * @deprecated
 * 	//TODO remove?
 * */
private void addDetailedFrequencyMaps(TextPanel tp1, ImagePlus imp, boolean signalPostCorr [][], double corrFreqRes [][][]) {
	String appText;
	tp1.append("Detailed results");
	tp1.append("	Primary Frequency (in Hz, post filtering)");
	for(int y = 0; y < imp.getHeight(); y++){
		appText = "";
		for(int x = 0; x < imp.getWidth(); x++){
			appText += "	";
			if(signalPostCorr [x][y] && corrFreqRes [x][y][0]!=0.0
					&& corrFreqRes [x][y][0] >= lowerLimit && corrFreqRes [x][y][0] <= upperLimit){
				appText += constants.df6US.format(corrFreqRes [x][y][0]);
			}
   		}
		tp1.append(appText);
 	}
	
	tp1.append("");
	tp1.append("	Power of primary frequency post filtering (dB)");
	for(int y = 0; y < imp.getHeight(); y++){
		appText = "";
		for(int x = 0; x < imp.getWidth(); x++){
			appText += "	";
			if(signalPostCorr [x][y] && corrFreqRes [x][y][0]!=0.0
					&& corrFreqRes [x][y][0] >= lowerLimit && corrFreqRes [x][y][0] <= upperLimit){
				appText += constants.df6US.format(Math.log10(corrFreqRes [x][y][1])*10);
			}
   		}
		tp1.append(appText);
 	}
	
	tp1.append("");
	tp1.append("	Secondary Frequency (in Hz, post filtering)");
	for(int y = 0; y < imp.getHeight(); y++){
		appText = "";
		for(int x = 0; x < imp.getWidth(); x++){
			appText += "	";
			if(signalPostCorr [x][y] && corrFreqRes [x][y][0]!=0.0
					&& corrFreqRes [x][y][0] >= lowerLimit && corrFreqRes [x][y][0] <= upperLimit){
				appText += constants.df6US.format(corrFreqRes [x][y][2]);
			}
   		}
		tp1.append(appText);
 	}
	
	tp1.append("");
	tp1.append("	Power of secondary frequency post filtering (dB)");
	for(int y = 0; y < imp.getHeight(); y++){
		appText = "";
		for(int x = 0; x < imp.getWidth(); x++){
			appText += "	";
			if(signalPostCorr [x][y] && corrFreqRes [x][y][0]!=0.0
					&& corrFreqRes [x][y][0] >= lowerLimit && corrFreqRes [x][y][0] <= upperLimit){
				appText += constants.df6US.format(Math.log10(corrFreqRes [x][y][3])*10);
			}
   		}
		tp1.append(appText);
 	}
	
	tp1.append("");			
	tp1.append("	COM of frequencies (in Hz, post filtering)");
	for(int y = 0; y < imp.getHeight(); y++){
		appText = "";
		for(int x = 0; x < imp.getWidth(); x++){
			appText += "	";
			if(signalPostCorr [x][y] && corrFreqRes [x][y][0]!=0.0
					&& corrFreqRes [x][y][0] >= lowerLimit && corrFreqRes [x][y][0] <= upperLimit){
				appText += constants.df6US.format(corrFreqRes [x][y][4]);
			}
   		}
		tp1.append(appText);
 	}
	
}
private void addFooter(TextPanel tp, Date currentDate){
	tp.append("");
	tp.append("Datafile was generated on " + FullDateFormatter2.format(currentDate) + " by '"+PLUGINNAME+"', an ImageJ plug-in by Jan Niklas Hansen (jan.hansen@uni-bonn.de). More information: " + PLUGINHTML + "");
	tp.append("The plug-in '"+PLUGINNAME+"' is distributed in the hope that it will be useful,"
			+ " but WITHOUT ANY WARRANTY; without even the implied warranty of"
			+ " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
	tp.append("Plug-in version:	V"+PLUGINVERSION);	
}

/**
 * @deprecated
 * Not fully developed
 * */
private static double [] getFrequenciesWithPower (double [] values, double sampleRate, boolean showPlot, boolean normalizePlusMinus, int smoothWindowSize, double lowerLimit, double upperLimit){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] magnitude = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
  		if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
  			double max = tools.getMaximum(fft)/2.0;
  			for(int i = 0; i < fft.length; i++){
  				fft [i] -= max;
  			}
  		}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	magnitude [j] = Math.sqrt(real*real+imaginary*imaginary) / magnitude.length;
    }
    
    //Smooth magnitude
    magnitude = smooth(magnitude, smoothWindowSize);
    
    //display as plot
    if(showPlot)	tools.showAsPlot(magnitude);
    
    int [] freqs = tools.get2HighestMaximaIndicesWithinRange(magnitude,
    		(int)Math.round(lowerLimit*(magnitude.length/sampleRate)), 
		(int)Math.round(upperLimit*(magnitude.length/sampleRate)));
    double [] ampl = new double [2];
    double com = tools.getCenterOfMassOfRange(magnitude, 0, (magnitude.length/2)-1);
    if(freqs[0] >= 0 && freqs [0] < magnitude.length){
    	ampl [0] = magnitude [freqs[0]] * 2.0;
    }else{
    	ampl [0] = 0.0;
    }
    if(freqs[1] >= 0 && freqs [1] < magnitude.length){
    	ampl [1] = magnitude [freqs[1]] * 2.0;
    }else{
    	ampl [1] = 0.0;
    }
    if(freqs [0] < freqs [1]){
   	 return new double [] {freqs [0] * sampleRate / magnitude.length, ampl [0],
   	    		freqs [1] * sampleRate / magnitude.length, ampl [1], com * sampleRate / magnitude.length};
    }else{
   	 return new double [] {freqs [1] * sampleRate / magnitude.length, ampl [1],
   	    		freqs [0] * sampleRate / magnitude.length, ampl [0], com * sampleRate / magnitude.length};
   }
}

/**
 * Introduced in version v0.2.1 to extract the phase
 * @returns a one-dimensional double array containing (in the order as follows): frequency of first peak, amplitude of first peak, freq of second peak, amplitude of second peak,
 * 	centre of mass of frequency spectrum, phase of first peak, phase of second peak.
 * 
 * */
private static double [] getFrequenciesWithPowerAndPhase (double [] values, double sampleRate, boolean showPlot, 
		boolean normalizePlusMinus, int smoothWindowSize, double lowerLimit, double upperLimit){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] magnitude = new double[values.length];
    double [] phase = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
  		if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
  			double max = tools.getMaximum(fft)/2.0;
  			for(int i = 0; i < fft.length; i++){
  				fft [i] -= max;
  			}
  		}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	phase [j] =  Math.atan2(imaginary, real);
    	magnitude [j] = Math.sqrt(real*real+imaginary*imaginary) / magnitude.length;
    }
    
    //Smooth magnitude
    magnitude = smooth(magnitude, smoothWindowSize);
    
    //display as plot
    if(showPlot)	tools.showAsPlot(magnitude);
    
    int [] freqs = tools.get2HighestMaximaIndicesWithinRange(magnitude,
    		(int)Math.round(lowerLimit*(magnitude.length/sampleRate)), 
		(int)Math.round(upperLimit*(magnitude.length/sampleRate)));
    double [] ampl = new double [2];
    double [] phases = new double [2];
    double com = tools.getCenterOfMassOfRange(magnitude, 0, (magnitude.length/2)-1);
    if(freqs[0] >= 0 && freqs [0] < magnitude.length){
    	ampl [0] = magnitude [freqs[0]] * 2.0;
    	phases [0] = phase[freqs[0]];
    }else{
    	ampl [0] = 0.0;
    }
    if(freqs[1] >= 0 && freqs [1] < magnitude.length){
    	ampl [1] = magnitude [freqs[1]] * 2.0;
    	phases [1] = phase[freqs[1]];
    }else{
    	ampl [1] = 0.0;
    }
    if(freqs [0] < freqs [1]){
   	 return new double [] {freqs [0] * sampleRate / magnitude.length, ampl [0],
   	    		freqs [1] * sampleRate / magnitude.length, ampl [1], com * sampleRate / magnitude.length,
   	    		phases [0], phases [1]};
    }else{
   	 return new double [] {freqs [1] * sampleRate / magnitude.length, ampl [1],
   	    		freqs [0] * sampleRate / magnitude.length, ampl [0], com * sampleRate / magnitude.length,
	    		phases [1], phases [0]};
   }
}

public static double [] getFrequencySpectrum (double [] values, boolean showPlot, boolean normalizePlusMinus, int smoothWindowSize){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] magnitude = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
  		if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
  			double max = tools.getMaximum(fft)/2.0;
  			for(int i = 0; i < fft.length; i++){
  				fft [i] -= max;
  			}
  		}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	magnitude [j] = Math.sqrt(real*real+imaginary*imaginary) / magnitude.length * 2.0;
    }
    magnitude = smooth(magnitude, smoothWindowSize);
    
    //display as plot
    if(showPlot)	tools.showAsPlot(magnitude);
    
   return magnitude;
}

/**
 * Only considers peaks above average + SDLimit*SD of power Spectrum Background 
 * SDLimit = 1.5 -> FDR = 4.4 %
 * SDLimit = 2 -> FDR = 1.7 %
 * SDLimit = 2.5 -> FDR = 0.5 %
 * SDLimit = 3 -> FDR = 0.1 %
 * */
public static double [] getSignificantFrequenciesWithPowers (double [] values, double sampleRate, boolean showPlot, boolean normalizePlusMinus, double [][] powerSpectrumBackground, 
		double SDLimit, double lowerLimit, double upperLimit, int smoothWindowSize){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] magnitude = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
  		if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
  			double max = tools.getMaximum(fft)/2.0;
  			for(int i = 0; i < fft.length; i++){
  				fft [i] -= max;
  			}
  		}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    boolean somethingRemains = false;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	magnitude [j] = Math.sqrt(real*real+imaginary*imaginary) / magnitude.length;
    	magnitude [j] -= powerSpectrumBackground [0][j] + SDLimit * powerSpectrumBackground [1][j]; 
    	if(magnitude [j] < 0.0){
    		magnitude [j] = 0.0;
    	}else{    		
    		somethingRemains = true;
    	}    	
    }    
    if(!somethingRemains)	return new double [] {0.0, 0.0, 0.0, 0.0, 0.0};
    magnitude = smooth(magnitude, smoothWindowSize);
    
    //display as plot
    if(showPlot)	tools.showAsPlot(magnitude);
    
    int [] freqs = tools.get2HighestMaximaIndicesWithinRange(magnitude,
    		(int)Math.round(lowerLimit*(magnitude.length/sampleRate)), 
		(int)Math.round(upperLimit*(magnitude.length/sampleRate)));
    double [] ampl = new double [2];
    double com = tools.getCenterOfMassOfRange(magnitude, 0, (magnitude.length/2)-1);
    if(freqs[0] >= 0 && freqs [0] < magnitude.length){
    	ampl [0] = 2.0 * (magnitude [freqs[0]] + powerSpectrumBackground [0][freqs[0]] + SDLimit * powerSpectrumBackground [1][freqs[0]]);
    }else{
    	ampl [0] = 0.0;
    }
    if(freqs[1] >= 0 && freqs [1] < magnitude.length){
    	ampl [1] = 2.0 * (magnitude [freqs[1]] + powerSpectrumBackground [0][freqs[1]] + SDLimit * powerSpectrumBackground [1][freqs[1]]);
    }else{
    	ampl [1] = 0.0;
    }
    if(freqs [0] < freqs [1]){
    	 return new double [] {freqs [0] * sampleRate / magnitude.length, ampl [0],
    	    		freqs [1] * sampleRate / magnitude.length, ampl [1], com * sampleRate / magnitude.length};
    }else{
    	 return new double [] {freqs [1] * sampleRate / magnitude.length, ampl [1],
    	    		freqs [0] * sampleRate / magnitude.length, ampl [0], com * sampleRate / magnitude.length};
    }
   
}

/**
 * Only considers peaks above average + SDLimit*SD of power Spectrum Background 
 * SDLimit = 1.5 -> FDR = 4.4 %
 * SDLimit = 2 -> FDR = 1.7 %
 * SDLimit = 2.5 -> FDR = 0.5 %
 * SDLimit = 3 -> FDR = 0.1 %
 * 
 * Function implemented from v0.2.1 on (first time phase is extracted)
 * */
public static double [] getSignificantFrequenciesWithPowersAndPhases (double [] values, double sampleRate, boolean showPlot, boolean normalizePlusMinus, double [][] powerSpectrumBackground, 
		double SDLimit, double lowerLimit, double upperLimit, int smoothWindowSize){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] magnitude = new double[values.length];
    double [] phase = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
  		if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
  			double max = tools.getMaximum(fft)/2.0;
  			for(int i = 0; i < fft.length; i++){
  				fft [i] -= max;
  			}
  		}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    boolean somethingRemains = false;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	phase [j] =  Math.atan2(imaginary, real);
    	magnitude [j] = Math.sqrt(real*real+imaginary*imaginary) / magnitude.length;
    	magnitude [j] -= powerSpectrumBackground [0][j] + SDLimit * powerSpectrumBackground [1][j]; 
    	if(magnitude [j] < 0.0){
    		magnitude [j] = 0.0;
    	}else{    		
    		somethingRemains = true;
    	}
    }    
    if(!somethingRemains)	return new double [] {0.0, 0.0, 0.0, 0.0, 0.0};
    magnitude = smooth(magnitude, smoothWindowSize);
    
    //display as plot
    if(showPlot)	tools.showAsPlot(magnitude);
    
    int [] freqs = tools.get2HighestMaximaIndicesWithinRange(magnitude,
    		(int)Math.round(lowerLimit*(magnitude.length/sampleRate)), 
		(int)Math.round(upperLimit*(magnitude.length/sampleRate)));
    double [] ampl = new double [2];
    double [] phases = new double [2];
    double com = tools.getCenterOfMassOfRange(magnitude, 0, (magnitude.length/2)-1);
    if(freqs[0] >= 0 && freqs [0] < magnitude.length){
    	ampl [0] = 2.0 * (magnitude [freqs[0]] + powerSpectrumBackground [0][freqs[0]] + SDLimit * powerSpectrumBackground [1][freqs[0]]);
    	phases [0] = phase[freqs[0]];
    }else{
    	ampl [0] = 0.0;
    }
    if(freqs[1] >= 0 && freqs [1] < magnitude.length){
    	ampl [1] = 2.0 * (magnitude [freqs[1]] + powerSpectrumBackground [0][freqs[1]] + SDLimit * powerSpectrumBackground [1][freqs[1]]);
    	phases [1] = phase[freqs[1]];
    }else{
    	ampl [1] = 0.0;
    }
    if(freqs [0] < freqs [1]){
    	 return new double [] {freqs [0] * sampleRate / magnitude.length, ampl [0],
    	    		freqs [1] * sampleRate / magnitude.length, ampl [1], com * sampleRate / magnitude.length,
    	    		phases [0], phases [1]};
    }else{
    	 return new double [] {freqs [1] * sampleRate / magnitude.length, ampl [1],
    	    		freqs [0] * sampleRate / magnitude.length, ampl [0], com * sampleRate / magnitude.length,
    	    		phases [1], phases [0]};
    }
   
}

/**
 * Only considers peaks above average + SDLimit*SD of power Spectrum Background 
 * SDLimit = 1.5 -> FDR = 4.4 %
 * SDLimit = 2 -> FDR = 1.7 %
 * SDLimit = 2.5 -> FDR = 0.5 %
 * SDLimit = 3 -> FDR = 0.1 %
 * 
 * Function implemented from v0.2.1 on (first time phase is extracted)
 * */
public static double [] getPhaseOfSpecificFrequencies (double [] values, double sampleRate, boolean showPlot, boolean normalizePlusMinus, double [] specificFrequencies){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] phase = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
	if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
		double max = tools.getMaximum(fft)/2.0;
		for(int i = 0; i < fft.length; i++){
			fft [i] -= max;
		}
	}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	phase [j] =  Math.atan2(imaginary, real);
    }
    
    //display as plot
    if(showPlot)	tools.showAsPlot(phase);

  	double phases [] = new double [specificFrequencies.length];
  	for(int i = 0; i < specificFrequencies.length; i++) {
  		if(specificFrequencies [i] <= 0.0) continue;
		phases [i] = phase [(int) Math.round(specificFrequencies [i] / sampleRate * (double) values.length)];
	}
  	
    return phases;
}



/**
 * Only considers peaks above average + SDLimit*SD of power Spectrum Background 
 * SDLimit = 1.5 -> FDR = 4.4 %
 * SDLimit = 2 -> FDR = 1.7 %
 * SDLimit = 2.5 -> FDR = 0.5 %
 * SDLimit = 3 -> FDR = 0.1 %
 * */
public static double [] getAboveMinimumFrequenciesWithPowers (double [] values, double sampleRate, boolean showPlot, boolean normalizePlusMinus, double [] powerSpectrumMinima,
		double lowerLimit, double upperLimit){
	//DoubleFFT package from: http://incanter.org/docs/parallelcolt/api/edu/emory/mathcs/jtransforms/fft/DoubleFFT_1D.html
	DoubleFFT_1D fftDo = new DoubleFFT_1D(values.length);	
    double [] fft = new double[values.length * 2];
    double [] magnitude = new double[values.length];
    System.arraycopy(values, 0, fft, 0, values.length); 
    
    //normalization of values to +/- range
  		if(normalizePlusMinus && tools.getMinimumWithinRange(fft, 0, fft.length-1) >= 0.0){
  			double max = tools.getMaximum(fft)/2.0;
  			for(int i = 0; i < fft.length; i++){
  				fft [i] -= max;
  			}
  		}
    
    fftDo.realForwardFull(fft);
                   
    double real, imaginary;
    boolean somethingRemains = false;
    for(int j = 0; j < values.length; j++){
    	real = fft[2*j];
    	imaginary = fft[2*j+1];
    	magnitude [j] = Math.sqrt(real*real+imaginary*imaginary);
    	magnitude [j] -= powerSpectrumMinima [j]; 
    	if(magnitude [j] < 0.0){
    		magnitude [j] = 0.0;
    	}else{    		
    		somethingRemains = true;
    	}
    }    
    if(!somethingRemains)	return new double [] {0.0, 0.0, 0.0, 0.0, 0.0};
    
    //display as plot
    if(showPlot)	tools.showAsPlot(magnitude);
    
    int [] freqs = tools.get2HighestMaximaIndicesWithinRange(magnitude,
    		(int)Math.round(lowerLimit*(magnitude.length/sampleRate)), 
		(int)Math.round(upperLimit*(magnitude.length/sampleRate)));
    double [] ampl = new double [2];
    double com = tools.getCenterOfMassOfRange(magnitude, 0, (magnitude.length/2)-1);
    if(freqs[0] >= 0 && freqs [0] < magnitude.length){
    	ampl [0] = 2.0 * (magnitude [freqs[0]] + powerSpectrumMinima [freqs[0]]);
    }else{
    	ampl [0] = 0.0;
    }
    if(freqs[1] >= 0 && freqs [1] < magnitude.length){
    	ampl [1] = 2.0 * (magnitude [freqs[1]] + powerSpectrumMinima [freqs[1]]);
    }else{
    	ampl [1] = 0.0;
    }
    return new double [] {freqs [0] * sampleRate / magnitude.length, ampl [0],
    		freqs [1] * sampleRate / magnitude.length, ampl [1], com * sampleRate / magnitude.length};
}

/**
 * Saves the matrix as a PNG file
 * Write LUT Jet to use the Jet LUT.
 * */
private void save2DPlot(double [][][] output, int index3rdDim, int indexDimensionForMasking, String unit, String savePath, boolean ignoreZero, double lowerLimit, double upperLimit, String usedLUT){
	double max = Double.NEGATIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(ignoreZero && output[x][y][indexDimensionForMasking] == 0.0)	continue;
			if(output[x][y][index3rdDim] > upperLimit)	continue;
			if(output[x][y][index3rdDim] < lowerLimit)	continue;
			if(Double.isNaN(output[x][y][index3rdDim]))	continue;
	   		if(output[x][y][index3rdDim] > max){
	   			max = output[x][y][index3rdDim]; 
	   		}
	   		if(output[x][y][index3rdDim] < min){
	   			min = output[x][y][index3rdDim]; 
	   		}
		}
	}
	boolean ticks = false;
	if(max-min>10){
		max = 10.0*(double)((int)(max/10.0)+1);
		min = 10.0*(double)((int)(min/10.0));
		ticks = true;
	}else{
//		max = (double)((int)max+1);
//		min = (double)((int)min);
	}	
	
	ImagePlus impOut = IJ.createImage("Output image", output.length, output[0].length + 20, 1, 8);
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(ignoreZero && output[x][y][indexDimensionForMasking] == 0.0)	continue;
			if(output[x][y][index3rdDim] > upperLimit)	continue;
			if(output[x][y][index3rdDim] < lowerLimit)	continue;
			
			impOut.getStack().setVoxel(x, y, 0, (255.0*(output[x][y][index3rdDim]-min)/(max-min)));
		}
	}
	
	//write bar	
	for(int x = 0; x < output.length; x++){
		impOut.getStack().setVoxel(x, output[0].length + 2, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 3, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 4, 0, (255.0*(double)x/(double)output.length));
	}
	
	if(usedLUT.equals("Jet") || usedLUT.equals("JET") || usedLUT.equals("jet")) {
		impOut.setLut(jetLUT);
	}else {
		IJ.run(impOut,usedLUT, "");	
	}	
	
	convertToRGB(impOut);
	
	for(int x = 0; x < output.length; x++){
		for(int y = output[0].length; y < output[0].length + 20; y++){
			if(y == output[0].length + 2)	continue;
			if(y == output[0].length + 3)	continue;
			if(y == output[0].length + 4)	continue;
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(ignoreZero && output[x][y][indexDimensionForMasking] == 0.0){				
			}else if(output[x][y][index3rdDim] > upperLimit){
			}else if(output[x][y][index3rdDim] < lowerLimit){
			}else{
				continue;
			}			
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	

	impOut.getImage().getGraphics().setColor(Color.WHITE);
	if(ticks){
		for(int i = (int)min; i <= (int)max; i++){
			if(i%10==0){
				impOut.getImage().getGraphics().drawLine((int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 4, (int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 5);				
			}
		}
	}
	
	impOut.getImage().getGraphics().setFont(RoiFont);
	impOut.getImage().getGraphics().drawString(df1US.format(min), 
			(int)Math.round(2), 
		(int)Math.round( output[0].length + 18));
	
	TextRoi txtID = new TextRoi((int)Math.round(output.length), 
			(int)Math.round(output[0].length + 18),
			df1US.format(max),
			RoiFont);	
	impOut.getImage().getGraphics().drawString(df1US.format(max), 
			(int)Math.round(output.length)-txtID.getBounds().width-2, 
			(int)Math.round(output[0].length + 18));
				
	txtID = new TextRoi((int)Math.round(output.length/2.0), 
			(int)Math.round(output[0].length + 18),
			unit,
			RoiFont);
	impOut.getImage().getGraphics().drawString(unit, 
		(int)Math.round((double)(output.length-txtID.getBounds().width)/2.0)-2, 
		(int)Math.round(output[0].length + 18));
		
//	IJ.saveAsTiff(impOut, savePath);
	IJ.saveAs(impOut,"PNG",savePath + ".png");
}

private void save2DPlotMasked(double [][][] output, double [][][] mask, int index3rdDim, int indexDimensionForMasking, String unit, String savePath, double lowerLimit, double upperLimit, String usedLUT){
	double max = Double.NEGATIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(mask[x][y][indexDimensionForMasking] == 0.0)	continue;
			if(output[x][y][index3rdDim] > upperLimit)	continue;
			if(output[x][y][index3rdDim] < lowerLimit)	continue;
	   		if(output[x][y][index3rdDim] > max){
	   			max = output[x][y][index3rdDim]; 
	   		}
	   		if(output[x][y][index3rdDim] < min){
	   			min = output[x][y][index3rdDim]; 
	   		}
		}
	}
	boolean ticks = false;
	if(max-min>10){
		max = 10.0*(double)((int)(max/10.0)+1);
		min = 10.0*(double)((int)(min/10.0));
		ticks = true;
	}else{
//		max = (double)((int)max+1);
//		min = (double)((int)min);
	}	
	
	ImagePlus impOut = IJ.createImage("Output image", output.length, output[0].length + 20, 1, 8);
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(mask[x][y][indexDimensionForMasking] == 0.0)	continue;
			if(output[x][y][index3rdDim] > upperLimit)	continue;
			if(output[x][y][index3rdDim] < lowerLimit)	continue;
			
			impOut.getStack().setVoxel(x, y, 0, (255.0*(output[x][y][index3rdDim]-min)/(max-min)));
		}
	}
	
	//write bar	
	for(int x = 0; x < output.length; x++){
		impOut.getStack().setVoxel(x, output[0].length + 2, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 3, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 4, 0, (255.0*(double)x/(double)output.length));
	}

	if(usedLUT.equals("Jet") || usedLUT.equals("JET") || usedLUT.equals("jet")) {
		impOut.setLut(jetLUT);
	}else {
		IJ.run(impOut,usedLUT, "");	
	}
	
	convertToRGB(impOut);
	
	for(int x = 0; x < output.length; x++){
		for(int y = output[0].length; y < output[0].length + 20; y++){
			if(y == output[0].length + 2)	continue;
			if(y == output[0].length + 3)	continue;
			if(y == output[0].length + 4)	continue;
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(mask[x][y][indexDimensionForMasking] == 0.0){				
			}else if(output[x][y][index3rdDim] > upperLimit){
			}else if(output[x][y][index3rdDim] < lowerLimit){
			}else{
				continue;
			}			
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	

	impOut.getImage().getGraphics().setColor(Color.WHITE);
	if(ticks){
		for(int i = (int)min; i <= (int)max; i++){
			if(i%10==0){
				impOut.getImage().getGraphics().drawLine((int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 4, (int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 5);				
			}
		}
	}
	
	impOut.getImage().getGraphics().setFont(RoiFont);
	impOut.getImage().getGraphics().drawString(df1US.format(min), 
			(int)Math.round(2), 
		(int)Math.round( output[0].length + 18));
	
	TextRoi txtID = new TextRoi((int)Math.round(output.length), 
			(int)Math.round(output[0].length + 18),
			df1US.format(max),
			RoiFont);	
	impOut.getImage().getGraphics().drawString(df1US.format(max), 
			(int)Math.round(output.length)-txtID.getBounds().width-2, 
			(int)Math.round(output[0].length + 18));
				
	txtID = new TextRoi((int)Math.round(output.length/2.0), 
			(int)Math.round(output[0].length + 18),
			unit,
			RoiFont);
	impOut.getImage().getGraphics().drawString(unit, 
		(int)Math.round((double)(output.length-txtID.getBounds().width)/2.0)-2, 
		(int)Math.round(output[0].length + 18));
		
//	IJ.saveAsTiff(impOut, savePath);
	IJ.saveAs(impOut,"PNG",savePath + ".png");
}

public void save2DPlotAsText(double [][][] output, int index3rdDim, int refDimensionForMasking, String unit, String savePath, boolean ignoreZero, double lowerLimit, double upperLimit, Date currentDate){
	OutputPanel = new TextPanel("Custom 2D Plot As Text");
	String appText = "y (px) \u2193 | x (px) \u2192";
	for(int x = 0; x < output.length; x++){
		appText += "	" + x;
	}
	OutputPanel.append(appText);
	
	for(int y = 0; y < output[0].length; y++){
		appText = y + "";
		for(int x = 0; x < output.length; x++){
			appText += "	";

			if(ignoreZero && output[x][y][refDimensionForMasking] == 0.0)	continue;
			if(output[x][y][index3rdDim] > upperLimit)	continue;
			if(output[x][y][index3rdDim] < lowerLimit)	continue;
			if(Double.isNaN(output[x][y][index3rdDim]))	continue;
			appText += constants.df6US.format(output [x][y][index3rdDim]);
   		}
		OutputPanel.append(appText);
 	}
	
	addFooter(OutputPanel, currentDate);				
	OutputPanel.saveAs(savePath + ".txt");
	
	OutputPanel = null;
}

public void save2DPlotAsTextMasked(double [][][] output, double mask [][][], int index3rdDim, int refDimensionForMasking, String unit, String savePath, double lowerLimit, double upperLimit, Date currentDate){
	OutputPanel = new TextPanel("Custom 2D Plot As Text");
	String appText = "y (px) \u2193 | x (px) \u2192";
	for(int x = 0; x < output.length; x++){
		appText += "	" + x;
	}
	OutputPanel.append(appText);
	
	for(int y = 0; y < output[0].length; y++){
		appText = y + "";
		for(int x = 0; x < output.length; x++){
			appText += "	";

			if(mask[x][y][refDimensionForMasking] == 0.0)	continue;
			if(output[x][y][index3rdDim] > upperLimit)	continue;
			if(output[x][y][index3rdDim] < lowerLimit)	continue;
			appText += constants.df6US.format(output [x][y][index3rdDim]);
   		}
		OutputPanel.append(appText);
 	}
	
	addFooter(OutputPanel, currentDate);				
	OutputPanel.saveAs(savePath + ".txt");
	
	OutputPanel = null;
}

public static void save2DPlot(double [][] output, String unit, String savePath, boolean ignoreZero, double lowerLimit, double upperLimit, String usedLUT){
	double max = Double.NEGATIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(ignoreZero && output[x][y] == 0.0)	continue;
			if(output[x][y] > upperLimit)	continue;
			if(output[x][y] < lowerLimit)	continue;
	   		if(output[x][y] > max){
	   			max = output[x][y]; 
	   		}
	   		if(output[x][y] < min){
	   			min = output[x][y]; 
	   		}
		}
	}
	boolean ticks = false;
	if(max-min>10){
		max = 10.0*(double)((int)(max/10.0)+1);
		min = 10.0*(double)((int)(min/10.0));
		ticks = true;
	}else{
		max = (double)((int)max+1);
		min = (double)((int)min);
	}	
	
	ImagePlus impOut = IJ.createImage("Output image", output.length, output[0].length + 20, 1, 8);
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(ignoreZero && output[x][y] == 0.0)	continue;
			if(output[x][y] > upperLimit)	continue;
			if(output[x][y] < lowerLimit)	continue;
			
			impOut.getStack().setVoxel(x, y, 0, (255.0*(output[x][y]-min)/(max-min)));
		}
	}
	
	//write bar	
	for(int x = 0; x < output.length; x++){
		impOut.getStack().setVoxel(x, output[0].length + 2, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 3, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 4, 0, (255.0*(double)x/(double)output.length));
	}	
	IJ.run(impOut, usedLUT, "");
	
	convertToRGB(impOut);
	
	for(int x = 0; x < output.length; x++){
		for(int y = output[0].length; y < output[0].length + 20; y++){
			if(y == output[0].length + 2)	continue;
			if(y == output[0].length + 3)	continue;
			if(y == output[0].length + 4)	continue;
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			if(ignoreZero && output[x][y] == 0.0){				
			}else if(output[x][y] > upperLimit){
			}else if(output[x][y] < lowerLimit){
			}else{
				continue;
			}			
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	

	impOut.getImage().getGraphics().setColor(Color.WHITE);
	if(ticks){
		for(int i = (int)min; i <= (int)max; i++){
			if(i%10==0){
				impOut.getImage().getGraphics().drawLine((int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 4, (int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 5);				
			}
		}
	}
	
	impOut.getImage().getGraphics().setFont(RoiFont);
	impOut.getImage().getGraphics().drawString(constants.df1US.format(min), 
			(int)Math.round(2), 
		(int)Math.round( output[0].length + 18));
	
	TextRoi txtID = new TextRoi((int)Math.round(output.length), 
			(int)Math.round(output[0].length + 18),
			constants.df1US.format(max),
			RoiFont);	
	impOut.getImage().getGraphics().drawString(constants.df1US.format(max), 
			(int)Math.round(output.length)-txtID.getBounds().width-2, 
			(int)Math.round(output[0].length + 18));
				
	txtID = new TextRoi((int)Math.round(output.length/2.0), 
			(int)Math.round(output[0].length + 18),
			unit,
			RoiFont);
	impOut.getImage().getGraphics().drawString(unit, 
		(int)Math.round((double)(output.length-txtID.getBounds().width)/2.0)-2, 
		(int)Math.round(output[0].length + 18));
		
//	IJ.saveAsTiff(impOut, savePath);
	IJ.saveAs(impOut,"PNG",savePath + ".png");
}

public static void save2DPlot(double [][] output, String unit, String savePath, LUT customLUT){
	double max = Double.NEGATIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
	   		if(output[x][y] > max){
	   			max = output[x][y]; 
	   		}
	   		if(output[x][y] < min){
	   			min = output[x][y]; 
	   		}
		}
	}
	boolean ticks = false;
	if(max-min>10){
		max = 10.0*(double)((int)(max/10.0)+1);
		min = 10.0*(double)((int)(min/10.0));
		ticks = true;
	}else{
		max = (double)((int)max+1);
		min = (double)((int)min);
	}	
	
	ImagePlus impOut = IJ.createImage("Output image", output.length, output[0].length + 20, 1, 8);
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output[x].length; y++){
			impOut.getStack().setVoxel(x, y, 0, (255.0*(output[x][y]-min)/(max-min)));
		}
	}
	
	//write bar	
	for(int x = 0; x < output.length; x++){
		impOut.getStack().setVoxel(x, output[0].length + 2, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 3, 0, (255.0*(double)x/(double)output.length));
		impOut.getStack().setVoxel(x, output[0].length + 4, 0, (255.0*(double)x/(double)output.length));
	}	

	if(customLUT.equals(null)) {
		IJ.run(impOut, "Ice", "");
	}else {
		impOut.setLut(customLUT);		
	}	
	
	convertToRGB(impOut);
	
	for(int x = 0; x < output.length; x++){
		for(int y = output[0].length; y < output[0].length + 20; y++){
			if(y == output[0].length + 2)	continue;
			if(y == output[0].length + 3)	continue;
			if(y == output[0].length + 4)	continue;
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}
	
	impOut.getImage().getGraphics().setColor(Color.WHITE);
	if(ticks){
		for(int i = (int)min; i <= (int)max; i++){
			if(i%10==0){
				impOut.getImage().getGraphics().drawLine((int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 4, (int)Math.round(output.length*(i-min)/(max-min)),
						output[0].length + 5);				
			}
		}
	}
	
	impOut.getImage().getGraphics().setFont(RoiFont);
	impOut.getImage().getGraphics().drawString(constants.df0.format(min), 
			(int)Math.round(2), 
		(int)Math.round( output[0].length + 18));
	
	TextRoi txtID = new TextRoi((int)Math.round(output.length), 
			(int)Math.round(output[0].length + 18),
			constants.df0.format(max),
			RoiFont);	
	impOut.getImage().getGraphics().drawString(constants.df0.format(max), 
			(int)Math.round(output.length)-txtID.getBounds().width-2, 
			(int)Math.round(output[0].length + 18));
				
	txtID = new TextRoi((int)Math.round(output.length/2.0), 
			(int)Math.round(output[0].length + 18),
			unit,
			RoiFont);
	impOut.getImage().getGraphics().drawString(unit, 
		(int)Math.round((double)(output.length-txtID.getBounds().width)/2.0)-2, 
		(int)Math.round(output[0].length + 18));
		
//	IJ.saveAsTiff(impOut, savePath);
	IJ.saveAs(impOut,"PNG",savePath + ".png");
}

public static void saveBooleanAsPlot(boolean [][] map, String title, String savePath){
	ImagePlus impOut = IJ.createImage("Output image", map.length, map[0].length + 20, 1, 8);
	for(int x = 0; x < map.length; x++){
		for(int y = 0; y < map[x].length; y++){
			if(map[x][y]){
				impOut.getStack().setVoxel(x, y, 0, 255.0);
			}
			
		}
	}
	
	convertToRGB(impOut);
	
	for(int x = 0; x < map.length; x++){
		for(int y = map[0].length; y < map[0].length + 20; y++){
			if(y == map[0].length + 2)	continue;
			if(y == map[0].length + 3)	continue;
			if(y == map[0].length + 4)	continue;
			impOut.setC(0);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(1);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
			impOut.setC(2);
			impOut.getStack().setVoxel(x, y, 0, 0.0);
		}
	}

	impOut.getImage().getGraphics().setColor(Color.WHITE);
	impOut.getImage().getGraphics().setFont(RoiFont);
	TextRoi txtID = new TextRoi((int)Math.round(map.length/2.0), 
			(int)Math.round(map[0].length + 18),
			title,
			RoiFont);
	impOut.getImage().getGraphics().drawString(title, 
		(int)Math.round((double)(map.length-txtID.getBounds().width)/2.0)-2, 
		(int)Math.round(map[0].length + 18));
		
	IJ.saveAsTiff(impOut, savePath);
}


private static boolean [][] getSignalRegionsBySD(double [][][] frequencies, double SDThreshold, boolean extendedRange){
	boolean [][] output = new boolean [frequencies.length][frequencies[0].length];
	for(int x = 0; x < output.length; x++){
		Arrays.fill(output [x], false);
	}
	
	double [] values = new double [13], valueRange;
	int counter;
	double SD;
	
	for(int x = 1; x < output.length-1; x++){
		for(int y = 1; y < output[x].length-1; y++){
			counter = 0;
			Arrays.fill(values, 0.0);
			for(int ix = x-1; ix < x+2; ix++){
				for(int iy = y-1; iy < y+2; iy++){
					values [counter] = frequencies[ix][iy][0];
					if(values[counter]>0.0){
						counter++;
					}					
				}
			}
			
			if(extendedRange) {
				if(x-2 >= 0) {
					values [counter] = frequencies[x-2][y][0];
					if(values[counter]>0.0){
						counter++;
					}
				}
				
				if(x+2 < frequencies.length) {
					values [counter] = frequencies[x+2][y][0];
					if(values[counter]>0.0){
						counter++;
					}
				}
				
				if(y-2 >= 0) {
					values [counter] = frequencies[x][y-2][0];
					if(values[counter]>0.0){
						counter++;
					}
				}
				
				if(y+2 < frequencies[0].length) {
					values [counter] = frequencies[x][y+2][0];
					if(values[counter]>0.0){
						counter++;
					}
				}
			}

			if(counter<6){
				continue;
			}else if(counter == 13){
				valueRange = values;
			}else{
//				IJ.log("counter go: " + counter);
				valueRange = Arrays.copyOfRange(values, 0, counter);
//				for(int vr = 0; vr < values.length; vr++) {
//					IJ.log("" + values[vr]);
//				}
//				for(int vr = 0; vr < valueRange.length; vr++) {
//					IJ.log("vr: " + valueRange[vr]);
//				}
			}
									
			SD = tools.getSD(valueRange);
//			IJ.log("SD: " + SD);
			if(SD < SDThreshold){
				for(int ix = x-1; ix < x+2; ix++){
					for(int iy = y-1; iy < y+2; iy++){
						output[ix][iy] = true;
					}
				}
			}
		}
	}
	return output;
}

private static boolean [][] getSignalRegionsByPowerThreshold(double [][][] frequencies, double PowerThreshold){
	boolean [][] output = new boolean [frequencies.length][frequencies[0].length];
	for(int x = 0; x < output.length; x++){
		Arrays.fill(output [x], false);
	}
	
	for(int x = 0; x < output.length; x++){
		for(int y = 0; y < output [x].length; y++){
			if(frequencies [x][y][1] >= PowerThreshold){
				output [x][y] = true;
			}
		}
	}
	return output;
}

private static double [][] getPowerSpectraBackground(ImagePlus imp, boolean [][] signalRegions, int smoothWindowSize){
	double [][] output = new double [2][imp.getStackSize()];	//0 = average, 1 = SD
	int maxCount = 0;
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			if(!signalRegions[x][y]){
				maxCount++;	
			}
		}
	}
	
	int counter = 0;
	double [][] magnitudes = new double[maxCount][imp.getStackSize()];
	double [] valueColumn = new double [imp.getStackSize()];
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			if(!signalRegions[x][y]){
				for(int i = 0; i < imp.getStackSize(); i++){
	   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
	   			}
				magnitudes [counter] = getFrequencySpectrum(valueColumn, false, true, smoothWindowSize);
				counter++;				
			}
		}
	}
	
	valueColumn = new double [maxCount];
	for(int f = 0; f < magnitudes [0].length; f++){
		for(int i = 0; i < maxCount; i++){
			valueColumn [i] = magnitudes [i][f];
		}
		output[0][f] = tools.getAverage(valueColumn);
		output[1][f] = tools.getSD(valueColumn);
	}
	
	return output;
}

private static double [] getPowerSpectraMinima(ImagePlus imp, int smoothWindowSize){
	double [] minima = new double [imp.getStackSize()];
	Arrays.fill(minima, Double.POSITIVE_INFINITY);
	int maxCount = 0;
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			maxCount++;	
		}
	}
	
	int counter = 0;
	double [][] magnitudes = new double[maxCount][imp.getStackSize()];
	double [] valueColumn = new double [imp.getStackSize()];
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
//			if(!signalRegions[x][y]){
				for(int i = 0; i < imp.getStackSize(); i++){
	   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
	   			}
				magnitudes [counter] = getFrequencySpectrum(valueColumn, false, true, smoothWindowSize);
				counter++;				
//			}
		}
	}
	
	valueColumn = new double [maxCount];
	for(int f = 0; f < magnitudes [0].length; f++){
		for(int i = 0; i < maxCount; i++){
			if(magnitudes [i][f] < minima[f]) minima[f] = magnitudes [i][f];
		} 
	}	
	return minima;
}

private static double [] getAvgSDOfLowerPowers(double [][][] frequencies, double percentLowest){
	if(percentLowest > 100.0)	percentLowest = 100.0;
	
	double primFreqPower [] = new double [frequencies.length*frequencies[0].length];
	int counter = 0;
	for(int x = 0; x < frequencies.length; x++){
		for(int y = 0; y < frequencies[0].length; y++){
			primFreqPower [counter] = frequencies [x][y][1];
			counter++;	
		}
	}
	Arrays.sort(primFreqPower);
	primFreqPower = Arrays.copyOf(primFreqPower, (int)Math.round(primFreqPower.length*percentLowest/100.0));
	
	double average = tools.getAverage(primFreqPower);
	double SD = tools.getSD(primFreqPower);
	return new double []{average, SD};
}

private static double [] getAssembledPowerSpectrumInRoi(ImagePlus imp, boolean [][] signalRegions, Roi selection, int smoothWindowSize){
	double [] output = new double [imp.getStackSize()];	//0 = average, 1 = SD
	int maxCount = 0;
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			if(signalRegions[x][y] && selection.contains(x, y)){
				maxCount++;	
			}
		}
	}
	if(maxCount == 0){
		return null;
	}
	
	int counter = 0;
	double [][] magnitudes = new double[maxCount][imp.getStackSize()];
	double [] valueColumn = new double [imp.getStackSize()];
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			if(signalRegions[x][y] && selection.contains(x, y)){
				for(int i = 0; i < imp.getStackSize(); i++){
	   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
	   			}
				magnitudes [counter] = getFrequencySpectrum(valueColumn, false, true, smoothWindowSize);
				counter++;
			}
		}
	}
	
	valueColumn = new double [maxCount];
	for(int f = 0; f < magnitudes [0].length; f++){
		for(int i = 0; i < maxCount; i++){
			valueColumn [i] = magnitudes [i][f];
		}
		output[f] = tools.getAverage(valueColumn);
	}
	
	return output;
}

private static double [] getAssembledPowerSpectrum(ImagePlus imp, boolean [][] signalRegions, int smoothWindowSize){
	double [] output = new double [imp.getStackSize()];	//0 = average, 1 = SD
	int maxCount = 0;
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			if(signalRegions[x][y]){
				maxCount++;	
			}
		}
	}
	if(maxCount == 0){
		return null;
	}
	
	int counter = 0;
	double [][] magnitudes = new double[maxCount][imp.getStackSize()];
	double [] valueColumn = new double [imp.getStackSize()];
	for(int x = 0; x < imp.getWidth(); x++){
		for(int y = 0; y < imp.getHeight(); y++){
			if(signalRegions[x][y]){
				for(int i = 0; i < imp.getStackSize(); i++){
	   				valueColumn [i] = imp.getStack().getVoxel(x, y, i);
	   			}
				magnitudes [counter] = getFrequencySpectrum(valueColumn, false, true, smoothWindowSize);
				counter++;
			}
		}
	}
	
	valueColumn = new double [maxCount];
	for(int f = 0; f < magnitudes [0].length; f++){
		for(int i = 0; i < maxCount; i++){
			valueColumn [i] = magnitudes [i][f];
		}
		output[f] = tools.getAverage(valueColumn);
	}
	
	return output;
}

private static double [] smooth (double [] curve, int windowSize){
	if(windowSize == 0)	return curve;
	int endPos, startPos;
	double avg;
	double outCurve [] = new double [curve.length];
	for(int i = 0; i < curve.length; i++){
		startPos = i - (int)((double)windowSize/2.0);
		if(startPos < 0)			startPos = 0;
		
		endPos = i + (int)((double)windowSize/2.0);
		if(endPos >= curve.length)	endPos = curve.length-1;
		
		avg = 0.0;
		for(int j = startPos; j <= endPos; j++){
			avg += curve [j];
		}
		avg /= (double)(endPos-startPos+1);
		outCurve [i] = avg;
	}
	return outCurve;
}

/**
 * automatic conversion to RGB
 * 
 * Method copied from the plugin "MotiQ_3D", plugin for ImageJ, Version v0.1.5,
 * 	Downloaded from https://github.com/hansenjn/MotiQ/releases/tag/v0.1.5 
 * 	on 23rd of April 2019.
 * */
private static void convertToRGB(ImagePlus imp){
	if(imp.isComposite()){
		Calibration cal = imp.getCalibration();
		RGBStackConverter.convertToRGB(imp);
		imp.setCalibration(cal);
	}else{
		ImageConverter iCv = new ImageConverter(imp);
		iCv.convertToRGB();
	}	        
}

/**
* @param array: 1st dimension > different graphs, 2nd dimension > y points
* */
private static void plot2DArray(double xValues [], double [][] array, String label, String xLabel, String yLabel, String savePath, boolean logarithmic, String legends []){
	double yMax = Double.NEGATIVE_INFINITY, yMin = Double.POSITIVE_INFINITY;
	double xMin = tools.getMinimum(xValues), xMax = tools.getMaximum(xValues);
	xMin -= Math.abs(xMin*0.05);
	if(xMin>0.0) xMin = 0.0;
	xMax += Math.abs(xMax*0.05);
	
	double max, min;
	for(int i = 0; i < array.length; i++){
		max = tools.getMaximum(array[i]);
		min = tools.getMinimum(array[i]);
		if(!Double.isNaN(max) && max < Double.POSITIVE_INFINITY && yMax < max) yMax = max;
		if(!Double.isNaN(min) && min > Double.NEGATIVE_INFINITY && yMin > min) yMin = min;
	}
	yMax += Math.abs(0.05*yMax);
	if(yMin>0.0)	yMin = 0.0;
	yMin -= Math.abs(0.05*yMin);
	
	Color c;
	Plot p;
	ImagePlus pImp;
	String legend = "";
	PlotWindow.noGridLines = true;
	
	p = new Plot(label, xLabel, yLabel);
	p.setAxisYLog(logarithmic);
	p.updateImage();
	p.setSize(1000, 700);
	p.setLimits(xMin, xMax, yMin, yMax);
	p.updateImage();
	for(int i = 0; i < array.length; i++){
		c = new Color(54+(int)(i/(double)array.length*200.0), 54+(int)(i/(double)array.length*200.0), 54+(int)(i/(double)array.length*200.0));
		p.setColor(c);
		p.addPoints(xValues,array[i],PlotWindow.LINE);
		legend += "" + legends [i];
		legend += "\n";
	}
	p.addLegend(legend);
	p.setLimitsToFit(true);
	pImp = p.makeHighResolution("plot",1,true,false);
	IJ.saveAs(pImp,"PNG",savePath + ".png");
	pImp.changes = false;
	pImp.close();
	p.dispose();			
 	System.gc();
}

/**
* @param array: 1st dimension > different graphs, 2nd dimension > y points
* @param lines: defines an array of vertical lines to be added to the plot
* @param linesLabels: defines an the names of the lines in a String array, to be shown in the legend
* */
private static void plot2DArrayWithAdditionalLines(double xValues [], double [][] array, String label, String xLabel, String yLabel, String savePath, boolean logarithmic, String legends [], double lines [], String lineLabels []){
	double yMax = Double.NEGATIVE_INFINITY, yMin = Double.POSITIVE_INFINITY;
	double xMin = tools.getMinimum(xValues), xMax = tools.getMaximum(xValues);
	xMin -= Math.abs(xMin*0.05);
	if(xMin>0.0) xMin = 0.0;
	xMax += Math.abs(xMax*0.05);
	
	double max, min;
	for(int i = 0; i < array.length; i++){
		max = tools.getMaximum(array[i]);
		min = tools.getMinimum(array[i]);
		if(!Double.isNaN(max) && max < Double.POSITIVE_INFINITY && yMax < max) yMax = max;
		if(!Double.isNaN(min) && min > Double.NEGATIVE_INFINITY && yMin > min) yMin = min;
	}
	yMax += Math.abs(0.05*yMax);
	if(yMin>0.0)	yMin = 0.0;
	yMin -= Math.abs(0.05*yMin);
	
	Color c;
	Plot p;
	ImagePlus pImp;
	String legend = "";
	PlotWindow.noGridLines = true;
	
	p = new Plot(label, xLabel, yLabel);
	p.setAxisYLog(logarithmic);
	p.updateImage();
	p.setSize(1000, 700);
	p.setLimits(xMin, xMax, yMin, yMax);
	p.updateImage();
	for(int i = 0; i < lines.length; i++) {
		double lineX [] = new double []{lines[i], lines[i]};
		double lineY [] = new double [] {yMin - Math.abs(0.025*yMin),yMax-Math.abs(0.025*yMax)};
		
		c = new Color(150+(int)(i/(double)array.length*100.0), 0, 150+(int)(i/(double)array.length*100.0));
		p.setColor(c);
		p.setLineWidth(2);
		p.addPoints(lineX, lineY,PlotWindow.LINE);
		legend += lineLabels [i] + " (" + df1US.format(lines [i]) + ")\n";
	}
	for(int i = 0; i < array.length; i++){
		c = new Color(54+(int)(i/(double)array.length*200.0), 54+(int)(i/(double)array.length*200.0), 54+(int)(i/(double)array.length*200.0));
		p.setColor(c);
		p.addPoints(xValues,array[i],PlotWindow.LINE);
		legend += "" + legends [i];
		legend += "\n";
	}
	p.addLegend(legend);
	p.setLimitsToFit(true);
	pImp = p.makeHighResolution("plot",1,true,false);
	IJ.saveAs(pImp,"PNG",savePath + ".png");
	pImp.changes = false;
	pImp.close();
	p.dispose();			
 	System.gc();
}

private static LUT getLUT(double [][] array, boolean multiplyWith255) {
	byte [] red = new byte [array.length];
	byte [] green = new byte [array.length];
	byte [] blue = new byte [array.length];
	for(int i = 0; i < red.length; i++) {		
		if(multiplyWith255) {
			red [i] = (byte) (array [i][0] * 255.0);
			green [i] = (byte) (array [i][1] * 255.0);
			blue [i] = (byte) (array [i][2] * 255.0);
		}else {
			red [i] = (byte) (array [i][0]);
			green [i] = (byte) (array [i][1]);
			blue [i] = (byte) (array [i][2]);
		}
	}
	
	if(red.length < 256) {
		//Interpolate
		byte [] newRed = new byte [256];
		byte [] newGreen = new byte [256];
		byte [] newBlue = new byte [256];
		int oldPos;
		for (int i = 0; i < newRed.length; i++) {
			oldPos = (int)((double) i * (double) red.length / 256.0);
			newRed [i] = red [oldPos];
			newGreen [i] = green [oldPos];
			newBlue [i] = blue [oldPos];
		}
		return new LUT (8,newRed.length,newRed,newGreen,newBlue);
	}else {
		return new LUT (8,red.length,red,green,blue);
	}	
}

/**
 * Saves a histogram for the @param input array, and if @param plotMedian is selected also adds a line indicating the median of the input data.
 * */
private void saveAsHistogram(double input [], double min, double max, double binSize, boolean logarithmic, boolean plotMedian, String header,  String xLabel, String unit, String savePath, Date currentDate) {
	int nrOfBins = (int) (max / binSize);
	
	int hist [] =  new int [nrOfBins];
	Arrays.fill(hist, 0);
	
	int val;
	int excl = 0;
	for(int i = 0; i < input.length; i++) {
		val = (int)(input[i] / binSize);
		if(val >= hist.length){
			excl++;
			continue;
		}
		hist [val]++;
	}
	if(excl>0) {
		progress.notifyMessage("Excluded " + excl + " pixels from histogram /<" + header + "/> as they exceeded upper plotting limit", ProgressDialog.LOG);
	}
	
	double xValues [] = new double [(hist.length)*2];
	double yValues [] = new double [(hist.length)*2];
	
	int yMax = 0;
	int ct = 0;
	for(int i = 0; i < hist.length; i++) {
		xValues [ct] = (double) (i) * binSize;
		yValues [ct] = (double) hist [i];
		ct ++;
		xValues [ct] = (double) (i+1) * binSize;
		yValues [ct] = (double) hist [i];
		if(yValues [ct] > yMax) {
			yMax = (int) yValues [ct];
		}
		ct ++;
	}
	yMax *= 1.2;
	
	Color c;
	Plot p;
	ImagePlus pImp;
	String legend = "";
	PlotWindow.noGridLines = true;
	
	p = new Plot("Histogram", xLabel + " [bin width: " + binSize + " " + unit + "]", "counts");
	p.setAxisYLog(logarithmic);
	p.updateImage();
	p.setSize(1000, 700);
	p.setLimits(min-1*binSize, max+1*binSize, 0.0, yMax);
	p.updateImage();
	if(plotMedian) {
		double median = tools.getMedian(input);
		double medX [] = new double []{median, median};
		double medY [] = new double [] {0.0,yMax};
		
		c = new Color(0, 0,255);
		p.setColor(c);
		p.setLineWidth(3);
		p.addPoints(medX, medY,PlotWindow.LINE);
		legend += "median = " + df1US.format(median) + " " + unit + "\n";
	}
	{
		c = new Color(50, 50,50);
		p.setColor(c);
		p.setLineWidth(1);
		p.addPoints(xValues, yValues,PlotWindow.LINE);
		legend += "histogram";
	}
	
	p.addLegend(legend);
//	p.setLimitsToFit(true);
	pImp = p.makeHighResolution("plot",1,true,false);
	IJ.saveAs(pImp,"PNG",savePath + ".png");
	pImp.changes = false;
	pImp.close();
	p.dispose();	
	
	OutputPanel = new TextPanel("Custom Plot As Text");
	String appText = "Histogram of " + header;
	OutputPanel.append(appText);
	
	appText = xLabel + " [bin width: " + binSize + " " + unit + "]"
			+ "	Counts";
	OutputPanel.append(appText);
	for(int x = 0; x <xValues.length; x+=2){
		appText = constants.df6US.format(xValues[x]+binSize/2.0) +"	" + constants.df0.format(yValues[x]);
		OutputPanel.append(appText);
	}
	
	addFooter(OutputPanel, currentDate);				
	OutputPanel.saveAs(savePath + ".txt");
	
	OutputPanel = null;
}
}//end main class