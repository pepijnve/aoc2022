import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day2Test {
    @Test
    public void testInput() {
        assertEquals(
                new Day2.Game(Day2.Shape.ROCK, Day2.Outcome.DRAW),
                Day2.parseLine("A Y")
        );

        assertEquals(
                new Day2.Game(Day2.Shape.PAPER, Day2.Outcome.LOSE),
                Day2.parseLine("B X")
        );

        assertEquals(
                new Day2.Game(Day2.Shape.SCISSORS, Day2.Outcome.WIN),
                Day2.parseLine("C Z")
        );
    }

    @Test
    public void testScoreDraw() {
        checkScore(new Day2.Draw(Day2.Shape.ROCK, Day2.Shape.SCISSORS), 3 + 0);
        checkScore(new Day2.Draw(Day2.Shape.ROCK, Day2.Shape.PAPER), 2 + 6);
        checkScore(new Day2.Draw(Day2.Shape.ROCK, Day2.Shape.ROCK), 1 + 3);

        checkScore(new Day2.Draw(Day2.Shape.PAPER, Day2.Shape.SCISSORS), 3 + 6);
        checkScore(new Day2.Draw(Day2.Shape.PAPER, Day2.Shape.ROCK), 1 + 0);
        checkScore(new Day2.Draw(Day2.Shape.PAPER, Day2.Shape.PAPER), 2 + 3);

        checkScore(new Day2.Draw(Day2.Shape.SCISSORS, Day2.Shape.ROCK), 1 + 6);
        checkScore(new Day2.Draw(Day2.Shape.SCISSORS, Day2.Shape.PAPER), 2 + 0);
        checkScore(new Day2.Draw(Day2.Shape.SCISSORS, Day2.Shape.SCISSORS), 3 + 3);
    }

    @Test
    public void testGameToDraw() {
        checkGame(new Day2.Game(Day2.Shape.ROCK, Day2.Outcome.WIN), Day2.Shape.PAPER);
        checkGame(new Day2.Game(Day2.Shape.ROCK, Day2.Outcome.LOSE), Day2.Shape.SCISSORS);
        checkGame(new Day2.Game(Day2.Shape.ROCK, Day2.Outcome.DRAW), Day2.Shape.ROCK);

        checkGame(new Day2.Game(Day2.Shape.PAPER, Day2.Outcome.WIN), Day2.Shape.SCISSORS);
        checkGame(new Day2.Game(Day2.Shape.PAPER, Day2.Outcome.LOSE), Day2.Shape.ROCK);
        checkGame(new Day2.Game(Day2.Shape.PAPER, Day2.Outcome.DRAW), Day2.Shape.PAPER);

        checkGame(new Day2.Game(Day2.Shape.SCISSORS, Day2.Outcome.WIN), Day2.Shape.ROCK);
        checkGame(new Day2.Game(Day2.Shape.SCISSORS, Day2.Outcome.LOSE), Day2.Shape.PAPER);
        checkGame(new Day2.Game(Day2.Shape.SCISSORS, Day2.Outcome.DRAW), Day2.Shape.SCISSORS);
    }

    @Test
    public void testScore() {
        assertEquals(
                12,
                Day2.score("""
                        A Y
                        B X
                        C Z
                        """)
        );
    }

    private static void checkScore(Day2.Draw draw, int score) {
        assertEquals(
                score,
                Day2.score(draw)
        );
    }

    private static void checkGame(Day2.Game game, Day2.Shape myShape) {
        assertEquals(
                new Day2.Draw(game.otherShape(), myShape),
                Day2.gameToDraw(game)
        );
    }
}
