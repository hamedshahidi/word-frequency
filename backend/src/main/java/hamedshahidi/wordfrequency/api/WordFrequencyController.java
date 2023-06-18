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
import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
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

@RestController
public class WordFrequencyController {

    private static final Logger logger = LoggerFactory.getLogger(WordFrequencyController.class);

    @Autowired
    private CacheManager cacheManager;

    @PostMapping("/upload")
    public ResponseEntity<WFResponse> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("k") @Positive int k) {

        Map<String, Integer> wordFrequencies = new HashMap<>();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Check if result is already cached
        String cacheKey = generateCacheKey(file, k);
        Map<String, Integer> cachedResult = getCachedResult(cacheKey);
        if (cachedResult != null) {
            WFResponse response = new WFResponse(new ArrayList<>(cachedResult.keySet()),
                    new ArrayList<>(cachedResult.values()));
            return ResponseEntity.ok(response);
        }

        try {
            // Save the file to a temporary location
            Path tempFilePath = Files.createTempFile(null, null);
            file.transferTo(tempFilePath.toFile());

            // Read file content
            String text = Files.readString(tempFilePath, StandardCharsets.UTF_8);

            // Process file content to calculate word frequencies
            String[] words = StringUtils.tokenizeToStringArray(text, " ");
            for (String word : words) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
            }
            // Get top K frequencies
            Map<String, Integer> topKFrequencies = getTopKFrequencies(wordFrequencies, k);

            // Delete temporary file
            Files.delete(tempFilePath);

            // Cache the result
            cacheResult(cacheKey, topKFrequencies);

            // Create WFResponse object with words and frequencies
            WFResponse response = new WFResponse(new ArrayList<>(topKFrequencies.keySet()),
                    new ArrayList<>(topKFrequencies.values()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("An error occurred while processing the uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private Map<String, Integer> getTopKFrequencies(Map<String, Integer> wordFrequencies, int k) {
        PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>(
                Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));

        // Sort by frequency
        boolean queueIsFull = false;
        for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
            if (!queueIsFull && !maxHeap.offer(entry)) {
                logger.error("Queue is full. Unable to add entry.");
                queueIsFull = true;
            }
        }
        // Get top K frequencies
        Map<String, Integer> topKFrequencies = new LinkedHashMap<>();
        for (int i = 0; i < k && !maxHeap.isEmpty(); i++) {
            Map.Entry<String, Integer> entry = maxHeap.poll();
            topKFrequencies.put(entry.getKey(), entry.getValue());
        }

        return topKFrequencies;
    }

    private String generateCacheKey(MultipartFile file, int k) {
        // Generate a unique cache key based on the file name, file size and k value
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        return fileName + "-" + fileSize + "-" + k;
    }

    private Map<String, Integer> getCachedResult(String cacheKey) {
        Cache cache = cacheManager.getCache("wordFrequencies");
        if (cache != null) {
            ValueWrapper valueWrapper = cache.get(cacheKey);
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

    private void cacheResult(String cacheKey, Map<String, Integer> result) {
        Cache cache = cacheManager.getCache("wordFrequencies");
        if (cache != null) {
            cache.put(cacheKey, result);
        }
    }

}