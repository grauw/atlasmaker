import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class Main
{
	public static void main(String[] args)
	{
		File[] files = getFiles(new File("/Users/rcuser/Development/game/client/Content/Art/GUI2/menu/200"));
		File outFile = new File("/Users/rcuser/Development/AtlasMaker/output/menu200");
		
		AtlasMaker atlasMaker = new AtlasMaker();
		BinPackerList texturePieces = atlasMaker.create(files, outFile);
	}
	
	public static File[] getFiles(File dir)
	{
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".png");
			}
		};
		return dir.listFiles(filter);
	}
}
