package com.dgf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriviaQuestionsTest {

    private TriviaQuestions question;
    private String correctAnswer;
    private List<String> incorrectAnswers;

    @BeforeEach
    void setUp() {
        correctAnswer = "Correct Answer";
        incorrectAnswers = Arrays.asList("Wrong 1", "Wrong 2", "Wrong 3");
        question = new TriviaQuestions("Test Question?", correctAnswer, incorrectAnswers);
    }

    @Test
    void testIsCorrectAnswer() {
        assertTrue(question.isCorrectAnswer(correctAnswer));
    }

    @Test
    void testIsIncorrectAnswer() {
        for (String incorrectAnswer : incorrectAnswers) {
            assertFalse(question.isCorrectAnswer(incorrectAnswer));
        }
    }

    @Test
    void testShuffledAnswers() {
        // This test checks if the answers are shuffled by seeing if they are not in the original order
        List<String> answers = question.getAnswers();
        boolean shuffled = !answers.get(answers.size() - 1).equals(correctAnswer);
        assertTrue(shuffled);
    }

    @Test
    void testNullAnswer() {
        assertFalse(question.isCorrectAnswer(null));
    }

    @Test
    void testEmptyAnswer() {
        assertFalse(question.isCorrectAnswer(""));
    }

    @Test
    void testGetAnswers() {
        List<String> answers = question.getAnswers();
        assertTrue(answers.containsAll(incorrectAnswers));
        assertTrue(answers.contains(correctAnswer));
    }
    
   @Test
    void testDuplicateAnswers() {
        TriviaQuestions duplicateQuestion = new TriviaQuestions("Duplicate Test?", "Duplicate", List.of("Duplicate", "Option 2", "Option 3"));
        assertTrue(duplicateQuestion.getAnswers().contains("Duplicate"));
    }

    @Test
    void testAllIncorrectAnswers() {
        TriviaQuestions allIncorrect = new TriviaQuestions("All Incorrect?", "Correct", Collections.emptyList());
        assertTrue(allIncorrect.isCorrectAnswer("Correct"));
        assertFalse(allIncorrect.isCorrectAnswer("Incorrect"));
    }

    @Test
    void testNoIncorrectAnswers() {
        TriviaQuestions noIncorrect = new TriviaQuestions("No Incorrect?", "Only Answer", Collections.emptyList());
        assertEquals(1, noIncorrect.getAnswers().size());
        assertTrue(noIncorrect.isCorrectAnswer("Only Answer"));
    }

    @Test
    void testSpecialCharactersInAnswers() {
        TriviaQuestions specialCharQuestion = new TriviaQuestions("Special Char?", "Answer!", List.of("Answer?", "Answer."));
        assertTrue(specialCharQuestion.isCorrectAnswer("Answer!"));
        assertFalse(specialCharQuestion.isCorrectAnswer("Answer?"));
    }

    @Test
    void testAnswerOrderPreservation() {
        TriviaQuestions orderPreservationQuestion = new TriviaQuestions("Order Test?", "Correct", List.of("First", "Second", "Third"));
        List<String> answers = orderPreservationQuestion.getAnswers();
        boolean orderPreserved = answers.indexOf("First") < answers.indexOf("Second") && answers.indexOf("Second") < answers.indexOf("Third");
        assertTrue(orderPreserved);
    }
}
