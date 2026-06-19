package com.sakny.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * JSON-serializable {@link Page} implementation.
 *
 * <p>Spring's {@link PageImpl} has no default constructor and does not round-trip through JSON, which
 * breaks Redis caching of paged results. This wrapper adds a {@link JsonCreator} constructor so cached
 * listings deserialize correctly. It still <em>is</em> a {@code Page}, so callers and API responses are
 * unaffected.
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
public class RestPage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(@JsonProperty("content") List<T> content,
                    @JsonProperty("number") int number,
                    @JsonProperty("size") int size,
                    @JsonProperty("totalElements") long totalElements) {
        super(content, PageRequest.of(size <= 0 ? 0 : number, size <= 0 ? 1 : size), totalElements);
    }

    public RestPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestPage(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }
}
