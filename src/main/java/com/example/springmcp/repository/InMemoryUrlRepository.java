package com.example.springmcp.repository;

import com.example.springmcp.model.UrlMapping;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUrlRepository implements UrlRepository {
    private final Map<String, UrlMapping> urlStore = new ConcurrentHashMap<>();

    @Override
    public UrlMapping save(UrlMapping urlMapping) {
        urlStore.put(urlMapping.getShortKey(), urlMapping);
        return urlMapping;
    }

    @Override
    public Optional<UrlMapping> findByShortKey(String shortKey) {
        return Optional.ofNullable(urlStore.get(shortKey));
    }

    @Override
    public boolean existsByShortKey(String shortKey) {
        return urlStore.containsKey(shortKey);
    }
}
