package org.library.app.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewBookPayload(

        @NotBlank(message = "Title is required")
        @Size(min = 3, message = "Title must be at least 3 characters long")
        @Pattern(regexp = "^[A-Z].*", message = "Title must start with a capital letter")
        String title,

        @NotBlank(message = "Author is required")
        @Pattern(regexp = "^[A-Z][a-z]+ [A-Z][a-z]+$", message = "Author must contain a capitalized name and surname")
        String author
        ) {
}
