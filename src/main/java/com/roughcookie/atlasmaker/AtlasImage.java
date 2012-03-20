package com.roughcookie.atlasmaker;

/*
Copyright (c) 2012, Rough Cookie and Ed Welch
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
import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

public class AtlasImage 
{
	private int border;
	private int x;
	private int y;
	private int xOffset;
	private int yOffset;
	private int opaqueWidth;
	private int opaqueHeight;
	private int transWidth;
	private int transHeight;
	private boolean transparent;
	private BufferedImage image;
	private File file;
	private File baseDir;  // base dir of source files for index (manifest location)
	
	public AtlasImage(File file, File baseDir)
	{
		this.file = file;
		this.baseDir = baseDir;
		border = 0;
	}
	
	public void setBorder(int border)
	{
		this.border = border;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public boolean isTransparent()
	{
		return transparent;
	}
	
	public void load() throws IOException
	{
		image = ImageIO.read(file);
	}
	
	public boolean hasAlphaBorder()
	{
		return opaqueWidth < image.getWidth() || opaqueHeight < image.getHeight();
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public int getNumPixels()
	{
		return (opaqueHeight + border) * (opaqueWidth + border);
	}
	
	public boolean pointInside(int xTest, int yTest)
	{
		return xTest > x && xTest < x + getWidth() &&
				yTest > y && yTest < y + getHeight();
	}
	
	public int getHeight()
	{
		return image.getHeight() + border;
	}
	
	public int getWidth()
	{
		return image.getWidth() + border;
	}
	
	public int getBottom()
	{
		return y + image.getHeight();
	}
	
	public int getRight()
	{
		return x + image.getWidth();
	}
	
	public void setImage(BufferedImage image)
	{
		this.image = image;
	}
	
	public void draw(Graphics2D g)
	{
		g.drawImage(image, null, x, y);		
	}
	
	public void drawWithBorder(Graphics2D g)
	{
		g.drawImage(image, null, x, y);		
		g.drawRect(x, y, getWidth() - 1 - border, getHeight() - 1 - border);
	}
	
	public int getX()
	{
		return x;
	}
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	public void setXY(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void cropTransparent(int alphaThresh) 
	{
		if (!detectAlpha(alphaThresh)) return;
		BufferedImage imageCut;
		imageCut = new BufferedImage(opaqueWidth, opaqueHeight, 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imageCut.createGraphics();
		g.drawImage(image, null, -xOffset, -yOffset);		
		g.dispose();
		image = imageCut;
	}

	public boolean detectAlpha(int alphaThresh) 
	{
		transWidth = image.getWidth(null);
		transHeight = image.getHeight(null);
		int[] srcPixels;
		try
		{
			srcPixels = new int[transWidth*transHeight];
		}
		catch (OutOfMemoryError e) 
		{
			return false;
		}
		image.getRGB(0, 0, transWidth, transHeight, srcPixels, 0, transWidth);
		
		int srcX, srcY;
		int alpha;
		int xFirst = transWidth + 1;
		int xLast = -1;
		int yFirst = transHeight + 1;
		int yLast = -1;
		transparent = false;
		for (srcY = 0; srcY < transHeight; ++srcY) 
		{
			for (srcX = 0; srcX < transWidth; ++srcX) 
			{
				alpha = (srcPixels[srcX + srcY * transWidth] >> 24) & 0xff;
				if (alpha > alphaThresh)
				{
					if (xLast < srcX) xLast = srcX;  
					if (xFirst > srcX) xFirst = srcX;  
					if (yLast < srcY) yLast = srcY;  
					if (yFirst > srcY) yFirst = srcY;  
				}
				if (alpha < 254)
				{
					transparent = true;
				}
			}
		}
		opaqueWidth = xLast - xFirst + 1;
		opaqueHeight = yLast - yFirst + 1;
		if (opaqueWidth < 1 || opaqueHeight < 1)
		{
			xOffset = 0;
			yOffset = 0;
			opaqueWidth = 1;
			opaqueHeight = 1;
			System.out.println("Warning: image with no opaque pixels! " + file.getName());
		}
		else
		{
			xOffset = xFirst;
			yOffset = yFirst;
		}
		return true;
	}
	
	public JSONObject toJSON() throws JSONException 
	{
		JSONObject image = new JSONObject();
		image.put("path", getRelativePath());
		JSONObject coordinates = new JSONObject();
		coordinates.put("x", x);
		coordinates.put("y", y);
		coordinates.put("width", opaqueWidth);
		coordinates.put("height", opaqueHeight);
		image.put("coordinates", coordinates);
		image.put("xOffset", xOffset);
		image.put("yOffset", yOffset);
		image.put("width", transWidth);
		image.put("height", transHeight);
		image.put("transparent", transparent);  // XXX not interesting?
		return image;
	}
	
	private String getRelativePath()
	{
		try
		{
			String baseDirPath = baseDir.getCanonicalPath() + File.separator;
			String filePath = file.getCanonicalPath();
			if (!filePath.startsWith(baseDirPath))
				throw new RuntimeException("Atlas file is not contained in manifest directory: " + file);
			return filePath.substring(baseDirPath.length()).replace(File.separatorChar, '/');
		}
		catch (IOException e)
		{
			throw new RuntimeException("IO exception while determining canonical path for file: " + file, e);
		}
	}
}
