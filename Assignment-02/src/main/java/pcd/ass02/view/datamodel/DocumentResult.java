package pcd.ass02.view.datamodel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DocumentResult {

    private final SimpleStringProperty documentName;
    private final SimpleIntegerProperty occurrences;

    public DocumentResult(String documentName, int occurrences){
        this.documentName = new SimpleStringProperty(documentName);
        this.occurrences = new SimpleIntegerProperty(occurrences);
    }

    public String getDocumentName() {
        return documentName.get();
    }

    public void setDocumentName(String documentName) {
        this.documentName.set(documentName);
    }

    public int getOccurrences() {
        return occurrences.get();
    }

    public void setOccurrences(int occurrences) {
        this.occurrences.set(occurrences);
    }
}
