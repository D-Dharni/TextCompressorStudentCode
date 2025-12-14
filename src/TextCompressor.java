/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Deven Dharni
 */



public class TextCompressor {
    // Variable for initial ASCII
    private static final int EOF = 128;

    // Number of bits per code
    private static final int bits = 10;

    // Maximum number of codes to store in the TST
    private static final int maximumNumCodes = 1024;

    private static void compress() {
        // Read in full text
        String text = BinaryStdIn.readString();

        // Create TST for storage
        TST wordBank = new TST();

        // Add all the initial characters to TST
        for (int i = 0; i < EOF; i++) {
            char character = (char) i;
            wordBank.insert("" + character, i);
        }

        // Add one to save 128/hexadecimal 80 for EOF
        int nextValue = EOF + 1;
        int currentIndex = 0;

        while (currentIndex < text.length()) {
            // Find the largest prefix match
            String prefix = wordBank.getLongestPrefix(text, currentIndex);

            // Get the code for that prefix and write it out
            int code = wordBank.lookup(prefix);
            BinaryStdOut.write(code, bits);

            int positionOfNextCharacter = currentIndex + prefix.length();

            // Add another code if we have space in TST
            if (positionOfNextCharacter < text.length() && nextValue < maximumNumCodes) {
                char characterToAdd = text.charAt(positionOfNextCharacter);
                wordBank.insert(prefix + characterToAdd, nextValue);
                nextValue++;
            }

            // Increment by the right size
            currentIndex += prefix.length();
        }

        // Write the end of file
        BinaryStdOut.write(EOF, bits);
        BinaryStdOut.close();
    }

    private static void expand() {
        // Create an array of codes of set size
        String[] currentCodes = new String[maximumNumCodes];

        // Add all the initial characters to TST
        for (int i = 0; i < EOF; i++) {
            char character = (char) i;
            currentCodes[i] = "" + character;
        }

        // Add one to save 128/hexadecimal 80 for EOF
        int nextValue = EOF + 1;

        // Read the first 8 bits for the int
        int code = BinaryStdIn.readInt(bits);

        // Add the first code to the prefix that has already been decoded
        String prefix = currentCodes[code];
        BinaryStdOut.write(prefix);

        // Keep a constant loop until break
        while (true) {
            // Read in another 8 bits
            code = BinaryStdIn.readInt(bits);

            // Base case
            if (code == EOF) {
                break;
            }

            String current = "";

            // Normal case
            if (currentCodes[code] != null) {
                current = currentCodes[code];
            }
            // Edge case, reading in a code that doesn't yet exist
            else {
                // Know what compressor added and what prefix was
                current = prefix + prefix.charAt(0);
            }

            BinaryStdOut.write(current);

            // Add the next dictionary value
            if (nextValue < maximumNumCodes) {
                // Add the next character
                currentCodes[nextValue] = prefix + current.charAt(0);
                nextValue++;
            }

            prefix = current;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
