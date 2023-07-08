package ru.chipization.achip.dto.search;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Request {
    private List<SearchRequest> searchRequest;
}
