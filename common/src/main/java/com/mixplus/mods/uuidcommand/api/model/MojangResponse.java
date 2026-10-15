package com.mixplus.mods.uuidcommand.api.model;

import java.util.UUID;

public record MojangResponse(
        String message,
        UUID uuid,
        boolean error
) {
}
