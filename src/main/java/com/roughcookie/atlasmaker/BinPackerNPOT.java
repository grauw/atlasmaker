package com.roughcookie.atlasmaker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class BinPackerNPOT extends BinPacker {
	
	private int maxSize;
	
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
		bin.trim();
		
		TextureBin tweakedBin1 = tweakWidth(bin);
		TextureBin tweakedBin2 = tweakHeight(bin);
		
		TextureBin smallestBin = bin;
		if (tweakedBin1.getSize().getPixels() < smallestBin.getSize().getPixels())
			smallestBin = tweakedBin1;
		if (tweakedBin2.getSize().getPixels() < smallestBin.getSize().getPixels())
			smallestBin = tweakedBin2;
		
		textureBins.remove(bin);
		textureBins.add(smallestBin);
	}
	
	private TextureBin tweakWidth(TextureBin bin)
	{
		while (true)
		{
			TextureSize size = bin.getSize();
			TextureBin smallerBin = tweakSize(bin, size.resize(size.getWidth() - 1, size.getHeight()));
			if (smallerBin == bin)
				return bin;
			bin = smallerBin;
		}
	}
	
	private TextureBin tweakHeight(TextureBin bin)
	{
		while (true)
		{
			TextureSize size = bin.getSize();
			TextureBin smallerBin = tweakSize(bin, size.resize(size.getWidth(), size.getHeight() - 1));
			if (smallerBin == bin)
				return bin;
			bin = smallerBin;
		}
	}
	
	private TextureBin tweakSize(TextureBin bin, TextureSize size)
	{
		ArrayList<AtlasImage> images = bin.getImages();
		Collections.sort(images, new CompareArea());  // need to resort
		
		TextureBin smallerBin = createTextureBin(size);
		smallerBin.pack(images);
		smallerBin.trim();
		
		if (images.isEmpty() && smallerBin.getSize().getPixels() < size.getPixels())
			return smallerBin;
		return bin;
	}
	
	public void trim()
	{
		for (TextureBin bin : textureBins)
			bin.trim();
	}
	
}
