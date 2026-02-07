package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.api;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.ui.LoginActivity;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils.ErrorHandler;
public class JWTManager {
    private static final String PREFS_NAME = "auth";
    private static final String PREFS_TOKEN_KEY = "token";

    // Funkcija za preverjanje JWT
    public static void preveriJWT(Context context, Runnable onSuccess) {
        String token = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(PREFS_TOKEN_KEY, null);

        if (token == null) {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return;
        }

        Retrofit retrofit = ApiClient.getClient(token);
        JWTApi api = retrofit.create(JWTApi.class);

        api.getMe().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body() == null) {
                        // Prazen odgovor strežnika = napaka
                        Toast.makeText(context, "Prazen odgovor strežnika.", Toast.LENGTH_LONG).show();
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                .edit().remove(PREFS_TOKEN_KEY).apply();
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        return;
                    }

                    // Token veljaven → izvedi onSuccess callback
                    if (onSuccess != null) onSuccess.run();

                } else {
                    // Token neveljaven → izbriši in pojdi na login
                    ErrorHandler.showToastError(context, response, null, "Token ni veljaven, prijavi se ponovno.");
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().remove(PREFS_TOKEN_KEY).apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().remove(PREFS_TOKEN_KEY).apply();
                ErrorHandler.showToastError(context, null, t, null);

                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
    }

    // Retrofit interface za /jaz endpoint
    interface JWTApi {
        @GET("uporabniki/jaz")
        Call<JsonObject> getMe();
    }
}
