import java.util.ArrayList;


public class BinPackerList
{
    ArrayList<BinPacker> listBinPacker = new ArrayList<BinPacker>();

    void add(BinPacker binPacker)
    {
    	listBinPacker.add(binPacker);
    }
    BinPacker get(int index)
    {
    	return listBinPacker.get(index);
    }
    int size() 
    {
    	return listBinPacker.size();
    }
    
	public void pack(ArrayList<AtlasImage> listImage)
	{
		int ixImage, n;
		boolean added;
		for (ixImage = listImage.size()-1; ixImage >= 0; ixImage--)
		{
			added = false;
			for (n = 0; n < listBinPacker.size(); n++)
			{
				
				if (listBinPacker.get(n).add(listImage.get(ixImage))) 
				{
					added = true;
					listImage.remove(ixImage);
					break;
				}
			}
			if (!added) break;
		} 
//		listImage.subList(0, ixImage).clear();
	}	
	
}
