import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Day14 {
    public static void main(String[] args) throws IOException {
        List<List<Point>> rockPaths = new ArrayList<>();

        try (var r = Files.newBufferedReader(Paths.get("day14_input.txt"))) {
            String line;
            while((line = r.readLine()) != null) {
                rockPaths.add(Arrays.stream(line.split(" -> "))
                        .map(p -> {
                            String[] coords = p.split(",");
                            return new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                        })
                        .toList());
            }
        }

        int minX = rockPaths.stream().flatMap(Collection::stream).mapToInt(p -> p.x).min().orElse(0);
        int maxX = rockPaths.stream().flatMap(Collection::stream).mapToInt(p -> p.x).max().orElse(0);
        int maxY = rockPaths.stream().flatMap(Collection::stream).mapToInt(p -> p.y).max().orElse(0);
        minX -= maxY * 2;
        maxX += maxY * 2;
        rockPaths.add(List.of(new Point(0, maxY + 2), new Point(maxX, maxY + 2)));
        maxY += 2;

        BufferedImage i = new BufferedImage(maxX - minX, maxY + 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = i.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, i.getWidth(), i.getHeight());

        g.setColor(Color.BLACK);
        for (List<Point> rockPath : rockPaths) {
            for (int j = 1; j < rockPath.size(); j++) {
                Point p1 = rockPath.get(j - 1);
                Point p2 = rockPath.get(j);
                g.drawLine(p1.x - minX, p1.y, p2.x - minX, p2.y);
            }
        }
        g.dispose();

        ImageComponent imageComponent = new ImageComponent(i, 4);

        JFrame f = new JFrame();
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(imageComponent, BorderLayout.CENTER);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);

        SimulateSand simulation = new SimulateSand(i, new Point(500 - minX, 0));
        int timerDelay = 10;
        Timer t = new Timer(timerDelay, e -> {
            if (simulation.tick()) {
                imageComponent.invalidate();
                imageComponent.repaint();
            } else {
                ((Timer) e.getSource()).stop();
                System.out.println("grainCount = " + simulation.grainCount);
            }
        });
        t.setInitialDelay(timerDelay);
        t.setRepeats(true);
        t.setCoalesce(false);
        t.start();
    }

    private static class ImageComponent extends JComponent {

        private final BufferedImage image;

        public ImageComponent(BufferedImage image, int scaleFactor) {
            this.image = image;
            setPreferredSize(new Dimension(image.getWidth() * scaleFactor, image.getHeight() * scaleFactor));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }

    private static class SimulateSand {

        private final BufferedImage grid;
        private final Graphics2D g;
        private final Point entryPoint;
        private final Color sandColor;
        private final Color airColor;

        private int grainCount;
        private Point currentGrain;

        public SimulateSand(BufferedImage grid, Point entryPoint) {
            this.grid = grid;
            g = grid.createGraphics();
            this.entryPoint = entryPoint;
            sandColor = Color.ORANGE;
            airColor = Color.WHITE;
        }

        public boolean tick() {
            do {
                if (!innerTick()) {
                    return false;
                }
            } while (currentGrain != null);

            return true;
        }

        public boolean innerTick() {
            if (currentGrain == null) {
                currentGrain = new Point(entryPoint);
                if (grid.getRGB(currentGrain.x, currentGrain.y) == airColor.getRGB()) {
                    g.setColor(sandColor);
                    g.fillRect(currentGrain.x, currentGrain.y, 1, 1);
                    return true;
                } else {
                    return false;
                }
            } else {
                Point next = null;
                try {
                    if (grid.getRGB(currentGrain.x, currentGrain.y + 1) == airColor.getRGB()) {
                        next = new Point(currentGrain.x, currentGrain.y + 1);
                    } else if (grid.getRGB(currentGrain.x - 1, currentGrain.y + 1) == airColor.getRGB()) {
                        next = new Point(currentGrain.x - 1, currentGrain.y + 1);
                    } else if (grid.getRGB(currentGrain.x + 1, currentGrain.y + 1) == airColor.getRGB()) {
                        next = new Point(currentGrain.x + 1, currentGrain.y + 1);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }

                if (next != null) {
                    g.setColor(airColor);
                    g.fillRect(currentGrain.x, currentGrain.y, 1, 1);

                    g.setColor(sandColor);
                    g.fillRect(next.x, next.y, 1, 1);
                    currentGrain = next;
                    return true;
                } else {
                    grainCount++;
                    currentGrain = null;
                }
                return true;
            }
        }
    }
}
