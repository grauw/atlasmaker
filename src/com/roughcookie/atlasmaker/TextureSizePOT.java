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

public class TextureSizePOT extends TextureSize
{
	protected int ixSize;
	
	public TextureSizePOT(int maxSize)
	{
		super(maxSize);
	}
	
	public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight)
	{
		ixSize = 0;
		width = 32;
		height = 32;
		while ((width*height < pixelsReq ||
				width < maxWidth || height < maxHeight) && !isMaxSize())
		{
			if (height > width)
				width *= 2;
			else
				height *= 2;
			ixSize++;
		}
	}
	
	public void inc()
	{
		// swap height and width if it's not square
		// this is to allow for better fitting of width and tall images
		if (height > width && ixSize % 1 == 0)
		{
			int temp = height;
			height = width;
			width = temp;
		}
		else if (height > width)
			width *= 2;
		else
			height *= 2;
		ixSize++;
		checkMax();
	}
	
	public void dec()
	{
		if (height < width)
			width /= 2;
		else
			height /= 2;
		checkMin();
	}
	
	public boolean isPowerOfTwo() {
		return true;
	}
}
