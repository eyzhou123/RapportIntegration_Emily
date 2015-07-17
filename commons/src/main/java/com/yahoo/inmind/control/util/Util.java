package com.yahoo.inmind.control.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rits.cloning.Cloner;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by oscarr on 12/8/14.
 */
public class Util {

    private static Gson gson = new GsonBuilder().create();
    private static Cloner cloner = new Cloner();

    public static Properties loadConfigAssets( Context app, String propName ) {
        Properties properties = new Properties();
        AssetManager am = app.getAssets();

        InputStream inputStream;
        try {
            inputStream = am.open( propName );
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propName + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static <T> List<T> fromJsonList( String jsonList, Class<T> element ){
        Type type = new TypeToken<List<T>>() {}.getType();
        return gson.fromJson(jsonList, type);
    }

//
//    public static <T> String toJsonList( List<T> list ){
//        Type listType = new TypeToken<List<T>>(){}.getType();
//        return gson.toJson( list, listType );
//    }

    //    public static JSONArray fromJsonList(String json){
//        JSONArray obj = null;
//        try {
//            obj = (JSONArray) parser.parse(json);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return obj;
//    }


    public static <T> String toJson( T object ){
        return gson.toJson( object );
    }

    public static <T> T fromJson( String json, Class<T> clazz ){
        return gson.fromJson( json, clazz );
    }

    public static <T> T clone( T object ){
        return cloner.deepClone( object );
    }

    public static <T extends ArrayList> T cloneList( T list ){
        return cloner.deepClone( list );
    }

    public static String toJsonList( List list ){
        StringBuilder sb = new StringBuilder("[");
        Field[] fields = null;
        boolean firstObject = true;
        for (Object obj : list){
            if (firstObject){
                sb.append("{");
                firstObject = false;
            }else{
                sb.append(", {");
            }
            if (fields == null){
                fields = obj.getClass().getFields();
            }
            //do sth to retrieve each field value -> json property of json object
            //add to json array
            for (int i = 0 ; i < fields.length ; i++){
                Field f = fields[i];
                //jsonFromField(sb, obj, i, f);
            }
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }


    public static String replaceAll(String str, String pat, String rep){
        if (str == null)
            return null;
        return str.replaceAll(pat, rep);
    }


    @TargetApi(19)
    public static String listToString( List list ){
        StringBuilder builder = new StringBuilder();
        for( Object obj : list ){
            builder.append( obj.toString() + System.lineSeparator() );
        }
        return builder.toString();
    }


    public static int[] convertYUVtoRGB(byte[] yuv, int width, int height)
            throws NullPointerException, IllegalArgumentException {
        int[] out = new int[width * height];
        int sz = width * height;

        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = yuv[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = yuv[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = yuv[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }

        return out;
    }

}
