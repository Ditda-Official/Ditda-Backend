package ditda.backend.domain.commission.draft.processor;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.draft.exception.WatermarkCallbackErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatermarkCallbackVerifier {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final long TOLERANCE_MILLIS = 5 * 60 * 1000L;        // 5분

	private final WatermarkProperties watermarkProperties;

	public void verify(String signature, String timestamp, String rawBody) {

		if (isBlank(signature) || isBlank(timestamp)) {
			throw new GeneralException(WatermarkCallbackErrorCode.INVALID_REQUEST);
		}

		long ts;
		try {
			ts = Long.parseLong(timestamp);
		} catch (NumberFormatException e) {
			throw new GeneralException(WatermarkCallbackErrorCode.INVALID_REQUEST);
		}

		// 재전송 방지 (5분 이내만 허용)
		if (Math.abs(System.currentTimeMillis() - ts) > TOLERANCE_MILLIS) {
			throw new GeneralException(WatermarkCallbackErrorCode.EXPIRED_TIMESTAMP);
		}

		String expected = sign(timestamp + "." + rawBody);
		if (!MessageDigest.isEqual(
			expected.getBytes(StandardCharsets.UTF_8),
			signature.getBytes(StandardCharsets.UTF_8))) {
			throw new GeneralException(WatermarkCallbackErrorCode.INVALID_SIGNATURE);
		}
	}

	private String sign(String data) {

		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(
				watermarkProperties.callback().secret().getBytes(StandardCharsets.UTF_8),
				HMAC_ALGORITHM
			));
			return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException | InvalidKeyException exception) {
			throw new IllegalStateException("HMAC 계산 실패", exception);
		}
	}

	private boolean isBlank(String string) {
		return string == null || string.isBlank();
	}
}
