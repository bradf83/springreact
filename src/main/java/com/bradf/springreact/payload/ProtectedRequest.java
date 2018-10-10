package com.bradf.springreact.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ProtectedRequest {
    @NotBlank
    @Size(max = 140)
    private String message;
}
