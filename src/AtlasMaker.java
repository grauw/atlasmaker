/*
Copyright (c) 2012, Ed Welch
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

*    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*    Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
     and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.
*/
import java.io.*;
import java.util.*;

public class AtlasMaker
{
	private String strErr;
	
	public AtlasMaker()
	{
	}

	
	public boolean create(DirNode dirNode, 
			String strOutFile, ArrayList<TexturePiece> listReturn, OptionsDialog optionsDialog)
	{
		strErr = "";
        File dir = new File(dirNode.getFileSpec());
        File[] files = null;
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        };
        files = dir.listFiles(filter);
		int maxSize = optionsDialog.getMaxSize();
        
        int n;
        if (files == null || files.length == 0) return false;
        TextureSize textureSize;
        if (dirNode.getImageFormat() == ImageFormat.PVR)
        {
        	textureSize = new TextureSizeSq(maxSize);
        }
        else
        {
        	if (optionsDialog.isPOT())
        		textureSize = new TextureSizePOT(maxSize);
        	else
        		textureSize = new TextureSizeNPOT(maxSize);
        }
    	AtlasImage atlasImage = null;
    	ArrayList<AtlasImage> listImage;
    	listImage = new ArrayList<AtlasImage>();
        try 
        {
            for (n = 0; n < files.length; n++) 
            {
                atlasImage = new AtlasImage();
                if (dirNode.getImageFormat() == ImageFormat.PVR)
                	atlasImage.setBorder(2);
                atlasImage.Load(dirNode.getFileSpec(), files[n].getName());
                atlasImage.CropTransparent(optionsDialog.getAlphaThresh());

    			// don't add images that don't fit
        		if (atlasImage.getWidth() > maxSize 
        			|| atlasImage.getHeight() > maxSize)
                {
                	strErr = "image too big to fit in texture: "+atlasImage.getFileName();
                }
        		else
        		{
	                listImage.add(atlasImage);
        		}
            }
        } catch (Exception e) 
        {
        	strErr = "error: "+atlasImage.getFileName()+": " +e.toString();
            return false;
        }
		if (listImage.size() == 0) return false;
        // sorts in reverse order
        // this is so we can remove from end of list as we add to atlas
        Collections.sort(listImage, new CompareArea());
        BinPackerList binPackerList = new BinPackerList();
        binPack(binPackerList, textureSize, listImage);
        
        // try making texture size smaller by making it more rectangular
    	if (textureSize.isNPOT())
    	{
    		TweekDimensions tweekDimensions = new TweekDimensions();
    		tweekDimensions.run(
    			binPackerList.get(binPackerList.size()-1), listImage);
    	}
               
        for (n = 0; n < binPackerList.size(); n++) 
        {
        	if (textureSize.isNPOT())
        	{
            	binPackerList.get(n).getTexturePiece().trim();
        	}
        	binPackerList.get(n).getTexturePiece().createStuff(dirNode, strOutFile, n, optionsDialog);
        	listReturn.add(binPackerList.get(n).getTexturePiece());
        }

        return true;
	}


	private void binPack(BinPackerList binPackerList, TextureSize textureSize, 
			ArrayList<AtlasImage> listImage)
	{
		BinPacker binPacker;
        int maxWidth;
        int maxHeight;
        int pixelsReq;
    	AtlasImage atlasImage;
    	int n;
	    do
	    {
	        maxWidth = 0;
	        maxHeight = 0;
	        pixelsReq = 0;
			for (n = 0; n < listImage.size(); n++)
			{
				atlasImage = listImage.get(n);
	            if (maxWidth < atlasImage.getWidth()) maxWidth = atlasImage.getWidth();
	            if (maxHeight < atlasImage.getHeight()) maxHeight = atlasImage.getHeight();
	            pixelsReq += atlasImage.getNumPixels();
			}
	    	textureSize.getNextSizeFits(pixelsReq, maxWidth, maxHeight);
	    	binPacker = new BinPacker(textureSize.getWidth(), textureSize.getHeight());
	    	binPackerList.add(binPacker);
	        binPackerList.pack(listImage);
			if (listImage.size() == 0) 
			{
				if (binPackerList.size() > 1 && !textureSize.isMinSize())
				{
					// there is opertunity for further reduction
					// because some images might fit in gaps of previous atlases
					int prevWidth, prevHeight;
					do
					{
						prevWidth = textureSize.getWidth();
						prevHeight = textureSize.getHeight();
			    		textureSize.dec();
			    		binPacker.resize(textureSize.getWidth(), textureSize.getHeight(),
			    				listImage); 
			    		binPackerList.pack(listImage);
						
					} while (listImage.size() == 0 && !textureSize.isMinSize());
					// revert back to last size that fit
		    		binPacker.resize(prevWidth, prevHeight, listImage); 
		    		binPackerList.pack(listImage);
		    		assert(listImage.size() == 0);
				}
				return;
			}
	        while (!textureSize.isMaxSize())
	        {
	    		textureSize.inc();
	    		binPacker.resize(textureSize.getWidth(), 
	    				textureSize.getHeight(), listImage); 
	    		binPackerList.pack(listImage);
				if (listImage.size() == 0) return;
	        }
	    	// recalculate values for remainder images
	    } while(true);
	}    

	
	public String getStrErr()
	{
		return strErr;
	}



}
