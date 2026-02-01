package com.backend.skillswap.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//  Simple wrapper for API success messages or confirmations. Ex: {"message": "Booking created successfully"}
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiMessageResponse {

    private String message;
}


// Ye class sirf success / simple confirmation ke liye best hai