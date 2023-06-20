/**

This class represents the response of a word frequency analysis.
It contains a list of words and their corresponding frequencies.
*/
package hamedshahidi.wordfrequency.model;

import java.util.List;

public class WFResponse {
    private List<String> words;
    private List<Integer> frequencies;

    /**
     * Constructs a new WFResponse object with the specified words and frequencies.
     *
     * @param words       a list of words
     * @param frequencies a list of frequencies corresponding to the words
     */
    public WFResponse(List<String> words, List<Integer> frequencies) {
        this.words = words;
        this.frequencies = frequencies;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public List<Integer> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(List<Integer> frequencies) {
        this.frequencies = frequencies;
    }
}
