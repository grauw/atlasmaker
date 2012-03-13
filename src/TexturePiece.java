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

	public void write(File outFile, int index)
	{
		write(new File(outFile.getPath() + "-" + index + ".png"));
	}
	
	public void write(File outFile)
	{
		BufferedImage imageOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imageOut.createGraphics();
		
		Collections.sort(images, new CompareName());
		
		for (AtlasImage image : images)
		{
			image.draw(g);
		}
		g.dispose();
		
		try
		{
			ImageIO.write(imageOut, "png", outFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not save " + outFile.getName(), e);
		}
	}
	
	public int getHeight()
	{
		return height;
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
	
	public String toXMLString(File outFile)
	{
		String strAtlasData = "  <texture file=\"" + outFile.getName() + "\" trans=\"";
		if (isTransparent())
			strAtlasData += "true";
		else
			strAtlasData += "false";
		strAtlasData += "\">\n";
		Collections.sort(images, new CompareName());
		
		for (AtlasImage image : images)
		{
			strAtlasData += image.toXMLString();
		}
		strAtlasData += "  </texture>\n";
		return strAtlasData;
	}
}
