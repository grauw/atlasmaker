import java.util.ArrayList;

public class BinPackerList extends ArrayList<BinPacker>
{
	private static final long serialVersionUID = 1L;
	
	public void pack(ArrayList<AtlasImage> images)
	{
		for (int i = images.size() - 1; i >= 0; i--)
		{
			boolean added = false;
			for (BinPacker binPacker : this)
			{
				if (binPacker.add(images.get(i)))
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
}
