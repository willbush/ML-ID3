import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ID3Test {
    private static final String SPAM_EXAMPLE = "nigeria viagra learning\n" +
            "1 0 0 1\n" +
            "0 0 1 1\n" +
            "0 0 0 0\n" +
            "1 1 0 0\n" +
            "0 0 0 0\n" +
            "1 0 1 1\n" +
            "0 1 1 0\n" +
            "1 0 0 1\n" +
            "0 0 0 0\n" +
            "1 0 0 1\n";
    private static DataSet spamSet = convertToSet(SPAM_EXAMPLE);

    @Test(expected = ID3.EmptyFileException.class)
    public void canThrowIfGivenEmptyFile() throws IOException {
        final String emptyFileTest = "resources/dataFormatTest/emptyFile.dat";
        Main.getSetFromFileAsString(emptyFileTest);
    }

    @Test
    public void canCorrectlyParseFileWithExtraWhiteSpace() throws IOException {
        final String dataFormatTest = "resources/dataFormatTest/extraWhiteSpace.dat";
        final String expected = "A B C\n" +
                "0 0 0 0\n" +
                "0 0 1 0\n" +
                "0 1 0 0\n" +
                "0 1 1 0\n" +
                "1 0 0 0\n" +
                "1 0 1 0\n" +
                "1 1 0 0\n" +
                "1 1 1 1\n";
        assertEquals(expected, Main.getSetFromFileAsString(dataFormatTest));
    }

    @Test
    public void canGetWhatWasGiven() {
        assertEquals(SPAM_EXAMPLE, Main.convertSetToString(spamSet));
    }

    @Test
    public void canCalculateEntropy() {
        // pure nodes (i.e. all class labels have same value) have no entropy
        assertEquals(0.0, ID3.calcEntropy(0, 5), 0.01);
        assertEquals(0.0, ID3.calcEntropy(5, 0), 0.01);

        // even split of class labels have max entropy (1.0)
        assertEquals(1.0, ID3.calcEntropy(2, 2), 0.001);

        assertEquals(0.59, ID3.calcEntropy(1, 6), 0.01);
        assertEquals(0.97, ID3.calcEntropy(3, 2), 0.01);
        assertEquals(0.918, ID3.calcEntropy(1, 2), 0.001);
    }

    @Test
    public void canCalculateConditionalEntropy() {
        double[] conditionalEntropies = ID3.calcConditionalEntropies(spamSet.getObservations(), spamSet.getLabels());
        assertEquals(0.7219, conditionalEntropies[0], 0.0001);
        assertEquals(0.7635, conditionalEntropies[1], 0.0001);
        assertEquals(0.9651, conditionalEntropies[2], 0.0001);
    }

    @Test
    public void canCalculateInformationGain() {
        double[] infoGains = ID3.calcInfoGain(spamSet.getObservations(), spamSet.getLabels());
        assertEquals(0.28, infoGains[0], 0.01);
        assertEquals(0.24, infoGains[1], 0.01);
        assertEquals(0.035, infoGains[2], 0.01);
    }

    @Test
    public void canDetermineIndexOfSplit() {
        assertEquals(0, ID3.determineIndexOfSplit(spamSet.getObservations(), spamSet.getLabels()));
    }

    @Test
    public void canSplitOnFirst() {
        Tuple<DataSet, DataSet> t = ID3.split(spamSet, 0);
        final String expectedLeft = "viagra learning\n" +
                "0 1 1\n" +
                "0 0 0\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n";
        final String expectedRight = "viagra learning\n" +
                "0 0 1\n" +
                "1 0 0\n" +
                "0 1 1\n" +
                "0 0 1\n" +
                "0 0 1\n";
        assertEquals(expectedLeft, Main.convertSetToString(t.getLeft()));
        assertEquals(expectedRight, Main.convertSetToString(t.getRight()));
    }

    @Test
    public void canSplitOnMiddle() {
        Tuple<DataSet, DataSet> t = ID3.split(spamSet, 1);
        final String expectedLeft = "nigeria learning\n" +
                "1 0 1\n" +
                "0 1 1\n" +
                "0 0 0\n" +
                "0 0 0\n" +
                "1 1 1\n" +
                "1 0 1\n" +
                "0 0 0\n" +
                "1 0 1\n";
        final String expectedRight = "nigeria learning\n" +
                "1 0 0\n" +
                "0 1 0\n";
        assertEquals(expectedLeft, Main.convertSetToString(t.getLeft()));
        assertEquals(expectedRight, Main.convertSetToString(t.getRight()));
    }

    @Test
    public void canSplitOnLast() {
        Tuple<DataSet, DataSet> t = ID3.split(spamSet, 2);
        final String expectedLeft = "nigeria viagra\n" +
                "1 0 1\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n" +
                "1 0 1\n" +
                "0 0 0\n" +
                "1 0 1\n";
        final String expectedRight = "nigeria viagra\n" +
                "0 0 1\n" +
                "1 0 1\n" +
                "0 1 0\n";
        assertEquals(expectedLeft, Main.convertSetToString(t.getLeft()));
        assertEquals(expectedRight, Main.convertSetToString(t.getRight()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void learnTree_throwsWhenNamesLenDoesNotMatchAttributeValueLen() {
        List<String> attributeNames = Arrays.asList("a", "b");
        List<List<Boolean>> observations = new LinkedList<>();
        observations.add(Arrays.asList(true, true, true));
        List<Boolean> labels = Collections.singletonList(true);
        // There must exists an attribute name for each attribute.
        new ID3(new DataSet(attributeNames, observations, labels));
    }

    @Test(expected = IllegalArgumentException.class)
    public void learnTree_throwsWhenLabelsLenDoesNotMatchAttributeListLen() {
        List<String> attributeNames = Arrays.asList("a", "b");
        List<List<Boolean>> observations = new LinkedList<>();
        observations.add(Arrays.asList(true, true));
        observations.add(Arrays.asList(true, false));
        List<Boolean> labels = Collections.singletonList(true);
        // There must exists an attribute name for each attribute.
        new ID3(new DataSet(attributeNames, observations, labels));
    }

    @Test
    public void canLearnSimplePureTree() {
        final String pureTest = "a b\n" +
                "1 0 1\n" +
                "0 0 1\n" +
                "1 1 1\n" +
                "0 0 1\n" +
                "1 0 1\n" +
                "0 0 1\n" +
                "1 0 1\n";
        ID3.Tree t1 = new ID3(convertToSet("a\n0 0")).getTree();
        assertTrue(t1.isLeafNode());

        assertEquals(false, t1.getPredictedValue().orElse(true));

        ID3.Tree t2 = new ID3(convertToSet("a\n0 0\n1 0\n1 0")).getTree();
        assertTrue(t2.isLeafNode());
        assertEquals(false, t2.getPredictedValue().orElse(true));

        ID3.Tree t3 = new ID3(convertToSet("a\n0 1\n1 1\n1 1")).getTree();
        assertTrue(t3.isLeafNode());
        assertEquals(true, t3.getPredictedValue().orElse(false));

        ID3.Tree t4 = new ID3(convertToSet(pureTest)).getTree();
        assertTrue(t4.isLeafNode());
        assertEquals(true, t4.getPredictedValue().orElse(false));
    }

    @Test
    public void canLearnSimpleImpureTree() {
        final String pureTest = "a b\n" +
                "1 0 1\n" +
                "0 0 1\n" +
                "1 1 1\n" +
                "0 0 1\n" +
                "1 0 1\n" +
                "0 0 1\n" +
                "1 0 0\n";
        ID3.Tree t2 = new ID3(convertToSet(pureTest)).getTree();
        assertFalse(t2.isLeafNode());
        assertFalse(t2.getPredictedValue().isPresent());
    }

    @Test
    public void learnTree_returnsLeafWhenGivenUnsplittable1() {
        final String unsplittableTree = "a b\n" +
                "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n" +
                "0 0 0\n" +
                "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n";
        ID3.Tree t = new ID3(convertToSet(unsplittableTree)).getTree();
        assertTrue(t.isLeafNode());
        assertEquals(true, t.getPredictedValue().orElse(false));
    }

    @Test
    public void learnTree_returnsNonLeafWhenAttributeValuesVaryByRowButNotColumn() {
        final String unsplittableTree = "a b\n" +
                "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "1 1 0\n";
        ID3.Tree t = new ID3(convertToSet(unsplittableTree)).getTree();
        assertFalse(t.isLeafNode());
    }

    @Test
    public void canPrintTree() {
        ID3.Tree t = new ID3(spamSet).getTree();
        final String expectedDiagram = "nigeria = 0 :\n" +
                "| learning = 0 :  0\n" +
                "| learning = 1 :\n" +
                "| | viagra = 0 :  1\n" +
                "| | viagra = 1 :  0\n" +
                "nigeria = 1 :\n" +
                "| viagra = 0 :  1\n" +
                "| viagra = 1 :  0\n";
        assertEquals(expectedDiagram, t.getTreeDiagram());
    }

    @Test
    public void learnTree_canLearnTrainingSet1() throws IOException {
        final String trainPath = "resources/dataSet1/train.dat";
        final String testPath = "resources/dataSet1/test.dat";
        final String treePath = "resources/dataSet1/tree.txt";
        String expected = new Scanner(new File(treePath)).useDelimiter("\\Z").next();

        DataSet trainSet = Main.getSetFromFile(trainPath);
        DataSet testSet = Main.getSetFromFile(testPath);
        ID3.Tree t = new ID3(trainSet).getTree();

        String treeDiagram = t.getTreeDiagram();
        String accuracyResult = ID3.getAccuracyResults(t, trainSet, testSet);
        assertEquals(expected, treeDiagram + accuracyResult);
    }

    @Test
    public void learnTree_canLearnTrainingSet2() throws IOException {
        final String trainPath = "resources/dataSet2/train2.dat";
        final String testPath = "resources/dataSet2/test2.dat";
        final String treePath = "resources/dataSet2/tree2.txt";
        String expected = new Scanner(new File(treePath)).useDelimiter("\\Z").next();

        DataSet trainSet = Main.getSetFromFile(trainPath);
        DataSet testSet = Main.getSetFromFile(testPath);
        ID3.Tree t = new ID3(trainSet).getTree();

        String treeDiagram = t.getTreeDiagram();
        String accuracyResult = ID3.getAccuracyResults(t, trainSet, testSet);
        assertEquals(expected, treeDiagram + accuracyResult);
    }

    @Test
    public void learnTree_canLearnBooleanFunction1() {
        // boolean function: (~A + B) * ~(C * A)
        final String trainingSet = "A B C\n" +
                "0 0 0 1\n" +
                "0 0 1 1\n" +
                "0 1 0 1\n" +
                "0 1 1 1\n" +
                "1 0 0 0\n" +
                "1 0 1 0\n" +
                "1 1 0 1\n" +
                "1 1 1 0\n";
        final String expectedDiagram = "A = 0 :  1\n" +
                "A = 1 :\n" +
                "| B = 0 :  0\n" +
                "| B = 1 :\n" +
                "| | C = 0 :  1\n" +
                "| | C = 1 :  0\n";
        ID3.Tree t = new ID3(convertToSet(trainingSet)).getTree();
        assertEquals(expectedDiagram, t.getTreeDiagram());
    }

    @Test
    public void learnTree_canLearnBooleanFunction2() {
        // boolean function: (A XOR B) * C
        final String trainingSet = "A B C\n" +
                "0 0 0 0\n" +
                "0 0 1 0\n" +
                "0 1 0 0\n" +
                "0 1 1 1\n" +
                "1 0 0 0\n" +
                "1 0 1 1\n" +
                "1 1 0 0\n" +
                "1 1 1 0\n";
        final String expectedDiagram = "C = 0 :  0\n" +
                "C = 1 :\n" +
                "| A = 0 :\n" +
                "| | B = 0 :  0\n" +
                "| | B = 1 :  1\n" +
                "| A = 1 :\n" +
                "| | B = 0 :  1\n" +
                "| | B = 1 :  0\n";
        ID3.Tree t = new ID3(convertToSet(trainingSet)).getTree();
        assertEquals(expectedDiagram, t.getTreeDiagram());
    }

    private static DataSet convertToSet(String data) {
        final String whitespaceRegex = "\\s+";
        String[] lines = data.split("\\n");
        List<List<Boolean>> observations = new LinkedList<>();
        List<String> attributeNames = Arrays.asList(lines[0].split(whitespaceRegex));
        List<Boolean> labels = new ArrayList<>(observations.size());

        for (int i = 1; i < lines.length; ++i) {
            String[] rowValues = lines[i].split(whitespaceRegex);
            labels.add(rowValues[rowValues.length - 1].equals("1"));

            List<Boolean> attributeValues = new ArrayList<>(rowValues.length);

            for (int j = 0; j < rowValues.length - 1; ++j) {
                attributeValues.add(rowValues[j].equals("1"));
            }

            observations.add(attributeValues);
        }
        return new DataSet(attributeNames, observations, labels);
    }
}