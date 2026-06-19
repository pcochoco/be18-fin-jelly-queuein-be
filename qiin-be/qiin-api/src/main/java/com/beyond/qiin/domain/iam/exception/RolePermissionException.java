package com.beyond.qiin.domain.iam.exception;

import com.beyond.qiin.common.exception.BaseException;
import com.beyond.qiin.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class RolePermissionException extends BaseException {

    private RolePermissionException(final RolePermissionErrorCode code) {
        super(code);
    }

    public static RolePermissionException notFound() {
        return new RolePermissionException(RolePermissionErrorCode.ROLE_PERMISSION_NOT_FOUND);
    }

    @Getter
    public enum RolePermissionErrorCode implements ErrorCode {
        ROLE_PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_PERMISSION_NOT_FOUND", "해당 역할 권한 매핑을 찾을 수 없습니다.");

        private final HttpStatus status;
        private final String error;
        private final String message;

        RolePermissionErrorCode(final HttpStatus status, final String error, final String message) {
            this.status = status;
            this.error = error;
            this.message = message;
        }
    }
}
