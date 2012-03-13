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
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class TargaWriter
{
    private final static int COLOR_MAP_ABSENT = 0;
    private final static int IMAGE_TYPE_BGR_RLE = 10;
    private final static int T_TO_B_BIT = 32;
    public static void write(BufferedImage im, File output, int format,
    		boolean transparent) throws IOException 
    {
        FileOutputStream fileOutput;
        DataOutputStream dataOut;
        fileOutput = new FileOutputStream(output);
        dataOut = new DataOutputStream (fileOutput);
        dataOut.writeByte(0);	// image_id_length
        dataOut.writeByte(COLOR_MAP_ABSENT);	// color_map_type
        dataOut.writeByte(IMAGE_TYPE_BGR_RLE);	// image_type
        writeShort(dataOut, 0);	// color_map_origin
        writeShort(dataOut, 0);	// color_map_length
        dataOut.writeByte(0);	// color_map_depth
        writeShort(dataOut, 0);	// origin_x
        writeShort(dataOut, 0);	// origin_y
        writeShort(dataOut, im.getWidth(null));	
        writeShort(dataOut, im.getHeight(null));
     // pixel_depth
        if (format == ImageFormat.TARGA_TRUE_COLOR)
        	if (transparent)
        		dataOut.writeByte(32);
        	else
        		dataOut.writeByte(24);
        else
        	dataOut.writeByte(16);	
        dataOut.writeByte(T_TO_B_BIT);	// image_descriptor
        int[] data = new int[im.getWidth(null)*im.getHeight(null)];
        im.getRGB(0, 0, im.getWidth(null), im.getHeight(null), data, 0, im.getWidth(null));
        writeRLE(dataOut, data, format, transparent);
        dataOut.writeBytes("\0\0\0\0\0\0\0\0TRUEVISION-XFILE.");
    }
        
    private static void writeRLE(DataOutputStream dataOut, int[] data, 
    		int format, boolean transparent) throws IOException 
    {
    	int indexIn = 0;
    	int startPacket;
    	int packetHeader;
    	int n;
    	// it's much faster to operate on a buffer rather than directly write to DataOutputStream
    	int safeSize = data.length*4 + data.length*4/100; 
    	byte[] dest = new byte[safeSize];
    	int indexOut = 0;
    	do
    	{
    		startPacket = indexIn;
	    	while (indexIn+1 < data.length && data[indexIn] == data[indexIn+1] &&
	    			indexIn - startPacket < 127)
	    	{
	    		indexIn++;
	    	}
	    	if (indexIn - startPacket < 2)
	    	{
	    		// the packet is raw, now we have to count how many more bytes until we reach
	    		// a sequence of 3
		    	while (indexIn+2 < data.length && (data[indexIn] != data[indexIn+1] || 
		    			data[indexIn] != data[indexIn+2]) &&
		    			indexIn - startPacket < 127)
		    	{
		    		indexIn++;
		    	}
	    		if (indexIn+2 >= data.length) 
	    		{
	    			indexIn = data.length;
	    		}
	    		packetHeader = (indexIn - startPacket-1);
	    		dest[indexOut] = (byte)(packetHeader & 0xFF);
	    		indexOut++;
	    		for (n = startPacket; n < indexIn; n++)
	    		{
		    		indexOut = outData(dest, indexOut, data[n], format, transparent); 
	    		}
	    	}
	    	else
	    	{
	    		if (indexIn+1 >= data.length) 
	    		{
	    			indexIn = data.length-1;
	    		}
	    		// output rle packet
	    		packetHeader = 0x80 | (indexIn - startPacket);
	    		dest[indexOut] = (byte)(packetHeader & 0xFF);
	    		indexOut++;
	    		indexOut = outData(dest, indexOut, data[indexIn], format, transparent); 
	    		indexIn++;
	    	}
    	} while (indexIn < data.length);
    	dataOut.write(dest, 0, indexOut);
    }
	// write everything backwards
    private static void writeShort(DataOutputStream dataOut, int v) throws IOException 
    {
    	dataOut.writeByte(v & 0xFF);
    	dataOut.writeByte((v >> 8) & 0xFF);
    }
    private static int outData(byte[] dest, int indexOut, int v, 
    		int format, boolean transparent)  
    {
        if (format == ImageFormat.TARGA_TRUE_COLOR)
        {
	    	dest[indexOut] = (byte)((v >> 16) & 0xFF);
	    	indexOut++;
	    	dest[indexOut] = (byte)((v >> 8) & 0xFF);
	    	indexOut++;
	    	dest[indexOut] = (byte)(v & 0xFF);
	    	indexOut++;
        	if (transparent)
        	{
		    	// alpha channel
		    	dest[indexOut] = (byte)((v >> 24) & 0xFF);
		    	indexOut++;
        	}
        }
        else
        {
        	if (transparent)
        	{
	        	int blue = (v & 0xFF);
	        	int blueNibble = blue >> 4;
	        	if ((blue & 0xf) > 8 && blueNibble < 0xf) blueNibble++;
	
	        	int alpha = ((v >> 24) & 0xFF);
	        	int alphaNibble = alpha >> 4;
	        	if ((alpha & 0xf) > 8 && alphaNibble < 0xf) alphaNibble++;
	
		    	int red = ((v >> 16) & 0xFF);
	        	int redNibble = red >> 4;
	        	if ((red & 0xf) > 8 && redNibble < 0xf) redNibble++;
	
	        	int green = ((v >> 8) & 0xFF);
	        	int greenNibble = green >> 4;
	        	if ((green & 0xf) > 8 && greenNibble < 0xf) greenNibble++;      	
	
	        	dest[indexOut] = (byte)((blueNibble << 4) | alphaNibble); 
		    	indexOut++;
	        	dest[indexOut] = (byte)((redNibble << 4) | greenNibble); 
		    	indexOut++;
        	}
        	else
        	{
        		// GL_UNSIGNED_SHORT_5_6_5 
        		// blue is five bits
	        	int blue = (v & 0xFF);
	        	int blueNibble = blue >> 3;
	        	if ((blue & 0x7) > 3 && blueNibble < 0x1f) blueNibble++;

		    	int red = ((v >> 16) & 0xFF);
	        	int redNibble = red >> 3;
	        	if ((red & 0x7) > 3 && redNibble < 0x1f) redNibble++;
	
	        	// green is six bits
	        	int green = ((v >> 8) & 0xFF);
	        	int greenNibble = green >> 2;
	        	if ((green & 0x3) > 1 && greenNibble < 0x3f) greenNibble++;
	        	int upperGreen = greenNibble >> 3;
	        	int lowerGreen = greenNibble & 0x7;
	
	        	dest[indexOut] = (byte)((lowerGreen << 5)| blueNibble); 
		    	indexOut++;
	        	dest[indexOut] = (byte)((redNibble << 3) | upperGreen); 
		    	indexOut++;
        	}
        }
    	return indexOut; 
    }
}

