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

import java.io.*;
import java.util.*;

public class Atlas
{
	private List<File> sourceFiles;
	private File baseDir;  // base dir of source files for index (manifest location)
	private File outputDir;
	private String name;
	private int maxSize = 1024;
	private boolean powersOfTwo = true;
	private boolean square = true;
	private int alphaThreshold = 0;
	private int imageBorder = 0;
	
	public Atlas(List<File> sourceFiles, File baseDir, File outputDir, String name)
	{
		if (sourceFiles == null || sourceFiles.size() == 0)
			throw new RuntimeException("No files specified.");
		if (outputDir == null || !outputDir.isDirectory())
			throw new RuntimeException("Output location is not a directory.");
		
		this.sourceFiles = sourceFiles;
		this.baseDir = baseDir;
		this.outputDir = outputDir;
		this.name = name;
	}
	
	public File getOutputDir()
	{
		return outputDir;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setMaxSize(int size)
	{
		if (size <= 0)
			throw new RuntimeException("Size out of range.");
		maxSize = size;
	}
	
	public void setPowersOfTwo(boolean enable)
	{
		powersOfTwo = enable;
	}
	
	public void setSquare(boolean enable)
	{
		square = enable;
	}
	
	/**
	 * Sets alpha threshold value for cropping transparent regions off atlas images.
	 * 
	 * @param threshold Threshold from 0 to 255, or -1 to disable alpha cropping.
	 */
	public void setAlphaThreshold(int threshold)
	{
		if (threshold < -1 || threshold > 254)
			throw new RuntimeException("Threshold out of range.");
		alphaThreshold = threshold;
	}
	
	public void setImageBorder(int border)
	{
		if (border < 0)
			throw new RuntimeException("Border out of range.");
		imageBorder = border;
	}
	
	private TextureSize getTextureSize()
	{
		if (square && powersOfTwo)
			return new TextureSizeSq(maxSize);
		if (!square && powersOfTwo)
			return new TextureSizePOT(maxSize);
		if (!square && !powersOfTwo)
			return new TextureSizeNPOT(maxSize);
		throw new RuntimeException("Unsupported square/powersOfTwo combination.");
	}
	
	public void create()
	{
		ArrayList<AtlasImage> images = createAtlasImages(sourceFiles);
		
		TextureSize textureSize = getTextureSize();
		BinPacker packer = new BinPacker(outputDir);
		packer.pack(textureSize, images);
		
		// try making texture size smaller by making it more rectangular
		if (!textureSize.isPowerOfTwo())
		{
			TweakDimensions tweakDimensions = new TweakDimensions();
			tweakDimensions.run(packer.getLastTextureBin(), images);
			
			packer.trim();
		}
		
		packer.write();
	}
	
	private ArrayList<AtlasImage> createAtlasImages(List<File> files)
	{
		ArrayList<AtlasImage> images = new ArrayList<AtlasImage>();
		for (File file : files)
		{
			try 
			{
				images.add(createAtlasImage(file));
			}
			catch (IOException e) 
			{
				throw new RuntimeException("Could not load " + file.getName(), e);
			}
		}
		if (images.size() == 0)
			throw new RuntimeException("No images created.");
		
		// sort images in reverse order
		// this is so we can remove from end of list as we add to atlas
		Collections.sort(images, new CompareArea());
		
		return images;
	}
	
	private AtlasImage createAtlasImage(File file) throws IOException
	{
		AtlasImage image = new AtlasImage(file, baseDir);
		image.setBorder(imageBorder);
		image.load();
		image.cropTransparent(alphaThreshold);

		// don't add images that don't fit
		if (image.getWidth() > maxSize || image.getHeight() > maxSize)
		{
			throw new RuntimeException("image too big to fit in texture: " + image.getFile());
		}
		
		return image;
	}
}
