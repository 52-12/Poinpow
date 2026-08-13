package com.udu3324.poinpow.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.udu3324.poinpow.Config;
import com.udu3324.poinpow.Poinpow;
import com.udu3324.poinpow.commands.Commands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class ChatPhraseFilter {
    public static String name = "chat_phrase_filter";
    public static String description = "This filters out messages in lobby that match the regex. Go to https://regex101.com to create some.";
    public static AtomicBoolean toggled = new AtomicBoolean(true);

    public static ArrayList<Pattern> list = Config.getListOfRegex();

    public static void check(String chat, CallbackInfo ci) {
        // Return if toggled off
        if (!toggled.get()) return;

        // Return if not on Minehut
        if (!Poinpow.onMinehut) return;

        // Return if there's nothing in the list
        if (list.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Return if there is no world
        if (client.world == null) return;

        // Check for scoreboard
        Scoreboard scoreboard = client.world.getScoreboard();
        ArrayList<String> scores = new ArrayList<>();

        for (ScoreboardObjective objective : scoreboard.getObjectives()) {
            scores.add(objective.getDisplayName().toString());
        }

        // Return if this isn't the Minehut lobby
        if (!scores.toString().toLowerCase().contains("minehut")) return;

        for (Pattern p : list) {
            if (p.matcher(chat).find()) {
                Poinpow.log.info("Filtered: {}", chat);
                ci.cancel();
                return;
            }
        }
    }

    public static int add(CommandContext<FabricClientCommandSource> context) {
        Commands.running = true;
        String input = StringArgumentType.getString(context, "regex");

        // Refresh list
        list = Config.getListOfRegex();

        // Check if regex already exists
        for (Pattern p : list) {
            if (p.toString().equals(input)) {
                context.getSource().sendFeedback(
                        Text.literal("The regex you provided is already in the filter.")
                                .styled(style -> style.withColor(Formatting.RED))
                );

                Commands.running = false;
                return Command.SINGLE_SUCCESS;
            }
        }

        context.getSource().sendFeedback(
                Text.literal("\nAdded the regex below to ChatPhraseFilter")
                        .styled(style -> style.withColor(Formatting.GREEN))
        );

        context.getSource().sendFeedback(
                Text.literal(input + "\n")
                        .styled(style -> style.withColor(Formatting.GOLD))
        );

        Poinpow.log.info("addRegex(): {}", input);

        Config.addRegex(input);
        list.add(Pattern.compile(input));

        Commands.running = false;
        return Command.SINGLE_SUCCESS;
    }

    public static int remove(CommandContext<FabricClientCommandSource> context) {
        Commands.running = true;
        String input = StringArgumentType.getString(context, "regex");

        // Refresh list
        list = Config.getListOfRegex();

        // Check if regex exists
        for (Pattern p : list) {
            if (p.toString().equals(input)) {
                Poinpow.log.info("removeRegex(): {}", input);

                Config.removeRegex(input);
                list.remove(p);

                context.getSource().sendFeedback(
                        Text.literal(
                                "Successfully removed \"" + input + "\" from the filter."
                        ).styled(style -> style.withColor(Formatting.GREEN))
                );

                Commands.running = false;
                return Command.SINGLE_SUCCESS;
            }
        }

        context.getSource().sendFeedback(
                Text.literal(
                        "Couldn't find \"" + input +
                                "\" in the list of regex! \nDo \"/poinpow " +
                                name +
                                " list\" to see a clickable list to remove regex."
                ).styled(style -> style
                        .withClickEvent(
                                new ClickEvent.SuggestCommand(
                                        "/poinpow " + name + " list"
                                )
                        )
                        .withUnderline(true)
                        .withColor(Formatting.RED)
                )
        );

        Commands.running = false;
        return Command.SINGLE_SUCCESS;
    }

    public static int list(FabricClientCommandSource source) {
        Commands.running = true;

        // Refresh list
        list = Config.getListOfRegex();

        // Check if list is empty
        if (list.isEmpty()) {
            source.sendFeedback(
                    Text.literal("\nThere is no regex in ChatPhraseFilter.")
                            .styled(style -> style.withColor(Formatting.BLUE))
            );

            Commands.running = false;
            return Command.SINGLE_SUCCESS;
        }

        // Show the list
        source.sendFeedback(
                Text.literal("\nHere's the list of regex in ChatPhraseFilter.")
                        .styled(style -> style.withColor(Formatting.GREEN))
        );

        for (Pattern p : list) {
            source.sendFeedback(
                    Text.literal(p.toString())
                            .styled(style -> style
                                    .withClickEvent(
                                            new ClickEvent.SuggestCommand(
                                                    "/poinpow " +
                                                            name +
                                                            " remove \"" +
                                                            p +
                                                            "\""
                                            )
                                    )
                                    .withColor(Formatting.GOLD)
                            )
            );
        }

        source.sendFeedback(
                Text.literal(
                        "Click on the text to remove it from the filter.\n"
                ).styled(style -> style.withColor(Formatting.RED))
        );

        Commands.running = false;
        return Command.SINGLE_SUCCESS;
    }
}