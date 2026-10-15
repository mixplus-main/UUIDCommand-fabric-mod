package com.mixplus.mods.uuidcommand.api;

import com.mixplus.library.network.Https;
import com.mixplus.library.network.Response;
import com.mixplus.library.util.StringUtil;
import com.mixplus.mods.uuidcommand.api.model.MojangResponse;

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.UUID;

public class MojangAPI {

    public static MojangResponse getUUID(String username) {
        try {
            Response response = Https.request(
                    "https://api.mojang.com/users/profiles/minecraft/" + username
            );

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                return new MojangResponse(
                        handleMessage(statusCode),
                        null,
                        true
                );
            }


            UUID uuid = getResponseUUID(response);

            return new MojangResponse(
                    "UUID of " + username,
                    uuid,
                    false
            );


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new MojangResponse(
                    "HTTP request interrupted",
                    null,
                    true
            );

        } catch (IOException e) {
            switch (e) {
                case HttpTimeoutException ignored -> {
                    return new MojangResponse(
                            "HTTP request timed out",
                            null,
                            true
                    );
                }

                case UnknownHostException ignored -> {
                    return new MojangResponse(
                            "Failed to resolve host",
                            null,
                            true
                    );
                }

                default -> {
                    return new MojangResponse(
                            "HTTP request failed",
                            null,
                            true
                    );
                }
            }
        }

    }

    private static String handleMessage(int statusCode) {
        return switch (statusCode) {
            case 204, 404 -> "Player not found.";
            case 400 -> "Invalid username.";
            case 429 -> "Too many requests. Please try again later.";
            case 500, 502, 503, 504 ->
                    "Internal server error. Please try again later.";
            default -> "Unexpected HTTP status: " + statusCode;
        };

    }

    private static UUID getResponseUUID(Response response) {
        String id = response.body().get("id").toString();

        return StringUtil.toUUID(id);

    }

}
