package fr.lille1.univ.android.architecture.btrshop.data.source.api;


import fr.lille1.univ.android.architecture.btrshop.App;
import fr.lille1.univ.android.architecture.btrshop.R;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by charlie on 28/10/16.
 *
 * Class ApiService : create initialize retrofit.
 *
 **/
public class ApiService{

    public static final String BASE_URL = App.getContext().getResources().getString(R.string.base_url_api);
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