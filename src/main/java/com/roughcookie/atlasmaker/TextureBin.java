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
	private TreeNode treeNode;
	private TextureSize textureSize;
	private int border;
	private List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	
	public TextureBin(TextureSize textureSize, int border)
	{
		this.textureSize = textureSize;
		this.border = border;
		treeNode = new TreeNode(0, 0, textureSize.getWidth(), textureSize.getHeight(), border);
	}
	
	public TextureSize getSize()
	{
		return textureSize;
	}
	
	public void resize(TextureSize size, ArrayList<AtlasImage> imageList)
	{
		textureSize = size;
		clear(imageList);
	}
	
	public ArrayList<AtlasImage> getImages()
	{
		ArrayList<AtlasImage> images = new ArrayList<AtlasImage>();
		for (TreeNode node : treeNode.getImageNodes())
			images.add(node.getImage());
		return images;
	}
	
	public boolean add(AtlasImage image)
	{
		return treeNode.add(image);
	}
	
	/**
	 * Clears all images from the bin and re-adds them to the image list.
	 * @param imageList The list to put the removed images into.
	 */
	public void clear(ArrayList<AtlasImage> imageList)
	{
		imageList.addAll(getImages());
		
		// need to resort, because they may have been put in wrong order
		Collections.sort(imageList, new CompareArea());
		
		treeNode = new TreeNode(0, 0, textureSize.getWidth(), textureSize.getHeight(), border);
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
	
	// trims dimensions to minimium
	public void trim()
	{
		int width = 0;
		int height = 0;
		for (TreeNode node : treeNode.getImageNodes())
		{
			width = Math.max(width, node.getX() + node.getImage().getWidth());
			height = Math.max(height, node.getY() + node.getImage().getHeight());
		}
		
		textureSize = textureSize.resize(width, height);
	}
	
	public boolean isTransparent()
	{
		for (AtlasImage image : getImages())
			if (image.isTransparent())
				return true;
		return false;
	}
	
	public void write(File outputFile)
	{
		BufferedImage imageOut = new BufferedImage(textureSize.getWidth(), textureSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imageOut.createGraphics();
		
		List<TreeNode> imageNodes = treeNode.getImageNodes();
		
		for (TreeNode node : imageNodes)
		{
			node.getImage().draw(g, node);
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
	
	public JSONObject toJSON(File outputFile) throws JSONException 
	{
		JSONObject texture = new JSONObject();
		texture.put("path", outputFile.getName());
		texture.put("width", textureSize.getWidth());
		texture.put("height", textureSize.getHeight());
		texture.put("transparent", isTransparent());
		JSONArray imagesJson = new JSONArray();
		for (TreeNode node : treeNode.getImageNodes())
		{
			imagesJson.put(node.getImage().toJSON(node));
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
