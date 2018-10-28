package com.bradf.springreact.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotBlank
    private String accessToken;
}
