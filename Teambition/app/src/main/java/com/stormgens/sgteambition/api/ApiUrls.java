package com.stormgens.sgteambition.api;

/**
 * Created by zlq on 15-10-10.
 */
public class ApiUrls {
    public static String AUTH="https://account.teambition.com/oauth2/authorize?client_id="+
            ApiValues.ClientId +"&redirect_uri="+ApiValues.RedirectUri;
    public static String GET_ACCESS_TOKEN="https://account.teambition.com/oauth2/access_token";
    public static String CHECK_ACCESS_TOKEN="https://api.teambition" + ".com/api/applications/"+ApiValues.ClientId+"/tokens/check";
}
