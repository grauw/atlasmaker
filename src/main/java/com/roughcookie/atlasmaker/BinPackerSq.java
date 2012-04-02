package com.roughcookie.atlasmaker;

import java.io.File;

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
	
}
