import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day7 {
    private interface TreeVisitor {
        boolean visit(Directory dir);

        boolean visit(File file);
    }

    private static abstract class Node {
        private final String name;

        public Node(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public abstract int size();

        public abstract int totalSize();

        public abstract void accept(TreeVisitor visitor);

        public void printTree(PrintWriter writer) {
            printTree(0, writer);
        }

        protected abstract void printTree(int indent, PrintWriter writer);
    }

    private static class Directory extends Node {
        private final Map<String, Node> children;

        public Directory(String name) {
            super(name);
            this.children = new HashMap<>();
        }

        public void addChild(Node child) {
            children.put(child.name(), child);
        }

        @Override
        public void accept(TreeVisitor visitor) {
            if (visitor.visit(this)) {
                for (Node child : children.values()) {
                    child.accept(visitor);
                }
            }
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int totalSize() {
            return children.values().stream().mapToInt(Node::totalSize).sum();
        }

        public Node getChild(String childName) {
            return children.get(childName);
        }

        @Override
        protected void printTree(int indent, PrintWriter writer) {
            for (int i = 0; i < indent; i++) {
                writer.print("  ");
            }
            writer.print("- ");
            writer.print(name());
            writer.println(" (dir)");
            for (Node child : children.values()) {
                child.printTree(indent + 1, writer);
            }
        }
    }

    private static class File extends Node {
        private final int size;

        public File(String name, int size) {
            super(name);
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int totalSize() {
            return size;
        }

        @Override
        public void accept(TreeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        protected void printTree(int indent, PrintWriter writer) {
            for (int i = 0; i < indent; i++) {
                writer.print("  ");
            }
            writer.print("- ");
            writer.print(name());
            writer.print(" (file, size=");
            writer.print(size);
            writer.println(")");
        }
    }

    private static final Pattern CD = Pattern.compile("\\$ cd (?<target>.+)");
    private static final Pattern LS = Pattern.compile("\\$ ls");
    private static final Pattern LS_DIR = Pattern.compile("dir (?<name>.*)");
    private static final Pattern LS_FILE = Pattern.compile("(?<size>[0-9]+) (?<name>.*)");

    private static Directory rootDir;
    private static Deque<Directory> dirStack;

    public static void main(String[] args) throws IOException {
        rootDir = new Directory("/");
        dirStack = new ArrayDeque<>();
        dirStack.push(rootDir);

        try (BufferedReader in = Files.newBufferedReader(Paths.get("day7_input.txt"))) {
            String line = in.readLine();

            while (line != null) {
                Matcher match;

                match = CD.matcher(line);
                if (match.matches()) {
                    String target = match.group("target");
                    if (target.equals("/")) {
                        dirStack.clear();
                        dirStack.push(rootDir);
                    } else if (target.equals("..")) {
                        dirStack.pop();
                        if (dirStack.isEmpty()) {
                            dirStack.push(rootDir);
                        }
                    } else {
                        Node child = dirStack.peek().getChild(target);
                        dirStack.push((Directory) child);
                    }

                    line = in.readLine();
                    continue;
                }

                match = LS.matcher(line);
                if (match.matches()) {
                    while ((line = in.readLine()) != null) {
                        match = LS_DIR.matcher(line);
                        if (match.matches()) {
                            String name = match.group("name");
                            if (dirStack.peek().getChild(name) == null) {
                                dirStack.peek().addChild(new Directory(name));
                            }
                            continue;
                        }

                        match = LS_FILE.matcher(line);
                        if (match.matches()) {
                            String name = match.group("name");
                            int size = Integer.parseInt(match.group("size"));
                            dirStack.peek().addChild(new File(name, size));
                            continue;
                        }

                        break;
                    }
                    continue;
                }

                throw new IllegalArgumentException();
            }
        }

        PrintWriter writer = new PrintWriter(System.out);
        rootDir.printTree(writer);
        writer.flush();

        MyTreeVisitor visitor = new MyTreeVisitor();
        rootDir.accept(visitor);
        System.out.println("visitor.sum = " + visitor.sum);

        int totalSize = 70000000;
        int requiredSize = 30000000;
        int usedSize = rootDir.totalSize();
        int availableSize = totalSize - usedSize;
        int minSizeToDelete = requiredSize - availableSize;
        FindSmallestDirVisitor smallest = new FindSmallestDirVisitor(minSizeToDelete);
        rootDir.accept(smallest);
        System.out.println("smallest.minTotalSize = " + smallest.minTotalSize);
    }

    private static class MyTreeVisitor implements TreeVisitor {
        public int sum;

        @Override
        public boolean visit(Directory dir) {
            int totalSize = dir.totalSize();
            if (totalSize <= 100000) {
                sum += totalSize;
            }
            return true;
        }

        @Override
        public boolean visit(File file) {
            return false;
        }
    }

    private static class FindSmallestDirVisitor implements TreeVisitor {
        private final int minSize;
        public int minTotalSize;

        public FindSmallestDirVisitor(int minSize) {
            this.minTotalSize = Integer.MAX_VALUE;
            this.minSize = minSize;
        }

        @Override
        public boolean visit(Directory dir) {
            int totalSize = dir.totalSize();
            if (totalSize >= minSize) {
                if (totalSize < minTotalSize) {
                    minTotalSize = totalSize;
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean visit(File file) {
            return false;
        }
    }
}
