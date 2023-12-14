package com.dgf;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.MessageHistory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BotListener extends ListenerAdapter {
    // Map to keep track of ongoing trivia games by channel
    private Map<Long, TriviaGame> ongoingGames = new HashMap<>();
    private Map<Long, Boolean> waitingForTriviaSettings = new HashMap<>();


    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String messageSent = event.getMessage().getContentRaw();

        if (messageSent.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        } else if (messageSent.toLowerCase().startsWith("!joke")) {
            handleJokeCommand(event, messageSent);
        } else if (messageSent.toLowerCase().startsWith("!ban")) {
            handleBanCommand(event, messageSent);
        } else if (messageSent.toLowerCase().startsWith("!poll")) {
            handlePollCommand(event, messageSent);
        } else if (messageSent.toLowerCase().startsWith("!clear")) {
            handleClearCommand(event, messageSent);
        } else if (messageSent.toLowerCase().startsWith("!weather")) {
            handleWeatherCommand(event, messageSent);
        } if (messageSent.toLowerCase().startsWith("!trivia")) {
                handleTriviaCommand(event);
        } else if (isWaitingForTriviaSettings(event.getChannel().getIdLong())) {
                processTriviaSettings(event, messageSent);
        }
        
            // New code to handle trivia responses
        long channelId = event.getChannel().getIdLong();
        User user = event.getAuthor();
        String messageContent = event.getMessage().getContentRaw();

        if (ongoingGames.containsKey(channelId)) {
        TriviaGame currentGame = ongoingGames.get(channelId);
        currentGame.handleResponse(user, messageContent);
        }
    }

    private void handleJokeCommand(GuildMessageReceivedEvent event, String message) {
        String[] parts = message.split("\\s+", 2);
        String category = parts.length > 1 ? parts[1].trim().toLowerCase() : null;
        String joke = JokeService.getRandomJoke(category);
        String response = joke != null ? joke : "No joke available for the specified category.";
        event.getChannel().sendMessage(response).queue();
    }

    private void handleBanCommand(GuildMessageReceivedEvent event, String message) {
        if (!hasBanPermission(event.getMember())) {
            event.getChannel().sendMessage("You don't have permission to use this command.").queue();
            return;
        }

        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
        if (mentionedUsers.isEmpty()) {
            event.getChannel().sendMessage("Please mention a user to ban.").queue();
            return;
        }

        User userToBan = mentionedUsers.get(0);
        if (userToBan == null) {
            event.getChannel().sendMessage("Invalid user mentioned.").queue();
            return;
        }

        event.getGuild().ban(userToBan, 0, TimeUnit.SECONDS).reason("Banned by command").queue(
            success -> event.getChannel().sendMessage("Banned " + userToBan.getAsMention()).queue(),
            error -> event.getChannel().sendMessage("Failed to ban the user. Error: " + error.getMessage()).queue()
        );
    }

    private void handlePollCommand(GuildMessageReceivedEvent event, String message) {
        String[] parts = message.split("\\s+", 2);
        if (parts.length < 2) {
            event.getChannel().sendMessage("Usage: !poll <question> | <option 1> | <option 2> | ...").queue();
            return;
        }

        String[] pollParts = parts[1].split("\\|");
        if (pollParts.length < 3) {
            event.getChannel().sendMessage("Please provide a question and at least two options.").queue();
            return;
        }

        String question = pollParts[0].trim();
        StringBuilder pollMessage = new StringBuilder("**Poll:** " + question + "\n");

        String[] emojis = {"\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4", "\uD83C\uDDF5", "\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9"};
        for (int i = 1; i < pollParts.length && i < 10; i++) {
            pollMessage.append(emojis[i - 1]).append(" - ").append(pollParts[i].trim()).append("\n");
        }

        final String poll = pollMessage.toString();
        if (poll != null) { // Check if 'poll' is not null
            event.getChannel().sendMessage(poll).queue(sentMessage -> {
            for (int i = 1; i < pollParts.length && i < 10; i++) {
                String emojiStr = emojis[i - 1];
                if (emojiStr != null) { // Check if emoji string is not null
                    try {
                        Emoji emoji = Emoji.fromUnicode(emojiStr);
                        sentMessage.addReaction(emoji).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }    
    }

    private boolean hasBanPermission(Member member) {
        if (member == null) return false;

        return member.hasPermission(Permission.BAN_MEMBERS) || 
               member.getRoles().stream().anyMatch(role ->
                   role.getName().equalsIgnoreCase("Mods") ||
                   role.getName().equalsIgnoreCase("The Crew"));
    }

    private boolean hasManageMessagesPermission(Member member) {
        if (member == null) return false;

        // Check if the member has the MESSAGE_MANAGE permission
        return member.hasPermission(Permission.MESSAGE_MANAGE);
    }
    private void handleClearCommand(GuildMessageReceivedEvent event, String message) {
        if (!hasManageMessagesPermission(event.getMember())) {
            event.getChannel().sendMessage("You don't have permission to clear messages.").queue();
            return;
        }
    
        String[] parts = message.split("\\s+");
        int numMessagesToDelete = 100; // Default to 100 messages
    
        if (parts.length > 1) {
            try {
                numMessagesToDelete = Integer.parseInt(parts[1]);
                numMessagesToDelete = Math.min(numMessagesToDelete, 100); // Limit to 100 messages
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid number format.").queue();
                return;
            }
        }
    
        MessageHistory history = event.getChannel().getHistory();
        history.retrievePast(numMessagesToDelete).queue(messages -> {
            if (messages == null || messages.isEmpty()) {
                event.getChannel().sendMessage("No messages to delete.").queue();
                return;
            }
    
            event.getChannel().deleteMessages(messages).queue(
                success -> event.getChannel().sendMessage("Deleted " + messages.size() + " messages.").queue(),
                error -> event.getChannel().sendMessage("Error deleting messages.").queue()
            );
        });
    }
    
    private void handleWeatherCommand(GuildMessageReceivedEvent event, String message) {
        // Split the message to get the city name
        String[] parts = message.split("\\s+", 2);
        if (parts.length < 2) {
            event.getChannel().sendMessage("Please specify a city name.").queue();
            return;
        }
    
        String city = parts[1].trim();
        try {
            // Use WeatherService to get weather info
            String weatherInfo = WeatherService.getWeather(city);
            if (weatherInfo != null) {
                event.getChannel().sendMessage(weatherInfo).queue();
            } else {
                event.getChannel().sendMessage("Weather information is currently unavailable.").queue();
            }
        } catch (Exception e) {
            // Log the exception and send a generic error message
            e.printStackTrace();
            event.getChannel().sendMessage("An error occurred while fetching weather information.").queue();
        }
    }    

    private void handleTriviaCommand(GuildMessageReceivedEvent event) {
        long channelId = event.getChannel().getIdLong();
        if (ongoingGames.containsKey(channelId)) {
            event.getChannel().sendMessage("There's already a trivia game ongoing in this channel!").queue();
            return;
        }

        event.getChannel().sendMessage("Starting a new trivia game! Please enter the trivia category and number of questions in the format: category,number (e.g., 'Science,5')").queue();
        waitingForTriviaSettings.put(channelId, true);
    }

    private boolean isWaitingForTriviaSettings(long channelId) {
        return waitingForTriviaSettings.getOrDefault(channelId, false);
    }
    
    private void processTriviaSettings(GuildMessageReceivedEvent event, String message) {
        String[] parts = message.split(",", 2);
        if (parts.length == 2) {
            try {
                String category = parts[0].trim();
                int numberOfQuestions = Integer.parseInt(parts[1].trim());
                
                // Additional parameters
                String difficulty = "medium"; // or extract from user input
                String type = "multiple"; // or extract from user input
    
                List<TriviaQuestions> questions = TriviaService.getTriviaQuestions(category, numberOfQuestions, difficulty, type);
                
                if (questions == null || questions.isEmpty()) {
                    event.getChannel().sendMessage("No trivia questions found for the specified settings.").queue();
                    return;
                }
        
                TriviaGame triviaGame = new TriviaGame(questions, event.getChannel());
                ongoingGames.put(event.getChannel().getIdLong(), triviaGame);
                triviaGame.start();
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Please enter a valid number format.").queue();
            }
        } else {
            event.getChannel().sendMessage("Invalid format. Please enter in the format: category,number").queue();
        }
    }

    private class TriviaGame {
        private AtomicInteger currentQuestionIndex = new AtomicInteger(0);
        private MessageChannel channel;
        private List<TriviaQuestions> questions;
        private ConcurrentHashMap<User, Integer> scores = new ConcurrentHashMap<>();
        private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        private ConcurrentHashMap<User, Long> responseTimes = new ConcurrentHashMap<>();

        public TriviaGame(List<TriviaQuestions> questions, MessageChannel channel) {
            this.questions = questions;
            this.channel = channel;
            this.currentQuestionIndex = new AtomicInteger(0);
            this.scores = new ConcurrentHashMap<>();
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            this.responseTimes = new ConcurrentHashMap<>();
        }

        public void start() {
            if (questions.isEmpty()) {
                channel.sendMessage("No trivia questions found for the specified category.").queue();
                ongoingGames.remove(channel.getIdLong());
                return;
            }
            askNextQuestion();
        }

        private void askNextQuestion() {
            if (currentQuestionIndex.get() >= questions.size()) {
                showLeaderboard();
                ongoingGames.remove(channel.getIdLong());
                executorService.shutdown();
                return;
            }

            TriviaQuestions currentQuestion = questions.get(currentQuestionIndex.getAndIncrement());
            StringBuilder message = new StringBuilder(currentQuestion.getQuestion() + "\n");
            List<String> answers = currentQuestion.getAnswers();
            for (int i = 0; i < answers.size(); i++) {
                message.append((i + 1) + ". " + answers.get(i) + "\n");
            }

            String messageToSend = message.toString();
            if (messageToSend != null) { // Null check added
                channel.sendMessage(messageToSend).queue();
            } else {
                // Handle the unlikely case of message being null
                System.out.println("Error: Message to send is null");
            }

            scheduleQuestionTimeout();
        }

        private void scheduleQuestionTimeout() {
            executorService.schedule(() -> {
                channel.sendMessage("Time's up!").queue();
                awardPoints();
                askNextQuestion();
            }, 10, TimeUnit.SECONDS);
        }

        private void awardPoints() {
            AtomicInteger position = new AtomicInteger(1);
            responseTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    User user = entry.getKey();
                    int pointsAwarded;
                    switch (position.getAndIncrement()) {
                        case 1:
                            pointsAwarded = 4;
                            break;
                        case 2:
                            pointsAwarded = 3;
                            break;
                        case 3:
                            pointsAwarded = 2;
                            break;
                        default:
                            pointsAwarded = 1;
                            break;
                    }
                    scores.merge(user, pointsAwarded, (a, b) -> Integer.valueOf(a + b));
                });
            responseTimes.clear(); // Clear response times for the next question
        }

        private void showLeaderboard() {
            StringBuilder leaderboard = new StringBuilder("Trivia Game Over! Leaderboard:\n");
            scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    User user = entry.getKey();
                    Integer points = entry.getValue();
                    leaderboard.append(user.getName()).append(": ").append(points).append(" points\n");
            });

            String leaderboardMessage = leaderboard.toString();
            if (leaderboardMessage != null) {
                channel.sendMessage(leaderboardMessage).queue();
            } else {
                // Handle the unlikely case of leaderboard being null
                System.out.println("Error: Leaderboard message is null");
            }
            scores.clear(); // Clear scores for the next game
        }

        public void handleResponse(User user, String message) {
            TriviaQuestions currentQuestion = questions.get(currentQuestionIndex.get() - 1);
        
            try {
                int answerIndex = Integer.parseInt(message.trim()) - 1;
                if (answerIndex >= 0 && answerIndex < currentQuestion.getAnswers().size()) {
                    String selectedAnswer = currentQuestion.getAnswers().get(answerIndex);
                    if (currentQuestion.isCorrectAnswer(selectedAnswer)) {
                        long responseTime = System.currentTimeMillis(); // Record response time
                        responseTimes.putIfAbsent(user, responseTime); // Only record the first response
                    } else {
                        // Optionally notify the user if their answer is incorrect
                        channel.sendMessage(user.getAsMention() + ", your answer is incorrect.").queue();
                    }
                } else {
                    // Answer index is out of range
                    channel.sendMessage(user.getAsMention() + ", please provide a valid answer number.").queue();
                }
            } catch (NumberFormatException e) {
                // Handle invalid answer format (not a number)
                channel.sendMessage(user.getAsMention() + ", please answer with the number of the option (e.g., '1' for the first option).").queue();
            }
        }
        
    }

    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        String channelId = "1060152546775277588"; // Your specific channel ID
        net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel != null) {
            channel.sendMessage("Welcome to the server, " + event.getUser().getAsMention() + "!" + " Please read the rules and have fun!").queue();
        } else {
            System.out.println("Channel not found.");
        }
    }
}
