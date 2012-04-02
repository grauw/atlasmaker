package com.roughcookie.atlasmaker;

import java.io.File;
import java.util.ArrayList;

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

		TweakDimensions tweakDimensions = new TweakDimensions();
		tweakDimensions.tweak(getLastTextureBin(), images);
		
		trim();
	}
	
}
