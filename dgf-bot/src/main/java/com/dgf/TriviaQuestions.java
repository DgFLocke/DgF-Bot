package com.dgf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriviaQuestions {
    private String question;
    private List<String> answers; // Includes both correct and incorrect answers
    private int correctAnswerIndex; // Index of the correct answer in the 'answers' list

    // Constructor
    public TriviaQuestions(String question, String correctAnswer, List<String> incorrectAnswers) {
        this.question = question;
        this.answers = new ArrayList<>(incorrectAnswers);
        this.answers.add(correctAnswer); // Add correct answer to the list
        this.correctAnswerIndex = this.answers.size() - 1; // Correct answer is the last item

        Collections.shuffle(this.answers); // Shuffle the answers
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    // Method to check if a given answer is correct
    public boolean isCorrectAnswer(String answer) {
        return answer != null && answer.equals(answers.get(correctAnswerIndex));
    }
}
