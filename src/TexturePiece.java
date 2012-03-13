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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;

public class TexturePiece
{
	private ArrayList<AtlasImage> images;
	private BufferedImage imageDisplay;
	private String name;
	private String strAtlasData;
	private int width;
	private int height;
	
	public TexturePiece(int width, int height)
	{
		this.width = width;
		this.height = height;
    	images = new ArrayList<AtlasImage>();
	}
	
	public void add(AtlasImage image)
	{
		images.add(image);
	}
	
	public void remove(ArrayList<AtlasImage> listImageReturn)
	{
		listImageReturn.addAll(images);
		images.clear();
	}
	
	public void resize(int width, int height)
	{
		this.width = width;
		this.height = height;
		images.clear();
	}
	
	// trims dimensions to minimium
	public void trim()
	{
		width = 0;
		height = 0;
		for (AtlasImage image : images)
		{
            if (width < image.getRight()) 
            	width = image.getRight();
            if (height < image.getBottom()) 
            	height = image.getBottom();
		} 
	}
	
	public boolean isTransparent()
	{
		for (AtlasImage image : images)
            if (image.isTransparent())
            	return true;
		return false;
	}
	
	public void createStuff(String strOutFile, int index)
	{
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
	    	stub = strOutFile.substring(posDir + 1);
	    	dir = strOutFile.substring(0, posDir + 1);
	    }
	    
		name = stub + "-" + index + ".png";
		fileName = name;
		
	    strAtlasData = "  <texture file=\"" + name + "\" trans=\"";
	    if (isTransparent())
	    	strAtlasData += "true";
	    else
	    	strAtlasData += "false";
	    strAtlasData += "\">\n";
	    Collections.sort(images, new CompareName());
	    int n;
	    for (n = 0; n < images.size(); n++) 
	    {
	    	images.get(n).draw(g);
	    	images.get(n).drawWithBorder(gDisplay);
	    	strAtlasData += images.get(n).GetSerialize();
	    } 
	    strAtlasData += "  </texture>\n";
	    gDisplay.setColor(Color.black);
	    gDisplay.drawRect(0, 0, imageOut.getWidth()-1, imageOut.getHeight()-1);       
	    g.dispose();
	    gDisplay.dispose();
	    
	    //  Write generated image to a file
	    File file = new File(dir, fileName);
	    try
	    {
	        // Save as PNG
    		ImageIO.write(imageOut, "png", file);
	    } 
	    catch (IOException e)
	    {
	    	throw new RuntimeException("Could not save " + file.getName(), e);
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
		return images;
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
		return width * height;
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
