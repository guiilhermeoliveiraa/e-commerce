package com.javacore.spring_api_app.domain.name;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class NameNormalizer {

    private NameNormalizer() {}

    public static String normalize(String name) {
        String[] words = name.trim().toLowerCase().split("\\s+");

        return Arrays
                .stream(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
