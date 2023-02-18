package org.woheller69.weather.http;

import org.woheller69.weather.weather_api.IProcessHttpRequest;

/**
 * This interface defines the template for making HTTP request. Furthermore, it provides a generic
 * way for handling the responses.
 */
public interface IHttpRequest {

    /**
     * Makes an HTTP request and processes the response.
     *
     * @param URL              The target of the HTTP request.
     * @param method           Which method to use for the HTTP request (e.g. GET or POST)
     * @param requestProcessor This object with its implemented methods processSuccessScenario and
     *                         processFailScenario defines how to handle the response in the success
     *                         and error case respectively.
     */
    void make(final String URL, HttpRequestType method, IProcessHttpRequest requestProcessor);

}
