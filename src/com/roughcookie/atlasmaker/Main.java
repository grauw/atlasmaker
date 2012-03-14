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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.esotericsoftware.wildcard.Paths;

public class Main
{
	public static Main main;
	private File manifest;
	private File outputDir;
	
	public static void main(String[] args)
	{
		if (args.length == 2)
		{
			main = new Main(new File(args[0]), new File(args[1]));
			main.createAtlases();
		}
		else
		{
			System.out.println("usage: java -jar AtlasMaker.jar MANIFEST_FILE OUTPUT_DIR");
		}
	}
	
	public Main(File manifest, File outputDir)
	{
		this.manifest = manifest;
		this.outputDir = outputDir;
	}
	
	public JSONObject getManifestJson()
	{
		BufferedReader manifestReader = null;
		try
		{
			manifestReader = new BufferedReader(new FileReader(manifest));
			return new JSONObject(new JSONTokener(manifestReader));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("File not found: " + manifest, e);
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Error parsing JSON in file " + manifest, e);
		}
		finally
		{
			try
			{
				if (manifestReader != null)
					manifestReader.close();
			}
			catch (IOException e) {}
		}
	}
	
	public List<Atlas> getAtlasMakers()
	{
		JSONObject manifestJson = getManifestJson();
		ArrayList<Atlas> atlases = new ArrayList<Atlas>();
		
		try
		{
			String[] atlasNames = JSONObject.getNames(manifestJson);
			for (String atlasName : atlasNames)
			{
				JSONArray atlasFilePatterns = manifestJson.getJSONArray(atlasName);
				
				Paths paths = new Paths();
				String baseDir = manifest.getParent();
				for (int i = 0; i < atlasFilePatterns.length(); i++)
				{
					paths.glob(baseDir, atlasFilePatterns.getString(i));
				}
				
				if (paths.count() == 0)
				{
					System.out.println("Warning: No files found for atlas: " + atlasName);
					continue;
				}
				
				File atlasOutputDir = new File(outputDir, atlasName);
				atlasOutputDir.mkdirs();
				Atlas atlas = new Atlas(paths.getFiles(), manifest.getParentFile(), atlasOutputDir);
				
				atlases.add(atlas);
			}
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Error processing JSON in file " + manifest, e);
		}
		
		return atlases;
	}
	
	public void createAtlases()
	{
		for (Atlas atlas : getAtlasMakers())
		{
			atlas.create();
		}
	}
}
