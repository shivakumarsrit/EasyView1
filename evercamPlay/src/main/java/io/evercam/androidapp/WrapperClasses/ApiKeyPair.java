package io.evercam.androidapp.WrapperClasses;

import org.json.JSONException;
import org.json.JSONObject;

import io.evercam.*;

public class ApiKeyPair {
    private JSONObject jsonObject;

    ApiKeyPair(JSONObject keyPairJSONObject) {
        jsonObject = keyPairJSONObject;
    }

    public String getApiKey() throws io.evercam.EvercamException {
        try {
            return jsonObject.getString("api_key");
        } catch (JSONException e) {
            throw new io.evercam.EvercamException(e);
        }
    }

    public String getApiId() throws io.evercam.EvercamException {
        try {
            return jsonObject.getString("api_id");
        } catch (JSONException e) {
            throw new io.evercam.EvercamException(e);
        }
    }
}
