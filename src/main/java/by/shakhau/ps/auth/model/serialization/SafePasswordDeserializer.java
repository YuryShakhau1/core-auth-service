package by.shakhau.ps.auth.model.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SafePasswordDeserializer extends JsonDeserializer<StringBuilder> {

    @Override
    public StringBuilder deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        char[] source = p.getTextCharacters();
        int offset = p.getTextOffset();
        int length = p.getTextLength();

        char[] data = new char[length];
        System.arraycopy(source, offset, data, 0, length);

        return new StringBuilder().append(data);
    }
}
