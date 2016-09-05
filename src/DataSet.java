import java.util.List;

class DataSet {
    private final List<String> attributeNames;
    private final List<List<Boolean>> observations;
    private final List<Boolean> labels;

    DataSet(List<String> attributeNames, List<List<Boolean>> observations, List<Boolean> labels) {
        this.attributeNames = attributeNames;
        this.observations = observations;
        this.labels = labels;
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
