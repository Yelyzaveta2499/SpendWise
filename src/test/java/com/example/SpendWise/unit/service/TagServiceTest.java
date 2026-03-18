package com.example.SpendWise.unit.service;

import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseTagRepository;
import com.example.SpendWise.model.repository.TagRepository;
import com.example.SpendWise.model.repository.UserRepository;
import com.example.SpendWise.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagServiceTest {

    private TagRepository tagRepository;
    private UserRepository userRepository;
    private ExpenseTagRepository expenseTagRepository;
    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagRepository = mock(TagRepository.class);
        userRepository = mock(UserRepository.class);
        expenseTagRepository = mock(ExpenseTagRepository.class);
        tagService = new TagService(tagRepository, userRepository, expenseTagRepository);
    }

    @Test
    void getTagsForUser_existingUser_returnsTags() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));

        TagEntity tag = new TagEntity();
        tag.setId(10L);
        tag.setUser(user);
        tag.setName("Food");

        when(tagRepository.findByUserOrderByNameAsc(user)).thenReturn(List.of(tag));

        List<TagEntity> result = tagService.getTagsForUser("indiv");

        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getName());
    }

    @Test
    void createTagForUser_withInvalidColor_throwsException() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));

        Map<String, Object> payload = Map.of(
                "name", "Food",
                "color", "not-a-color"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tagService.createTagForUser("indiv", payload));

        assertTrue(ex.getMessage().contains("Color must be a valid hex color"));
    }

    @Test
    void getTagStats_returnsUsageCount() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        TagEntity tag = new TagEntity();
        tag.setId(10L);
        tag.setUser(user);
        tag.setName("Food");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));
        when(tagRepository.findById(10L)).thenReturn(Optional.of(tag));
        when(expenseTagRepository.countByTag(tag)).thenReturn(5L);

        Map<String, Object> stats = tagService.getTagStats("indiv", 10L);

        assertEquals(10L, stats.get("tagId"));
        assertEquals("Food", stats.get("tagName"));
        assertEquals(5L, stats.get("expenseCount"));
    }
}
