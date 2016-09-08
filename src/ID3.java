import java.io.IOException;
import java.util.*;

class ID3 {
    private final boolean majorityLabelOfEntireSet;
    private final Tree tree;

    ID3(DataSet dataSet) {
        if (dataSet.getAttributeNames().size() != dataSet.getObservations().get(0).size())
            throw new IllegalArgumentException("There must exist an attribute name for each attribute.");
        if (dataSet.getLabels().size() != dataSet.getObservations().size())
            throw new IllegalArgumentException("There must exists a label for each observation.");

        majorityLabelOfEntireSet = getMajorityLabelValue(dataSet.getLabels());
        tree = learnTree(dataSet);
    }

    private Tree learnTree(DataSet set) {
        Tree tree = new Tree(set);

        if (tree.isLeafNode)
            return tree;

        final int indexOfSplit = determineIndexOfSplit(set.getObservations(), set.getLabels());
        tree.setAttributeName(set.getAttributeNames().get(indexOfSplit));
        tree.setAttributeIndex(indexOfSplit);
        Tuple<DataSet, DataSet> tuple = split(set, indexOfSplit);
        tree.setLeft(learnTree(tuple.getLeft()));
        tree.setRight(learnTree(tuple.getRight()));
        return tree;
    }

    static int determineIndexOfSplit(List<List<Boolean>> observations, List<Boolean> labels) {
        double[] infoGains = calcInfoGain(observations, labels);
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

    static double[] calcInfoGain(List<List<Boolean>> observations, List<Boolean> labels) {
        double[] conditionalEntropies = calcConditionalEntropies(observations, labels);
        double setEntropy = calcEntropy(observations, labels);
        double[] infoGains = new double[conditionalEntropies.length];

        for (int i = 0; i < infoGains.length; ++i)
            infoGains[i] = setEntropy - conditionalEntropies[i];

        return infoGains;
    }

    static double[] calcConditionalEntropies(List<List<Boolean>> observations, List<Boolean> labels) {
        final int attributeLen = observations.get(0).size();
        int[] classTrueCountOnTrueBranch = new int[attributeLen];
        int[] classFalseCountOnTrueBranch = new int[attributeLen];
        int[] classTrueCountOnFalseBranch = new int[attributeLen];
        int[] classFalseCountOnFalseBranch = new int[attributeLen];

        int labelIndex = 0;
        for (List<Boolean> attributeValues : observations) {
            for (int i = 0; i < attributeLen; ++i) {
                if (attributeValues.get(i) && labels.get(labelIndex))
                    classTrueCountOnTrueBranch[i] += 1;
                else if (attributeValues.get(i) && !labels.get(labelIndex))
                    classFalseCountOnTrueBranch[i] += 1;
                else if (!attributeValues.get(i) && labels.get(labelIndex))
                    classTrueCountOnFalseBranch[i] += 1;
                else if (!attributeValues.get(i) && !labels.get(labelIndex))
                    classFalseCountOnFalseBranch[i] += 1;
            }
            ++labelIndex;
        }
        double[] conditionalEntropies = new double[attributeLen];

        for (int i = 0; i < attributeLen; ++i) {
            conditionalEntropies[i] = getWeightedAverage(observations.size(), classTrueCountOnFalseBranch[i], classFalseCountOnFalseBranch[i])
                    + getWeightedAverage(observations.size(), classTrueCountOnTrueBranch[i], classFalseCountOnTrueBranch[i]);
        }
        return conditionalEntropies;
    }

    private static double getWeightedAverage(double setSize, int trueCount, int falseCount) {
        return ((trueCount + falseCount) / setSize) * calcEntropy(trueCount, falseCount);
    }

    private static double calcEntropy(List<List<Boolean>> observations, List<Boolean> labels) {
        int trueCount = 0;

        for (Boolean labelIsTrue : labels)
            if (labelIsTrue)
                trueCount += 1;

        return calcEntropy(trueCount, observations.size() - trueCount);
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

    static Tuple<DataSet, DataSet> split(DataSet set, int index) {
        List<List<Boolean>> leftObservations = new LinkedList<>();
        List<Boolean> leftLabels = new ArrayList<>();

        List<List<Boolean>> rightObservations = new LinkedList<>();
        List<Boolean> rightLabels = new ArrayList<>();

        for (int i = 0; i < set.getObservations().size(); ++i) {
            if (set.getObservations().get(i).get(index)) {
                rightObservations.add(removeElement(index, set.getObservations().get(i)));
                rightLabels.add(set.getLabels().get(i));
            } else {
                leftObservations.add(removeElement(index, set.getObservations().get(i)));
                leftLabels.add(set.getLabels().get(i));
            }
        }
        List<String> attributeNames = removeElement(index, set.getAttributeNames());
        DataSet left = new DataSet(attributeNames, leftObservations, leftLabels);
        DataSet right = new DataSet(attributeNames, rightObservations, rightLabels);
        return new Tuple<>(left, right);
    }

    private static <T> List<T> removeElement(int index, List<T> observations) {
        List<T> result = new ArrayList<>(observations);
        result.remove(index);
        return Collections.unmodifiableList(result);
    }

    Tree getTree() {
        return tree;
    }

    private Boolean getMajorityLabelValue(List<Boolean> labels) {
        int trueCount = 0;
        int falseCount = 0;

        for (Boolean labelIsTrue : labels) {
            if (labelIsTrue)
                trueCount += 1;
            else
                falseCount += 1;
        }
        if (trueCount == falseCount)
            return majorityLabelOfEntireSet;

        return trueCount > falseCount;
    }

    private static double getAccuracyPercent(Tree t, DataSet testSet) {
        int hitCount = 0;
        int labelIndex = 0;

        for (List<Boolean> obs : testSet.getObservations()) {

            if (isHit(t, obs, testSet.getLabels().get(labelIndex++)))
                hitCount++;
        }

        return (double) hitCount / testSet.getLabels().size() * 100;
    }

    private static boolean isHit(Tree t, List<Boolean> obs, Boolean expectedLabel) {
        if (t.isLeafNode)
            return t.predicatedValue == expectedLabel;
        else {
            final int attributeIndex = t.getAttributeIndex().orElseThrow(
                    () -> new TreeException("Non leaf node has no attribute index."));
            return isHit(t, obs, attributeIndex, expectedLabel);
        }
    }

    private static boolean isHit(Tree t, List<Boolean> obs, int attributeIndex, Boolean expectedLabel) {
        if (t.isLeafNode)
            return t.predicatedValue == expectedLabel;
        else {
            final String nodeError = "Non leaf node missing";

            if (obs.get(attributeIndex)) {
                Tree right = t.getRight().orElseThrow(() -> new TreeException(nodeError + " right branch"));
                final int i = right.getAttributeIndex().orElseThrow(() -> new TreeException(nodeError + " attribute index."));
                obs.remove(attributeIndex);
                return isHit(right, obs, i, expectedLabel);
            } else {
                Tree left = t.getLeft().orElseThrow(() -> new TreeException(nodeError + " left branch"));
                final int i = left.getAttributeIndex().orElseThrow(() -> new TreeException(nodeError + " attribute index."));
                obs.remove(attributeIndex);
                return isHit(left, obs, i, expectedLabel);
            }
        }
    }

    static String getAccuracyResults(Tree t, DataSet trainSet, DataSet testSet) {
        final double trainAcc = ID3.getAccuracyPercent(t, trainSet);
        final double testAcc = ID3.getAccuracyPercent(t, testSet);
        final String trainFormat = "\nAccuracy on training set (%d instances):  %.1f%%\n";
        final String testFormat = "\nAccuracy on test set (%s instances):  %.1f%%\n";

        final String trainAccResult = String.format(trainFormat, trainSet.getLabels().size(), trainAcc);
        final String testAccResult = String.format(testFormat, testSet.getLabels().size(), testAcc);
        return trainAccResult + testAccResult;
    }

    class Tree {
        private final boolean isLeafNode;
        private final Boolean predicatedValue;
        private Tree left;
        private Tree right;
        private String attributeName;
        private int attributeIndex;

        Tree(DataSet set) {
            Tuple<Boolean, Boolean> t = determineIsLeaf(set);
            isLeafNode = t.getLeft();
            predicatedValue = t.getRight();
        }

        private Tuple<Boolean, Boolean> determineIsLeaf(DataSet set) {
            final List<List<Boolean>> obs = set.getObservations();
            final List<Boolean> labels = set.getLabels();

            Boolean isPredicated = null;
            final boolean isLeaf;

            final boolean initialValue = labels.get(0);
            final List<Boolean> initialObs = obs.get(0);
            final boolean isPure = !labels.stream().anyMatch(b -> b != initialValue);

            if (isPure) {
                isPredicated = initialValue;
                isLeaf = true;
            } else if (initialObs.isEmpty()) {
                isLeaf = true;
                isPredicated = getMajorityLabelValue(labels);
            } else if (!obs.stream().anyMatch(o -> !listsAreEqual(initialObs, o))) {
                isLeaf = true;
                isPredicated = getMajorityLabelValue(labels);
            } else {
                isLeaf = false;
            }
            return new Tuple<>(isLeaf, isPredicated);
        }

        private boolean listsAreEqual(List<Boolean> listA, List<Boolean> listB) {
            Iterator<Boolean> itA = listA.iterator();
            Iterator<Boolean> itB = listB.iterator();

            while (itA.hasNext() && itB.hasNext()) {
                Boolean a = itA.next();
                Boolean b = itB.next();
                if (a != b)
                    return false;
            }
            return true;
        }

        boolean isLeafNode() {
            return isLeafNode;
        }

        Optional<Boolean> getPredictedValue() {
            return Optional.ofNullable(predicatedValue);
        }

        Optional<Tree> getLeft() {
            return Optional.ofNullable(left);
        }

        Optional<Tree> getRight() {
            return Optional.ofNullable(right);
        }

        void setLeft(Tree left) {
            this.left = left;
        }

        void setRight(Tree right) {
            this.right = right;
        }

        void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        Optional<String> getAttributeName() {
            return Optional.ofNullable(attributeName);
        }

        Optional<Integer> getAttributeIndex() {
            return Optional.ofNullable(attributeIndex);
        }

        void setAttributeIndex(int attributeIndex) {
            this.attributeIndex = attributeIndex;
        }

        String getTreeDiagram() {
            return createDiagram(this, "");
        }

        private String createDiagram(Tree t, final String padding) {
            String result;
            if (t.isLeafNode()) {
                final boolean isPredicated = t.getPredictedValue().orElseThrow(() -> new TreeException("Leaf node has no predicated value."));
                result = isPredicated ? "  1" : "  0";
                result += "\n";
            } else {
                result = padding.isEmpty() ? "" : "\n";
                final String name = t.getAttributeName().orElseThrow(() -> new TreeException("Inner node has no attribute name"));
                result += padding + name + " = 0 :";
                Tree l = t.getLeft().orElseThrow(() -> new TreeException("Inner node has no left node."));
                result += createDiagram(l, padding + "| ");
                result += padding + name + " = 1 :";
                Tree r = t.getRight().orElseThrow(() -> new TreeException("Inner node has no right node."));
                result += createDiagram(r, padding + "| ");
            }
            return result;
        }
    }

    private static class TreeException extends RuntimeException {
        TreeException(String msg) {
            super(msg);
        }
    }

    static class EmptyFileException extends IOException {
        EmptyFileException(String msg) {
            super(msg);
        }
    }
}
