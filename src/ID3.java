import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    int determineIndexOfSplit(List<Boolean[]> spamSet) {
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

        for (Boolean[] bools : set) {
            for (int i = 0; i < attributeLen; ++i) {
                if (bools[i] && bools[classIndex])
                    classTrueCountOnTrueBranch[i] += 1;
                else if (bools[i] && !bools[classIndex])
                    classFalseCountOnTrueBranch[i] += 1;
                else if (!bools[i] && bools[classIndex])
                    classTrueCountOnFalseBranch[i] += 1;
                else if (!bools[i] && !bools[classIndex])
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

        for (Boolean[] bools : set)
            if (bools[classIndex])
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

    Tuple<List<Boolean[]>, List<Boolean[]>> split(List<Boolean[]> set, int index) {
        List<Boolean[]> left = new LinkedList<>();
        List<Boolean[]> right = new LinkedList<>();

        for (Boolean[] bools : set) {
            if (bools[index])
                right.add(removeElement(index, bools));
            else
                left.add(removeElement(index, bools));
        }
        return new Tuple<>(right, left);
    }

    Boolean[] removeElement(int elementIndex, Boolean[] bools) {
        Boolean[] result = new Boolean[bools.length - 1];
        int j = 0;
        for (int i = 0; i < result.length; ++i, ++j) {
            if (j != elementIndex)
                result[i] = bools[j];
            else
                --i;
        }

        return result;
    }

    Tree learnTree(List<Boolean[]> set) {
        Tree tree = new Tree(set);

        if (tree.isLeafNode)
            return tree;

        Tuple<List<Boolean[]>, List<Boolean[]>> tuple = split(set, determineIndexOfSplit(set));
        tree.setLeft(learnTree(tuple.getLeft()));
        tree.setRight(learnTree(tuple.getRight()));
        return tree;
    }

    class Tree {
        private final boolean isLeafNode;
        private Boolean predicatedValue;
        private Tree left;
        private Tree right;

        Tree(List<Boolean[]> set) {
            isLeafNode = determineIfLeaf(set);
        }

        private boolean determineIfLeaf(List<Boolean[]> set) {
            final boolean isLeaf;
            final int classIndex = set.get(0).length - 1;
            final boolean initialValue = set.get(0)[classIndex];
            final boolean isPure = !set.parallelStream().anyMatch(bools -> bools[classIndex] != initialValue);

            if (isPure) {
                predicatedValue = initialValue;
                isLeaf = true;
            } else {
                boolean isUnsplittable = !set.parallelStream().anyMatch(this::isVaried);

                if (isUnsplittable) {
                    isLeaf = true;
                    predicatedValue = getMajorityClassValue(set);
                } else {
                    isLeaf = false;
                }
            }
            return isLeaf;
        }

        private boolean isVaried(Boolean[] bools) {
            boolean initialAttributeValue = bools[0];
            int attributeLen = bools.length - 1;
            for (int i = 0; i < attributeLen; ++i)
                if (bools[i] != initialAttributeValue)
                    return true;

            return false;
        }

        private Boolean getMajorityClassValue(List<Boolean[]> set) {
            int trueCount = 0;
            int falseCount = 0;
            int classIndex = set.get(0).length - 1;

            for (Boolean[] bools : set) {
                if (bools[classIndex])
                    trueCount += 1;
                else
                    falseCount += 1;
            }
            return trueCount >= falseCount; // returns true if no majority
        }

        Tree getLeft() {
            return left;
        }

        Tree getRight() {
            return right;
        }

        void setLeft(Tree left) {
            this.left = left;
        }

        void setRight(Tree right) {
            this.right = right;
        }

        Boolean getPredictedValue() {
            return predicatedValue;
        }

        boolean isLeafNode() {
            return isLeafNode;
        }
    }
}
