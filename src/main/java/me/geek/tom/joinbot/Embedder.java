package me.geek.tom.joinbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.LocalDate;

public class Embedder {

    public static MessageEmbed createError(String message) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("JoinBot error!")
                .setDescription("An error occurred, sorry:\n`" + message + "`")
                .setColor(Color.RED);
        return builder.build();
    }

    public static MessageEmbed createJoin(Member member, String message) {
        Guild guild = member.getGuild();
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Welcome to " + guild.getName() + ", " + member.getEffectiveName() + "!")
                .setDescription(message.replace("${name}", member.getEffectiveName()).replace("${mention}", member.getAsMention()))
                .addField("Current member count:", String.valueOf(guild.getMemberCount()), false)
                .setFooter(getCurrentDate())
                .setColor(Color.GREEN)
                .setAuthor(guild.getName(), null, guild.getIconUrl());
        return builder.build();
    }

    public static MessageEmbed createLeave(Member member, String message) {
        Guild guild = member.getGuild();
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(member.getEffectiveName() + " left " + guild.getName() + "...")
                .setDescription(message.replace("${name}", member.getEffectiveName()))
                .addField("Current member count:", String.valueOf(guild.getMemberCount()), false)
                .setFooter(getCurrentDate())
                .setColor(Color.RED)
                .setAuthor(guild.getName(), null, guild.getIconUrl());
        return builder.build();
    }

    private static String getCurrentDate() {
        StringBuilder builder = new StringBuilder();
        LocalDate date = LocalDate.now();
        builder.append(date.getDayOfMonth()).append("/").append(date.getMonthValue()).append("/").append(date.getYear());
        return builder.toString();
    }

    public static MessageEmbed createSuccess(String message) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Success!")
                .setDescription(message)
                .setColor(Color.GREEN);
        return builder.build();
    }
}
