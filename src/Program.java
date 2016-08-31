import java.io.*;

public class Program {
    public static void main(String[] args) {
        if (args.length != 2) {
            printProgramUsage();
        }

        try {
            printFileContents(args[0]);
            printFileContents(args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printProgramUsage() {
        String usage = "The program requires two arguments that specify the path" +
                " to the training and test data.\n" +
                "For example:\n" +
                "java Program \"/dataSet/train.dat\" \"/dataSet/test.dat\"";
        System.out.println(usage);
    }

    public static void printFileContents(String path) {
        File f = new File(path);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
