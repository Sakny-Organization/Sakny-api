package com.sakny.auth.dto;

import com.sakny.common.model.OtpChannel;
import com.sakny.common.model.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    private String code;

    @NotNull
    private OtpChannel channel;

    @NotNull
    private OtpPurpose purpose;
}
