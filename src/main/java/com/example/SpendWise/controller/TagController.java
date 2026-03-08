package com.example.SpendWise.controller;

import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // Get all tags for authenticated user
    @GetMapping
    public List<TagEntity> listTags(Authentication authentication) {
        String username = authentication.getName();
        return tagService.getTagsForUser(username);
    }

    // Get a specific tag by ID
    @GetMapping("/{id}")
    public ResponseEntity<TagEntity> getTag(@PathVariable("id") Long id,
                                           Authentication authentication) {
        String username = authentication.getName();
        TagEntity tag = tagService.getTagById(username, id);
        return ResponseEntity.ok(tag);
    }

    // Create a new tag
    @PostMapping
    public ResponseEntity<TagEntity> createTag(@RequestBody Map<String, Object> body,
                                              Authentication authentication) {
        String username = authentication.getName();
        TagEntity created = tagService.createTagForUser(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Update a tag
    @PutMapping("/{id}")
    public ResponseEntity<TagEntity> updateTag(@PathVariable("id") Long id,
                                              @RequestBody Map<String, Object> body,
                                              Authentication authentication) {
        String username = authentication.getName();
        TagEntity updated = tagService.updateTagForUser(username, id, body);
        return ResponseEntity.ok(updated);
    }

    // Delete a tag
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable("id") Long id,
                                          Authentication authentication) {
        String username = authentication.getName();
        tagService.deleteTagForUser(username, id);
        return ResponseEntity.noContent().build();
    }

    // Get tag statistics (usage count)
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getTagStats(@PathVariable("id") Long id,
                                                           Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> stats = tagService.getTagStats(username, id);
        return ResponseEntity.ok(stats);
    }

    // Exception handlers
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({SecurityException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}

