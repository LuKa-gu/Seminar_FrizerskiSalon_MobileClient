package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;
import si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.R;

public class ErrorHandler {

    /**
     * Prebere sporočilo iz ne-uspšnega HTTP response-a.
     * Če ni message polja, vrne fallbackMessage.
     */
    public static String extractErrorMessage(Response<?> response, String fallbackMessage) {
        if (response == null || response.errorBody() == null) {
            return fallbackMessage;
        }

        try {
            String errorJson = response.errorBody().string();
            JsonObject jsonObject = JsonParser.parseString(errorJson).getAsJsonObject();

            if (jsonObject.has("message")) {
                return jsonObject.get("message").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fallbackMessage;
    }

    /**
     * Prebere sporočilo iz network napake (onFailure throwable)
     */
    public static String extractNetworkError(Context context, Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return context.getString(R.string.napaka_timeout);
        } else if (t instanceof java.net.UnknownHostException) {
            return context.getString(R.string.napaka_ni_interneta);
        } else {
            return context.getString(R.string.napaka_povezava_streznik);
        }
    }

    /**
     * Prikaz toast-a za napako HTTP response ali network failure
     */
    public static void showToastError(Context context, Response<?> response, Throwable t, String fallbackMessage) {
        String msg = response != null && !response.isSuccessful()
                ? extractErrorMessage(response, fallbackMessage)
                : extractNetworkError(context, t);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
