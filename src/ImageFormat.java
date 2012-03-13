
public class ImageFormat
{
    public final static int TARGA_TRUE_COLOR = 0;
    public final static int TARGA16 = 1;
    public final static int PNG = 2;
    public final static int PVR = 3;
    private final static String name[] = new String[]{"TARGA32", "Targa16", "PNG", "PVR"}; 
    private final static String nameLong[] = new String[]{"Targa True Color", "16-bit Targa", "PNG", "PVR"};
    static String getNameLong(int index) { return nameLong[index]; }
    static String getName(int index) { return name[index]; }
    static int getType(String inName)
    {
    	for (int n = 0; n < name.length; n++)
    	{
    		if (name[n].equals(inName)) return n;
    	}
    	return 0;
    }
//	private int type;
}
