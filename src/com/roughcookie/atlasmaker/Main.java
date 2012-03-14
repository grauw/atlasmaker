package com.roughcookie.atlasmaker;
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
	
	public static void main(String[] args)
	{
		main = new Main();
		main.createAtlases();
	}
	
	public Main()
	{
		manifest = new File("/Users/rcuser/Development/game/client/Content/Art/atlasManifest.json");
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
				String baseDir = manifest.getAbsoluteFile().getParent();
				for (int i = 0; i < atlasFilePatterns.length(); i++)
				{
					paths.glob(baseDir, atlasFilePatterns.getString(i));
				}
				
				if (paths.count() == 0)
				{
					System.out.println("Warning: No files found for atlas: " + atlasName);
					continue;
				}
				
				File outputDir = new File("./output/", atlasName);
				outputDir.mkdirs();
				Atlas atlas = new Atlas(paths.getFiles(), outputDir);
				
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
