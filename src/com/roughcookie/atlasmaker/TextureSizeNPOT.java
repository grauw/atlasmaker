package com.roughcookie.atlasmaker;

/*
Copyright (c) 2012, Ed Welch
Copyright (c) 2012, Rough Cookie
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

public class TextureSizeNPOT extends TextureSize
{
	protected int ixSize;
	public static int STEP_SIZE = 64;
	
	public TextureSizeNPOT(int maxSize)
	{
		super(maxSize);
	}
	
	public void inc()
	{
//		if (height > width && ixSize % 1 == 0)
//		{
//			int temp = height;
//			height = width;
//			width = temp;
//		}
		if (height > width)
			width += STEP_SIZE;
		else
			height += STEP_SIZE;
//		ixSize++;
		checkMax();
	}
	
	public void dec()
	{
		if (height < width)
			width -= STEP_SIZE;
		else
			height -= STEP_SIZE;
//		ixSize++;
		checkMin();
	}
	
	public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight)
	{
		// some of the smaller images fit into the other textures
		// so deliberly choose smaller size
		ixSize = 0;
		int size = (int)Math.round(Math.sqrt(pixelsReq));
		if (size < 32) size = 32;
		if (maxWidth > size)
		{
			width = maxWidth;
			height = (int)pixelsReq / width+1;
		}
		else
		{
			width = size;
		}
		if (maxHeight > size)
		{
			height = maxHeight;
			width = (int)pixelsReq / height+1;
		}
		else
		{
			height = size;
		}
		checkMax();
	}
	
	public boolean isPowerOfTwo() {
		return false;
	}

}
