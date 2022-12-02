import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day2 {
    public enum Shape {
        ROCK, PAPER, SCISSORS;

        public Shape shapeForOutcome(Outcome desiredOutcome) {
            Shape[] values = Shape.values();
            return switch (desiredOutcome) {
                case WIN -> values[(ordinal() + 1) % values.length];
                case LOSE -> values[(values.length + ordinal() - 1) % values.length];
                case DRAW -> this;
            };
        }

        public Outcome outcomeForShape(Shape otherShape) {
            if (shapeForOutcome(Outcome.WIN) == otherShape) {
                return Outcome.WIN;
            } else if (shapeForOutcome(Outcome.LOSE) == otherShape) {
                return Outcome.LOSE;
            } else {
                return Outcome.DRAW;
            }
        }

        public int score() {
            return switch (this) {
                case ROCK -> 1;
                case PAPER -> 2;
                case SCISSORS -> 3;
            };
        }
    }

    public enum Outcome {
        WIN, LOSE, DRAW;

        public int score() {
            return switch (this) {
                case WIN -> 6;
                case DRAW -> 3;
                case LOSE -> 0;
            };
        }
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
        return draw.myShape.score() + draw.otherShape.outcomeForShape(draw.myShape).score();
    }

    public static Draw gameToDraw(Game game) {
        return new Draw(
                game.otherShape,
                game.otherShape.shapeForOutcome(game.outcome)
        );
    }
}
