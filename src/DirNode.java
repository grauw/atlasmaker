import java.io.File;
import java.io.FilenameFilter;

import javax.swing.tree.DefaultMutableTreeNode;


public class DirNode
{
	private DefaultMutableTreeNode node;
	private String name;
	private String fileSpec;
	private int imageFormat;
	public DefaultMutableTreeNode getNode()
	{
		return node;
	}
	public DirNode(String name, String fileSpec, String strImageFormat)
	{
		super();
		node = new DefaultMutableTreeNode(name);
		this.name = name;
		this.fileSpec = fileSpec;
		imageFormat = ImageFormat.getType(strImageFormat);
	}
	public void setImageFormat(int imageFormat)
	{
		this.imageFormat = imageFormat;
	}
	public int getImageFormat()
	{
		return imageFormat;
	}
	public String getStrImageFormat()
	{
		return ImageFormat.getName(imageFormat);
	}
	public boolean isTarga()
	{
		return imageFormat == ImageFormat.TARGA16 || 
			imageFormat == ImageFormat.TARGA_TRUE_COLOR;
	}
	public File[] getFiles()
	{
	    File dir = new File(fileSpec);
	    FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.indexOf('.') == -1;
	        }
	    };
	    return dir.listFiles(filter);
	}
	public String getName()
	{
		return name;
	}
	public String getFileSpec()
	{
		return fileSpec;
	}
}
