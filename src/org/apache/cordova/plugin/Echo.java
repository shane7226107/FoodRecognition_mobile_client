package org.apache.cordova.plugin;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.widget.Toast;


/**
 * This class echoes a string called from JavaScript.
 */
public class Echo extends Plugin {

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        try {
            if (action.equals("echo")) {
            	//get the first input argument
                String echo = args.getString(0); 
                if (echo != null && echo.length() > 0) {
                	//image processing
                	imageProcessing();
                	//return plugin status and first input argument
                    return new PluginResult(PluginResult.Status.OK, echo);
                } else {
                    return new PluginResult(PluginResult.Status.ERROR);
                }
            } else {
                return new PluginResult(PluginResult.Status.INVALID_ACTION);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        }
    }
    
    /*Image Processing*/
    public void imageProcessing(){
    	    	
    	//Load image by BitmapFactory
    	//Bitmap src = BitmapFactory.decodeFile("/sdcard/ntufrs/tmp.jpg");
    	Bitmap src = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ntufrs/tmp.jpg");
    	Bitmap dst;    	
    	
    	//Make a mutable copy
    	//First thing is to made a scale copy, avoiding memory leak
    	if(src.getHeight() > 300 || src.getWidth() > 300){
    		
    		if(src.getWidth() > src.getHeight()){
    			float dstHeight = (float)src.getHeight()*((float)300/(float)src.getWidth());
            	
            	dst = Bitmap.createScaledBitmap(src, 300, (int)dstHeight, false);
            	dst = dst.copy(Bitmap.Config.ARGB_8888, true);
    		}else{
    			
    			float dstWidth = (float)src.getWidth()*((float)300/(float)src.getHeight());
            	
            	dst = Bitmap.createScaledBitmap(src, (int)dstWidth ,300, false);
            	dst = dst.copy(Bitmap.Config.ARGB_8888, true);
    			
    		}
    		
        	
    	}else{
    		
    		dst = src.copy(Bitmap.Config.ARGB_8888, true);
    		
    	}
    	        
 
    	
    	int w=dst.getWidth(),h=dst.getHeight();  
    	String Hist = "";
    	
    	// RGBHistogram 
    	int nBins = 16;
    	int patchNum = 16;
    	float [][] desc = new float [patchNum][nBins*6];
    	int [] rhist = new int [nBins];
    	int [] ghist = new int [nBins];
    	int [] bhist = new int [nBins];
    	int patchWlen = w/4+1;
    	int patchHlen = h/4+1;
    	int pixel;
    	
    	for(int i =0;i<nBins;i++)
    		rhist[i]=ghist[i]=bhist[i] = 0;
    	int r=0,g=0,b = 0;
    	int sizePatchW,sizePatchH,indexPatchW,indexPatchH,patchID; 
    	float [] hsv = new float [3];
    	int []int_hsv = new int [3];
    	for(int i=0;i<h;i++)
    	{
    		for(int j=0;j<w;j++)
    		{
    			pixel = dst.getPixel(j, i);
    			 r = Color.red(pixel);
    			 g = Color.green(pixel);
    			 b = Color.blue(pixel);
    			 Color.RGBToHSV(r, g, b, hsv);
    			 sizePatchH = (indexPatchH= i / patchHlen)==3?h%patchHlen:patchHlen;
    			sizePatchW = (indexPatchW= j / patchWlen)==3?w%patchWlen:patchWlen;
    			patchID = indexPatchH*4+indexPatchW;
    			
    			for(int k = 0;k<3;k++)
    				int_hsv[k] = (int)hsv[k];
    			
    			float tmpPix = 1 / (float)(sizePatchH*sizePatchW);
    			desc[patchID][r/nBins] =desc[patchID][r/nBins]+tmpPix;
    			desc[patchID][16+g/nBins]=desc[patchID][16+g/nBins]+tmpPix;
    			desc[patchID][32+b/nBins]=desc[patchID][32+b/nBins]+tmpPix;
    		    
    			desc[patchID][48+(int)(hsv[0]/22.6)] =desc[patchID][48+(int)(hsv[0]/22.6)]+tmpPix;
    			desc[patchID][64+(int)(hsv[1]*(nBins-1))] =desc[patchID][64+(int)(hsv[1]*(nBins-1))]+tmpPix;
    			desc[patchID][80+(int)(hsv[2]*(nBins-1))] =desc[patchID][80+(int)(hsv[2]*(nBins-1))]+tmpPix;
    			
    		}
    	}
    	
    	Hist = "1 ";
    	for (int i = 0;i<patchNum;i++)
    		for(int j = 0;j<nBins*6;j++)
    			Hist = Hist + (int)(i*nBins*6+j+1) +":" +(float)(Math.round((double)desc[i][j]*1000000)/1000000.00)+" ";
    	Hist = Hist + "\n";
    	
    	
    	//Save Color feature file
    	//Hist = "123";
    	try{
    		byte buf[] = Hist.getBytes(); 
    		OutputStream f0 = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ntufrs/RGB_feature.txt"); 
    		for (int i=0; i < buf.length; i++) { 
    			f0.write(buf[i]); 
    		} 
    		f0.close(); 
    	}catch(Exception e){
    		e.printStackTrace();

    	}
    	
    	// Gray-scale image 
    	w = dst.getWidth();
    	h = dst.getHeight();
    			
    	int[] pix = new int[w * h]; 
    	dst.getPixels(pix, 0, w, 0, 0, w, h); 

    	int alpha=0xFF<<24; 
    	for (int i = 0; i < h; i++) { 
    		for (int j = 0; j < w; j++) {  
    			int color = pix[w * i + j]; 
    			int red = ((color & 0x00FF0000) >> 16); 
    			int green = ((color & 0x0000FF00) >> 8); 
    			int blue = color & 0x000000FF;
    			//Naive weighting
    			//color = (red + green + blue)/3;
    			//Matlab weighting
    			float tmp = (float)red;
    			int new_red = (int)(0.299*tmp);
    			tmp  = (float)green;
    			int new_green = (int)(0.587*tmp);
    			tmp = (float)blue;
    			int new_blue = (int)(0.114*tmp);
    			color = new_red+new_green+new_blue;
    					
    			color = alpha | (color << 16) | (color << 8 )| color; 
    			pix[w * i + j] = color; 
    	} 
    	} 
    	Bitmap grayBitmap=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); 
    	grayBitmap.setPixels(pix, 0, w, 0, 0,w, h);
    	
    	
        //Save image
        try {
        	
            //FileOutputStream out = new FileOutputStream("/sdcard/ntufrs/tmp.jpg");
        	FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ntufrs/tmp.jpg");
            //RGB
            //dst.compress(Bitmap.CompressFormat.JPEG, 100, out);
            //Grayscale
            grayBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
            
        } catch (Exception e) {
        	
            e.printStackTrace();
            
        }
        
    	
    }

    

}
