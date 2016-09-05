import org.junit.Test;

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
    private static final ID3 id3 = new ID3();
    private static DataSet spamSet = convertToSet(SPAM_EXAMPLE);

    @Test(expected = ID3.EmptyFileException.class)
    public void canThrowIfGivenEmptyFile() throws IOException {
        final String emptyFileTest = "resources/dataFormatTest/emptyFile.dat";
        ID3.getSetFromFileAsString(emptyFileTest);
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
        assertEquals(expected, ID3.getSetFromFileAsString(dataFormatTest));
    }

    @Test
    public void canGetWhatWasGiven() {
        assertEquals(SPAM_EXAMPLE, ID3.convertSetToString(spamSet));
    }

    @Test
    public void canCalculateEntropy() {
        assertEquals(0.0, id3.calcEntropy(0, 5), 0.01);
        assertEquals(0.0, id3.calcEntropy(5, 0), 0.01);

        assertEquals(1.0, id3.calcEntropy(2, 2), 0.001);

        assertEquals(0.59, id3.calcEntropy(1, 6), 0.01);
        assertEquals(0.97, id3.calcEntropy(3, 2), 0.01);
        assertEquals(0.918, id3.calcEntropy(1, 2), 0.001);
    }

    @Test
    public void canCalculateConditionalEntropy() {
        double[] conditionalEntropies = id3.calcConditionalEntropies(spamSet.getObservations(), spamSet.getLabels());
        assertEquals(0.7219, conditionalEntropies[0], 0.0001);
        assertEquals(0.7635, conditionalEntropies[1], 0.0001);
        assertEquals(0.9651, conditionalEntropies[2], 0.0001);
    }

    @Test
    public void canCalculateInformationGain() {
        double[] infoGains = id3.calcInfoGain(spamSet.getObservations(), spamSet.getLabels());
        assertEquals(0.28, infoGains[0], 0.01);
        assertEquals(0.24, infoGains[1], 0.01);
        assertEquals(0.035, infoGains[2], 0.01);
    }

    @Test
    public void canDetermineIndexOfSplit() {
        assertEquals(0, id3.determineIndexOfSplit(spamSet.getObservations(), spamSet.getLabels()));
    }

    @Test
    public void canSplitOnFirst() {
        Tuple<DataSet, DataSet> t = id3.split(spamSet, 0);
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
        assertEquals(expectedLeft, ID3.convertSetToString(t.getLeft()));
        assertEquals(expectedRight, ID3.convertSetToString(t.getRight()));
    }

    @Test
    public void canSplitOnMiddle() {
        Tuple<DataSet, DataSet> t = id3.split(spamSet, 1);
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
        assertEquals(expectedLeft, ID3.convertSetToString(t.getLeft()));
        assertEquals(expectedRight, ID3.convertSetToString(t.getRight()));
    }

    @Test
    public void canSplitOnLast() {
        Tuple<DataSet, DataSet> t = id3.split(spamSet, 2);
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
        assertEquals(expectedLeft, ID3.convertSetToString(t.getLeft()));
        assertEquals(expectedRight, ID3.convertSetToString(t.getRight()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void learnTree_throwsWhenNamesLenDoesNotMatchAttributeValueLen() {
        List<String> attributeNames = Arrays.asList("a", "b");
        List<List<Boolean>> observations = new LinkedList<>();
        observations.add(Arrays.asList(true, true, true));
        List<Boolean> labels = Collections.singletonList(true);
        // There must exists an attribute name for each attribute.
        id3.learnTree(attributeNames, observations, labels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void learnTree_throwsWhenLabelsLenDoesNotMatchAttributeListLen() {
        List<String> attributeNames = Arrays.asList("a", "b");
        List<List<Boolean>> observations = new LinkedList<>();
        observations.add(Arrays.asList(true, true));
        observations.add(Arrays.asList(true, false));
        List<Boolean> labels = Collections.singletonList(true);
        // There must exists an attribute name for each attribute.
        id3.learnTree(attributeNames, observations, labels);
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
        ID3.Tree t1 = id3.learnTree(convertToSet("a\n0 0"));
        assertTrue(t1.isLeafNode());

        assertEquals(false, t1.getPredictedValue().orElse(true));

        ID3.Tree t2 = id3.learnTree(convertToSet("a\n0 0\n1 0\n1 0"));
        assertTrue(t2.isLeafNode());
        assertEquals(false, t2.getPredictedValue().orElse(true));

        ID3.Tree t3 = id3.learnTree(convertToSet("a\n0 1\n1 1\n1 1"));
        assertTrue(t3.isLeafNode());
        assertEquals(true, t3.getPredictedValue().orElse(false));

        ID3.Tree t4 = id3.learnTree(convertToSet(pureTest));
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
        ID3.Tree t1 = id3.learnTree(convertToSet("a\n0 0\n1 1"));
        assertTrue(t1.isLeafNode());
        assertEquals(true, t1.getPredictedValue().orElse(false));

        ID3.Tree t2 = id3.learnTree(convertToSet(pureTest));
        assertFalse(t2.isLeafNode());
        assertFalse(t2.getPredictedValue().isPresent());
    }

    @Test
    public void learnTree_returnsLeafWhenGivenUnsplittable1() {
        final String unsplittableTree = "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n" +
                "0 0 0\n" +
                "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n";
        ID3.Tree t = id3.learnTree(convertToSet(unsplittableTree));
        assertTrue(t.isLeafNode());
        assertEquals(true, t.getPredictedValue().orElse(false));
    }

    @Test
    public void learnTree_returnsLeafWhenGivenUnsplittable2() {
        final String unsplittableTree = "a b" +
                "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "1 1 0\n";
        ID3.Tree t = id3.learnTree(convertToSet(unsplittableTree));
        assertTrue(t.isLeafNode());
        assertEquals(false, t.getPredictedValue().orElse(true));
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