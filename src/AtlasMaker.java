/*
Copyright (c) 2012, Ed Welch
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

public class AtlasMaker
{
	private int maxSize = 1024;
	private boolean powersOfTwo = true;
	private boolean square = true;
	private int alphaThreshold = 0;
	private int imageBorder = 0;
	
	public AtlasMaker()
	{
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
	
	public void setAlphaThreshold(int threshold)
	{
		if (threshold < 0 || threshold > 254)
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
	
	public BinPacker create(File[] files, File outFile)
	{
		if (files == null || files.length == 0)
			throw new RuntimeException("No files specified.");
		
		ArrayList<AtlasImage> listImage = createAtlasImages(files);
		
		TextureSize textureSize = getTextureSize();
		BinPacker packer = new BinPacker();
		packer.binPack(textureSize, listImage);
		
		// try making texture size smaller by making it more rectangular
		if (!textureSize.isPowerOfTwo())
		{
			TweakDimensions tweakDimensions = new TweakDimensions();
			tweakDimensions.run(packer.get(packer.size() - 1), listImage);
			
			packer.trim();
		}
		
		packer.write(outFile);
		
		return packer;
	}
	
	private ArrayList<AtlasImage> createAtlasImages(File[] files)
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
		AtlasImage image = new AtlasImage();
		image.setBorder(imageBorder);
		image.load(file);
		image.cropTransparent(alphaThreshold);

		// don't add images that don't fit
		if (image.getWidth() > maxSize || image.getHeight() > maxSize)
		{
			throw new RuntimeException("image too big to fit in texture: " + image.getFileName());
		}
		
		return image;
	}
}
