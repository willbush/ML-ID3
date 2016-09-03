import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ID3Test {
    private static List<Boolean[]> spamSet;
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

    @BeforeClass
    public static void setupClass() {
        spamSet = convertToSet(SPAM_EXAMPLE);
    }

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
        assertEquals(0.0, ID3.calcEntropy(0, 5), 0.01);
        assertEquals(0.0, ID3.calcEntropy(5, 0), 0.01);

        assertEquals(1.0, ID3.calcEntropy(2, 2), 0.001);

        assertEquals(0.59, ID3.calcEntropy(1, 6), 0.01);
        assertEquals(0.97, ID3.calcEntropy(3, 2), 0.01);
        assertEquals(0.918, ID3.calcEntropy(1, 2), 0.001);
    }

    @Test
    public void canCalculateConditionalEntropy() {
        double[] conditionalEntropies = ID3.calcConditionalEntropies(spamSet);
        assertEquals(0.7219, conditionalEntropies[0], 0.0001);
        assertEquals(0.7635, conditionalEntropies[1], 0.0001);
        assertEquals(0.9651, conditionalEntropies[2], 0.0001);
    }

    @Test
    public void canCalculateInformationGain() {
        double[] infoGains = ID3.calcInfoGain(spamSet);
        assertEquals(0.28, infoGains[0], 0.01);
        assertEquals(0.24, infoGains[1], 0.01);
        assertEquals(0.035, infoGains[2], 0.01);
    }

    @Test
    public void canDetermineIndexOfSplit() {
        assertEquals(0, ID3.determineIndexOfSplit(spamSet));
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