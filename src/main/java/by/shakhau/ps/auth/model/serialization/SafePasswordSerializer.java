package by.shakhau.ps.auth.model.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class SafePasswordSerializer extends JsonSerializer<StringBuilder> {

    @Override
    public void serialize(
            StringBuilder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        int length = value.length();
        char[] data = new char[length];
        value.getChars(0, length, data, 0);
        gen.writeString(data, 0, length);
        Arrays.fill(data, '\0');
    }
}
