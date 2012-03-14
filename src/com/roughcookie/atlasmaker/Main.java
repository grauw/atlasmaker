package com.roughcookie.atlasmaker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
		manifest = new File("atlasManifest.json");
	}
	
	public JSONObject getManifestJson()
	{
		try
		{
			BufferedReader manifestReader = new BufferedReader(new FileReader(manifest));
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
	}
	
	public List<AtlasMaker> getAtlasMakers()
	{
		JSONObject manifestJson = getManifestJson();
		ArrayList<AtlasMaker> atlases = new ArrayList<AtlasMaker>();
		
		try
		{
			String[] atlasNames = JSONObject.getNames(manifestJson);
			for (String atlasName : atlasNames)
			{
				JSONArray atlasFilePatterns = manifestJson.getJSONArray(atlasName);
				
				Paths paths = new Paths();
				String baseDir = "/"; //manifest.getAbsoluteFile().getParent();
				for (int i = 0; i < atlasFilePatterns.length(); i++)
				{
					paths.glob(baseDir, atlasFilePatterns.getString(i));
				}
				
				if (paths.count() == 0)
				{
					System.out.println("No files found for atlas: " + atlasName);
					continue;
				}
				
				File outputDir = new File("./output/", atlasName);
				outputDir.mkdirs();
				AtlasMaker atlas = new AtlasMaker(paths.getFiles(), outputDir);
				
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
		for (AtlasMaker atlas : getAtlasMakers())
		{
			atlas.create();
		}
	}
}
