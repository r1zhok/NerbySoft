package org.library.app.controller.payload;

import jakarta.validation.constraints.NotBlank;

public record NewMemberPayload(
        @NotBlank(message = "Name is required")
        String name
) {
}
