package ru.chipization.achip.dto.search;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SearchRequest {
    private String key;
    private Object value;
}
