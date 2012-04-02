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

import java.util.ArrayList;

public class TweakDimensions
{
	private int bestSavings;
	private int bestWidth;
	private int bestHeight;
	private int origWidth;
	private int origHeight;
	
	public void run(TextureBin bin, ArrayList<AtlasImage> imageList)
	{
		bestSavings = 0;
		bin.trim();
		origWidth = bin.getWidth();
		origHeight = bin.getHeight();
		bestWidth = origWidth;
		bestHeight = origHeight;
		tweakWidthHeight(bin, imageList, true);
		bin.setSize(origWidth, origHeight);
		tweakWidthHeight(bin, imageList, false);
		if (bestWidth != bin.getWidth() || bestHeight != bin.getHeight())
		{
			bin.resize(bestWidth, bestHeight, imageList); 
			bin.pack(imageList);
		}
		assert(imageList.size() == 0);
	}
	
	private void tweakWidthHeight(TextureBin bin, ArrayList<AtlasImage> imageList, boolean doWidth)
	{
		int width = origWidth; 
		int height = origHeight;
		int savings, prevSavings;
		savings = 0;
		int prevWidth, prevHeight;
		do
		{
			prevWidth = width;
			prevHeight = height;
			prevSavings = savings;
			// reduce by two pixels to force minimum re-organise
			if (doWidth)
			{
				width = bin.getWidth() - 1;
//				height = origWidth * origHeight / width;
			}
			else
			{
				height = bin.getHeight() - 1;
//				width = origWidth * origHeight / height;
			}
			bin.resize(width, height, imageList); 
			bin.pack(imageList);
			if (imageList.size() > 0)
			{
				break;
			}
			// check if we gained anything
			bin.trim();
			savings = origWidth * origHeight - bin.getPixels();
		} while (savings >= prevSavings);
		
		if (bestSavings < prevSavings)
		{
			bestSavings = prevSavings;
			bestWidth = prevWidth;
			bestHeight = prevHeight;
		}
	}
}
