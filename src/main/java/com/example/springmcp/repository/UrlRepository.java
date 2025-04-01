package com.example.springmcp.repository;

import com.example.springmcp.model.UrlMapping;
import java.util.Optional;

public interface UrlRepository {
    UrlMapping save(UrlMapping urlMapping);
    Optional<UrlMapping> findByShortKey(String shortKey);
    boolean existsByShortKey(String shortKey);
}
