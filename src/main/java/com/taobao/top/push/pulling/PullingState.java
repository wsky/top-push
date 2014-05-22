package com.taobao.top.push.pulling;

public enum PullingState {
	UNKNOWN,
	TRUE,
	FALSE,
	NO_VALID_CONNECTION,
	AMOUNT_ZERO,
	STEP_ZERO,
	MAX_PENDING,
	LOCK,

	BREAK,
	LESS_THAN_AMOUNT,
	CONTINUE
}
