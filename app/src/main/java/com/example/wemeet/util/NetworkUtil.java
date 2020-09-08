package com.example.wemeet.util;

import com.example.wemeet.pojo.WeMeetMisc;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NetworkUtil {
    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            String baseUrl = "http://101.37.172.100:8080/";
            // 这行可以解决报错：Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $
            //            Gson gson = new GsonBuilder().setLenient().create();
            //            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
            //                    .addConverterFactory(GsonConverterFactory.create(gson)).build();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.addInterceptor(chain -> {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", WeMeetMisc.authString)
                        //                        .header("Accept", "application/json")
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });

            OkHttpClient okHttpClient = okHttpClientBuilder.build();

            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
