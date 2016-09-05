import java.util.Collections;
import java.util.List;

class DataSet {
    private final List<String> attributeNames;
    private final List<List<Boolean>> observations;
    private final List<Boolean> labels;

    DataSet(List<String> attributeNames, List<List<Boolean>> observations, List<Boolean> labels) {
        this.attributeNames = Collections.unmodifiableList(attributeNames);
        this.observations = Collections.unmodifiableList(observations);
        this.labels = Collections.unmodifiableList(labels);
    }

    List<List<Boolean>> getObservations() {
        return observations;
    }

    List<Boolean> getLabels() {
        return labels;
    }

    List<String> getAttributeNames() {
        return attributeNames;
    }
}
