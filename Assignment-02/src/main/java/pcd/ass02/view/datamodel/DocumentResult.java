package pcd.ass02.view.datamodel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class DocumentResult {

    private final SimpleStringProperty documentName;
    private final SimpleLongProperty occurrences;

    public DocumentResult(String documentName, long occurrences){
        this.documentName = new SimpleStringProperty(documentName);
        this.occurrences = new SimpleLongProperty(occurrences);
    }

    public String getDocumentName() {
        return documentName.get();
    }

    public void setDocumentName(String documentName) {
        this.documentName.set(documentName);
    }

    public long getOccurrences() {
        return occurrences.get();
    }

    public void setOccurrences(int occurrences) {
        this.occurrences.set(occurrences);
    }
}
