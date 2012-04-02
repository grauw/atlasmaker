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

public class TreeNode
{
	private int x;
	private int y;
	private int width;
	private int height;
	private TreeNode leaf1 = null;
	private TreeNode leaf2 = null;
	private int border;
	public AtlasImage image = null;		// used for debugging only

	public TreeNode(int x, int y, int width, int height, int border)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.border = border;
	}
	
	public void createBranches(AtlasImage image)
	{
		int dx = width - image.getWidth();
		int dy = height - image.getHeight();
		// we split to give one very small leaf and one very big one
		// because it allows more efficent use of space
		// if you don't do this, the bottom right corner never gets used
		if (dx < dy)
		{
			//	split so the top is cut in half and the rest is one big rect below
			leaf1 = new TreeNode(x + image.getWidth() + border, y, width - image.getWidth() - border, image.getHeight(), border);
			leaf2 = new TreeNode(x, y + image.getHeight() + border, width, height - image.getHeight() - border, border);
		}
		else
		{
			//	split so the left is cut in half and the rest is one big rect to the right
			leaf1 = new TreeNode(x, y + image.getHeight() + border, image.getWidth(), height - image.getHeight() - border, border);
			leaf2 = new TreeNode(x + image.getWidth() + border, y, width - image.getWidth() - border, height, border);
		}
	}
	
	public boolean add(AtlasImage image)
	{
		if (isEmpty())
		{
			if (fits(image))
			{
				this.image = image;
				
				createBranches(image);
				image.setXY(x, y);
				return true;
			}
			return false;
		}
		if (leaf1.add(image))
		{
			return true;
		}
		if (leaf2.add(image))
		{
			return true;
		}
		return false;
	}
	
	public boolean fits(AtlasImage image)
	{
		return image.getWidth() <= width && image.getHeight() <= height;
	}
	
	public boolean isEmpty()
	{
		return leaf1 == null && leaf2 == null;
	}
}
