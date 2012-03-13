import java.util.ArrayList;


public class TweakDimensions
{
	private int bestSavings;
	private int bestWidth;
	private int bestHeight;
	private int origWidth;
	private int origHeight;
	
	public void run(BinPacker binPacker, ArrayList<AtlasImage> listImage)
	{
		bestSavings = 0;
		binPacker.getTexturePiece().trim();
		origWidth = binPacker.getTexturePiece().getWidth();
		origHeight = binPacker.getTexturePiece().getHeight();
		bestWidth = origWidth;
		bestHeight = origHeight;
		tweakWidthHeight(binPacker, listImage, true);
		binPacker.getTexturePiece().setSize(origWidth, origHeight);
		tweakWidthHeight(binPacker, listImage, false);
		if (bestWidth != binPacker.getTexturePiece().getWidth() ||
			bestHeight != binPacker.getTexturePiece().getHeight())
		{	
			binPacker.resize(bestWidth, bestHeight, listImage); 
			binPacker.pack(listImage);
		}
		assert(listImage.size() == 0);
	}
	
	private void tweakWidthHeight(BinPacker binPacker, ArrayList<AtlasImage> listImage, boolean doWidth)
	{
		int width = origWidth; 
		int height = origHeight;
		int savings, prevSavings;
		savings = 0;
		int prevWidth, prevHeight;
		do
		{
			prevWidth = width;
			prevHeight = height;
			prevSavings = savings;
			// reduce by two pixes to force minimum re-organise
			if (doWidth)
			{
				width = binPacker.getTexturePiece().getWidth()-1;
//				height = origWidth*origHeight/width;
			}
			else
			{
				height = binPacker.getTexturePiece().getHeight()-1;
//				width = origWidth*origHeight/height;
			}
			binPacker.resize(width, height, listImage); 
			binPacker.pack(listImage);
			if (listImage.size() > 0)
			{
				break;
			}
			// check if we gained anything
			binPacker.getTexturePiece().trim();
			savings = origWidth*origHeight-
				binPacker.getTexturePiece().getPixels();
		} while (savings >= prevSavings);
		
		if (bestSavings < prevSavings)
		{
			bestSavings = prevSavings;
			bestWidth = prevWidth;
			bestHeight = prevHeight;
		}
	}
}
