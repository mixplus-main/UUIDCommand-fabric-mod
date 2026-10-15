package com.mixplus.mods.uuidcommand.client;

import com.mixplus.mods.uuidcommand.client.command.CommandRegistrar;
import com.mixplus.mods.uuidcommand.client.commands.UUIDCommand;
import net.fabricmc.api.ClientModInitializer;

public class UuidcommandClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UUIDCommand uuidCommand = new UUIDCommand();

        CommandRegistrar.register(uuidCommand);
    }
}
