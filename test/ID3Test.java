import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class ID3Test {
    private static final String SPAM_EXAMPLE = "1 0 0 1\n" +
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
    private static List<Boolean[]> spamSet = Collections.unmodifiableList(convertToSet(SPAM_EXAMPLE));

    @Test
    public void canCorrectlyParseFileWithExtraWhiteSpace() {
        final String dataFormatTest = "resources/dataFormatTest/test.dat";
        final String expected = "0 0 0 0\n" +
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
        double[] conditionalEntropies = id3.calcConditionalEntropies(spamSet);
        assertEquals(0.7219, conditionalEntropies[0], 0.0001);
        assertEquals(0.7635, conditionalEntropies[1], 0.0001);
        assertEquals(0.9651, conditionalEntropies[2], 0.0001);
    }

    @Test
    public void canCalculateInformationGain() {
        double[] infoGains = id3.calcInfoGain(spamSet);
        assertEquals(0.28, infoGains[0], 0.01);
        assertEquals(0.24, infoGains[1], 0.01);
        assertEquals(0.035, infoGains[2], 0.01);
    }

    @Test
    public void canDetermineIndexOfSplit() {
        assertEquals(0, id3.determineIndexOfSplit(spamSet));
    }

    @Test
    public void canRemoveElementFromArray() {
        Boolean[] test = new Boolean[]{true, false, true, false};
        assertArrayEquals(new Boolean[]{false, true, false}, id3.removeElement(0, test));
    }

    @Test
    public void canSplitOnFirst() {
        Tuple<List<Boolean[]>, List<Boolean[]>> n = id3.split(spamSet, 0);
        final String expectedLeft = "0 0 1\n" +
                "1 0 0\n" +
                "0 1 1\n" +
                "0 0 1\n" +
                "0 0 1\n";
        final String expectedRight = "0 1 1\n" +
                "0 0 0\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n";
        assertEquals(expectedLeft, ID3.convertSetToString(n.getLeft()));
        assertEquals(expectedRight, ID3.convertSetToString(n.getRight()));
    }

    @Test
    public void canSplitOnMiddle() {
        Tuple<List<Boolean[]>, List<Boolean[]>> n = id3.split(spamSet, 1);
        final String expectedLeft = "1 0 0\n" +
                "0 1 0\n";
        final String expectedRight = "1 0 1\n" +
                "0 1 1\n" +
                "0 0 0\n" +
                "0 0 0\n" +
                "1 1 1\n" +
                "1 0 1\n" +
                "0 0 0\n" +
                "1 0 1\n";
        assertEquals(expectedLeft, ID3.convertSetToString(n.getLeft()));
        assertEquals(expectedRight, ID3.convertSetToString(n.getRight()));
    }

    @Test
    public void canSplitOnLast() {
        Tuple<List<Boolean[]>, List<Boolean[]>> n = id3.split(spamSet, 2);
        final String expectedLeft = "0 0 1\n" +
                "1 0 1\n" +
                "0 1 0\n";
        final String expectedRight = "1 0 1\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n" +
                "1 0 1\n" +
                "0 0 0\n" +
                "1 0 1\n";
        assertEquals(expectedLeft, ID3.convertSetToString(n.getLeft()));
        assertEquals(expectedRight, ID3.convertSetToString(n.getRight()));
    }

    @Test
    public void canLearnSimplePureTree() {
        final String pureTest = "1 0 1\n" +
                "0 0 1\n" +
                "1 1 1\n" +
                "0 0 1\n" +
                "1 0 1\n" +
                "0 0 1\n" +
                "1 0 1\n";
        ID3.Tree t1 = id3.learnTree(convertToSet("0 0"));
        assertTrue(t1.isLeafNode());
        assertEquals(false, t1.getPredictedValue());

        ID3.Tree t2 = id3.learnTree(convertToSet("0 0\n1 0\n1 0"));
        assertTrue(t2.isLeafNode());
        assertEquals(false, t2.getPredictedValue());

        ID3.Tree t3 = id3.learnTree(convertToSet("0 1\n1 1\n1 1"));
        assertTrue(t3.isLeafNode());
        assertEquals(true, t3.getPredictedValue());

        ID3.Tree t4 = id3.learnTree(convertToSet(pureTest));
        assertTrue(t4.isLeafNode());
        assertEquals(true, t4.getPredictedValue());
    }

    @Test
    public void canLearnSimpleImpureTree() {
        final String pureTest = "1 0 1\n" +
                "0 0 1\n" +
                "1 1 1\n" +
                "0 0 1\n" +
                "1 0 1\n" +
                "0 0 1\n" +
                "1 0 0\n";
        ID3.Tree t1 = id3.learnTree(convertToSet("0 0\n1 1"));
        assertTrue(t1.isLeafNode());
        assertEquals(true, t1.getPredictedValue());

        ID3.Tree t2 = id3.learnTree(convertToSet(pureTest));
        assertFalse(t2.isLeafNode());
        assertNull(t2.getPredictedValue());
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
        assertEquals(true, t.getPredictedValue());
    }

    @Test
    public void learnTree_returnsLeafWhenGivenUnsplittable2() {
        final String unsplittableTree = "0 0 1\n" +
                "0 0 1\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "0 0 0\n" +
                "1 1 0\n" +
                "1 1 0\n";
        ID3.Tree t = id3.learnTree(convertToSet(unsplittableTree));
        assertTrue(t.isLeafNode());
        assertEquals(false, t.getPredictedValue());
    }

    @Test
    public void learnTree_canLearnSpamExample() {
        ID3.Tree t = id3.learnTree(spamSet);
        //TODO: verify correctness of tree.
    }

    private static List<Boolean[]> convertToSet(String setData) {
        final String whitespaceRegex = "\\s+";
        List<Boolean[]> result = new LinkedList<>();
        String[] lines = setData.split("\\n");

        for (String line : lines) {
            String[] words = line.split(whitespaceRegex);
            Boolean[] row = new Boolean[words.length];
            for (int i = 0; i < words.length; ++i) {
                row[i] = words[i].equals("1");
            }
            result.add(row);
        }

        return result;
    }
}