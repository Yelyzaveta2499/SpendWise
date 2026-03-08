package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseTagRepository;
import com.example.SpendWise.model.repository.TagRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TagService {

    private static final String USER_NOT_FOUND_PREFIX = "User not found: ";
    private static final String TAG_NOT_FOUND_PREFIX = "Tag not found: ";

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ExpenseTagRepository expenseTagRepository;

    public TagService(TagRepository tagRepository,
                     UserRepository userRepository,
                     ExpenseTagRepository expenseTagRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.expenseTagRepository = expenseTagRepository;
    }

    /**
     * Get all tags for a user
     */
    public List<TagEntity> getTagsForUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));
        return tagRepository.findByUserOrderByNameAsc(user);
    }

    /**
     * Get a specific tag by ID
     */
    public TagEntity getTagById(String username, Long tagId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException(TAG_NOT_FOUND_PREFIX + tagId));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot access tag that does not belong to user: " + username);
        }

        return tag;
    }

    /**
     * Create a new tag
     */
    @SuppressWarnings("unchecked")
    public TagEntity createTagForUser(String username, Object tagCreateRequest) {
        if (!(tagCreateRequest instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Unsupported request type for tagCreateRequest");
        }
        Map<String, Object> map = (Map<String, Object>) rawMap;

        String name = (String) map.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name is required");
        }
        name = name.trim();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        // Check if tag with this name already exists for user
        if (tagRepository.existsByUserAndName(user, name)) {
            throw new IllegalArgumentException("Tag with name '" + name + "' already exists");
        }

        String color = (String) map.getOrDefault("color", "#3b82f6");
        String description = (String) map.getOrDefault("description", "");

        // Validate color format (basic validation)
        if (color != null && !color.matches("^#[0-9a-fA-F]{6}$")) {
            throw new IllegalArgumentException("Color must be a valid hex color (e.g., #3b82f6)");
        }

        TagEntity tag = new TagEntity(user, name, color, description);
        return tagRepository.save(tag);
    }

    /**
     * Update an existing tag
     */
    @SuppressWarnings("unchecked")
    public TagEntity updateTagForUser(String username, Long tagId, Object updateRequest) {
        if (!(updateRequest instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Unsupported request type for updateRequest");
        }
        Map<String, Object> map = (Map<String, Object>) rawMap;

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException(TAG_NOT_FOUND_PREFIX + tagId));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot update tag that does not belong to user: " + username);
        }

        Object nameRaw = map.get("name");
        if (nameRaw instanceof String name && !name.isBlank()) {
            String trimmedName = name.trim();
            // Check if another tag with this name exists (excluding current tag)
            tagRepository.findByUserAndName(user, trimmedName).ifPresent(existingTag -> {
                if (!existingTag.getId().equals(tagId)) {
                    throw new IllegalArgumentException("Tag with name '" + trimmedName + "' already exists");
                }
            });
            tag.setName(trimmedName);
        }

        Object colorRaw = map.get("color");
        if (colorRaw instanceof String color) {
            if (!color.matches("^#[0-9a-fA-F]{6}$")) {
                throw new IllegalArgumentException("Color must be a valid hex color (e.g., #3b82f6)");
            }
            tag.setColor(color);
        }

        Object descriptionRaw = map.get("description");
        if (descriptionRaw instanceof String description) {
            tag.setDescription(description);
        }

        return tagRepository.save(tag);
    }

    /**
     * Delete a tag
     */
    public void deleteTagForUser(String username, Long tagId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException(TAG_NOT_FOUND_PREFIX + tagId));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete tag that does not belong to user: " + username);
        }

        tagRepository.delete(tag);
    }

    /**
     * Get tag statistics (usage count)
     */
    public Map<String, Object> getTagStats(String username, Long tagId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException(TAG_NOT_FOUND_PREFIX + tagId));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot access tag that does not belong to user: " + username);
        }

        long expenseCount = expenseTagRepository.countByTag(tag);

        Map<String, Object> stats = new HashMap<>();
        stats.put("tagId", tag.getId());
        stats.put("tagName", tag.getName());
        stats.put("expenseCount", expenseCount);

        return stats;
    }
}

