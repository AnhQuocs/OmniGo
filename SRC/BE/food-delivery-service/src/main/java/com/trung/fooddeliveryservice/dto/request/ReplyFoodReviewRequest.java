package com.trung.fooddeliveryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyFoodReviewRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String reply;
}
