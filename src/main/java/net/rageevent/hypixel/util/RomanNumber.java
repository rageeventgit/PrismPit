package net.rageevent.hypixel.util;

public class RomanNumber {

    private static final int[] VALUES = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    private static final String[] SYMBOLS = {
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };

    private RomanNumber() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts an integer (1 to 3999) into a standard Roman numeral.
     *
     * @param number the integer to convert
     * @return the Roman numeral representation
     * @throws IllegalArgumentException if the number is outside the 1-3999 range
     */
    public static String toRoman(int number) {
        if (number < 1 || number > 3999) {
            throw new IllegalArgumentException(
                    "Input must be between 1 and 3999. Received: " + number
            );
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < VALUES.length && number > 0; i++) {
            while (number >= VALUES[i]) {
                number -= VALUES[i];
                result.append(SYMBOLS[i]);
            }
        }

        return result.toString();
    }

}
