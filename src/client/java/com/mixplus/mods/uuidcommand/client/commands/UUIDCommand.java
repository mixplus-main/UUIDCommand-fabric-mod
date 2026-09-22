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

import java.time.Duration;
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
            this.send(
                    source,
                    uuid,
                    States.Get,
                    args[0]
            );

        } else {
            executor.submit(() -> {
                try {
                    Response response = Https.request(
                            "https://api.mojang.com/users/profiles/minecraft/" + args[0]
                    );

                    int statusCode = response.statusCode();
                    switch (statusCode) {
                        case 404 -> {
                            this.send(
                                    source,
                                    null,
                                    States.PlayerNotfound,
                                    args[0]
                            );
                            return;
                        }

                        case 500, 502, 503, 504 -> {
                            this.send(
                                    source,
                                    null,
                                    States.APIError,
                                    args[0]
                            );
                            return;
                        }

                        default -> {}
                    }

                    Map<String, Object> profile = response.body();

                    if (!profile.containsKey("id")) {
                        this.send(
                                source,
                                null,
                                States.APIError,
                                args[0]
                        );
                        return;
                    }

                    this.send(
                            source,
                            StringUtil.toUUID(profile.get("id").toString()),
                            States.Get,
                            args[0]
                    );


                } catch (RuntimeException e) {
                    this.send(
                            source,
                            null,
                            States.APIError,
                            args[0]
                    );
                }
            });
        }

    }

    @SubCommand("generate")
    public void generate(
            FabricClientCommandSource source,
            String[] args
    ) {
        this.send(
                source,
                UUID.randomUUID(),
                States.Generate,
                null
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

    private void send(
            @NotNull FabricClientCommandSource source,
            UUID uuid,
            @NotNull States mode,
            String username
    ) {
        Text message;

        switch (mode) {
            case Get -> message = Text.literal("UUID of " + username + "\n")
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

            case PlayerNotfound -> message = Text.literal("Player not found.")
                    .formatted(Formatting.RED);

            case APIError -> message = Text.literal("Failed to retrieve player information.")
                    .formatted(Formatting.RED);

            default -> message = Text.literal("");
        }

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

