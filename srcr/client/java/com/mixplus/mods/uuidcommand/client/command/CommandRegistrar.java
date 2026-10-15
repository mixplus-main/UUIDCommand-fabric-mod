package com.mixplus.mods.uuidcommand.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void register(Object commandInstance) {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) ->
                        register(dispatcher, commandInstance)
        );
    }

    private static void register(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            Object commandInstance
    ) {
        Class<?> commandClass =
                commandInstance.getClass();

        ClientCommand clientCommand =
                commandClass.getAnnotation(ClientCommand.class);

        if (clientCommand == null) {
            throw new IllegalArgumentException(
                    "Missing @ClientCommand: "
                            + commandClass.getName()
            );
        }

        var root = ClientCommandManager.literal(
                clientCommand.value()
        );

        for (Method method : commandClass.getDeclaredMethods()) {

            SubCommand subCommand =
                    method.getAnnotation(SubCommand.class);

            if (subCommand == null) {
                continue;
            }

            var command = ClientCommandManager.literal(
                    subCommand.value()
            );


            command.executes(context -> {

                invoke(
                        method,
                        commandInstance,
                        context.getSource(),
                        new String[0]
                );

                return 1;
            });


            RequiredArgumentBuilder<
                    FabricClientCommandSource,
                    String
                    > argument =
                    ClientCommandManager.argument(
                            "args",
                            StringArgumentType.greedyString()
                    );


            Method tabComplete =
                    findTabComplete(
                            commandClass,
                            subCommand.value()
                    );

            if (tabComplete != null) {

                SuggestionProvider<FabricClientCommandSource>
                        suggestionProvider =
                        (context, builder) -> {

                            String remaining =
                                    builder.getRemaining();

                            String[] args =
                                    remaining.isBlank()
                                            ? new String[0]
                                            : remaining.split("\\s+");

                            List<String> suggestions =
                                    invokeTabComplete(
                                            tabComplete,
                                            commandInstance,
                                            context.getSource(),
                                            args
                                    );

                            for (String suggestion : suggestions) {
                                builder.suggest(suggestion);
                            }

                            return CompletableFuture.completedFuture(
                                    builder.build()
                            );
                        };

                argument.suggests(suggestionProvider);
            }

            argument.executes(context -> {

                String value =
                        StringArgumentType.getString(
                                context,
                                "args"
                        );

                String[] args =
                        value.isBlank()
                                ? new String[0]
                                : value.split("\\s+");

                invoke(
                        method,
                        commandInstance,
                        context.getSource(),
                        args
                );

                return 1;
            });

            command.then(argument);
            root.then(command);
        }

        dispatcher.register(root);
    }

    private static Method findTabComplete(
            Class<?> commandClass,
            String subCommand
    ) {
        for (Method method :
                commandClass.getDeclaredMethods()) {

            TabComplete annotation =
                    method.getAnnotation(TabComplete.class);

            if (annotation == null) {
                continue;
            }

            if (annotation.value().equals(subCommand)) {
                return method;
            }
        }

        return null;
    }

    private static List<String> invokeTabComplete(
            Method method,
            Object commandInstance,
            FabricClientCommandSource source,
            String[] args
    ) {
        try {
            Object result =
                    method.invoke(
                            commandInstance,
                            source,
                            args
                    );

            if (result instanceof List<?> list) {
                return list.stream()
                        .map(String::valueOf)
                        .toList();
            }

            throw new IllegalArgumentException(
                    "@TabComplete method must return List<String>: "
                            + method.getName()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to execute tab complete: "
                            + method.getName(),
                    e
            );
        }
    }

    private static void invoke(
            Method method,
            Object commandInstance,
            FabricClientCommandSource source,
            String[] args
    ) {
        try {
            method.invoke(
                    commandInstance,
                    source,
                    args
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to execute command: "
                            + method.getName(),
                    e
            );
        }
    }
}