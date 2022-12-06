import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day6 {
    private static class ReadBuffer {
        private final char[] data;
        private int charsRead;
        private int position;

        private ReadBuffer(int size) {
            data = new char[size];
        }

        public void init(Reader r) throws IOException {
            int offset = 0;
            int remaining = data.length;
            while(remaining > 0) {
                int amountRead = r.read(data, offset, remaining);
                if (amountRead == -1) {
                    throw new EOFException();
                } else {
                    remaining -= amountRead;
                    offset += amountRead;
                }
            }

            charsRead = data.length;
            position = 0;
        }

        public void readChar(Reader r) throws IOException {
            int c = r.read();
            if (c == -1) {
                throw new EOFException();
            }

            data[position] = (char)c;
            position = (position + 1) % data.length;
            charsRead++;
        }

        public boolean isAtMarker() {
            for (int i = 0; i < data.length - 1; i++) {
                for (int j = i + 1; j < data.length; j++) {
                    if (data[i] == data[j]) {
                        return false;
                    }
                }
            }
            return true;
        }

        public int getCharsRead() {
            return charsRead;
        }
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("day6_input.txt"))) {
            ReadBuffer buffer = new ReadBuffer(14);
            buffer.init(reader);
            while(!buffer.isAtMarker()) {
                buffer.readChar(reader);
            }
            System.out.println(buffer.getCharsRead());
        }
    }
}
