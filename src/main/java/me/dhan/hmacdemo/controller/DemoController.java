package me.dhan.hmacdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo Controller", description = "API for demo operations")
public class DemoController {

    @Operation(summary = "Tính tổng của hai số", description = "Nhận hai số nguyên và trả về tổng của chúng")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tính tổng thành công",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "400", description = "Tham số đầu vào không hợp lệ")
    })
    @GetMapping("/sum")
    public int sum(
            @Parameter(description = "Số thứ nhất để cộng", required = true) @RequestParam("a") int a,
            @Parameter(description = "Số thứ hai để cộng", required = true) @RequestParam("b") int b) {
        return a + b;
    }
}
