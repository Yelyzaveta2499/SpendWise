package com.example.SpendWise.controller;

import com.example.SpendWise.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TagControllerUnitTest {

    private TagService tagService;
    private TagController tagController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        tagService = mock(TagService.class);
        tagController = new TagController(tagService);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("alice");
    }

    @Test
    void getTagStats_returnsStatsFromService() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("usageCount", 5);

        when(tagService.getTagStats("alice", 42L)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = tagController.getTagStats(42L, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(stats, response.getBody());
        verify(tagService).getTagStats("alice", 42L);
    }
}

