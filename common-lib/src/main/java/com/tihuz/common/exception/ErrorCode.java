package com.tihuz.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {


    //USER-SERVICE
    USER_INVALID(4001, "User Invalid.", HttpStatus.BAD_REQUEST),
    USER_EXITS(4001, "User exits.", HttpStatus.BAD_REQUEST),
    USER_NOT_EXITS(4001, "User not exits.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(4002, "Password Invalid.", HttpStatus.BAD_REQUEST),
    COMPANY_NAME_REQUIRED(400, "Company name is required for company registration", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(400, "Email already exists.", HttpStatus.BAD_REQUEST),

    // APPLICATION-SERVICE
    APPLICATION_ALREADY_EXISTS(1008, "Application already exists", HttpStatus.BAD_REQUEST),

    APPLICATION_NOT_FOUND(1009, "Application not found", HttpStatus.NOT_FOUND),

    //JOB-SERVICE
    JOB_NOT_FOUND(1010, "Job not found", HttpStatus.NOT_FOUND),
    EXPIRED_DATE_INVALID(1011, "Expired date is invalid", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_JOB_WITH_PENDING_APPLICATIONS(400, "Không thể xóa bài tuyển dụng đang có ứng viên chờ duyệt",HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_JOB(1012, "Cannot delete job because related applications exist or deletion failed.", HttpStatus.BAD_REQUEST),


    //COMPANY-SERVICE
    COMPANY_NOT_FOUND(4002, "Company not found.", HttpStatus.NOT_FOUND),
    COMPANY_ALREADY_EXISTS(4003, "Company already exists.", HttpStatus.BAD_REQUEST),
    COMPANY_SERVICE_ERROR(5001, "Company service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE),
    COMPANY_CREATE_FAILED(5001, "Failed to create company. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR),


    //POST
    POST_INVALID(5000, "Post Invalid.", HttpStatus.BAD_REQUEST),
    POST_NO_EXITS(5001, "Post no exits.", HttpStatus.BAD_REQUEST),


    //GLOBAL
    UNCATEGORIZED_EXCEPTION(500, "uncategorized exception.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_EXCEPTION(401, "Authentication required. User has not logged in.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_EXCEPTION(403, "Access denied. You do not have permission.", HttpStatus.FORBIDDEN),
    INVALID_REQUEST_BODY(400, "Request body is missing or invalid", HttpStatus.BAD_REQUEST),


    //TOKEN
    TOKEN_EXPIRED(401, "Token has expired. Please log in again.", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(401, "Invalid token.", HttpStatus.UNAUTHORIZED),


    //GATEWAY
    API_NOT_FOUND(404, "API Path not found", HttpStatus.NOT_FOUND),


    // SERVICE_ENDPOINT
    INVALID_PARAMETER_TYPE(400, "Invalid parameter type", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_PARAMETER(402, "Missing required parameter", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(405, "HTTP method not allowed for this endpoint", HttpStatus.METHOD_NOT_ALLOWED),


    // SERVICE DOWN
    SERVICE_UNAVAILABLE(503, "Service is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    CONNECTION_REFUSED(503, "Connection refused. Service may be down or unreachable", HttpStatus.SERVICE_UNAVAILABLE),
    POST_SERVICE_DOWN(503, "Post service unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // FALL BACK GATEWAY
    FALLBACK_TRIGGERED(1001, "Fallback executed due to service failure or timeout", HttpStatus.SERVICE_UNAVAILABLE),

    // DEPENDENCY CALL DOWN OR INVALID URL
    DEPENDENCY_FAILED(503, "Dependency service call failed", HttpStatus.SERVICE_UNAVAILABLE),



    ACCESS_DENIED(1011, "You do not have permission", HttpStatus.FORBIDDEN),


    ;


    int code;
    String message;
    HttpStatus status;
}
