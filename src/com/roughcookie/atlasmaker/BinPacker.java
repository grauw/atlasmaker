package com.roughcookie.atlasmaker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

public class BinPacker
{
	private File outputDir;
	private ArrayList<TextureBin> textureBins;
	
	public BinPacker(File outputDir) {
		this.outputDir = outputDir;
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
	
	public void pack(TextureSize textureSize, ArrayList<AtlasImage> atlasImages)
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
				pixelsReq += atlasImage.getNumPixels();
			}
			textureSize.getNextSizeFits(pixelsReq, maxWidth, maxHeight);
			TextureBin bin = new TextureBin(outputDir, i, textureSize);
			textureBins.add(bin);
			this.pack(atlasImages);
			
			if (atlasImages.size() == 0) 
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
						textureSize.dec();
						bin.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
						this.pack(atlasImages);
						
					} while (atlasImages.size() == 0 && !textureSize.isMinSize());
					// revert back to last size that fit
					bin.resize(prevWidth, prevHeight, atlasImages); 
					this.pack(atlasImages);
					
					assert(atlasImages.size() == 0);
				}
				return;
			}
			
			while (!textureSize.isMaxSize())
			{
				textureSize.inc();
				bin.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
				this.pack(atlasImages);
				
				if (atlasImages.size() == 0)
					return;
			}
			// recalculate values for remainder images
		}
	}
	
	private void pack(ArrayList<AtlasImage> images)
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
