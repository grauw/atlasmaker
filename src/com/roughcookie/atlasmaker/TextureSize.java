package com.roughcookie.atlasmaker;
public abstract class TextureSize
{
	protected int width;
	protected int height;
	protected int maxSize;
	protected int minSize;
	
	public TextureSize(int maxSize)
	{
		this.width = 0;
		this.height = 0;
		this.maxSize = maxSize;
		this.minSize = 32;
	}
	
	public void setMaxSize(int maxSize)
	{
		this.maxSize = maxSize;
	}
	
	public int getNumPixels()
	{
		return width*height;
	}
	
	public boolean isMaxSize()
	{
		return width == maxSize && height == maxSize;
	}
	
	public boolean isMinSize()
	{
		return width == minSize && height == minSize;
	}
	
	protected void checkMax()
	{
		if (width > maxSize)
			width = maxSize;
		if (height > maxSize)
			height = maxSize;
	}
	
	protected void checkMin()
	{
		if (width < minSize)
			width = minSize;
		if (height < minSize)
			height = minSize;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public void setWidth(int width)
	{
		this.width = width;
	}
	
	abstract public void inc();
	abstract public void dec();
	abstract public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight);
	abstract public boolean isPowerOfTwo();
}
