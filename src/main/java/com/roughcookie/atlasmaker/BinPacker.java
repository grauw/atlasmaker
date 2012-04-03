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

import org.json.JSONArray;
import org.json.JSONException;

import com.esotericsoftware.wildcard.Paths;

public abstract class BinPacker
{
	private File outputDir;
	private ArrayList<TextureBin> textureBins;
	private int border;
	
	public BinPacker(File outputDir, int border) {
		this.outputDir = outputDir;
		this.border = border;
		textureBins = new ArrayList<TextureBin>();
	}
	
	public abstract TextureSize createTextureSize();
	
	public TextureBin[] getTextureBins()
	{
		return textureBins.toArray(new TextureBin[] {});
	}
	
	public TextureBin getLastTextureBin()
	{
		return textureBins.get(textureBins.size() - 1);
	}
	
	public void pack(ArrayList<AtlasImage> atlasImages)
	{
		for (int i = 0; ; i++)
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
			TextureSize textureSize = createTextureSize().getNextSizeFits(pixelsReq, maxWidth, maxHeight);
			
			TextureBin bin = new TextureBin(new File(outputDir, "atlas-" + i + ".png"), textureSize, border);
			textureBins.add(bin);
			this.pack2(atlasImages);
			
			if (atlasImages.isEmpty()) 
			{
				if (textureBins.size() > 1 && !textureSize.isMinSize())
				{
					// there is opportunity for further reduction
					// because some images might fit in gaps of previous atlases
					int prevWidth, prevHeight;
					do
					{
						prevWidth = textureSize.getWidth();
						prevHeight = textureSize.getHeight();
						textureSize = textureSize.dec();
						bin.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
						this.pack2(atlasImages);
						
					} while (atlasImages.isEmpty() && !textureSize.isMinSize());
					// revert back to last size that fit
					bin.resize(prevWidth, prevHeight, atlasImages); 
					this.pack2(atlasImages);
					
					assert(atlasImages.isEmpty());
				}
				return;
			}
			
			while (!textureSize.isMaxSize())
			{
				textureSize = textureSize.inc();
				bin.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
				this.pack2(atlasImages);
				
				if (atlasImages.isEmpty())
					return;
			}
			// recalculate values for remainder images
		}
	}
	
	private void pack2(ArrayList<AtlasImage> images)
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
	
	public void trim()
	{
		for (TextureBin bin : textureBins)
			bin.trim();
	}
	
	public void write()
	{
		outputDir.mkdirs();
		
		// remove old atlases first
		Paths oldAtlasRemover = new Paths(outputDir.getAbsolutePath(), "atlas*.png");
		oldAtlasRemover.delete();
		
		for (TextureBin bin : textureBins)
			bin.write();
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
		for (TextureBin bin : textureBins)
		{
			texturesJson.put(bin.toJSON());
		}
		return texturesJson;
	}
}
