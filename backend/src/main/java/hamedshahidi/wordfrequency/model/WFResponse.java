package hamedshahidi.wordfrequency.model;

import java.util.List;

public class WFResponse {
    private List<String> words;
    private List<Integer> frequencies;

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
