/**
 * Copyright 2021 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.cloud.appconfiguration.sdk.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.cloud.appconfiguration.sdk.configurations.internal.ConfigConstants;
import com.ibm.cloud.sdk.core.http.HttpHeaders;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.http.RequestBuilder;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ResponseConverter;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.BaseService;
import com.ibm.cloud.sdk.core.util.ResponseConverterUtils;
import org.json.JSONObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A wrapper class consisting of methods that perform API request/response handling of the AppConfiguration SDK
 * by extending the {@link BaseService}.
 */
public class ServiceImpl extends BaseService {
    private static final String SDK_PROPERTIES_FILE_NAME = "appconfiguration-java-sdk.properties";
    private static final String DEFAULT_IAM_DEV_STAGE_URL = "iam.test.cloud.ibm.com";
    private static ServiceImpl instance;
    private static IamAuthenticator iamAuthenticator = null;
    private static String version;
    private static String artifactId;
    private static String apikey = "";
    private static String overrideServerHost = null;

    static {
        readSdkProperties();
    }

    private ServiceImpl() {
    }

    /**
     * @param apikey the apikey
     * @param overrideServerHost Server Host. Use for testing purpose.
     * @return instance of {@link ServiceImpl}
     */
    public static ServiceImpl getInstance(String apikey, String overrideServerHost) {
        if (instance == null) {
            instance = init(apikey, overrideServerHost);
        }
        return instance;
    }

    private ServiceImpl(String serviceName, Authenticator authenticator) {
        super(serviceName, authenticator);
    }

    private static ServiceImpl init(String apikey, String overrideServerHost) {
        ServiceImpl.apikey = apikey;
        ServiceImpl.overrideServerHost = overrideServerHost;
        IamAuthenticator iamAuthenticator = ServiceImpl.getIamAuthenticator();
        ServiceImpl service = new ServiceImpl(ConfigConstants.DEFAULT_SERVICE_NAME, iamAuthenticator);
        service.configureService(ConfigConstants.DEFAULT_SERVICE_NAME);
        return service;
    }

    protected static void readSdkProperties() {
        final Properties properties = new Properties();
        try {
            properties.load(ServiceImpl.class.getClassLoader().getResourceAsStream(SDK_PROPERTIES_FILE_NAME));
            version = properties.getProperty("version", "unknown");
            artifactId = properties.getProperty("artifactId", "unknown");
        } catch (Exception e) {
            version = "unknown";
            artifactId = "unknown";
        }
    }

    /**
     * @return SDK version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * @return artifact id
     */
    public static String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the IAM Authenticator, which will be used to authenticate requests to App configuration service.
     *
     * @return iam authenticator object
     */
    public static IamAuthenticator getIamAuthenticator() {
        if (iamAuthenticator == null) {
            createIamAuth();
        }
        return iamAuthenticator;
    }

    private static IamAuthenticator createIamAuth() {
        if (overrideServerHost != null) {
            iamAuthenticator = new IamAuthenticator.Builder().
            url(ConfigConstants.DEFAULT_HTTP_TYPE + DEFAULT_IAM_DEV_STAGE_URL).
            apikey(apikey).
            build();
        } else {
            //this automatically calls iam prod url
            iamAuthenticator = new IamAuthenticator.Builder()
            .apikey(apikey).build();
        }
        return iamAuthenticator;
    }

    /**
     * @return formatted current date
     */
    public static String getCurrentDateTime() {
        Instant currentDateTimeInstant = Instant.now();
        return String.valueOf(currentDateTimeInstant).split("\\.")[0] + "Z";
    }

    private HashMap<String, String> getServiceHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.USER_AGENT, ServiceImpl.getArtifactId() + "/" + ServiceImpl.getVersion());
        headers.put(HttpHeaders.CONTENT_TYPE, HttpMediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Execute GET API request.
     *
     * @param url url to get configurations
     * @return the HTTP response
     */
    public Response getConfig(String url) {
        try {
            RequestBuilder builder = RequestBuilder.get(RequestBuilder.resolveRequestUrl(url, null, null));
            for (Map.Entry<String, String> header : this.getServiceHeaders().entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }

            ResponseConverter<String> responseConverter = ResponseConverterUtils.getString();
            return createServiceCall(builder.build(), responseConverter).execute();
        } catch (Exception e) {
            BaseLogger.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Execute POST API request.
     *
     * @param meteringUrl url to send metering data
     * @param data data to send
     * @return the HTTP response
     */
    public Response postMetering(String meteringUrl, JSONObject data) {
        try {
            RequestBuilder builder = RequestBuilder.
            post(RequestBuilder.resolveRequestUrl(meteringUrl, null, null));
            for (Map.Entry<String, String> header : this.getServiceHeaders().entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
            JsonObject jsonData = new Gson().fromJson(String.valueOf(data), JsonObject.class);
            builder.bodyJson(jsonData);
            ResponseConverter<String> responseConverter = ResponseConverterUtils.getString();
            return createServiceCall(builder.build(), responseConverter).execute();
        } catch (Exception e) {
            BaseLogger.error(e.getLocalizedMessage());
        }
        return null;
    }
}
