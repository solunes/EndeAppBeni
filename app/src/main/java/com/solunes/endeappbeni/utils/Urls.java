package com.solunes.endeappbeni.utils;

import android.content.Context;

import com.solunes.endeappbeni.activities.AdminActivity;


/**
 * Esta clase hacer el manejo de las url de los endpoints
 */
public class Urls {

    public static String endpointBase(Context context) {
        String url = UserPreferences.getString(context, AdminActivity.KEY_DOMAIN);
        return "http://" + url;
    }

    public static String urlDescarga(Context context, int area, String usrLec) {
        return endpointBase(context) + "/api/descarga/" + area + "/" + usrLec;
    }

    public static String urlSubida(Context context) {
        return endpointBase(context) + "/api/subida";
    }

    public static String urlParametros(Context context, int area) {
        return endpointBase(context) + "/api/parametros-fijos/" + area;
    }

    public static String urlauthenticate(Context context) {
        return endpointBase(context) + "/api-auth/authenticate";
    }
}
