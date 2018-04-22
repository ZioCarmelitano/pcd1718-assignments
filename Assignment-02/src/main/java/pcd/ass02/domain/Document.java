package pcd.ass02.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Document {

    private String name;
    private final List<String> lines;

    public Document(List<String> lines, String name) {
        this.lines = lines;
        this.name = name;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getName() {
        return name;
    }

    public static Document fromFile(File file) throws IOException {
        List<String> lines = new LinkedList<String>();
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
        return new Document(lines, file.getName());
    }
}
