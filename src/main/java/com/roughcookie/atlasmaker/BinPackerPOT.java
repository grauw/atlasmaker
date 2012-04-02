package com.roughcookie.atlasmaker;

import java.io.File;

public class BinPackerPOT extends BinPacker {
	
	private int maxSize;
	
	public BinPackerPOT(File outputDir, int maxSize, int border) {
		super(outputDir, border);
		this.maxSize = maxSize;
	}
	
	@Override
	public TextureSize createTextureSize() {
		return new TextureSizePOT(maxSize);
	}
	
}
