/**
 * The entry point of the application.
 * <p>
 * This application is console based as it expects two path parameters as input. The first parameter is for the
 * training data set and the second parameter is for the test data set.
 * The training sets must be binary classification tasks (1 or 0) see the training and test dat files in resources
 * for examples.
 * <p>
 * The program's output is simply printing the resulting decision tree and the accuracy percent of the decision tree
 * against the test set and also against the training set it was trained on. A decision tree tested against the same
 * set it was trained on should only have 100% accuracy if that set of data has no inconsistencies.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            printProgramUsage();
            System.exit(1);
        }

        try {
            DataSet trainSet = DataSet.fromFile(args[0]);
            DataSet testSet = DataSet.fromFile(args[1]);
            ID3.Tree t = new ID3(trainSet).getTree();

            System.out.println(t.getTreeDiagram());
            System.out.println(ID3.getAccuracyResults(t, trainSet, testSet));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printProgramUsage() {
        String usage = "The program requires two arguments that specify the path" +
                " to the training and test data.\n" +
                "For example:\n" +
                "java ID3 \"/dataSet/train.dat\" \"/dataSet/test.dat\"";
        System.out.println(usage);
    }
}
