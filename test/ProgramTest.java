import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ProgramTest {
    @Test
    public void canPrintTestData() {
        Program.main(new String[]{"resources/dataSet1/train.dat", "resources/dataSet1/test.dat"});
        assertTrue(true);
    }
}