package com.cardpro.card.service;

import com.cardpro.card.repository.CardProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SlugService {

    // Regex: Only lowercase letters, numbers, and hyphens allowed. Must start/end with alphanumeric.
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final CardProfileRepository cardProfileRepository;

    /**
     * Validates slug format and ensures it does not already exist in the database.
     *
     * @param slug The slug to validate
     * @throws RuntimeException if the slug is invalid or already exists
     */
    public void validateSlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new RuntimeException("Slug cannot be null or empty.");
        }

        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new RuntimeException("Invalid slug format. Slugs can only contain lowercase letters, numbers, and hyphens, and cannot start or end with a hyphen.");
        }

        // Ensure uniqueness using your repository method
        if (cardProfileRepository.existsBySlug(slug)) {
            throw new RuntimeException("The slug '" + slug + "' is already in use.");
        }
    }
}