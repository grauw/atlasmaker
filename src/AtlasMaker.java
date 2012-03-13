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
		if (square)
			return new TextureSizeSq(maxSize);
		if (powersOfTwo)
			return new TextureSizePOT(maxSize);
		else
			return new TextureSizeNPOT(maxSize);
	}
	
	public ArrayList<TexturePiece> create(File[] files, String outFile)
	{
		if (files == null || files.length == 0)
			throw new RuntimeException("No files specified.");
		
		ArrayList<AtlasImage> listImage = createAtlasImages(files);
		
		TextureSize textureSize = getTextureSize();
		BinPackerList binPackerList = binPack(textureSize, listImage);
		
		// try making texture size smaller by making it more rectangular
		if (textureSize.isNPOT())
		{
			TweekDimensions tweekDimensions = new TweekDimensions();
			tweekDimensions.run(binPackerList.get(binPackerList.size() - 1), listImage);
		}
		
		int i = 0;
		ArrayList<TexturePiece> texturePieces = new ArrayList<TexturePiece>();
		for (BinPacker binPacker : binPackerList)
		{
			if (textureSize.isNPOT())
			{
				binPacker.getTexturePiece().trim();
			}
			binPacker.getTexturePiece().createStuff(outFile, i++);
			texturePieces.add(binPacker.getTexturePiece());
		}
		
		return texturePieces;
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
	
	private BinPackerList binPack(TextureSize textureSize, ArrayList<AtlasImage> atlasImages)
	{
		BinPackerList binPackerList = new BinPackerList();
		BinPacker binPacker;
		
		while(true)
		{
			int maxWidth = 0;
			int maxHeight = 0;
			int pixelsReq = 0;
			for (AtlasImage atlasImage : atlasImages)
			{
				if (maxWidth < atlasImage.getWidth())
					maxWidth = atlasImage.getWidth();
				if (maxHeight < atlasImage.getHeight())
					maxHeight = atlasImage.getHeight();
				pixelsReq += atlasImage.getNumPixels();
			}
			textureSize.getNextSizeFits(pixelsReq, maxWidth, maxHeight);
			binPacker = new BinPacker(textureSize.getWidth(), textureSize.getHeight());
			binPackerList.add(binPacker);
			binPackerList.pack(atlasImages);
			
			if (atlasImages.size() == 0) 
			{
				if (binPackerList.size() > 1 && !textureSize.isMinSize())
				{
					// there is opportunity for further reduction
					// because some images might fit in gaps of previous atlases
					int prevWidth, prevHeight;
					do
					{
						prevWidth = textureSize.getWidth();
						prevHeight = textureSize.getHeight();
						textureSize.dec();
						binPacker.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
						binPackerList.pack(atlasImages);
						
					} while (atlasImages.size() == 0 && !textureSize.isMinSize());
					// revert back to last size that fit
					binPacker.resize(prevWidth, prevHeight, atlasImages); 
					binPackerList.pack(atlasImages);
					
					assert(atlasImages.size() == 0);
				}
				return binPackerList;
			}
			
			while (!textureSize.isMaxSize())
			{
				textureSize.inc();
				binPacker.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
				binPackerList.pack(atlasImages);
				
				if (atlasImages.size() == 0)
					return binPackerList;
			}
			// recalculate values for remainder images
		}
	}
}
