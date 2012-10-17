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
    	        
        //Manipulate
    	for (int y=0 ; y < dst.getHeight() ; y ++){
    		for(int x = 0 ; x < dst.getWidth() ; x++){
    			//dst.setPixel(x, y, Color.RED);
    		}    		
    	}
    	
    	/*
    	//Save RGB feature file
    	String Hist = "123";
    	try{
    		byte buf[] = Hist.getBytes(); 
    		OutputStream f0 = new FileOutputStream("/sdcard/ntufrs/RGB_feature.txt"); 
    		for (int i=0; i < buf.length; i ++) { 
    			f0.write(buf[i]); 
    		} 
    		f0.close(); 
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	*/
    	
    	// Gray-scale image 
    	int w = dst.getWidth();
    	int h = dst.getHeight();
    			
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
        	//Environment.getExternalStorageDirectory().getPath();
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
