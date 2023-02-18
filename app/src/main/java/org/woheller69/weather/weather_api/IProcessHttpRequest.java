package org.woheller69.weather.weather_api;

import com.android.volley.VolleyError;

/**
 * This interface is used for providing methods for processing success and failure scenarios of
 * HTTP requests. Implementations of this interface can work with any (weather) API.
 */
public interface IProcessHttpRequest {

    /**
     * The method that will be executed in case of a successful HTTP request.
     *
     * @param response The response of the HTTP request.
     */
    void processSuccessScenario(String response, int cityId);

    /**
     * This method will be executed in case any error arose while executing the HTTP request.
     *
     * @param error The error that occurred while executing the HTTP request.
     */
    void processFailScenario(VolleyError error);

}
