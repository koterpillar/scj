package org.nebohodimo.scj;

/**
 * Yandex.XML error codes
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public enum YandexErrorCode {
	UNKNOWN(0), SYNTAX_ERROR(1), EMPTY_REQUEST(2), ZONE_NOT_INDEXED(8), ATTRIBUTE_NOT_INDEXED(
			9), ATTRIBUTE_AND_ELEMENT_NOT_COMPATIBLE(10), PREVIOUS_REQUEST_DELETED(
			11), NOTHING_FOUND(15), XML_ERROR(18), INCORRECT_PARAMETERS(19), UNKNOWN_ERROR(
			20);

	private final int code_;

	public int getCode() {
		return code_;
	}

	YandexErrorCode(int code) {
		code_ = code;
	}

	public static YandexErrorCode valueOf(int code) {
		for (YandexErrorCode value : values()) {
			if (value.getCode() == code)
				return value;
		}
		return UNKNOWN;
	}
}