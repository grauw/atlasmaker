package com.roughcookie.atlasmaker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class BinPackerSq extends BinPacker {
	
	private int maxSize;
	
	public BinPackerSq(File outputDir, int maxSize, int border) {
		super(outputDir, border);
		this.maxSize = maxSize;
	}
	
	@Override
	public TextureSize createTextureSize() {
		return new TextureSizeSq(maxSize);
	}
	
	@Override
	protected void repackLastBin()
	{
		super.repackLastBin();
		
		// try to cut the texture in four
		TextureBin bin = getLastTextureBin();
		if (bin.getSize().isMinSize())
			return;
		
		int halfSize = bin.getSize().getWidth() / 2;
		for (AtlasImage image : bin.getImages())
			if (image.getWidth() > halfSize || image.getHeight() > halfSize)
				return;
		
		// take all images
		ArrayList<AtlasImage> atlasImages = new ArrayList<AtlasImage>();
		bin.clear(atlasImages);
		
		BinPacker packer = new BinPackerSq(outputDir, halfSize, border);
		
		packer.pack(atlasImages);
		
		int pixels = 0;
		for (TextureBin newBin : packer.getTextureBins())
			pixels += newBin.getSize().getPixels();
		
		if (pixels < bin.getSize().getPixels())
		{
			textureBins.remove(bin);
			textureBins.addAll(Arrays.asList(packer.getTextureBins()));
		}
		else
		{
			// restore bin (yuck!)
			// needed so images are positioned correctly
			for (TextureBin smallBin : packer.getTextureBins())
				smallBin.clear(atlasImages);
			this.pack2(atlasImages);
			
			assert(atlasImages.isEmpty());
		}
	}
	
}
