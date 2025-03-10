package com.example.wayhome.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDTO {

    @NotBlank
    private String account;

    @NotBlank
    private String password;

}
