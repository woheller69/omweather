package org.woheller69.weather.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.weather_api.IProcessHttpRequest;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * This class implements the IHttpRequest interface. It provides HTTP requests by using Volley.
 * See: https://developer.android.com/training/volley/simple.html
 */
public class VolleyHttpRequest implements IHttpRequest {

    private Context context;
    private int cityId;

    /**
     * Constructor.
     *
     * @param context Volley needs a context "for creating the cache dir".
     * @see Volley#newRequestQueue(Context)
     */
    public VolleyHttpRequest(Context context, int cityId) {

        this.context = context;
        this.cityId = cityId;
    }

    /**
     * @see IHttpRequest#make(String, HttpRequestType, IProcessHttpRequest)
     */
    @Override
    public void make(String URL, HttpRequestType method, final IProcessHttpRequest requestProcessor) {
        RequestQueue queue = Volley.newRequestQueue(context);

        // Set the request method
        int requestMethod;
        switch (method) {
            case POST:
                requestMethod = Request.Method.POST;
                break;
            case GET:
                requestMethod = Request.Method.GET;
                break;
            case PUT:
                requestMethod = Request.Method.PUT;
                break;
            case DELETE:
                requestMethod = Request.Method.DELETE;
                break;
            default:
                requestMethod = Request.Method.GET;
        }

        // Execute the request and handle the response
        StringRequest stringRequest = new StringRequest(requestMethod, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        requestProcessor.processSuccessScenario(response,cityId);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestProcessor.processFailScenario(error);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {  //from https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME);
                return params;
            }
        };

        queue.add(stringRequest);
    }


}
