import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @author Axel DUMONT M2-MIAGE (DLIS)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        // Timer
        long startTime = System.nanoTime();

        // Disable warnings from pdfbox
        java.util.logging.Logger
                .getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);

        Properties prop = new Properties();
        InputStream input = new FileInputStream("src/main/config.properties");

        // get Vocabulary file
        File file = new File("src/main/vocabulary.csv");

        // create List of words
        List<String> voc = Arrays.asList(readVoc(file).split("\\s*,\\s*"));

        // List of vectors
        List<int[]> vectors = new ArrayList<int[]>();

        // iterate over pdf files
        int nbPdfToCheck = Integer.parseInt(getProp(prop, "nbPdfToCheck", input));
        File dir = new File(getProp(prop, "folder", input));
        File[] directoryListing = dir.listFiles((d, name) -> name.endsWith(".pdf"));
        if (directoryListing != null) {
            for (int i = 0; (i < directoryListing.length && i < nbPdfToCheck); i++) {
                File pdfFile = directoryListing[i];
                String text = getText(pdfFile);
                int[] vector = generateVector(text, voc);
                vectors.add(vector);
            }
        } else {
            System.out.println(prop.getProperty("folder") + " is not a directory.");
        }

        // Show similar files
        showDuplicates(vectors, directoryListing);

        long endTime = System.nanoTime();
        System.out.println("Execution time : " + (endTime - startTime) / 1000000 + " ms");
    }


    /**
     * Get the pdf text.
     *
     * @param pdfFile
     * @return text
     * @throws IOException
     */
    private static String getText(File pdfFile) throws IOException {
        PDDocument doc = PDDocument.load(pdfFile);
        return new PDFTextStripper().getText(doc);
    }

    /**
     * CSV to string.
     *
     * @param voc csv file
     * @return words separated with comma in a String object.
     * @throws IOException
     */
    private static String readVoc(File voc) throws IOException {
        String vocStr = "";
        BufferedReader bufRdr = new BufferedReader(
                new InputStreamReader(new FileInputStream(voc), "ISO-8859-1"));
        String line = null;
        while ((line = bufRdr.readLine()) != null) {
            vocStr += line + ",";
        }
        if (vocStr != null && vocStr.length() > 0 && vocStr.charAt(vocStr.length() - 1) == 'x') {
            vocStr = vocStr.substring(0, vocStr.length() - 1);
        }
        return vocStr;
    }

    /**
     * Get a prop from config.properties
     *
     * @param prop     properties
     * @param propName propertie name
     * @param input    input stream
     * @return the prop. value
     * @throws IOException
     */
    private static String getProp(Properties prop, String propName, InputStream input) throws IOException {
        prop.load(input);
        return prop.getProperty(propName);
    }

    /**
     * Counte the number of occurences of the word in a text.
     *
     * @param text text
     * @param word word
     * @return nb occurences
     */
    private static int countWord(String text, String word) {
        int i = 0;
        Pattern p = Pattern.compile(word);
        Matcher m = p.matcher(text);
        while (m.find()) {
            i++;
        }
        return i;
    }

    /**
     * Generate a vector from a text and a vocabulary list.
     *
     * @param text
     * @param voc
     * @return vector
     */
    private static int[] generateVector(String text, List<String> voc) {
        System.out.println("Generate vectors...");
        int[] vector = new int[voc.size()];
        for (int i = 0; i < voc.size(); i++) {
            vector[i] = countWord(text, voc.get(i));
        }
        return vector;
    }

    /**
     * Display similar files
     *
     * @param vectors          vectors list
     * @param directoryListing files
     */
    private static void showDuplicates(List<int[]> vectors, File[] directoryListing) {
        int nb = 0;
        for (int i = 0; i < vectors.size(); i++) {
            int[] vector = vectors.get(i);
            for (int j = 0; j < vectors.size(); j++) {
                if (i != j && Arrays.equals(vector, vectors.get(j))) {
                    nb++;
                    System.out.println("Similar files fount : [" + directoryListing[i] + "] and [" + directoryListing[j] + "]");
                    System.out.println(Arrays.toString(vector));
                    System.out.println(Arrays.toString(vectors.get(j)));
                }
            }
        }
        System.out.println("Checking completed : " + nb / 2 + " similarities fount.");
    }
}
