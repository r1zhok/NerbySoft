package org.library.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.payload.NewBookPayload;
import org.library.app.service.BooksService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/library-api/books")
public class BooksRestController {

    private final BooksService service;

    @GetMapping("/list")
    @Operation(
            summary = "Повертає список книжок",
            description = "Повертає список книжок"

    )
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        return ResponseEntity.ok(this.service.getAllBooks());
    }

    @GetMapping("/{bookId:\\d+}")
    @Operation(
            summary = "Повертає книжку по id",
            description = "Повертає книжку по id, коли книжка не найдена, вертається 404",
            responses = {
                    @ApiResponse(
                            description = "запит успішний",
                            responseCode = "200",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "title", value = String.class),
                                                            @StringToClassMapItem(key = "author", value = String.class),
                                                            @StringToClassMapItem(key = "amount", value = int.class)
                                                    }
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            description = "запит не успішний, об'єкт не знайдено",
                            responseCode = "404",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<BookDTO> getBookById(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.ok(this.service.getBookById(bookId));
    }

    @GetMapping("/all-borrowed-books-count")
    @Operation(
            summary = "Повертає список назв книжок і скільки раз їх брали користувачі",
            description = "Повертає список назв книжок і скільки раз їх брали користувачі",
            responses = {
                    @ApiResponse(
                            description = "запит успішний",
                            responseCode = "200",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "title", value = String.class),
                                                            @StringToClassMapItem(key = "author", value = String.class),
                                                            @StringToClassMapItem(key = "amount", value = int.class)
                                                    }
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            description = "запит не успішний, об'єкт не знайдено",
                            responseCode = "404",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<List<String>> getAllBorrowedBooksAndCountWasBorrowed() {
        return ResponseEntity.ok(this.service.getAllBorrowedBooksAndCountWasBorrowed());
    }

    @GetMapping("/all-borrowed-books")
    @Operation(
            summary = "Повертає список унікальних назв книжок",
            description = "Повертає список унікальних назв книжок"
    )
    public ResponseEntity<List<String>> getAllDistinctBorrowedBooks() {
        return ResponseEntity.ok(this.service.getAllDistinctBorrowedBooks());
    }

    @PostMapping
    @Operation(
            summary = "Створює книгу",
            description = "Створює книгу. Якщо валідація не успішна, вертає 400 статус, якщо книга вже існує, то збільше її к-сть на 1",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @StringToClassMapItem(key = "title", value = String.class),
                                            @StringToClassMapItem(key = "author", value = String.class),
                                    }
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "запит успішний",
                            responseCode = "201",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "title", value = String.class),
                                                            @StringToClassMapItem(key = "author", value = String.class),
                                                            @StringToClassMapItem(key = "amount", value = int.class)
                                                    }
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            description = "запит не успішний, валідація даних не пройдена успішно",
                            responseCode = "400",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody NewBookPayload newBook,
                                              BindingResult bindingResult)
            throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.service.createBook(newBook));
        }
    }

    @PatchMapping("/{bookId:\\d+}")
    @Operation(
            summary = "Оновлює інформацію про книгу",
            description = "Оновлює інформацію про книгу. Якщо валідація не успішна, або користувача не існує, вертає 400 статус.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @StringToClassMapItem(key = "title", value = String.class),
                                            @StringToClassMapItem(key = "author", value = String.class),
                                    }
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Запит виконався успішно, інформація оновилась",
                            responseCode = "204"
                    ),
                    @ApiResponse(
                            description = "Вертає BindException при невдалій валідації даних",
                            responseCode = "400",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            description = "Вертає NoSuchElementException, коли книжку не найдено",
                            responseCode = "404",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<Void> updateBook(@PathVariable("bookId") Long bookId,
                                           @Valid @RequestBody NewBookPayload newBook,
                                           BindingResult bindingResult
    ) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.service.updateBook(bookId, newBook);
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping("/{bookId:\\d+}")
    @Operation(
            summary = "Оновлює інформацію про книгу",
            description = "Оновлює інформацію про книгу. Якщо валідація не успішна, або користувача не існує, вертає 400 статус.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @StringToClassMapItem(key = "title", value = String.class),
                                            @StringToClassMapItem(key = "author", value = String.class),
                                    }
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Запит виконався успішно, к-сть певної кники зменшується на 1",
                            responseCode = "204"
                    ),
                    @ApiResponse(
                            description = "Вертає BookIsOverException, коли к-сть книжок дорівнює 0",
                            responseCode = "400",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            description = "Вертає NoSuchElementException, коли користувача не найдено",
                            responseCode = "404",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "error", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<Void> deleteBook(@PathVariable("bookId") Long bookId) {
        this.service.deleteBook(bookId);
        return ResponseEntity.noContent()
                .build();
    }
}