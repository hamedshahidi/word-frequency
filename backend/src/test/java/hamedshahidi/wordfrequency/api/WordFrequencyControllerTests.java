package hamedshahidi.wordfrequency.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import hamedshahidi.wordfrequency.model.WFResponse;

@WebMvcTest(WordFrequencyController.class)
public class WordFrequencyControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CacheManager cacheManager;

        @Test
        public void testHandleFileUpload_NormalFile() throws Exception {
                // Prepare test data
                String fileContent = "word3 word2 word4 word1 word2 word5 word3 word3 word4 word5 word4 word5 word4 word5 word5";
                MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain",
                                fileContent.getBytes());
                int k = 3;

                // Mock the cached result
                when(cacheManager.getCache(any())).thenReturn(null);

                // Perform the file upload request
                mockMvc.perform(multipart("/upload")
                                .file(file)
                                .param("k", String.valueOf(k))
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.words").isArray())
                                .andExpect(jsonPath("$.words.length()").value(k))
                                .andExpect(jsonPath("$.words[0]").value("word5"))
                                .andExpect(jsonPath("$.words[1]").value("word4"))
                                .andExpect(jsonPath("$.words[2]").value("word3"))
                                .andExpect(jsonPath("$.frequencies").isArray())
                                .andExpect(jsonPath("$.frequencies.length()").value(k))
                                .andExpect(jsonPath("$.frequencies[0]").value(5))
                                .andExpect(jsonPath("$.frequencies[1]").value(4))
                                .andExpect(jsonPath("$.frequencies[2]").value(3));
        }

        @Test
        public void testHandleFileUpload_EmptyFile() throws Exception {
                // Prepare test data
                MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);
                int k = 3;

                // Mock the cached result
                when(cacheManager.getCache(any())).thenReturn(null);

                // Perform the file upload request
                mockMvc.perform(multipart("/upload")
                                .file(file)
                                .param("k", String.valueOf(k))
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testHandleFileUpload_CachedResult() throws Exception {
                // Prepare test data
                String fileContent = "word3 word2 word4 word1 word2 word5 word3 word3 word4 word5 word4 word5 word4 word5 word5";
                MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain",
                                fileContent.getBytes());
                int k = 3;

                // Mock the cached result
                Map<String, Integer> cachedResult = new HashMap<>();
                cachedResult.put("word5", 5);
                cachedResult.put("word4", 4);
                cachedResult.put("word3", 3);

                Cache cache = getMockCache(cachedResult);
                when(cacheManager.getCache(any())).thenReturn(cache);

                // Perform the file upload request
                mockMvc.perform(multipart("/upload")
                                .file(file)
                                .param("k", String.valueOf(k))
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.words").isArray())
                                .andExpect(jsonPath("$.words.length()").value(k))
                                .andExpect(jsonPath("$.words[0]").value("word5"))
                                .andExpect(jsonPath("$.words[1]").value("word4"))
                                .andExpect(jsonPath("$.words[2]").value("word3"))
                                .andExpect(jsonPath("$.frequencies").isArray())
                                .andExpect(jsonPath("$.frequencies.length()").value(k))
                                .andExpect(jsonPath("$.frequencies[0]").value(5))
                                .andExpect(jsonPath("$.frequencies[1]").value(4))
                                .andExpect(jsonPath("$.frequencies[2]").value(3));
        }

        private Cache getMockCache(Map<String, Integer> cachedResult) {
                Cache cache = Mockito.mock(Cache.class);
                ValueWrapper valueWrapper = Mockito.mock(ValueWrapper.class);
                when(cache.get(any())).thenReturn(valueWrapper);
                when(valueWrapper.get()).thenReturn(new WFResponse(new ArrayList<>(cachedResult.keySet()),
                                new ArrayList<>(cachedResult.values())));
                return cache;
        }
}
