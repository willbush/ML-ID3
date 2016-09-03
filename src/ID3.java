import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ID3 {
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
                "java ID3 \"/dataSet/train.dat\" \"/dataSet/test.dat\"";
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

    public int determineIndexOfSplit(List<Boolean[]> spamSet) {
        double[] infoGains = calcInfoGain(spamSet);
        int splitIndex = 0;

        double previous = 0;
        for (int i = 0; i < infoGains.length; ++i) {
            if (infoGains[i] > previous) {
                splitIndex = i;
                previous = infoGains[i];
            }
        }
        return splitIndex;
    }

    double[] calcInfoGain(List<Boolean[]> set) {
        double[] conditionalEntropies = calcConditionalEntropies(set);
        double setEntropy = calcEntropy(set);
        double[] infoGains = new double[conditionalEntropies.length];

        for (int i = 0; i < infoGains.length; ++i)
            infoGains[i] = setEntropy - conditionalEntropies[i];

        return infoGains;
    }

    double[] calcConditionalEntropies(List<Boolean[]> set) {
        final int classIndex = set.get(0).length - 1;
        final int attributeLen = classIndex;
        int[] classTrueCountOnTrueBranch = new int[attributeLen];
        int[] classFalseCountOnTrueBranch = new int[attributeLen];
        int[] classTrueCountOnFalseBranch = new int[attributeLen];
        int[] classFalseCountOnFalseBranch = new int[attributeLen];

        for (Boolean[] booleans : set) {
            for (int i = 0; i < attributeLen; ++i) {
                if (booleans[i] && booleans[classIndex])
                    classTrueCountOnTrueBranch[i] += 1;
                else if (booleans[i] && !booleans[classIndex])
                    classFalseCountOnTrueBranch[i] += 1;
                else if (!booleans[i] && booleans[classIndex])
                    classTrueCountOnFalseBranch[i] += 1;
                else if (!booleans[i] && !booleans[classIndex])
                    classFalseCountOnFalseBranch[i] += 1;
            }
        }
        double[] conditionalEntropies = new double[attributeLen];

        for (int i = 0; i < attributeLen; ++i) {
            conditionalEntropies[i] = getWeightedAverage(set.size(), classTrueCountOnFalseBranch[i], classFalseCountOnFalseBranch[i])
                    + getWeightedAverage(set.size(), classTrueCountOnTrueBranch[i], classFalseCountOnTrueBranch[i]);
        }
        return conditionalEntropies;
    }

    private double getWeightedAverage(double setSize, int trueCount, int falseCount) {
        return ((trueCount + falseCount) / setSize) * calcEntropy(trueCount, falseCount);
    }

    private double calcEntropy(List<Boolean[]> set) {
        final int classIndex = set.get(0).length - 1;
        int trueCount = 0;

        for (Boolean[] booleans : set)
            if (booleans[classIndex])
                trueCount += 1;

        return calcEntropy(trueCount, set.size() - trueCount);
    }

    double calcEntropy(int classACount, int classBCount) {
        final int setTotalCount = classACount + classBCount;
        final double probabilityA = (double) classACount / setTotalCount;
        final double probabilityB = (double) classBCount / setTotalCount;

        final double A = (classACount == 0) ? 0.0 : probabilityA * log2(probabilityA);
        final double B = (classBCount == 0) ? 0.0 : probabilityB * log2(probabilityB);

        return -A - B;
    }

    private double log2(double d) {
        return Math.log(d) / Math.log(2);
    }

    Node split(List<Boolean[]> set, int index) {
        List<Boolean[]> left = new LinkedList<>();
        List<Boolean[]> right = new LinkedList<>();

        for (Boolean[] booleans : set) {
            if (booleans[index])
                right.add(removeElement(index, booleans));
            else
                left.add(removeElement(index, booleans));
        }
        return new Node(right, left);
    }

    Boolean[] removeElement(int elementIndex, Boolean[] booleans) {
        Boolean[] result = new Boolean[booleans.length - 1];
        int j = 0;
        for (int i = 0; i < result.length; ++i, ++j) {
            if (j != elementIndex)
                result[i] = booleans[j];
            else
                --i;
        }

        return result;
    }

    class Node {
        private final List<Boolean[]> left;
        private final List<Boolean[]> right;

        Node(List<Boolean[]> left, List<Boolean[]> right) {
            this.left = Collections.unmodifiableList(left);
            this.right = Collections.unmodifiableList(right);
        }

        List<Boolean[]> getLeft() {
            return left;
        }

        List<Boolean[]> getRight() {
            return right;
        }
    }
}
