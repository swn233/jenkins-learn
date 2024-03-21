package com.example.backend.entity.vo.response;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ConfirmResetVO {
    @Email
    String email;
    @Length(max=6,min=6)
    String code;
}
