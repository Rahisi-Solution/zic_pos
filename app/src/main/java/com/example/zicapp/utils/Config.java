package com.example.zicapp.utils;

public class Config {
    public static final String INCOMING_TAG = "incoming_tag";
    public static final String LOG_TAG = "ZIC_APP";
    // public static final String ENDPOINT = "http://172.16.10.121:1910/api/pos/";
    public static final String ENDPOINT = "https://earrival.rahisi.co.tz/api/pos/";

    public static final String OFFICER_LOGIN = ENDPOINT + "officer_login";
    public static final String GET_APPLICANT = ENDPOINT + "get_applicant";

    public static final String AUTH_TOKEN = "AUTH_TOKEN";
    public static final String LOGIN_CREDENTIAL = "LOGIN_CREDENTIAL";
    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String DOMAIN = "DOMAIN";
    public static final String ENTRYPOINT_ID = "ENTRYPOINT_ID";
    public static final String ENTRYPOINT = "ENTRYPOINT";

    /** SHARED PREFS */
    public static final String SHARED_PREF_NAME = "ZIC_APP";
    public static final String LOGGED_IN_PREF = "LOGGED_IN_PREF";

    public static String removeDoubleQuotes(String request) {
        return request.replace("\"", "");
    }
}
