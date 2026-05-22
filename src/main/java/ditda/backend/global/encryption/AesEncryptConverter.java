package ditda.backend.global.encryption;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class AesEncryptConverter implements AttributeConverter<String, String> {

	private final AesEncryptor aesEncryptor;

	@Override
	public String convertToDatabaseColumn(String attribute) {
		if (attribute == null) {
			return null;
		}

		return aesEncryptor.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		return aesEncryptor.decrypt(dbData);
	}
}
