import java.io.File;
import java.util.ArrayList;

public class BinPacker
{
	private ArrayList<TextureBin> textureBins;
	
	public BinPacker() {
		textureBins = new ArrayList<TextureBin>();
	}
	
	public TextureBin[] getTextureBins()
	{
		return textureBins.toArray(new TextureBin[] {});
	}
	
	public TextureBin getLastTextureBin()
	{
		return textureBins.get(textureBins.size() - 1);
	}
	
	public void binPack(TextureSize textureSize, ArrayList<AtlasImage> atlasImages)
	{
		while(true)
		{
			int maxWidth = 0;
			int maxHeight = 0;
			int pixelsReq = 0;
			for (AtlasImage atlasImage : atlasImages)
			{
				if (maxWidth < atlasImage.getWidth())
					maxWidth = atlasImage.getWidth();
				if (maxHeight < atlasImage.getHeight())
					maxHeight = atlasImage.getHeight();
				pixelsReq += atlasImage.getNumPixels();
			}
			textureSize.getNextSizeFits(pixelsReq, maxWidth, maxHeight);
			TextureBin bin = new TextureBin(textureSize.getWidth(), textureSize.getHeight());
			textureBins.add(bin);
			this.pack(atlasImages);
			
			if (atlasImages.size() == 0) 
			{
				if (textureBins.size() > 1 && !textureSize.isMinSize())
				{
					// there is opportunity for further reduction
					// because some images might fit in gaps of previous atlases
					int prevWidth, prevHeight;
					do
					{
						prevWidth = textureSize.getWidth();
						prevHeight = textureSize.getHeight();
						textureSize.dec();
						bin.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
						this.pack(atlasImages);
						
					} while (atlasImages.size() == 0 && !textureSize.isMinSize());
					// revert back to last size that fit
					bin.resize(prevWidth, prevHeight, atlasImages); 
					this.pack(atlasImages);
					
					assert(atlasImages.size() == 0);
				}
				return;
			}
			
			while (!textureSize.isMaxSize())
			{
				textureSize.inc();
				bin.resize(textureSize.getWidth(), textureSize.getHeight(), atlasImages); 
				this.pack(atlasImages);
				
				if (atlasImages.size() == 0)
					return;
			}
			// recalculate values for remainder images
		}
	}
	
	public void pack(ArrayList<AtlasImage> images)
	{
		for (int i = images.size() - 1; i >= 0; i--)
		{
			boolean added = false;
			for (TextureBin bin : textureBins)
			{
				if (bin.add(images.get(i)))
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
	
	public void write(File outFile)
	{
		int i = 0;
		for (TextureBin bin : textureBins)
			bin.write(outFile, i++);
	}
	
	public void trim()
	{
		for (TextureBin bin : textureBins)
			bin.trim();
	}
}
