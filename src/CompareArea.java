import java.util.Comparator;


public class CompareArea implements Comparator<AtlasImage>
{
	public int compare(AtlasImage ai1, AtlasImage ai2) 
	{
        return ai1.getImage().getWidth() * ai1.getImage().getHeight() - 
        	ai2.getImage().getWidth() * ai2.getImage().getHeight();
    }
}
