import java.util.Comparator;

public class CompareHeight implements Comparator<AtlasImage>
{
	public int compare(AtlasImage ai1, AtlasImage ai2) 
	{
		return ai2.getImage().getWidth() - ai1.getImage().getWidth();
	}
}
