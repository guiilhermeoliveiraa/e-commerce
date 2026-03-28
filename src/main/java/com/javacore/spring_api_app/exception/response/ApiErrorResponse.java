package com.javacore.spring_api_app.exception.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class ApiErrorResponse {

    @Builder.Default
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private Instant timestamp = Instant.now();
    private Integer status;
    private String error;
    private String message;
    private String errorCode;

    @Builder.Default
    private List<String> details = List.of();
    private String path;
}
