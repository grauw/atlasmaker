import java.io.File;
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
	
	public ArrayList<TexturePiece> getTexturePieces()
	{
		ArrayList<TexturePiece> pieces = new ArrayList<TexturePiece>();
		for (BinPacker packer : this)
			pieces.add(packer.getTexturePiece());
		return pieces;
	}
	
	public void write(File outFile)
	{
		int i = 0;
		for (TexturePiece piece : getTexturePieces())
			piece.write(outFile, i++);
	}
	
	public void trim()
	{
		for (TexturePiece piece : getTexturePieces())
			piece.trim();
	}
}
