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
import org.library.app.controller.dto.MemberDTO;
import org.library.app.controller.payload.NewMemberPayload;
import org.library.app.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/library-api/members/{memberId:\\d+}")
public class MemberRestController {

    private final MemberService service;

    @GetMapping
    @Operation(
            summary = "Повертає користувача по id",
            description = "Повертає користувача по id, коли користувач не знайдений, повертається статус 404",
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
                                                            @StringToClassMapItem(key = "name", value = String.class),
                                                            @StringToClassMapItem(key = "creationDate", value = Date.class),
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
    public ResponseEntity<MemberDTO> getMember(@PathVariable("memberId") Long memberId) {
        return ResponseEntity.ok(this.service.getMemberById(memberId));
    }

    @PatchMapping
    @Operation(
            summary = "Оновлює інформацію про користувача",
            description = "Оновлює інформацію про користувача. Якщо валідація не успішна, або користувач з таким іменем існує -- вертає 400 статус, якщо користувача не існує, вертає 404 статус.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @StringToClassMapItem(key = "name", value = String.class)
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
                            description = "Вертає BindException при невдалій валідації даних або MemberAlreadyExistException, коли користувач з таким іменем існує",
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
    public ResponseEntity<Void> updateMember(
            @PathVariable("memberId") Long memberId,
            @Valid NewMemberPayload payload,
            BindingResult bindingResult
    ) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.service.updateMember(payload, memberId);
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    @Operation(
            summary = "Видаляє інформацію про користувача",
            description = "Видаляє інформацію про користувача. Якщо користувач має ще книги - вертається 400 статус, якщо користувача не існує, вертає 404 статус.",
            responses = {
                    @ApiResponse(
                            description = "Запит виконався успішно, інформація видалилась",
                            responseCode = "204"
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
                    ),
                    @ApiResponse(
                            description = "Вертає MemberHasBookException, коли користувач має книжку-и, його видалити не можна",
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
    public ResponseEntity<Void> deleteMember(@PathVariable("memberId") Long memberId) {
        this.service.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/book/{bookId:\\d+}")
    @Operation(
            summary = "Користувач бере книжку",
            description = "Користувач бере книжку. Якщо користувача, або книжки не існує - вертається 404 статус. Якщо користувач має вже цю книжку у власності, або досяг ліміту - вертається 400 статус",
            responses = {
                    @ApiResponse(
                            description = "Запит виконався успішно, Користувач взяв книжку",
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
                            description = "Вертає NoSuchElementException, користувача або книжку не найдено",
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
                    ),
                    @ApiResponse(
                            description = "Вертає MemberAlreadyHaveThisBookException,MemberReachedLimitException, коли користувач має книжку-и, його видалити не можна",
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
    public ResponseEntity<BookDTO> memberBorrowBook(
            @PathVariable("memberId") Long memberId,
            @PathVariable("bookId") Long bookId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.memberBorrowBook(memberId, bookId));
    }

    @DeleteMapping("/book/{bookId:\\d+}")
    @Operation(
            summary = "Користувач вертає книжку",
            description = "Користувач вертає книжку. Якщо користувача, або книжки не існує - вертається 404 статус",
            responses = {
                    @ApiResponse(
                            description = "Запит виконався успішно, Користувач повернув книжку",
                            responseCode = "204"
                    ),
                    @ApiResponse(
                            description = "Вертає NoSuchElementException, користувача або книжку не найдено",
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
    public ResponseEntity<Void> memberReturnBook(
            @PathVariable("memberId") Long memberId,
            @PathVariable("bookId") Long bookId) {
        this.service.memberReturnBook(memberId, bookId);
        return ResponseEntity.noContent().build();
    }
}