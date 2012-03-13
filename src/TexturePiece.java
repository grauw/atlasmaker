/*
Copyright (c) 2012, Ed Welch
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

*    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*    Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
     and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;


public class TexturePiece
{
	private ArrayList<AtlasImage> listImage;
	private BufferedImage imageDisplay;
	private String strErr;
	private String name;
	private String strAtlasData;
	private int width;
	private int height;
	private boolean transparent;
	public TexturePiece(int width, int height)
	{
		this.width = width;
		this.height = height;
    	listImage = new ArrayList<AtlasImage>();
	}
	public void add(AtlasImage image)
	{
		listImage.add(image);
	}
	public void remove(ArrayList<AtlasImage> listImageReturn)
	{
		listImageReturn.addAll(listImage);
		listImage.clear();
	}
	public void resize(int width, int height)
	{
		this.width = width;
		this.height = height;
		listImage.clear();
	}
	
	// trims dimensions to minimium
	public boolean trim()
	{
		width = 0;
		height = 0;
    	AtlasImage atlasImage;
		int ixImage;
		for (ixImage = 0; ixImage < listImage.size(); ixImage++)
		{
			atlasImage = listImage.get(ixImage);
            if (width < atlasImage.getRight()) 
            	width = atlasImage.getRight();
            if (height < atlasImage.getBottom()) 
            	height = atlasImage.getBottom();
		} 
		return true;
	}	
	
	public void createStuff(DirNode dirNode, String strOutFile, int index, OptionsDialog optionsDialog)
	{
		transparent = false;
		int ixImage;
		for (ixImage = 0; ixImage < listImage.size(); ixImage++)
		{
            if (listImage.get(ixImage).isTransparent())
            {
            	transparent = true;
            	break;
            }
		} 
		
	    imageDisplay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    BufferedImage imageOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = imageOut.createGraphics();
	    Graphics2D gDisplay = imageDisplay.createGraphics();
	    gDisplay.setColor(new Color(0x022370));
	    String stub = strOutFile;
	    String dir = "";
	    String fileName ="";
	    name = strOutFile;
	    
	    int posDir = strOutFile.lastIndexOf(File.separatorChar);
	    if (posDir != -1)
	    {
	    	stub = strOutFile.substring(posDir+1);
	    	dir = strOutFile.substring(0, posDir+1);
	    }
		stub += index;
        if (dirNode.getImageFormat() == ImageFormat.PVR)
        {
    		name = stub + ".pvr";
    		fileName = stub + ".png";
    	}
    	else if (dirNode.getImageFormat() == ImageFormat.PNG)
    	{
    		name = stub + ".png";
	    	fileName = name;
    	}
    	else if (dirNode.isTarga())
    	{
    		name = stub + ".tga";
	    	fileName = name;
    	}
	    		
	    strAtlasData = "  <texture file=\"" + name+"\" trans=\"";
	    if (transparent)
	    	strAtlasData += "true";
	    else
	    	strAtlasData += "false";
	    strAtlasData += "\">\n";
	    Collections.sort(listImage, new CompareName());
	    int n;
	    for (n = 0; n < listImage.size(); n++) 
	    {
	    	listImage.get(n).draw(g);
	    	listImage.get(n).drawWithBorder(gDisplay);
	    	strAtlasData += listImage.get(n).GetSerialize();
	    } 
	    strAtlasData += "  </texture>\n";
	    gDisplay.setColor(Color.black);
	    gDisplay.drawRect(0, 0, imageOut.getWidth()-1, imageOut.getHeight()-1);       
	    g.dispose();
	    gDisplay.dispose();
	//  Write generated image to a file
	    File file;
	    try 
	    {
    		file = new File(dir+fileName);
	    	if (dirNode.isTarga())
	    	{
	    		TargaWriter.write(imageOut, file, dirNode.getImageFormat(), transparent);       
	    	}
	    	else
	    	{
		        // Save as PNG
	    		ImageIO.write(imageOut, "png", file);
	    		if (optionsDialog.convPVR() && dirNode.getImageFormat() == ImageFormat.PVR)
	    		{
	    			String strCmd = optionsDialog.getTextToolPath();
	    			if (optionsDialog.isPVR4Bit())
	    				strCmd += " -fOGLPVRTC4 ";
	    			else
	    				strCmd += " -fOGLPVRTC2 ";
	    			strCmd += " -pvrtciterations7 -yflip0 -i" + dir+fileName + " -o" + dir+name;
	    			Runtime.getRuntime().exec(strCmd);
	    		}
	    	}
	    } 
	    catch (Exception e)
	    {
	        System.out.println("error: "+e.toString());
	    	strErr = "error: "+e.toString() + "processing " + name;
			JOptionPane.showMessageDialog(null, strErr, "Error", JOptionPane.INFORMATION_MESSAGE); 
	    }
		catch (OutOfMemoryError e) 
		{
	    	strErr = "error: "+e.toString() + "processing " + name;
			JOptionPane.showMessageDialog(null, strErr, "Error", JOptionPane.INFORMATION_MESSAGE); 
		}
	}
	public int getHeight()
	{
		return height;
	}
	public BufferedImage getImageDisplay()
	{
		return imageDisplay;
	}
	public ArrayList<AtlasImage> getListImage()
	{
		return listImage;
	}
	public String getStrErr()
	{
		return strErr;
	}
	public int getWidth()
	{
		return width;
	}
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	public int getPixels()
	{
		return width*height;
	}
	public String getName()
	{
		return name;
	}
	public String getStrAtlasData()
	{
		return strAtlasData;
	}
}
