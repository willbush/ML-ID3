import java.util.*;

//@formatter:off
/**
 * Iterative Dichotomiser 3 (ID3) is a learning algorithm which builds decision trees. This implementation only handles
 * binary classification tasks and does not implement pruning.
 *
 * How ID3 works is best explained with an example. Consider the boolean function: (A XOR B) AND C.
 * The un-parsed training data for its truth table would look like this:
 *
 *   A B C class
 *   0 0 0 0
 *   0 0 1 0
 *   0 1 0 0
 *   0 1 1 1
 *   1 0 0 0
 *   1 0 1 1
 *   1 1 0 0
 *   1 1 1 0
 *
 * Where A, B, C are inputs to the function and "class" is the class label, which is the output of the function.
 * The ID3 program is expected to print the following decision tree on the left. On the right is my attempt to draw
 * the decision tree where you go down a left branch on 0 (or false) and right branch for 1 (or true).
 *
 *   C = 0 :  0                         C
 *   C = 1 :                           / \
 *   | A = 0 :                        0  A
 *   | | B = 0 :  0                     / \
 *   | | B = 1 :  1                    B   B
 *   | A = 1 :                        /\   /\
 *   | | B = 0 :  1                  0 1  1 0
 *   | | B = 1 :  0
 *
 * But why does the root start with C? The root starts with C because C gives the most information of the 3 variables
 * about whether the output will be true or false. If C is false, then we're done the output of the function is false.
 * The ID3 algorithm calculates information gain by calculating entropy, and splits based on the attribute with the
 * most information gain. For more information please see the following link.
 *
 * @see <a href="https://en.wikipedia.org/wiki/ID3_algorithm">ID3 on wikipedia</a>
 */
//@formatter:on
class ID3 {
    private final boolean majorityLabelOfEntireSet;
    private final Tree tree;

    ID3(DataSet dataSet) {
        majorityLabelOfEntireSet = getMajorityLabelValue(dataSet.getLabels());
        tree = learnTree(dataSet);
    }

    /**
     * @param labels class labels.
     * @return the majority boolean value in a list of booleans. If no majority exists, then the majority
     * of the entire class label set is returned.
     */
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

    /**
     * A recursive function that learns the decision tree for the given data set.
     *
     * @param set data set.
     * @return the learned decision tree.
     */
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

    /**
     * Determines the index of which attribute that will be used to split the data set.
     *
     * @param observations observations (aka examples)
     * @param labels       labeled classes
     * @return the index of which attribute will be used to split.
     */
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

    /**
     * Creates an array of information gains where each index corresponds to the index of the attributes.
     *
     * @param observations observations (aka examples)
     * @param labels       class labels.
     * @return information gains array.
     */
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

        for (int row = 0; row < observations.size(); ++row) {
            for (int col = 0; col < attributeLen; ++col) {
                final boolean isAttributeTrue = observations.get(row).get(col);
                final boolean isLabelTrue = labels.get(row);

                if (isAttributeTrue && isLabelTrue)
                    classTrueCountOnTrueBranch[col] += 1;
                else if (isAttributeTrue)
                    classFalseCountOnTrueBranch[col] += 1;
                else if (isLabelTrue)
                    classTrueCountOnFalseBranch[col] += 1;
                else
                    classFalseCountOnFalseBranch[col] += 1;
            }
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

    /**
     * @param d input value assumed to not be zero.
     * @return log base 2 of the input
     */
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

    private static <T> List<T> removeElement(int index, List<T> elements) {
        List<T> result = new ArrayList<>(elements.size() - 1);

        for (int i = 0; i < elements.size(); ++i)
            if (i != index)
                result.add(elements.get(i));

        return Collections.unmodifiableList(result);
    }

    Tree getTree() {
        return tree;
    }

    /**
     * Determines the accuracy percent of a decision tree against a given data set.
     *
     * @param t       tree
     * @param testSet data set
     * @return accuracy percent of a decision tree against a data set.
     */
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
            return t.predictedValue == expectedLabel;
        else {
            final int attributeIndex = t.getAttributeIndex().orElseThrow(
                    () -> new TreeException("Non leaf node has no attribute index."));
            return isHit(t, obs, attributeIndex, expectedLabel);
        }
    }

    private static boolean isHit(Tree t, List<Boolean> obs, int attributeIndex, Boolean expectedLabel) {
        if (t.isLeafNode)
            return t.predictedValue == expectedLabel;
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

    /**
     * A binary Tree which is either a leaf node or an inner node which contains left and right sub tree.
     */
    class Tree {
        private final boolean isLeafNode;
        // Tree only has a predicted value if it is a leaf node.
        private final Boolean predictedValue;
        // Tree only has left or right nodes if it is inner node.
        private Tree left;
        private Tree right;
        // Tree only an attribute name or index if it is an inner node.
        private String attributeName;
        private int attributeIndex;

        Tree(DataSet set) {
            Tuple<Boolean, Boolean> t = determineIsLeaf(set);
            isLeafNode = t.getLeft();
            predictedValue = t.getRight();
        }

        /**
         * Determines the base case for the learn tree recursive calls.
         *
         * @param set data set.
         * @return left value is if this tree is a leaf and right value is the predicted value
         */
        private Tuple<Boolean, Boolean> determineIsLeaf(DataSet set) {
            final List<List<Boolean>> obs = set.getObservations();
            final List<Boolean> labels = set.getLabels();

            Boolean isPredicted = null;
            final boolean isLeaf;
            final boolean initialValue = labels.get(0);
            // "pure" means all class labels are the same value.
            final boolean isPure = !labels.stream().anyMatch(b -> b != initialValue);

            if (isPure) {
                isPredicted = initialValue;
                isLeaf = true;
            } else if (isUnsplittable(obs)) {
                isLeaf = true;
                isPredicted = getMajorityLabelValue(labels);
            } else {
                isLeaf = false;
            }
            return new Tuple<>(isLeaf, isPredicted);
        }

        /**
         * A data set is unsplittable if there are no observations or the elements of each observation are identical.
         * In reality if all observations are identical, it is splittable, but it would be  pointless to do so.
         *
         * @param obs observations
         * @return whether the data set can split based on the observations.
         */
        private boolean isUnsplittable(List<List<Boolean>> obs) {
            final List<Boolean> initialObs = obs.get(0);
            return initialObs.isEmpty() || !obs.stream().anyMatch(o -> !listsAreEqual(initialObs, o));
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
            return Optional.ofNullable(predictedValue);
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

        /**
         * recursively builds a tree diagram.
         *
         * @param t       the decision tree.
         * @param padding padding from the left that builds up through recursive method calls.
         * @return a decision tree diagram.
         */
        private String createDiagram(Tree t, final String padding) {
            String result;
            if (t.isLeafNode()) {
                final boolean isPredicted = t.getPredictedValue().orElseThrow(() -> new TreeException("Leaf node has no predicted value."));
                result = isPredicted ? "  1" : "  0";
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
}
