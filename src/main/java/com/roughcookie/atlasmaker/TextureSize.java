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
	protected final int width;
	protected final int height;
	protected final int maxSize;
	protected final int minSize;
	
	protected TextureSize(int width, int height, int maxSize)
	{
		int minSize = 32;
		
		if (maxSize <= minSize)
			throw new RuntimeException("Maximum texture size can not be lower than or equal to minimum.");
		
		this.width = Math.min(Math.max(minSize, width), maxSize);
		this.height = Math.min(Math.max(minSize, height), maxSize);
		this.maxSize = maxSize;
		this.minSize = minSize;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getPixels()
	{
		return width * height;
	}
	
	public boolean isMaxSize()
	{
		return width == maxSize && height == maxSize;
	}
	
	public boolean isMinSize()
	{
		return width == minSize && height == minSize;
	}
	
	abstract public TextureSize inc();
	abstract public TextureSize dec();
	abstract public TextureSize getNextSizeFits(int pixelsReq, int minWidth, int minHeight);
	abstract public TextureSize resize(int width, int height);
}
