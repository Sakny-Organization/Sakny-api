package com.sakny.auth.dto;

import com.sakny.common.model.OtpChannel;
import com.sakny.common.model.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank
    private String identifier;

    @NotNull
    private OtpChannel channel;

    @NotNull
    private OtpPurpose purpose;
}
