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
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/library-api/members")
public class MembersRestController {

    private final MemberService service;

    @GetMapping
    @Operation(
            summary = "Повертає масив з користувачів",
            description = "Повертає масив з користувачів"
    )
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        return ResponseEntity.ok(service.getAllMembers());
    }

    @GetMapping("/{name:\\w*}")
    @Operation(
            summary = "Повертає всі книжки які взяв користувач за його ім'ям",
            description = "Повертає всі книжки які взяв користувач за його ім'ям",
            responses = {
                    @ApiResponse(
                            description = "Запит виконався успішно",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Вертає NoSuchElementException, користувача не найдено",
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
    public ResponseEntity<List<BookDTO>> getAllBooksByMemberName(@PathVariable("name") String memberName) {
        return ResponseEntity.ok(this.service.retrieveAllBookByMemberName(memberName));
    }

    @PostMapping
    @Operation(
            summary = "Створює користувача",
            description = "Створює користувача. Якщо валідація не успішна, або користувач вже існує - вертає 400 статус",
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
                            description = "запит успішний",
                            responseCode = "201",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "name", value = String.class),
                                                            @StringToClassMapItem(key = "creationDate", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            description = "запит не успішний, валідація даних не пройдена успішно, або користувач з такий ім'ям існує",
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
    public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody NewMemberPayload payload,
                                                  BindingResult bindingResult
    ) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.createMember(payload));
        }
    }
}
