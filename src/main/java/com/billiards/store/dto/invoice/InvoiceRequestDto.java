package com.billiards.store.dto.invoice;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for creating an invoice")
public class InvoiceRequestDto {
    @Schema(description = "Code of the play session to bill", example = "SES000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionCode;
}
