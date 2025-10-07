package org.siriusxi.htec.fa.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown=true)
public record CreateUserRequest(@JsonProperty("firstName") @NotBlank String firstName,
                                @JsonProperty("lastName") @NotBlank String lastName,
                                @JsonProperty("username") @NotBlank @Email String username,
                                @JsonProperty("password")
                                @NotBlank
                                @Size(min = 10, max = 25,
                                    message = "Password minimum length is 10.")
                                String password,
                                @JsonProperty("rePassword") @NotBlank String rePassword) {
}

