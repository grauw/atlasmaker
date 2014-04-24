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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;

import com.esotericsoftware.wildcard.Paths;

public abstract class BinPacker
{
	protected File outputDir;
	protected ArrayList<TextureBin> textureBins;
	protected int border;
	
	public BinPacker(File outputDir, int border) {
		this.outputDir = outputDir;
		this.border = border;
		textureBins = new ArrayList<TextureBin>();
	}
	
	public TextureBin[] getTextureBins()
	{
		return textureBins.toArray(new TextureBin[] {});
	}
	
	public TextureBin getLastTextureBin()
	{
		return textureBins.get(textureBins.size() - 1);
	}
	
	public abstract TextureSize createTextureSize();
	
	private TextureSize createFittingTextureSize(ArrayList<AtlasImage> atlasImages)
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
			pixelsReq += atlasImage.getPixelCount();
		}
		return createTextureSize().getNextSizeFits(pixelsReq, maxWidth, maxHeight);
	}
	
	protected TextureBin createTextureBin(TextureSize size)
	{
		return new TextureBin(size, border);
	}
	
	public void pack(ArrayList<AtlasImage> atlasImages)
	{
		while (!atlasImages.isEmpty())
		{
			TextureBin bin = createTextureBin(createFittingTextureSize(atlasImages));
			textureBins.add(bin);
			
			this.pack2(atlasImages);
			
			// scale up the texture if useful
			while (!atlasImages.isEmpty() && !bin.getSize().isMaxSize())
			{
				atlasImages.addAll(bin.getImages());
				Collections.sort(atlasImages, new CompareArea());  // need to resort
				
				TextureBin biggerBin = createTextureBin(bin.getSize().inc());
				replaceTextureBin(bin, biggerBin);
				bin = biggerBin;
				
				this.pack2(atlasImages);
			}
		}
		
		if (!textureBins.isEmpty())
			repackLastBin();
	}
	
	protected void pack2(ArrayList<AtlasImage> images)
	{
		for (int i = images.size() - 1; i >= 0; i--)
		{
			boolean added = false;
			for (TextureBin bin : textureBins)
			{
				if (bin.add(images.get(i)))
				{
					added = true;
					images.remove(i);
					break;
				}
			}
			if (!added)
				break;
		}
	}
	
	/**
	 * Attempt to downsize the last atlas.
	 */
	protected void repackLastBin()
	{
		TextureBin bin = getLastTextureBin();
		
		while (!bin.getSize().isMinSize())
		{
			ArrayList<AtlasImage> atlasImages = bin.getImages();
			Collections.sort(atlasImages, new CompareArea());  // need to resort
			
			TextureBin smallerBin = createTextureBin(bin.getSize().dec());
			if (!atlasImages.isEmpty())
				break;
			
			bin = smallerBin;
		}
		
		replaceTextureBin(getLastTextureBin(), bin);
	}
	
	protected void replaceTextureBin(TextureBin oldBin, TextureBin... newBins)
	{
		int index = textureBins.indexOf(oldBin);
		if (index < 0)
			throw new RuntimeException("Old bin does not exist!");
		textureBins.remove(index);
		textureBins.addAll(index, Arrays.asList(newBins));
	}
	
	public void write()
	{
		outputDir.mkdirs();
		
		// remove old atlases first
		Paths oldAtlasRemover = new Paths(outputDir.getAbsolutePath(), "atlas*.png");
		oldAtlasRemover.delete();

		for (int i = 0; i < textureBins.size(); i++)
		{
			textureBins.get(i).write(createAtlasPath(i));
		}
		writeIndex();
	}
	
	public void writeIndex()
	{
		File index = new File(outputDir, "index.json");
		
		BufferedWriter indexWriter = null;
		try
		{
			indexWriter = new BufferedWriter(new FileWriter(index));
			toJSON().write(indexWriter);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error writing index: " + index, e);
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Error serializing index: " + index, e);
		}
		finally
		{
			try
			{
				if (indexWriter != null)
					indexWriter.close();
			}
			catch (IOException e) {}
		}
	}
	
	public JSONArray toJSON() throws JSONException 
	{
		JSONArray texturesJson = new JSONArray();
		for (int i = 0; i < textureBins.size(); i++)
		{
			texturesJson.put(textureBins.get(i).toJSON(createAtlasPath(i)));
		}
		return texturesJson;
	}
	
	private File createAtlasPath(int index)
	{
		return new File(outputDir, "atlas-" + index + ".png");
	}
	
}
