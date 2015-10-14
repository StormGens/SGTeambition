package com.stormgens.library.common.network;

/**
 * Created by zlq on 15-10-13.
 */
public class ParamsUtil {

    public static Params decodeUrl(String content){
        Params params=new Params();
        try{
            String decodeSource="";
            if (content.contains("?")){
                decodeSource=content.substring(content.indexOf("?")+1,content.length());
            }else {
                decodeSource=content;
            }
            String[] decodeParams = decodeSource.split("&");

            for (String keyValues : decodeParams) {
                String[] keyValue = keyValues.split("=");
                params.addParameter(keyValue[0], keyValue[1]);
            }
        } catch (Exception e) {
        }

        return params;
    }

}
