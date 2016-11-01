package fr.lille1.univ.android.architecture.btrshop.data.source.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by charlie on 28/10/16.
 *
 * Class ApiService : create initialize retrofit.
 *
 **/
public class ApiService {

    public static final String BASE_URL = "https://todoappcloud.herokuapp.com/";
    private static Retrofit retrofit = null;

    /**
     * Function return the instance of retrofit.
     *
     * @return the instance of retrofit
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }



}