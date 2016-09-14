import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class is data model of the data set. It contains helper functions for converting from / to a data set.
 * <p>
 * The data structures, while of interface type List, should be of actual type ArrayList for constant time indexing,
 * except in the case of the list of observations where constant time indexing does not matter. However, each list in
 * the observations should be ArrayList, again for constant time indexing. List are used instead of simple 2 dimensional
 * arrays because they are more flexible and give easy access to Java 8 streams.
 */
class DataSet {
    private static final String WHITE_SPACE_REGEX = "\\s+";
    // attribute names are names for attributes and their order is in the same as the attribute values order
    // in the observations.
    private final List<String> attributeNames;
    // observations (aka examples) is a list of observations where each has a list of attribute values.
    private final List<List<Boolean>> observations;
    // labels are an ordered list of class labels associated with each observation.
    private final List<Boolean> labels;

    /**
     * @param attributeNames attribute names associated with the attribute values in the observations list.
     * @param observations   a list of observations (aka examples) where each contains a list of attribute values.
     * @param labels         class labels that are associated with each observation.
     * @throws IllegalArgumentException if size of attribute names does not equal observation size.
     *                                  if size of labels does not equal observation size.
     */
    DataSet(List<String> attributeNames, List<List<Boolean>> observations, List<Boolean> labels) {
        if (attributeNames.size() != observations.get(0).size())
            throw new IllegalArgumentException("There must exist an attribute name for each attribute.");
        if (labels.size() != observations.size())
            throw new IllegalArgumentException("There must exists a label for each observation.");

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

    /**
     * This is mostly a conveyance method for unit testing.
     *
     * @return a string similar to the format of the input data set files, but with out the
     * "class" label on the last column.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> names = attributeNames;

        for (int i = 0; i < names.size(); ++i) {
            sb.append(names.get(i));
            if (i != names.size() - 1)
                sb.append(" ");
        }
        sb.append("\n");

        int labelIndex = 0;
        for (List<Boolean> elements : observations) {
            for (int i = 0; i < elements.size(); ++i) {
                sb.append(elements.get(i) ? "1 " : "0 ");
                if (i == elements.size() - 1)
                    sb.append(labels.get(labelIndex) ? "1" : "0");
            }
            sb.append("\n");
            ++labelIndex;
        }
        return sb.toString();
    }

    /**
     * Converts data in a data set file into a data object.
     *
     * @param path path to the data set dat file.
     * @return a data set data object.
     * @throws IOException if file is not found or the reader throws an exception while performing readLine.
     */
    static DataSet fromFile(String path) throws IOException {
        List<String> attributeNames;
        List<List<Boolean>> observations = new LinkedList<>();
        List<Boolean> labels = new ArrayList<>();

        File f = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            final String fileEmptyError = "The file at the following given path is empty: " + path;
            attributeNames = readAttributesNames(br).orElseThrow(() -> new EmptyFileException(fileEmptyError));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] elements = line.split(WHITE_SPACE_REGEX);
                    List<Boolean> attributeValues = new ArrayList<>(elements.length - 1);

                    for (int i = 0; i < elements.length; ++i) {
                        boolean isOne = elements[i].equals("1");
                        if (i == elements.length - 1)
                            labels.add(isOne);
                        else
                            attributeValues.add(isOne);
                    }
                    observations.add(attributeValues);
                }
            }
        }
        return new DataSet(attributeNames, observations, labels);
    }

    /**
     * Moves past any white space and parsing the attribute names (excluding the "class" word in the last column).
     *
     * @param br a buffered reader
     * @return Attribute names (the names on the top of the columns) not including "class" in the last column.
     * @throws IOException if the reader throws an exception while performing readLine.
     */
    private static Optional<List<String>> readAttributesNames(BufferedReader br) throws IOException {
        String line;

        // move reader past blank lines.
        do {
            line = br.readLine();
        } while (line != null && line.trim().isEmpty());


        List<String> attributeNames = null;

        if (line != null) {
            String[] words = line.split(WHITE_SPACE_REGEX);
            attributeNames = Arrays.asList(Arrays.copyOfRange(words, 0, words.length - 1));
        }
        return Optional.ofNullable(attributeNames);
    }

    static class EmptyFileException extends IOException {
        EmptyFileException(String msg) {
            super(msg);
        }
    }
}
