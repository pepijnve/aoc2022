import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day2 {
    public enum Shape {
        ROCK, PAPER, SCISSORS
    }

    public enum Outcome {
        WIN, LOSE, DRAW
    }

    public record Game(Shape otherShape, Outcome outcome) {
    }

    public record Draw(Shape otherShape, Shape myShape) {
    }

    public static void main(String[] args) throws IOException {
        String input = Files.readString(Paths.get("day2_input.txt"));
        System.out.println("score = " + score(input));
    }

    public static int score(String input) {
        return new BufferedReader(new StringReader(input)).lines()
                .peek(System.out::println)
                .map(Day2::parseLine)
                .peek(System.out::println)
                .map(Day2::gameToDraw)
                .peek(System.out::println)
                .mapToInt(Day2::score)
                .peek(System.out::println)
                .sum();
    }

    public static Game parseLine(String line) {
        String[] parts = line.split("\\s+");
        return new Game(switch (parts[0]) {
            case "A" -> Shape.ROCK;
            case "B" -> Shape.PAPER;
            case "C" -> Shape.SCISSORS;
            default -> throw new IllegalArgumentException("Invalid input: " + parts[0]);
        }, switch (parts[1]) {
            case "X" -> Outcome.LOSE;
            case "Y" -> Outcome.DRAW;
            case "Z" -> Outcome.WIN;
            default -> throw new IllegalArgumentException("Invalid input: " + parts[1]);
        });
    }

    public static int score(Draw draw) {
        return switch (draw.myShape) {
            case ROCK -> 1 + switch (draw.otherShape) {
                case ROCK -> 3;
                case PAPER -> 0;
                case SCISSORS -> 6;
            };
            case PAPER -> 2 + switch (draw.otherShape) {
                case ROCK -> 6;
                case PAPER -> 3;
                case SCISSORS -> 0;
            };
            case SCISSORS -> 3 + switch (draw.otherShape) {
                case ROCK -> 0;
                case PAPER -> 6;
                case SCISSORS -> 3;
            };
        };
    }

    public static Draw gameToDraw(Game game) {
        return new Draw(
                game.otherShape,
                switch (game.otherShape) {
                    case ROCK -> switch (game.outcome) {
                        case DRAW -> Shape.ROCK;
                        case WIN -> Shape.PAPER;
                        case LOSE -> Shape.SCISSORS;
                    };
                    case PAPER -> switch (game.outcome) {
                        case DRAW -> Shape.PAPER;
                        case WIN -> Shape.SCISSORS;
                        case LOSE -> Shape.ROCK;
                    };
                    case SCISSORS -> switch (game.outcome) {
                        case DRAW -> Shape.SCISSORS;
                        case WIN -> Shape.ROCK;
                        case LOSE -> Shape.PAPER;
                    };
                });
    }
}
