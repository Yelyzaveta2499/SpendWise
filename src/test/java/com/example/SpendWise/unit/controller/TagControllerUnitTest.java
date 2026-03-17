package com.example.SpendWise.unit.controller;

import com.example.SpendWise.controller.TagController;
import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
        when(authentication.getName()).thenReturn("indiv");
    }

    @Test
    void listTags_returnsTagsForUser() {
        TagEntity tag = new TagEntity();
        tag.setId(1L);
        tag.setName("Food");

        when(tagService.getTagsForUser("indiv")).thenReturn(List.of(tag));

        List<TagEntity> result = tagController.listTags(authentication);

        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getName());
    }

    @Test
    void getTagStats_returnsStatsForUserAndTag() {
        when(tagService.getTagStats("indiv", 10L)).thenReturn(Map.of("usageCount", 3));

        ResponseEntity<Map<String, Object>> response = tagController.getTagStats(10L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.get("usageCount"));
    }

    @Test
    void handleBadRequest_returnsBadRequestStatusAndMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid");

        ResponseEntity<Map<String, String>> response = tagController.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Invalid", body.get("error"));
    }

    @Test
    void handleForbidden_returnsForbiddenStatusAndMessage() {
        SecurityException ex = new SecurityException("Not allowed");

        ResponseEntity<Map<String, String>> response = tagController.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Not allowed", body.get("error"));
    }
}
