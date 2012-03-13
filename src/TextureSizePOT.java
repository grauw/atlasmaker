
public class TextureSizePOT extends TextureSize
{
	protected int ixSize;
	
	public TextureSizePOT(int maxSize)
	{
		super(maxSize);
	}
	
	public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight)
	{
		ixSize = 0;
		width = 32;
		height = 32;
		while ((width*height < pixelsReq ||
				width < maxWidth || height < maxHeight) && !isMaxSize())
		{
			if (height > width)
				width *= 2;
			else
				height *= 2;
			ixSize++;
		}
	}
	
	public void inc()
	{
		// swap height and width if it's not square
		// this is to allow for better fitting of width and tall images
		if (height > width && ixSize % 1 == 0)
		{
			int temp = height;
			height = width;
			width = temp;
		}
		else if (height > width)
			width *= 2;
		else
			height *= 2;
		ixSize++;
		checkMax();
	}
	
	public void dec()
	{
		if (height < width)
			width /= 2;
		else
			height /= 2;
		checkMin();
	}
	
	public boolean isPowerOfTwo() {
		return true;
	}
}
