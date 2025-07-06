
package com.example.springmcp.repository;

import com.example.springmcp.model.UrlEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlEntryRepository extends JpaRepository<UrlEntry, Long> {
    UrlEntry findByShortUrl(String shortUrl);
}
