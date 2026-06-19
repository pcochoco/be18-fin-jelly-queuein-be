package com.beyond.qiin.domain.accounting.exception;

import com.beyond.qiin.common.exception.BaseException;
import com.beyond.qiin.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class SettlementException extends BaseException {

    private SettlementException(final SettlementErrorCode code) {
        super(code);
    }

    public static SettlementException notFound() {
        return new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND);
    }

    public static SettlementException invalidAssetName() {
        return new SettlementException(SettlementErrorCode.INVALID_ASSET_NAME);
    }

    @Getter
    public enum SettlementErrorCode implements ErrorCode {
        SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_NOT_FOUND", "해당 정산 정보를 찾을 수 없습니다."),
        INVALID_ASSET_NAME(HttpStatus.BAD_REQUEST, "INVALID_ASSET_NAME", "존재하지 않는 자원명입니다.");

        private final HttpStatus status;
        private final String error;
        private final String message;

        SettlementErrorCode(final HttpStatus status, final String error, final String message) {
            this.status = status;
            this.error = error;
            this.message = message;
        }
    }
}
