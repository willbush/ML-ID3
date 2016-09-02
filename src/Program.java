import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Program {
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

    static String getSetFromFileAsString(String path) {
        return convertSetToString(getSetFromFile(path));
    }

    static String convertSetToString(List<Boolean[]> set) {
        StringBuilder sb = new StringBuilder();

        for (Boolean[] elements : set) {
            for (int i = 0; i < elements.length; ++i) {
                sb.append(elements[i] ? "1" : "0");
                if (i != elements.length - 1)
                    sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void printProgramUsage() {
        String usage = "The program requires two arguments that specify the path" +
                " to the training and test data.\n" +
                "For example:\n" +
                "java Program \"/dataSet/train.dat\" \"/dataSet/test.dat\"";
        System.out.println(usage);
    }

    private static List<Boolean[]> getSetFromFile(String path) {
        List<Boolean[]> result = new LinkedList<>();
        File f = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            readClassAttributes(br);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] words = line.split(WHITE_SPACE_REGEX);
                    Boolean[] row = new Boolean[words.length];
                    for (int i = 0; i < words.length; ++i) {
                        row[i] = words[i].equals("1");
                    }
                    result.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void readClassAttributes(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null && line.isEmpty()) {
            //TODO set attributes.
        }
    }

    static double calcEntropy(int classACount, int classBCount) {
        final int setTotalCount = classACount + classBCount;
        final double probabilityA = (double) classACount / setTotalCount;
        final double probabilityB = (double) classBCount / setTotalCount;

        final double A = (classACount == 0) ? 0.0 : probabilityA * log2(probabilityA);
        final double B = (classBCount == 0) ? 0.0 : probabilityB * log2(probabilityB);

        return -A - B;
    }

    private static double log2(double d) {
        return Math.log(d) / Math.log(2);
    }

    static double calcInfoGain(List<Boolean[]> set) {
        return 0;
    }
}
