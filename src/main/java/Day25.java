import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day25 {
    private static long snafuToDecimal(String snafu) {
        long decimal = 0;
        long multiplier = 1;
        char[] chars = snafu.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            decimal += multiplier * switch (c) {
                case '=' -> -2;
                case '-' -> -1;
                case '0' -> 0;
                case '1' -> 1;
                case '2' -> 2;
                default -> throw new IllegalArgumentException();
            };
            multiplier *= 5;
        }

        return decimal;
    }

    private static String decimalToSnafu(long decimal) {
        StringBuilder b = new StringBuilder();
        while (decimal != 0) {
            int modulo = (int) (decimal % 5);

            switch (modulo) {
                case 0:
                    b.insert(0, '0');
                    break;
                case 1:
                    b.insert(0, '1');
                    break;
                case 2:
                    b.insert(0, '2');
                    break;
                case 3:
                    b.insert(0, '=');
                    decimal += 5;
                    break;
                case 4:
                    b.insert(0, '-');
                    decimal += 5;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            decimal /= 5;
        }
        return b.toString();
    }

    public static void main(String[] args) throws IOException {
        long sum = Files.readAllLines(Paths.get("day25_input.txt"))
                .stream()
                .mapToLong(Day25::snafuToDecimal)
                .sum();
        System.out.println(decimalToSnafu(sum));
    }
}
