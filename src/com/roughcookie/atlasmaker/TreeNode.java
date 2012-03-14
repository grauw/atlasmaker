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
	private AtlasImage image;		// used for debugging only

	public TreeNode(int x, int y, int width, int height)
	{
		this(x, y, width, height, null);
	}
	
	public TreeNode(int x, int y, int width, int height, AtlasImage image)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.image = image;
	}
	
	public void resize(int width, int height)
	{
		this.x = 0;
		this.y = 0;
		this.width = width;
		this.height = height;
		this.image = null;
		leaf1 = null;
		leaf2 = null;
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
			leaf1 = new TreeNode(x + image.getWidth(), y,
					width - image.getWidth(), image.getHeight(), image);
			leaf2 = new TreeNode(x, y + image.getHeight(),
					width, height - image.getHeight(), image);
		}
		else
		{
			//	leaf1 = left (cut in half)
			leaf1 = new TreeNode(x, y + image.getHeight(),
					image.getWidth(), height - image.getHeight(), image);
			// leaf2 = right (not cut)
			leaf2 = new TreeNode(x + image.getWidth(), y,
					width - image.getWidth(), height, image);
		}
	}
	
	public boolean add(AtlasImage image)
	{
		if (empty())
		{
			if (fits(image))
			{
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
	
	public boolean empty()
	{
		return leaf1 == null && leaf2 == null;
	}
}
