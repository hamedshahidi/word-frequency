package hamedshahidi.wordfrequency.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hamedshahidi.wordfrequency.model.WFResponse;
import jakarta.validation.constraints.Positive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API Controller class that handles word frequency requests.
 */
@RestController
public class WordFrequencyController {

    private static final Logger logger = LoggerFactory.getLogger(WordFrequencyController.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * Handles the file upload request and calculates word frequencies.
     *
     * @param file the uploaded file
     * @param k    the number of top frequencies to retrieve
     * @return a ResponseEntity containing the word frequencies
     */
    @PostMapping("/upload")
    public ResponseEntity<WFResponse> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("k") @Positive int k) {

        // Check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if result is already cached
        String cacheKey = generateCacheKey(file, k, 0);
        Map<String, Integer> cachedResult = getCachedResult(cacheKey);
        if (cachedResult != null) {
            // Return response from cache
            return ResponseEntity.ok(createResponse(cachedResult));
        }

        try {
            // Save the file to a temporary location
            Path tempFilePath = Files.createTempFile(null, null);
            file.transferTo(tempFilePath.toFile());

            // Read file content
            byte[] fileBytes = Files.readAllBytes(tempFilePath);
            String text = new String(fileBytes, StandardCharsets.UTF_8);

            // Process file content to calculate word frequencies
            Map<String, Integer> wordFrequencies = new HashMap<>();
            String[] words = StringUtils.tokenizeToStringArray(text, " ");
            for (String word : words) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
            }

            // Get top K frequencies
            Map<String, Integer> topKFrequencies = getTopKFrequencies(wordFrequencies, k);

            // Delete temporary file
            Files.delete(tempFilePath);

            // Cache the result
            cacheResult(cacheKey, topKFrequencies, 0);

            // Return response
            return ResponseEntity.ok(createResponse(topKFrequencies));

        } catch (IOException e) {
            logger.error("An error occurred while processing the uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Handles the file chunk upload request and calculates word frequencies for the
     * given chunk.
     *
     * @param file   the uploaded file chunk
     * @param offset the offset value indicating the position of the chunk in the
     *               complete file
     * @param k      the number of top frequencies to retrieve
     * @return a ResponseEntity containing the word frequencies for the chunk
     */
    @PostMapping("/upload-chunk")
    public ResponseEntity<WFResponse> handleFileChunkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("offset") long offset,
            @RequestParam("k") @Positive int k) {

        // Check if result is already cached
        String cacheKey = generateCacheKey(file, k, offset);
        Map<String, Integer> cachedResult = getCachedResult(cacheKey);
        if (cachedResult != null) {
            return ResponseEntity.ok(createResponse(cachedResult));
        }

        try {
            // Save the file chunk to a temporary location
            Path tempFilePath = Files.createTempFile(null, null);
            file.transferTo(tempFilePath.toFile());

            // Read file chunk content
            byte[] fileBytes = Files.readAllBytes(tempFilePath);
            String text = new String(fileBytes, StandardCharsets.UTF_8);

            // Process file chunk content to calculate word frequencies
            Map<String, Integer> wordFrequencies = new HashMap<>();
            String[] words = StringUtils.tokenizeToStringArray(text, " ");
            for (String word : words) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
            }

            // Get top K frequencies
            Map<String, Integer> topKFrequencies = getTopKFrequencies(wordFrequencies, k);

            // Delete temporary file chunk
            Files.delete(tempFilePath);

            // Cache the result
            cacheResult(cacheKey, topKFrequencies, offset);

            // Return response
            return ResponseEntity.ok(createResponse(topKFrequencies));

        } catch (IOException e) {
            logger.error("An error occurred while processing the uploaded file chunk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves the top K frequencies from the given word frequencies map.
     *
     * @param wordFrequencies the word frequencies map
     * @param k               the number of top frequencies to retrieve
     * @return a map containing the top K frequencies
     */
    private Map<String, Integer> getTopKFrequencies(Map<String, Integer> wordFrequencies, int k) {
        Map<String, Integer> topKFrequencies = new LinkedHashMap<>();

        wordFrequencies.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(k)
                .forEachOrdered(entry -> topKFrequencies.put(entry.getKey(), entry.getValue()));

        return topKFrequencies;
    }

    /**
     * Generates a cache key based on the file, K value, and offset.
     *
     * @param file   the file
     * @param k      the number of top frequencies
     * @param offset the offset value
     * @return the cache key as a string
     */
    private String generateCacheKey(MultipartFile file, int k, long offset) {
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        return fileName + "-" + fileSize + "-" + k + "-" + offset;
    }

    /**
     * Retrieves the cached result for the given cache key.
     *
     * @param cacheKey the cache key
     * @return the cached result as a map, or null if not found
     */
    private Map<String, Integer> getCachedResult(String cacheKey) {
        Cache cache = cacheManager.getCache("wordFrequencies");
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null) {
                Object value = valueWrapper.get();
                if (value instanceof Map) {
                    Map<String, Integer> cachedResult = (Map<String, Integer>) value;
                    return cachedResult;
                }
            }
        }
        return null;
    }

    /**
     * Caches the result for the given cache key and offset.
     *
     * @param cacheKey the cache key
     * @param result   the result to cache
     * @param offset   the offset value
     */
    private void cacheResult(String cacheKey, Map<String, Integer> result, long offset) {
        Cache cache = cacheManager.getCache("wordFrequencies");
        if (cache != null) {
            cache.put(cacheKey + "-" + offset, result);
        }
    }

    /**
     * Creates a WFResponse object from the given map of word frequencies.
     *
     * @param map the map of word frequencies
     * @return a WFResponse object containing the word frequencies
     */
    private WFResponse createResponse(Map<String, Integer> map) {
        return new WFResponse(new ArrayList<>(map.keySet()),
                new ArrayList<>(map.values()));
    }

}
