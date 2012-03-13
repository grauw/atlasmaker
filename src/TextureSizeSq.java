
public class TextureSizeSq extends TextureSize
{
	public TextureSizeSq(int maxSize)
	{
		super(maxSize);
	}
	
	public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight)
	{
		width = 32;
		height = 32;
		while ((width*height < pixelsReq ||
				width < maxWidth || height < maxHeight) && !isMaxSize())
		{
			inc();
		}
	}
	
	public void inc()
	{
		height *= 2;
		width *= 2;
	}
	
	public void dec()
	{
		height /= 2;
		width /= 2;
	}
	
	public boolean isPowerOfTwo() {
		return true;
	}
}
