package com.roughcookie.atlasmaker;

import java.io.File;
import java.util.ArrayList;

public class BinPackerNPOT extends BinPacker {
	
	private int maxSize;
	private int bestSavings;
	private TextureSize bestSize;
	private TextureSize origSize;
	
	public BinPackerNPOT(File outputDir, int maxSize, int border) {
		super(outputDir, border);
		this.maxSize = maxSize;
	}
	
	@Override
	public TextureSize createTextureSize() {
		return new TextureSizeNPOT(maxSize);
	}
	
	@Override
	public void pack(ArrayList<AtlasImage> images) {
		super.pack(images);
		
		trim();
	}
	
	@Override
	protected void repackLastBin() {
		super.repackLastBin();
		
		TextureBin bin = getLastTextureBin();
		ArrayList<AtlasImage> imageList = new ArrayList<AtlasImage>();
		
		bestSavings = 0;
		bin.trim();
		origSize = bin.getSize();
		bestSize = origSize;
		tweakWidthHeight(bin, imageList, true);
		bin.setSize(origSize);
		tweakWidthHeight(bin, imageList, false);
		if (!bestSize.equals(bin.getSize()))
		{
			bin.resize(bestSize, imageList); 
			bin.pack(imageList);
		}
		assert(imageList.isEmpty());
	}
	
	private void tweakWidthHeight(TextureBin bin, ArrayList<AtlasImage> imageList, boolean doWidth)
	{
		TextureSize size = origSize;
		int savings = 0;
		TextureSize prevSize;
		int prevSavings;
		do
		{
			prevSize = size;
			prevSavings = savings;
			// reduce by two pixels to force minimum re-organise
			if (doWidth)
			{
				size = size.resize(size.getWidth() - 1, size.getHeight());
			}
			else
			{
				size = size.resize(size.getWidth(), size.getHeight() - 1);
			}
			bin.resize(size, imageList);
			bin.pack(imageList);
			if (!imageList.isEmpty())
			{
				break;
			}
			// check if we gained anything
			bin.trim();
			savings = origSize.getPixels() - bin.getSize().getPixels();
		} while (savings >= prevSavings);
		
		if (bestSavings < prevSavings)
		{
			bestSavings = prevSavings;
			bestSize = prevSize;
		}
	}
	
	public void trim()
	{
		for (TextureBin bin : textureBins)
			bin.trim();
	}
	
}
