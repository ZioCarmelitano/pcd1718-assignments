package pcd.ass02.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
        List<String> lines = new LinkedList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Document(file.getName(), lines);
    }
}
