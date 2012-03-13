import java.util.ArrayList;
import java.util.Collections;



public class BinPacker
{
	private TreeNode treeNode;
	private TexturePiece texturePiece;
	public TexturePiece getTexturePiece()
	{
		return texturePiece;
	}
	public BinPacker(int width, int height)
	{
		treeNode = new TreeNode(0, 0, width, height, "");
		texturePiece = new TexturePiece(width, height);
	}
	public boolean add(AtlasImage image)
	{
		boolean added = treeNode.add(image);
		if (added)
			texturePiece.add(image);
		return added;
	}
	public void pack(ArrayList<AtlasImage> listImage)
	{
		int ixImage;
		for (ixImage = listImage.size()-1; ixImage >= 0; ixImage--)
		{
			if (!add(listImage.get(ixImage)))
				return;
			listImage.remove(ixImage);			
		} 
	}		
	public void resize(int width, int height, ArrayList<AtlasImage> listImage)
	{
		// puts images removed back on the list
		texturePiece.remove(listImage);
		// need to resort, because they may have been put in wrong order
		Collections.sort(listImage, new CompareArea());
		// resize resets everything
		treeNode.resize(width, height);
		texturePiece.resize(width, height);
	}
}
