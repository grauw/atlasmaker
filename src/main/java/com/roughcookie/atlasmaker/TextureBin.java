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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TextureBin
{
	private File outputFile;
	private TreeNode treeNode;
	private ArrayList<AtlasImage> images;
	private int width;
	private int height;
	private List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	
	public TextureBin(File outputFile, TextureSize textureSize, int border)
	{
		this.outputFile = outputFile;
		this.width = textureSize.getWidth();
		this.height = textureSize.getHeight();
		treeNode = new TreeNode(0, 0, width, height, border);
		images = new ArrayList<AtlasImage>();
	}
	
	public boolean add(AtlasImage image)
	{
		boolean added = treeNode.add(image);
		if (added)
			images.add(image);
		return added;
	}
	
	public void pack(ArrayList<AtlasImage> imageList)
	{
		for (int ixImage = imageList.size() - 1; ixImage >= 0; ixImage--)
		{
			if (!add(imageList.get(ixImage)))
				return;
			imageList.remove(ixImage);
		}
	}
	
	public void resize(int width, int height, ArrayList<AtlasImage> imageList)
	{
		// puts images removed back on the list
		this.remove(imageList);
		// need to resort, because they may have been put in wrong order
		Collections.sort(imageList, new CompareArea());
		// resize resets everything
		treeNode.resize(width, height);
		this.resize(width, height);
	}
	
	/**
	 * Clears all images from the bin and re-adds them to the image list.
	 * @param imageList
	 */
	private void remove(ArrayList<AtlasImage> imageList)
	{
		imageList.addAll(images);
		images.clear();
	}
	
	private void resize(int width, int height)
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
	
	public void write()
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
			ImageIO.write(imageOut, "png", outputFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not save " + outputFile.getName(), e);
		}
		
		fireProgressTick();
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
	
	public JSONObject toJSON() throws JSONException 
	{
		JSONObject texture = new JSONObject();
		texture.put("path", outputFile.getName());
		texture.put("width", width);
		texture.put("height", height);
		texture.put("transparent", isTransparent());
		JSONArray imagesJson = new JSONArray();
		for (AtlasImage image : images)
		{
			imagesJson.put(image.toJSON());
		}
		texture.put("images", imagesJson);
		return texture;
	}
	
	public void addProgressListener(ProgressListener listener)
	{
		progressListeners.add(listener);
	}
	
	public void removeProgressListener(ProgressListener listener)
	{
		progressListeners.remove(listener);
	}
	
	private void fireProgressTick() {
		for (ProgressListener listener : progressListeners)
			listener.progressTick();
	}
}
