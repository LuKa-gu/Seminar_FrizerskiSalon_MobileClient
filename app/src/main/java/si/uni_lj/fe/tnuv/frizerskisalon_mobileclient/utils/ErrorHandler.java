package si.uni_lj.fe.tnuv.frizerskisalon_mobileclient.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;

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
    public static String extractNetworkError(Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return "Povezava je potekla (timeout).";
        } else if (t instanceof java.net.UnknownHostException) {
            return "Ni internetne povezave.";
        } else {
            return "Napaka pri povezavi s strežnikom.";
        }
    }

    /**
     * Prikaz toast-a za napako HTTP response ali network failure
     */
    public static void showToastError(Context context, Response<?> response, Throwable t, String fallbackMessage) {
        String msg = response != null && !response.isSuccessful()
                ? extractErrorMessage(response, fallbackMessage)
                : extractNetworkError(t);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
