package com.mixplus.mods.uuidcommand.client.commands;

import com.mixplus.mods.uuidcommand.api.MojangAPI;
import com.mixplus.mods.uuidcommand.api.model.MojangResponse;
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

import java.util.List;
import java.util.UUID;
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

            this.sendMessage(
                    source,
                    createMessage(
                            "UUID of ",
                            uuid,
                            false
                    )
            );

        } else {
            executor.submit(() -> {

                MojangResponse response = MojangAPI.getUUID(args[0]);

                this.sendMessage(
                        source,
                        createMessage(
                                response.message(),
                                response.uuid(),
                                response.error()
                        )
                );

            });
        }

    }

    @SubCommand("generate")
    public void generate(
            FabricClientCommandSource source,
            String[] args
    ) {
        this.sendMessage(
                source,
                createMessage(
                        "Generated UUID",
                        UUID.randomUUID(),
                        false
                )
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
                        player.getProfile().name()
                )
                .filter(name ->
                        name.toLowerCase()
                                .startsWith(input.toLowerCase())
                )
                .toList();
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
                        player.getProfile().name().equalsIgnoreCase(name)
                )
                .map(player -> player.getProfile().id())
                .findFirst()
                .orElse(null);
    }

    private Text createMessage(String mainMessage, UUID uuid, boolean error) {

        if (error || uuid == null) {
            return Text.literal(mainMessage).formatted(Formatting.RED);
        }

        return Text.literal(mainMessage + "\n")
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
    }


}
