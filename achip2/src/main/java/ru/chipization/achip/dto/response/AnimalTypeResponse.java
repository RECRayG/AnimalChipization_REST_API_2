package ru.chipization.achip.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AnimalTypeResponse {
    private Long id;
    private String type;
}
