package com.upply.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Standard error response returned by the API when an exception occurs")
public class ExceptionResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2026-02-12T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP error reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Validation failed")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/resource")
    private String path;

    @Schema(description = "Map of field-level validation errors (field name → error message)", example = "{\"email\": \"must not be blank\"}")
    private Map<String, String> validationErrors;

}
