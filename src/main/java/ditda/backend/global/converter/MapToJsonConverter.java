package ditda.backend.global.converter;

import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class MapToJsonConverter implements AttributeConverter<Map<String, Object>, String> {

	private static final JsonMapper jsonMapper = new JsonMapper();
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {
		if (attribute == null) {
			return "{}";
		}

		try {
			return jsonMapper.writeValueAsString(attribute);
		} catch (JacksonException e) {
			throw new IllegalArgumentException("Failed to serialize template variables", e);
		}

	}

	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return Map.of();
		}

		try {
			return jsonMapper.readValue(dbData, MAP_TYPE);
		} catch (JacksonException e) {
			throw new IllegalArgumentException("Failed to deserialize template variables", e);
		}
	}

}
