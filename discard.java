import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

	public void CropTransparent() 
	{
		int srcW = image.getWidth(null);
		int srcH = image.getHeight(null);
		int[] srcPixels = new int[srcW*srcH];
	    image.getRGB(0, 0, srcW, srcH, srcPixels, 0, srcW);
	    
		int srcX, srcY;
		int alpha;
		int pixel;
		int xFirst = srcW+1;
		int xLast = -1;
		int yFirst = srcH+1;
		int yLast = -1;
		int xFirstNoThresh = srcW+1;
		int yFirstNoThresh = srcH+1;
		transparent = false;
		for (srcY = 0; srcY < srcH; ++srcY) 
		{
			for (srcX = 0; srcX < srcW; ++srcX) 
			{
				pixel = srcPixels[srcX + srcY * srcW];
				alpha = (srcPixels[srcX + srcY * srcW] >>24) & 0xff;
				if (alpha > 2)
				{
					if (xLast < srcX) xLast = srcX;  
					if (xFirst > srcX) xFirst = srcX;  
					if (yLast < srcY) yLast = srcY;  
					if (yFirst > srcY) yFirst = srcY;  
				}
				if (alpha > 0)
				{
					if (xFirstNoThresh > srcX) xFirstNoThresh = srcX;  
					if (yFirstNoThresh > srcY) yFirstNoThresh = srcY;  
				}
				if (alpha < 254)
				{
					transparent = true;
				}
			}
		}
		int xSize = xLast-xFirst+1;
		int ySize = yLast-yFirst+1;
		BufferedImage imageCut;
		if (xSize < 1 || ySize < 1)
		{
	        imageCut = new BufferedImage(1, 1, 
	        		BufferedImage.TYPE_INT_ARGB);
	        System.out.println("Warning: image with no opaque pixels!" + fileName);
	        xOffset = 0;
	        yOffset = 0;
		}
		else
		{
	        imageCut = new BufferedImage(xSize, ySize, 
	        		BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g = imageCut.createGraphics();
	        g.drawImage(image, null, -xFirst, -yFirst);		
	        g.dispose();
	        xOffset = xFirstNoThresh;
	        yOffset = yFirstNoThresh;
		}
		if (this.fileName.equals("twoStory"))
		{
	        System.out.println("Warning: image with no opaque pixels!" + fileName);
		}
//        try 
//        {
//            // Save as PNG
//            file = new File(fileName+"x");
//            ImageIO.write(imageCut, "png", file);       
//        } 
//        catch (Exception e)
//        {
//            System.out.println("error: "+e.toString());
//        }
		image = imageCut;
	}

    Collections.sort(listImage, new CompareHeight());
    listTexturePiece = treeBinPack(listImage, listSize[ixSize]);
	if (listTexturePiece != null) break;
    Collections.sort(listImage, new CompareWidth());
    listTexturePiece = treeBinPack(listImage, listSize[ixSize]);
	if (listTexturePiece != null) break;
	
	
    private static void writeRLE(DataOutputStream dataOut, int[] data) throws IOException 
    {
    	int index = 0;
    	// count pixels that are the same
//    	int count = 0;
    	int startPacket;
    	int packetHeader;
    	int n;
    	byte[] dataByte = new byte[data.length*4];
    	do
    	{
    		startPacket = index;
	    	while (index+1 < data.length && data[index] == data[index+1] &&
	    			index - startPacket < 127)
	    	{
	    		index++;
	    	}
	    	if (index - startPacket < 2)
	    	{
	    		// the packet is raw, now we have to count how many more bytes until we reach
	    		// a sequence of 3
		    	while (index+2 < data.length && (data[index] != data[index+1] || 
		    			data[index] != data[index+2]) &&
		    			index - startPacket < 127)
		    	{
		    		index++;
		    	}
	    		if (index+2 >= data.length) 
	    		{
	    			index = data.length;
	    		}
	    		packetHeader = (index - startPacket-1);
//		        System.out.println("header: "+packetHeader + " index:"+index);
	    		dataOut.writeByte(packetHeader);
	    		for (n = startPacket; n < index; n++)
	    		{
	    			writeBGRA(dataOut, data[n]);
	    		}
	    	}
	    	else
	    	{
	    		if (index+1 >= data.length) 
	    		{
	    			index = data.length-1;
	    		}
	    		// output rle packet
	    		packetHeader = 0x80 | (index - startPacket);
//		        System.out.println("header: "+packetHeader + " index:"+index);
	    		dataOut.writeByte(packetHeader);
	    		writeBGRA(dataOut, data[index]);
	    		index++;
	    	}
    	} while (index < data.length);
    }
	// write everything backwards
    private static void writeShort(DataOutputStream dataOut, int v) throws IOException 
    {
    	dataOut.writeByte(v & 0xFF);
    	dataOut.writeByte((v >> 8) & 0xFF);
    }
    private static void writeBGRA(DataOutputStream dataOut, int v) throws IOException 
    {
    	dataOut.writeByte((v >> 16) & 0xFF);
    	dataOut.writeByte((v >> 8) & 0xFF);
    	dataOut.writeByte(v & 0xFF);
    	dataOut.writeByte((v >> 24) & 0xFF);	// ALPHA
    }
	
	public void drawAlphaRect(Graphics2D g)
	{
        g.drawImage(image, null, x, y);		
        g.setColor(Color.black);
        g.drawRect(x, y, getWidth()-1, getHeight()-1);
        g.setColor(Color.red);
        g.drawRect(x+xOffset, y+yOffset, opaqueWidth-1, opaqueHeight-1);
	}

    private boolean checkUpToDate(File[] files, String strOutFile)
    {
    	Long timeStamp;
    	Long max = new Long(0);
    	for (int n = 0; n < files.length; n++) 
    	{
    		timeStamp = files[n].lastModified();
    		if (timeStamp > max) max = timeStamp;
    	}
    	File outFile = new File(strOutFile);
    	if (max > outFile.lastModified()) return false;
    	strErr = "Atlas already created";
    	return true;
    }
	

	GLTexturePuzzle texturePuzzle;
	listSizePVR = new ArrayList<GLTexturePuzzle>();
	listSizePVR.add(new GLTexturePuzzle(32, 32, 1));
	listSizePVR.add(new GLTexturePuzzle(64, 64, 1));
	listSizePVR.add(new GLTexturePuzzle(128, 128, 1));
	listSizePVR.add(new GLTexturePuzzle(128, 128, 2));
	listSizePVR.add(new GLTexturePuzzle(128, 128, 3));
	listSizePVR.add(new GLTexturePuzzle(256, 256, 1));
	listSizePVR.add(new GLTexturePuzzle(256, 256, 2));
	listSizePVR.add(new GLTexturePuzzle(256, 256, 3));
	listSizePVR.add(new GLTexturePuzzle(512, 512, 1));
	listSizePVR.add(new GLTexturePuzzle(512, 512, 2));

	texturePuzzle = new GLTexturePuzzle(512, 512, 3);
	texturePuzzle.set(2, 256, 256);
	listSizePVR.add(texturePuzzle);

	listSizePVR.add(new GLTexturePuzzle(512, 512, 3));
	listSizePVR.add(new GLTexturePuzzle(1024, 1024, 1));

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	texturePuzzle.set(1, 256, 256);
	listSizePVR.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	texturePuzzle.set(1, 512, 512);
	listSizePVR.add(texturePuzzle);
	
	listSizeNormal = new ArrayList<GLTexturePuzzle>();
	listSizeNormal.add(new GLTexturePuzzle(32, 32, 1));
	listSizeNormal.add(new GLTexturePuzzle(64, 64, 1));
	listSizeNormal.add(new GLTexturePuzzle(128, 128, 1));
	listSizeNormal.add(new GLTexturePuzzle(256, 128, 1));
	listSizeNormal.add(new GLTexturePuzzle(128, 256, 1));
//	listSizeNormal.add(new GLTexturePuzzle(128, 128, 3));
	listSizeNormal.add(new GLTexturePuzzle(256, 256, 1));
	listSizeNormal.add(new GLTexturePuzzle(512, 256, 1));
	listSizeNormal.add(new GLTexturePuzzle(256, 512, 1));
//	listSizeNormal.add(new GLTexturePuzzle(256, 256, 3));
	listSizeNormal.add(new GLTexturePuzzle(512, 512, 1));
	listSizeNormal.add(new GLTexturePuzzle(1024, 512, 1));
	listSizeNormal.add(new GLTexturePuzzle(512, 1024, 1));

	texturePuzzle = new GLTexturePuzzle(1024, 512, 2);
	texturePuzzle.set(1, 256, 256);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 512, 2);
	texturePuzzle.set(1, 512, 256);
	listSizeNormal.add(texturePuzzle);
	
	texturePuzzle = new GLTexturePuzzle(1024, 512, 2);
	texturePuzzle.set(1, 512, 512);
	listSizeNormal.add(texturePuzzle);
	
	listSizeNormal.add(new GLTexturePuzzle(1024, 1024, 1));
	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	texturePuzzle.set(1, 256, 256);
	listSizeNormal.add(texturePuzzle);
	
	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	texturePuzzle.set(1, 512, 256);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	texturePuzzle.set(1, 512, 512);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	texturePuzzle.set(1, 1024, 512);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 3);
	texturePuzzle.set(1, 1024, 512);
	texturePuzzle.set(2, 256, 256);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 3);
	texturePuzzle.set(1, 1024, 512);
	texturePuzzle.set(2, 512, 256);
	listSizeNormal.add(texturePuzzle);
	
	texturePuzzle = new GLTexturePuzzle(1024, 1024, 3);
	texturePuzzle.set(1, 1024, 512);
	texturePuzzle.set(2, 512, 512);
	listSizeNormal.add(texturePuzzle);
	
	texturePuzzle = new GLTexturePuzzle(1024, 1024, 2);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 3);
	texturePuzzle.set(2, 256, 256);
	listSizeNormal.add(texturePuzzle);

	texturePuzzle = new GLTexturePuzzle(1024, 1024, 3);
	texturePuzzle.set(2, 512, 256);
	listSizeNormal.add(texturePuzzle);
	
	texturePuzzle = new GLTexturePuzzle(1024, 1024, 3);
	texturePuzzle.set(2, 512, 512);
	listSizeNormal.add(texturePuzzle);
	
	
	public boolean SplitImages(String strInDir)
	{
		strErr = "";
	    File dir = new File(strInDir);
	    File[] files = null;
	    FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.endsWith("_split.png");
	        }
	    };
	    files = dir.listFiles(filter);
	    
	    int n, x, y, xCount, yCount;
	    int size = 256;
	    int width, height;
	    if (files == null || files.length == 0) return true; 
		BufferedImage imageIn;
	    String name ="";
	    try 
	    {
	        // Read from a file
	        for (n = 0; n < files.length; n++) 
	        {
	        	name = files[n].getName();
	    	    File file = new File(strInDir + "\\" + name);
	    	    imageIn = ImageIO.read(file);
	    	    y = 0;
	    	    yCount = 0;
	    	    do
	    	    {
	        	    if (y + size > imageIn.getHeight())
	        	    {
	        	    	height = imageIn.getHeight() - y;
	        	    }
	        	    else
	        	    {
	        	    	height = size;
	        	    }
	        	    x = 0;
	        	    xCount = 0;
		    	    do
		    	    {
		        	    if (x + size > imageIn.getWidth())
		        	    {
		        	    	width = imageIn.getWidth() - x;
		        	    }
		        	    else
		        	    {
		        	    	width = size;
		        	    }
		    	        BufferedImage imageOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		    	        Graphics2D g = imageOut.createGraphics();
		    	    	g.drawImage(imageIn, 0, 0, width, height, x, y, x+width, y+height, null);
		    	    	x += width;
		    	    	xCount++;
			            String newName = strInDir + "\\" + name.substring(0, name.length()-10) + 
			            	xCount +"_" + yCount + ".png";
			    	    File fileOut;
			       		fileOut = new File(newName);
			       		ImageIO.write(imageOut, "png", fileOut);
			    	    g.dispose();
		    	    } while (x < imageIn.getWidth());
	    	    	y += height;
	        	    yCount++;
	    	    } while (y < imageIn.getHeight());
	    	    file.delete();
	        }
	    } catch (Exception e) 
	    {
	    	strErr = "error: "+name+": " +e.toString();
	        return false;
	    }
	    return true;
	}
	
    JLabel labelImageSize;
	labelImageSize = new JLabel();
	panelDisplay.add(labelImageSize);

	
	  //      long start = System.currentTimeMillis();
    
//  if (checkConvPVR.isSelected()) xx
//  int format = optionsDialog.getImageFormat();
//  int alphaThresh = optionsDialog.getAlphaThresh();

	public int getImageFormat()
	{
		int format;
	    if (btnPNG.isSelected())
	    	format = TexturePiece.PNG;
	    else if (btnTARGA_TRUE_COLOR.isSelected())
	    	format = TexturePiece.TARGA_TRUE_COLOR;
	    else 
			format = TexturePiece.TARGA16;
	    return format;
	}

	public enum ImageFormat
	{
		PNG (0, "PNG", "PNG"),
		TARGA_TRUE_COLOR(1, "TARGA_TRUE_COLOR", "32-bit Targa");
		TARGA16(2, "Targa16", "16-bit Targa");
		PVR(3, "PVR", "PVR");
		private final int index;
		private final String name;
		private final String nameLong;
	}
	for (Planet p : Planet.values())
	{

		public void loadSizes(String fileName)
		{
			try 
			{
				File fXmlFile = new File(fileName);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
	 			NodeList nodeList = doc.getElementsByTagName("textureRange");
				
				for (int n = 0; n < nodeList.getLength(); n++) 
				{
					Node node = nodeList.item(n);
					if (node.getNodeType() == Node.ELEMENT_NODE) 
					{	 
						Element element = (Element)node;
						if (element.getAttribute("type").equals("normal"))
						{
							rangeNormal.load(element);
						}
						else
						{
							rangeSquare.load(element);
						}
					}
				}	
			} catch (Exception e) 
			{
				e.printStackTrace();
			}		
		}
		atlasMaker.loadSizes("textureSizes.xml");
		private TextureRange rangeNormal, rangeSquare; 
		rangeNormal = new TextureRange(); 
		rangeSquare = new TextureRange();

	    private void Resize()
	    {
	    	// we already have created the textures
	    	labelError.setText("");
//	        int index = selectDisplay.getSelectedIndex();
//	        if (index == -1) return;
//	        int height = Integer.parseInt(textHeight.getText());
//	        int width = Integer.parseInt(textWidth.getText());
//	    	if (!listTexture.get(index).resize(width, height))
//	    	{
//	    		labelError.setText("images did not fit in "+width+"x"+height);
//	    		return;
//	    	}    		
//	    	
//	    	String strOutFile = textOutFile.getText();
//	        int pos = strOutFile.indexOf('.');
//	        if (strOutFile.length() < 8 || pos == -1)
//	        {
//	    		labelError.setText("Invalid Output file name");
//	        	return;
//	        }
//	    	String strOutDir = "";
//	    	String atlasName = "main";
//	        pos = strOutFile.lastIndexOf('\\');
//	        if (pos != -1)
//	        {
//	        	strOutDir = strOutFile.substring(0, pos+1);
//	        }
//	        String inDir = textInputDir.getText();
//	        pos = inDir.lastIndexOf('\\');
//	        if (pos != -1)
//	        {
//	        	atlasName = inDir.substring(pos+1);
//	        }
//	    	
//	    	listTexture.get(index).createStuff(strOutDir+atlasName, 
//	    			index, optionsDialog);
//	        changeImage(listTexture.get(index).getImageDisplay());
//	        WriteTXT();
	    }
	    
        else 
        {
        	if (event.getSource() instanceof JButton)
        	{
	        	JButton button = (JButton)event.getSource();
	        	if (button.getText() == "Resize")
	        		Resize();
        	}
        }

        JPanel panelResize = new JPanel();
        JButton btnResize = new JButton("Resize");
    	btnResize.addActionListener(this);
    	JTextField textHeight = new JTextField(3);
    	JTextField textWidth = new JTextField(3);
        panelResize.add(btnResize);
        panelResize.add(textWidth);
        panelResize.add(textHeight);		
    	textHeight.setText(""+imageIcon.getIconHeight());
    	textWidth.setText(""+imageIcon.getIconWidth());
        panelDisplay.add(panelResize);
        
    	public void getNextSizeFits(int pixelsReq, int maxWidth, int maxHeight)
    	{
            do
            {
        		int numPixels = getNumPixels();
            	if (pixelsReq <= numPixels && 
            			maxWidth <= getWidth(0) &&
            			maxHeight <= getHeight(0))
            	{
            		break;
            	}
            	int nextStep = pixelsReq;
                for (int n = 0; n < listSize.size()-1; n++) 
                {
                	numPixels -= listSize.get(n).getNumPixels();
                	nextStep -= listSize.get(n).getNumPixels();
                }
        		TextureSize lastTextureSize = listSize.get(listSize.size()-1);
            	if (nextStep > lastTextureSize.getMaxPixels()
            			&& !lastTextureSize.isMaxSize())
            		lastTextureSize.maximize();
            	else
            		incTextureSize();
            } while(true);
    	}
    	public void remove(ArrayList<AtlasImage> listImage, int ixStart, int ixEnd)
    	{
    		int ixImage, n;
    		boolean added;
    		for (ixImage = ixStart; ixImage < ixEnd; ixImage++)
    		{
    			for (n = 0; n < listBinPacker.size(); n++)
    			{
    				
    				if (listBinPacker.get(n).remove(listImage.get(ixImage))) 
    				{
    					break;
    				}
    			}
    		} 
    	}	
      
    	
    	public boolean forceResize(int width, int height)
    	{
            // sort by biggest image first
            Collections.sort(listImage, new CompareArea());
    		this.width = width;
    		this.height = height;
    		TreeNode treeNode = new TreeNode(0, 0, 
        			width, height, "");
    		
    		int ixImage;
    		for (ixImage = 0; ixImage < listImage.size(); ixImage++)
    		{
    			if (!treeNode.add(listImage.get(ixImage)))
    				return false;
    		} 
    		return true;
    	}	
    	double reduction = 1.0 - binPackerList.size()*0.25;
    	if (reduction < 0.2) reduction = 0.2;
    	pixelsReq = (int)(pixelsReq*reduction);

		// GL_UNSIGNED_SHORT_5_6_5 
		// blue is five bits
    	int blue = (v & 0xFF);
    	int blueNibble = blue >> 3;

    	int red = ((v >> 16) & 0xFF);
    	int redNibble = red >> 3;

    	// green is six bits
    	int green = ((v >> 8) & 0xFF);
    	int greenNibble = green >> 2;
    	int upperGreen = greenNibble >> 3;
    	int lowerGreen = greenNibble & 0x7;

    	dest[indexOut] = (byte)((redNibble << 4) | upperGreen); 
    	indexOut++;
    	dest[indexOut] = (byte)((blueNibble << 4) | lowerGreen); 
    	indexOut++;

    	{
    		// GL_UNSIGNED_SHORT_5_6_5 
    		// blue is five bits
        	int blue = (v & 0xFF);
        	int blueNibble = blue >> 3;
	
	    	int red = ((v >> 16) & 0xFF);
        	int redNibble = red >> 3;

        	// green is six bits
        	int green = ((v >> 8) & 0xFF);
        	int greenNibble = green >> 2;
        	int upperGreen = greenNibble >> 3;
        	int lowerGreen = greenNibble & 0x7;

        	dest[indexOut] = (byte)((blueNibble << 3) | lowerGreen); 
	    	indexOut++;
        	dest[indexOut] = (byte)((redNibble << 3) | upperGreen); 
	    	indexOut++;
    	}
    	
        