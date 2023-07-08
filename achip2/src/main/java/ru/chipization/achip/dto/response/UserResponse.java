package ru.chipization.achip.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;

    @JsonIgnore
    public String getPassword() {
        return password;
    }
}
