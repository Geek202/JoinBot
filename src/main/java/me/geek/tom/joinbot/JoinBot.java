package me.geek.tom.joinbot;

import me.geek.tom.joinbot.config.Config;
import me.geek.tom.joinbot.config.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class JoinBot extends ListenerAdapter {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<GatewayIntent> intents = Arrays.asList(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES
    );

    private final ConfigManager configManager;

    public static void main(String[] args) {
        try {
            JDABuilder builder = JDABuilder.create(System.getProperty("joinbot.token", "<ENTER TOKEN HERE>"), intents);
            builder.setActivity(Activity.watching("people come and go."));
            try {
                builder.addEventListeners(new JoinBot());
            } catch (IOException e) {
                LOGGER.fatal("Failed to load configs from Github!", e);
                return;
            }
            JDA jda = builder.build();
            jda.awaitReady();
        } catch (LoginException e) {
            LOGGER.fatal("Failed to login!", e);
        } catch (InterruptedException e) {
            LOGGER.fatal("Interrupted while waiting for ready!", e);
        }
    }

    private JoinBot() throws IOException {
        configManager = new ConfigManager();
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        try {
            configManager.loadServers(event.getJDA().getGuilds().stream().map(ISnowflake::getId).toArray(String[]::new));
        } catch (IOException e) {
            LOGGER.fatal("Failed to load server configs!", e);
            event.getJDA().shutdownNow();
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        Config serverConfig = configManager.getServerConfig(guild.getId());
        if (serverConfig == null) return;

        Member member = event.getMember();
        if (member.getRoles().stream().anyMatch(role -> role.getId().equals(serverConfig.verifiedRole))) {
            sendLeaveMessage(member, serverConfig, guild);
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        Config serverConfig = configManager.getServerConfig(guild.getId());
        String message = event.getMessage().getContentRaw();
        User author = event.getAuthor();
        Member member = guild.getMember(author);
        if (message.equals("j!reloadConfig") &&
                member.hasPermission(Permission.MANAGE_SERVER)) {
            try {
                configManager.reloadServerConfig(guild.getId());
                event.getChannel().sendMessage(Embedder.createSuccess("Reloaded server config!")).queue();
            } catch (IOException e) {
                LOGGER.warn("Failed to reload config", e);
                event.getChannel().sendMessage(Embedder.createError(e)).queue();
            }
        } else if (serverConfig != null && message.equals(serverConfig.joinCommand) && event.getChannel().getId().equals(serverConfig.verificationChannel)) {
            sendJoinMessage(member, serverConfig, guild);
            guild.addRoleToMember(member, guild.getRoleById(serverConfig.verifiedRole)).queue();
        }
    }

    private void sendJoinMessage(Member member, Config serverConfig, Guild guild) {
        guild.getTextChannelById(serverConfig.joinLeaveChannel).sendMessage(Embedder.createJoin(member, serverConfig.joinMessage)).queue();
    }

    private void sendLeaveMessage(Member member, Config serverConfig, Guild guild) {
        guild.getTextChannelById(serverConfig.joinLeaveChannel).sendMessage(Embedder.createLeave(member, serverConfig.leaveMessage)).queue();
    }
}
