package ru.chipization.achip.dto.search;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserSearchQuery {
    private String firstName;
    private String lastName;
    private String email;
    private Integer from;
    private Integer size;
}
