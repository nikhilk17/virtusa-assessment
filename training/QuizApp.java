import java.util.*;
import java.io.*;
import java.util.concurrent.*;

// question class
class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    String question;
    String[] options;
    int correctAnswer; // index (0-3)

    public Question(String question, String[] options, int correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public void display() {
        System.out.println("\n" + question);
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
    }
}

// quiz system - handles all the quiz logic
class QuizSystem {
    private ArrayList<Question> questions = new ArrayList<>();

    // one reader for all input so scanner and bufferedreader dont fight
    private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    // queue for passing input lines between threads
    private final LinkedBlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

    // background thread that reads input - start this once at the beginning
    public void startReaderThread() {
        Thread readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    inputQueue.put(line);
                }
            } catch (Exception e) {
                // stream closed or something, just stop
            }
        });
        readerThread.setDaemon(true); // won't block JVM exit
        readerThread.start();
    }

    // reads input with no timeout - for menu and stuff
    public String readMenuInput() throws InterruptedException {
        return inputQueue.take(); // just waits
    }

    // same but with timeout for quiz questions
    private String readLineWithTimeout(long timeoutMillis) throws InterruptedException {
        return inputQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    // load saved questions from file
    public void loadQuestions() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("questions.dat"))) {
            questions = (ArrayList<Question>) ois.readObject();
            System.out.println("Loaded " + questions.size() + " question(s).");
        } catch (Exception e) {
            System.out.println("No existing questions found. Start adding!");
        }
    }

    public void saveQuestions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("questions.dat"))) {
            oos.writeObject(questions);
        } catch (IOException e) {
            System.out.println("Error saving questions!");
        }
    }

    // admin adds a new question
    public void addQuestion() {
        try {
            System.out.print("Enter question: ");
            String q = readMenuInput();

            String[] options = new String[4];
            for (int i = 0; i < 4; i++) {
                System.out.print("Option " + (i + 1) + ": ");
                options[i] = readMenuInput();
            }

            int correct = -1;
            while (correct < 0 || correct > 3) {
                System.out.print("Enter correct option (1-4): ");
                try {
                    correct = Integer.parseInt(readMenuInput().trim()) - 1;
                    if (correct < 0 || correct > 3) {
                        System.out.println("Please enter a number between 1 and 4.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }

            questions.add(new Question(q, options, correct));
            saveQuestions();
            System.out.println("Question added!");

        } catch (InterruptedException e) {
            System.out.println("Input interrupted.");
        }
    }

    // start the quiz - each question has a timer
    public void startQuiz() {
        if (questions.isEmpty()) {
            System.out.println("No questions yet! Add some first.");
            return;
        }

        int score = 0;
        int answered = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("\n--- Question " + (i + 1) + " of " + questions.size() + " ---");
            q.display();
            System.out.println("You have 10 seconds to answer.");
            System.out.print("Your answer (1-4): ");

            int ans = -1;
            try {
                String input = readLineWithTimeout(10_000); // 10 seconds per question

                if (input == null) {
                    System.out.println("\nTime's up! Moving to next question.");
                    continue;
                }

                ans = Integer.parseInt(input.trim());

            } catch (NumberFormatException e) {
                System.out.println("Invalid input — skipping question.");
                continue;
            } catch (InterruptedException e) {
                System.out.println("Quiz interrupted.");
                break;
            }

            if (ans < 1 || ans > 4) {
                System.out.println("Answer must be 1-4 — skipping question.");
                continue;
            }

            answered++;
            if (ans - 1 == q.correctAnswer) {
                System.out.println("Correct!");
                score++;
            } else {
                System.out.println("Wrong! Correct answer: " + q.options[q.correctAnswer]);
            }
        }

        showResult(score, answered);
    }

    // show how they did
    public void showResult(int score, int attempted) {
        int total = questions.size();
        System.out.println("\n======= Result =======");
        System.out.println("Attempted : " + attempted + "/" + total);
        System.out.println("Score     : " + score + "/" + total);

        if (attempted == 0) {
            System.out.println("No questions were answered.");
            return;
        }

        double percentage = (score * 100.0) / total;
        System.out.printf("Percentage: %.1f%%%n", percentage);

        if (percentage >= 80) System.out.println("Excellent!");
        else if (percentage >= 50) System.out.println("Good Job!");
        else System.out.println("Needs Improvement");
    }
}

// main
public class QuizApp {
    public static void main(String[] args) {
        QuizSystem quiz = new QuizSystem();
        quiz.loadQuestions();

        // start input reader thread first
        quiz.startReaderThread();

        while (true) {
            System.out.println("\n==== Quiz System ====");
            System.out.println("1. Admin: Add Question");
            System.out.println("2. Start Quiz");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            int choice = -1;
            try {
                String line = quiz.readMenuInput(); // blocking, no timeout
                choice = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            } catch (InterruptedException e) {
                System.out.println("Interrupted. Exiting.");
                return;
            }

            switch (choice) {
                case 1: quiz.addQuestion();  break;
                case 2: quiz.startQuiz();    break;
                case 3:
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}