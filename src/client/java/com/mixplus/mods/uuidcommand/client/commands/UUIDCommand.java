package com.mixplus.mods.uuidcommand.client.commands;

import com.mixplus.library.network.Https;
import com.mixplus.library.network.Response;
import com.mixplus.library.util.StringUtil;
import com.mixplus.mods.uuidcommand.client.command.ClientCommand;
import com.mixplus.mods.uuidcommand.client.command.SubCommand;
import com.mixplus.mods.uuidcommand.client.command.TabComplete;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
@ClientCommand("uuid")
public class UUIDCommand {
    private static final ExecutorService executor =
            Executors.newVirtualThreadPerTaskExecutor();

    @SubCommand("get")
    public void get(
            FabricClientCommandSource source,
            String[] args
    ) {
        if (args.length == 0) {
            return;
        }

        UUID uuid = findOnlinePlayer(args[0]);
        if (uuid != null) {
            Text message = this.handleMessage(
                    States.Get,
                    null,
                    args[0],
                    uuid
            );

            this.sendMessage(
                    source,
                    message
            );

        } else {
            executor.submit(() -> {

                try {
                    Response response = Https.request(
                            "https://api.mojang.com/users/profiles/minecraft/" + args[0]
                    );

                    handleResponse(response, source, args);


                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                } catch (IOException e) {
                    switch (e) {
                        case HttpTimeoutException exception -> this.handleError(source, ErrorType.TIMEOUT);

                        case UnknownHostException exception -> this.handleError(source, ErrorType.UNKNOWN_HOST);

                        default -> this.handleError(source, ErrorType.IO_ERROR);
                    }
                }

            });
        }

    }

    @SubCommand("generate")
    public void generate(
            FabricClientCommandSource source,
            String[] args
    ) {
        Text message = this.handleMessage(States.Generate, null, null, UUID.randomUUID());
        this.sendMessage(
                source,
                message
        );
    }

    @TabComplete("get")
    public List<String> getTabComplete(
            FabricClientCommandSource source,
            String[] args
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.getNetworkHandler() == null) {
            return List.of();
        }

        String input = args.length > 0
                ? args[0]
                : "";

        return client.getNetworkHandler()
                .getPlayerList()
                .stream()
                .map(player ->
                        player.getProfile().getName()
                )
                .filter(name ->
                        name.toLowerCase()
                                .startsWith(input.toLowerCase())
                )
                .toList();
    }

    private void handleError(FabricClientCommandSource source, ErrorType errorType) {
        Text message = this.handleMessage(States.APIError, errorType, null, null);
        this.sendMessage(source, message);
    }

    private void handleResponse(
            Response response,
            FabricClientCommandSource source,
            String[] args
    ) {
        int statusCode = response.statusCode();
        switch (statusCode) {
            case 404 -> {
                Text message = this.handleMessage(States.PlayerNotfound, null, null, null);
                this.sendMessage(source, message);
                return;
            }

            case 500, 502, 503, 504 -> {
                Text message = this.handleMessage(States.APIError, null, null, null);
                this.sendMessage(
                        source,
                        message
                );
                return;
            }

            default -> {}
        }

        Map<String, Object> profile = response.body();

        if (!profile.containsKey("id")) {
            Text message = this.handleMessage(States.PlayerNotfound, null, null, null);
            this.sendMessage(source, message);
            return;
        }

        Text message = this.handleMessage(
                States.Get,
                null,
                args[0],
                StringUtil.toUUID(profile.get("id").toString())
        );
        this.sendMessage(
                source,
                message
        );

    }

    private Text handleMessage(
            @NotNull States states,
            ErrorType errorType,
            String username,
            UUID uuid
    ) {
        Text message;
        switch (states) {
            case Get -> message = Text.literal("UUId of " + username + "\n")
                    .append(
                            Text.literal(uuid.toString())
                                    .formatted(Formatting.GREEN)
                                    .styled(style -> style
                                            .withClickEvent(
                                                    new ClickEvent.CopyToClipboard(uuid.toString())
                                            )
                                            .withHoverEvent(
                                                    new HoverEvent.ShowText(
                                                            Text.literal("Click to copy")
                                                    )
                                            ))
                    );

            case Generate -> message = Text.literal("Generated UUID\n")
                    .append(
                            Text.literal(uuid.toString())
                                    .formatted(Formatting.GREEN)
                                    .styled(style -> style
                                            .withClickEvent(
                                                    new ClickEvent.CopyToClipboard(
                                                            uuid.toString()
                                                    )
                                            )
                                            .withHoverEvent(
                                                    new HoverEvent.ShowText(
                                                            Text.literal("Click to copy")
                                                    )
                                            )
                                    )
                    );

            case APIError, Error -> {
                if (Objects.requireNonNull(errorType) == ErrorType.TIMEOUT) {
                    message = Text.literal(ErrorType.TIMEOUT.getMessage())
                            .formatted(Formatting.RED);
                } else {
                    message = Text.literal(ErrorType.IO_ERROR.getMessage())
                            .formatted(Formatting.RED);
                }
            }

            case PlayerNotfound -> message = Text.literal("Player not found.")
                    .formatted(Formatting.RED);

            default -> message = Text.literal("An issue may have occurred.")
                    .formatted(Formatting.RED);
        }

        return message;
    }

    private void sendMessage(
            @NotNull FabricClientCommandSource source,
            Text message
    ) {
        source.sendFeedback(message);
    }

    private UUID findOnlinePlayer(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) {
            return null;
        }

        return client.getNetworkHandler()
                .getPlayerList()
                .stream()
                .filter(player ->
                        player.getProfile().getName().equalsIgnoreCase(name)
                )
                .map(player -> player.getProfile().getId())
                .findFirst()
                .orElse(null);
    }

}

