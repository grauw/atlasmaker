package com.roughcookie.atlasmaker;
import java.util.Comparator;

public class CompareName implements Comparator<AtlasImage>
{
	public int compare(AtlasImage ai1, AtlasImage ai2) 
	{
		return ai1.getFile().getPath().compareToIgnoreCase(ai2.getFile().getPath());
	}
}
