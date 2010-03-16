/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nebohodimo.scj;

import java.io.IOException;

/**
 * Error from Yandex.XML services
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class YandexException extends IOException {
	private static final long serialVersionUID = 1;
	private YandexErrorCode errorCode_;
	private String xml_;

	public String getXml() {
		return xml_;
	}

	public YandexErrorCode getErrorCode() {
		return errorCode_;
	}

	public YandexException(YandexErrorCode errorCode, String message, String xml) {
		super(message);
		errorCode_ = errorCode;
		xml_ = xml;
	}

	public YandexException(String message, String xml) {
		super(message);
		errorCode_ = YandexErrorCode.UNKNOWN;
		xml_ = xml;
	}

	public YandexException(YandexErrorCode errorCode, String message) {
		super(message);
		errorCode_ = errorCode;
	}

	public YandexException(String message) {
		super(message);
		errorCode_ = YandexErrorCode.UNKNOWN;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", errorCode_, getMessage(), getXml());
	}
}
