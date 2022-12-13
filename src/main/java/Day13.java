import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Day13 {
    public static void main(String[] args) throws IOException {
        Object divider1 = List.of(List.of(2));
        Object divider2 = List.of(List.of(6));

        List<Object> packets = new ArrayList<>();
        packets.add(divider1);
        packets.add(divider2);

        int index = 1;
        int sum = 0;

        try (var r = Files.newBufferedReader(Paths.get("day13_input.txt"))) {
            do {
                Object packet1 = parse(r.readLine());
                Object packet2 = parse(r.readLine());

                packets.add(packet1);
                packets.add(packet2);

                int result = compare(packet1, packet2);

                System.out.println(packet1);
                System.out.println(packet2);
                System.out.println("--> " + result);
                if (result < 0) {
                    sum += index;
                }
                index++;
            } while (r.readLine() != null);
        }

        System.out.println("sum = " + sum);

        packets.sort(Day13::compare);
        int indexDivider1 = packets.indexOf(divider1);
        int indexDivider2 = packets.indexOf(divider2);
        System.out.println("decoder key = " + ((indexDivider1 + 1) * (indexDivider2 + 1)));
    }

    private static int compare(Object o1, Object o2) {
        if (o1 instanceof List<?>) {
            if (o2 instanceof List<?>) {
                return compare((List<?>) o1, (List<?>) o2);
            } else {
                return compare((List<?>) o1, List.of((Integer) o2));
            }
        } else if (o2 instanceof List<?>) {
            return compare(List.of((Integer) o1), (List<?>) o2);
        } else {
            return compare((Integer) o1, (Integer) o2);
        }
    }

    private static int compare(Integer i1, Integer i2) {
        return i1.compareTo(i2);
    }

    private static int compare(List<?> l1, List<?> l2) {
        Iterator<?> i1 = l1.iterator();
        Iterator<?> i2 = l2.iterator();
        while(i1.hasNext() && i2.hasNext()) {
            int result = compare(i1.next(), i2.next());
            if (result != 0) {
                return result;
            }
        }

        if (i1.hasNext()) {
            return 1;
        } else if (i2.hasNext()) {
            return -1;
        } else {
            return 0;
        }
    }

    private static Object parse(String line) throws IOException {
        return parseValue(new PushbackReader(new StringReader(line)));
    }

    private static Object parseValue(PushbackReader r) throws IOException {
        int c = r.read();
        if (c == '[') {
            r.unread(c);
            return parseList(r);
        } else if (Character.isDigit(c)) {
            r.unread(c);
            return parseInteger(r);
        } else {
            throw new IOException("Unexpected character: " + (char)c);
        }
    }

    private static Object parseInteger(PushbackReader r) throws IOException {
        StringBuilder b = new StringBuilder();
        int c;
        while((c = r.read()) != -1 && Character.isDigit(c)) {
            b.append((char)c);
        }
        r.unread(c);
        return Integer.parseInt(b, 0, b.length(), 10);
    }

    private static Object parseList(PushbackReader r) throws IOException {
        int c = r.read();
        if (c != '[') {
            throw new IOException("Unexpected character: " + (char)c);
        }

        List<Object> list = new ArrayList<>();

        c = r.read();

        if (c != ']') {
            r.unread(c);
            do {
                list.add(parseValue(r));
                c = r.read();
            } while (c == ',');
        }

        if (c != ']') {
            throw new IOException("Unexpected character: " + (char)c);
        }

        return list;
    }
}
