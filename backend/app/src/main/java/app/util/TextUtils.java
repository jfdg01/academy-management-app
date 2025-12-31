package app.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility for text normalization and accent/case-insensitive searches
 */
public class TextUtils {
    
    // Pattern to remove diacritical marks (accents, tildes, etc.)
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    /**
     * Normalizes text by removing accents and converting to lowercase
     * @param text The text to normalize
     * @return The normalized text, or null if input is null
     */
    public static String normalizar(String text) {
        if (text == null) {
            return null;
        }
        
        // Convert to lowercase
        String textLowercase = text.toLowerCase();
        
        // Normalize to separate base characters from diacritics
        String normalized = Normalizer.normalize(textLowercase, Normalizer.Form.NFD);
        
        // Remove diacritics
        String withoutAccents = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        
        return withoutAccents.trim();
    }
    
    /**
     * Checks if a text contains another text, ignoring accents and case
     * @param text The text to search in
     * @param search The text to search for
     * @return true if the text contains the search term
     */
    public static boolean contiene(String text, String search) {
        if (text == null || search == null) {
            return false;
        }
        
        String normalizedText = normalizar(text);
        String normalizedSearch = normalizar(search);
        
        return normalizedText.contains(normalizedSearch);
    }
    
    /**
     * Checks if two texts are equal, ignoring accents and case
     * @param text1 The first text
     * @param text2 The second text
     * @return true if the texts are equivalent
     */
    public static boolean sonIguales(String text1, String text2) {
        if (text1 == null && text2 == null) {
            return true;
        }
        
        if (text1 == null || text2 == null) {
            return false;
        }
        
        String normalizedText1 = normalizar(text1);
        String normalizedText2 = normalizar(text2);
        
        return normalizedText1.equals(normalizedText2);
    }
    
    /**
     * Prepares a search term for use in LIKE queries with wildcards
     * @param term The term to prepare
     * @return The normalized term ready for LIKE
     */
    public static String prepararParaLike(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        
        return normalizar(term);
    }
}
