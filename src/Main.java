import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final String WHITE_SPACE_REGEX = "\\s+";

    public static void main(String[] args) {
        if (args.length != 2) {
            printProgramUsage();
        }

        try {
            System.out.println(getSetFromFileAsString(args[0]));
            System.out.println(getSetFromFileAsString(args[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printProgramUsage() {
        String usage = "The program requires two arguments that specify the path" +
                " to the training and test data.\n" +
                "For example:\n" +
                "java ID3 \"/dataSet/train.dat\" \"/dataSet/test.dat\"";
        System.out.println(usage);
    }

    static String getSetFromFileAsString(String path) throws IOException {
        return convertSetToString(getSetFromFile(path));
    }

    static String convertSetToString(DataSet set) {
        StringBuilder sb = new StringBuilder();
        List<String> names = set.getAttributeNames();

        for (int i = 0; i < names.size(); ++i) {
            sb.append(names.get(i));
            if (i != names.size() - 1)
                sb.append(" ");
        }
        sb.append("\n");

        int labelIndex = 0;
        for (List<Boolean> elements : set.getObservations()) {
            for (int i = 0; i < elements.size(); ++i) {
                sb.append(elements.get(i) ? "1 " : "0 ");
                if (i == elements.size() - 1)
                    sb.append(set.getLabels().get(labelIndex) ? "1" : "0");
            }
            sb.append("\n");
            ++labelIndex;
        }
        return sb.toString();
    }

    static DataSet getSetFromFile(String path) throws IOException {
        List<String> attributeNames;
        List<List<Boolean>> observations = new LinkedList<>();
        List<Boolean> labels = new ArrayList<>();

        File f = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            final String fileEmptyError = "The file at the following given path is empty: " + path;
            attributeNames = readAttributesNames(br).orElseThrow(() -> new ID3.EmptyFileException(fileEmptyError));
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
}
