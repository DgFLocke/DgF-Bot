package com.dgf;
import javax.security.auth.login.LoginException;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;



public class Bot {
    private static JDA jda;

    public static void main(String[] args) throws LoginException {
        String token = System.getenv("DISCORD_BOT_TOKEN"); // Token is stored in an environment variable
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.playing("Type !help"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES);
        builder.addEventListeners(new BotListener());
        jda = builder.build();
        scheduleDailyMessage();
    }

    private static void scheduleDailyMessage() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        LocalTime targetTime = LocalTime.of(7, 30); // 7:30 AM
        LocalTime now = LocalTime.now();
        long initialDelay = now.until(targetTime, ChronoUnit.MINUTES);
        if (initialDelay < 0) {
            initialDelay += 1440; // Add 24 hours in minutes if the time has already passed for today
        }

        scheduler.scheduleAtFixedRate(() -> sendDailyMessage(), initialDelay, 1440, TimeUnit.MINUTES);
    }

    private static void sendDailyMessage() {
        String channelId = "1036273633003315313"; // Replace with the actual channel ID
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            StringBuilder messageBuilder = new StringBuilder("Good morning degenerates, how are we today?");
        
            String[] locations = {"Barnsley", "Birmingham", "Huntingdon", "Worcester"};
            for (String location : locations) {
                messageBuilder.append("\nThe weather for ").append(location).append(" today is ");
                try {
                    String weatherInfo = WeatherService.getWeather(location);
                    messageBuilder.append(weatherInfo);
                } catch (Exception e) {
                    messageBuilder.append("not available at the moment.");
                }
            }
            String finalMessage = messageBuilder.toString();
            if (finalMessage != null) { // Additional null check
                channel.sendMessage(finalMessage).queue();
            }
        }
    }
    
    
}
