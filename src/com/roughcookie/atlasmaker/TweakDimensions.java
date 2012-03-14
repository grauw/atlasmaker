package com.roughcookie.atlasmaker;
import java.util.ArrayList;


public class TweakDimensions
{
	private int bestSavings;
	private int bestWidth;
	private int bestHeight;
	private int origWidth;
	private int origHeight;
	
	public void run(TextureBin bin, ArrayList<AtlasImage> listImage)
	{
		bestSavings = 0;
		bin.trim();
		origWidth = bin.getWidth();
		origHeight = bin.getHeight();
		bestWidth = origWidth;
		bestHeight = origHeight;
		tweakWidthHeight(bin, listImage, true);
		bin.setSize(origWidth, origHeight);
		tweakWidthHeight(bin, listImage, false);
		if (bestWidth != bin.getWidth() || bestHeight != bin.getHeight())
		{
			bin.resize(bestWidth, bestHeight, listImage); 
			bin.pack(listImage);
		}
		assert(listImage.size() == 0);
	}
	
	private void tweakWidthHeight(TextureBin bin, ArrayList<AtlasImage> listImage, boolean doWidth)
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
			bin.resize(width, height, listImage); 
			bin.pack(listImage);
			if (listImage.size() > 0)
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
