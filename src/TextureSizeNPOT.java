
public class TextureSizeNPOT extends TextureSize
{
	protected int ixSize;
	public static int STEP_SIZE = 64;
	
	public TextureSizeNPOT(int maxSize)
	{
		super(maxSize);
	}
	
	public void inc()
	{
//		if (height > width && ixSize % 1 == 0)
//		{
//			int temp = height;
//			height = width;
//			width = temp;
//		}
		if (height > width)
			width += STEP_SIZE;
		else
			height += STEP_SIZE;
//		ixSize++;
		checkMax();
	}
	
	public void dec()
	{
		if (height < width)
			width -= STEP_SIZE;
		else
			height -= STEP_SIZE;
//		ixSize++;
		checkMin();
	}
	
	public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight)
	{
		// some of the smaller images fit into the other textures
		// so deliberly choose smaller size
		ixSize = 0;
		int size = (int)Math.round(Math.sqrt(pixelsReq));
		if (size < 32) size = 32;
		if (maxWidth > size)
		{
			width = maxWidth;
			height = (int)pixelsReq / width+1;
		}
		else
		{
			width = size;
		}
		if (maxHeight > size)
		{
			height = maxHeight;
			width = (int)pixelsReq / height+1;
		}
		else
		{
			height = size;
		}
		checkMax();
	}
	
	public boolean isNPOT() {
		return true;
	}

}
