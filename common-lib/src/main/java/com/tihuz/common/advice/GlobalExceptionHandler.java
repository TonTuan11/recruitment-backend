package com.tihuz.common.advice;


import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
@Getter
public class GlobalExceptionHandler
{

    //500
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlerRuntimeException(RuntimeException exception)
    {
        exception.printStackTrace();


        ErrorCode errorCode=ErrorCode.UNCATEGORIZED_EXCEPTION;

        var apiResponse= ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatus())
                .body(apiResponse);
    }


    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlerAppException(AppException exception)
    {
        ErrorCode errorCode=exception.getErrorCode();

        var apiResponse=ApiResponse.builder()
                .code(errorCode.getCode())
                .message(exception.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatus())
                .body(apiResponse);

    }

    //403
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlerAccessDeniedHandler(AccessDeniedException exception)
    {
        ErrorCode errorCode=ErrorCode.FORBIDDEN_EXCEPTION;

        var apiResponse=ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();


        return ResponseEntity.status(errorCode.getStatus())
                .body(apiResponse);

    }

    // @Valid (@NonNull, @Size,..)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception)
    {
        String enumKey=exception.getFieldError().getDefaultMessage(); //Get the first error and extract its message.

        var apiResponse=ApiResponse.builder()
                .code(400)
                .message(enumKey)
                .build();
        return ResponseEntity.status(exception.getStatusCode())
                .body(apiResponse);
    }

    // Detect Invalid URL (ex: /logins, /posts/sf/tf)
    @ExceptionHandler(value = NoHandlerFoundException.class)
    ResponseEntity<ApiResponse> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        ErrorCode errorCode = ErrorCode.API_NOT_FOUND;
        var apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(apiResponse);
    }


    // Detect data type errors (ex: /user/2s)
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        ErrorCode errorCode=ErrorCode.INVALID_PARAMETER_TYPE;
        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    // 405
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        var apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatus()).body(apiResponse);
    }

    // empty body
    @ExceptionHandler(value= HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<?>> handleEmptyBody(HttpMessageNotReadableException exception)
    {
        ErrorCode errorCode=ErrorCode.INVALID_REQUEST_BODY;
        var apiResponse=ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatus())
                .body(apiResponse);

    }


    // Service down
/*
    @ExceptionHandler(value = ServiceUnavailableException.class)
    ResponseEntity<ApiResponse<?>> handleServiceDown( ServiceUnavailableException exception)
    {
        var apiResponse= ApiResponse.builder()
                .code(503)
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(apiResponse);
    }
*/






    // Invalid router (miss @RequestParam)
//    @ExceptionHandler(value = MissingServletRequestParameterException.class)
//    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MissingServletRequestParameterException ex) {
//
//        ErrorCode errorCode=ErrorCode.MISSING_REQUIRED_PARAMETER;
//        ApiResponse<?> response = ApiResponse.builder()
//                .code(errorCode.getCode())
//                .message(errorCode.getMessage())
//                .build();
//
//        return ResponseEntity.badRequest().body(response);
//    }

}
