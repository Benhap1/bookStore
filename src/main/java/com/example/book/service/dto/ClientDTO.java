package com.example.book.service.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private Boolean enabled;

}

