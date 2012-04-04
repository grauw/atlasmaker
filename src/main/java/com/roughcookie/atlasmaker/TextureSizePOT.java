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
	public TextureSizePOT(int maxSize)
	{
		this(0, 0, maxSize);
	}
	
	private TextureSizePOT(int width, int height, int maxSize)
	{
		super(width, height, maxSize);
	}
	
	@Override
	public TextureSize inc()
	{
		// swap height and width if it's not square
		// this is to allow for better fitting of wide and tall images
		if (width > height)
			return new TextureSizePOT(height, width, maxSize);  // swap
		else if (height > width)
			return new TextureSizePOT(width * 2, height, maxSize);
		else
			return new TextureSizePOT(width, height * 2, maxSize);
	}
	
	@Override
	public TextureSize dec()
	{
		if (height > width)
			return new TextureSizePOT(height, width, maxSize);  // swap
		else if (height < width)
			return new TextureSizePOT(width / 2, height, maxSize);
		else
			return new TextureSizePOT(width, height / 2, maxSize);
	}
	
	@Override
	public TextureSize getNextSizeFits(int pixelsReq, int minWidth, int minHeight)
	{
		if ((width * height < pixelsReq || width < minWidth || height < minHeight) && !isMaxSize())
			return inc().getNextSizeFits(pixelsReq, minWidth, minHeight);
		return this;
	}
	
	@Override
	public TextureSize resize(int width, int height) {
		if (width <= 0 || (width & (width - 1)) != 0 || height <= 0 || (height & (height - 1)) != 0)
			throw new RuntimeException("Size width and height must be positive and a power of two.");
		
		return new TextureSizePOT(width, height, maxSize);
	}
	
}
