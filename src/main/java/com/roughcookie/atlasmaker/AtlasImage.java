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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

public class AtlasImage 
{
	private int xOffset;
	private int yOffset;
	private int opaqueWidth;
	private int opaqueHeight;
	private boolean transparent;
	private BufferedImage image;
	private File file;
	private File baseDir;  // base dir of source files for index (manifest location)
	
	public AtlasImage(File file, File baseDir)
	{
		this.file = file;
		this.baseDir = baseDir;
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
	
	public int getPixelCount()
	{
		return opaqueHeight * opaqueWidth;
	}
	
	public int getHeight()
	{
		return opaqueHeight;
	}
	
	public int getWidth()
	{
		return opaqueWidth;
	}
	
	public void draw(BufferedImage target, TreeNode node)
	{
		draw(target, node.getX(), node.getY());
	}
	
	public void draw(BufferedImage target, int x, int y)
	{
		// when possible, copy image data to preserve colours for pixels with alpha 0
		if ((target.getType() == BufferedImage.TYPE_4BYTE_ABGR || target.getType() == BufferedImage.TYPE_INT_ARGB) &&
				(image.getType() == BufferedImage.TYPE_4BYTE_ABGR || image.getType() == BufferedImage.TYPE_INT_ARGB))
		{
			target.getRaster().setRect(x - xOffset, y - yOffset, image.getData(new Rectangle(xOffset, yOffset, opaqueWidth, opaqueHeight)));
		}
		else
		{
			Graphics2D g = target.createGraphics();
			g.setClip(x, y, opaqueWidth, opaqueHeight);
			g.drawImage(image, null, x - xOffset, y - yOffset);
			g.dispose();
		}
	}
	
	public boolean detectAlpha(int alphaThresh)
	{
		int transWidth = image.getWidth();
		int transHeight = image.getHeight();
		int[] srcPixels;
		try
		{
			srcPixels = new int[transWidth * transHeight];
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
	
	public JSONObject toJSON(TreeNode node) throws JSONException 
	{
		JSONObject imageJson = new JSONObject();
		imageJson.put("path", getRelativePath());
		JSONObject coordinates = new JSONObject();
		coordinates.put("x", node.getX());
		coordinates.put("y", node.getY());
		coordinates.put("width", opaqueWidth);
		coordinates.put("height", opaqueHeight);
		imageJson.put("coordinates", coordinates);
		imageJson.put("xOffset", xOffset);
		imageJson.put("yOffset", yOffset);
		imageJson.put("width", image.getWidth());
		imageJson.put("height", image.getHeight());
		imageJson.put("transparent", transparent);  // XXX not interesting?
		return imageJson;
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
