package pcd.ass02.domain;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Document {

    private final String name;
    private final List<String> lines;

    public Document(String name, List<String> lines) {
        this.lines = lines;
        this.name = name;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getName() {
        return name;
    }

    public static Document fromFile(File file) {
        final List<String> lines = readLines(file);
        return new Document(file.getName(), lines);
    }

    private static List<String> readLines(File file) {
        final List<String> lines = new LinkedList<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            return lines;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
