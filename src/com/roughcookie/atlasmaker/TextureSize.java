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

public abstract class TextureSize
{
	protected int width;
	protected int height;
	protected int maxSize;
	protected int minSize;
	
	public TextureSize(int maxSize)
	{
		this.width = 0;
		this.height = 0;
		this.maxSize = maxSize;
		this.minSize = 32;
	}
	
	public void setMaxSize(int maxSize)
	{
		this.maxSize = maxSize;
	}
	
	public int getNumPixels()
	{
		return width*height;
	}
	
	public boolean isMaxSize()
	{
		return width == maxSize && height == maxSize;
	}
	
	public boolean isMinSize()
	{
		return width == minSize && height == minSize;
	}
	
	protected void checkMax()
	{
		if (width > maxSize)
			width = maxSize;
		if (height > maxSize)
			height = maxSize;
	}
	
	protected void checkMin()
	{
		if (width < minSize)
			width = minSize;
		if (height < minSize)
			height = minSize;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public void setWidth(int width)
	{
		this.width = width;
	}
	
	abstract public void inc();
	abstract public void dec();
	abstract public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight);
	abstract public boolean isPowerOfTwo();
}
