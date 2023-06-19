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
    private final CacheManager cacheManager;

    public WordFrequencyController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

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
            WFResponse cachedResponse = new WFResponse(new ArrayList<>(cachedResult.keySet()),
                    new ArrayList<>(cachedResult.values()));
            return ResponseEntity.ok(cachedResponse);
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

            return ResponseEntity.ok(createResponse(topKFrequencies));

        } catch (IOException e) {
            logger.error("An error occurred while processing the uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

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

            WFResponse response = new WFResponse(new ArrayList<>(topKFrequencies.keySet()),
                    new ArrayList<>(topKFrequencies.values()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("An error occurred while processing the uploaded file chunk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private Map<String, Integer> getTopKFrequencies(Map<String, Integer> wordFrequencies, int k) {
        Map<String, Integer> topKFrequencies = new LinkedHashMap<>();

        wordFrequencies.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(k)
                .forEachOrdered(entry -> topKFrequencies.put(entry.getKey(), entry.getValue()));

        return topKFrequencies;
    }

    private String generateCacheKey(MultipartFile file, int k, long offset) {
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        return fileName + "-" + fileSize + "-" + k + "-" + offset;
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

    private void cacheResult(String cacheKey, Map<String, Integer> result, long offset) {
        Cache cache = cacheManager.getCache("wordFrequencies");
        if (cache != null) {
            cache.put(cacheKey + "-" + offset, result);
        }
    }

    private WFResponse createResponse(Map<String, Integer> map) {
        // Create WFResponse object with words and frequencies
        return new WFResponse(new ArrayList<>(map.keySet()),
                new ArrayList<>(map.values()));
    }

}