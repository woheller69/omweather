package org.woheller69.weather.ui.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by MG on 04-03-2018.
 *
 * Taken from https://github.com/Truiton/AutoSuggestTextViewAPICall
 * Modified by woheller69
 */

public class geocodingApiCall {
    private static geocodingApiCall mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public geocodingApiCall(Context ctx) {
        mCtx = ctx.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized geocodingApiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new geocodingApiCall(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void make(Context ctx, String query, String url, String lang, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {
        url = url + query+"&language="+lang;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errorListener);
        geocodingApiCall.getInstance(ctx).addToRequestQueue(stringRequest);
    }
}
