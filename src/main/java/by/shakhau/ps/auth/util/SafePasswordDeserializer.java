package by.shakhau.ps.auth.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.nio.CharBuffer;

public class SafePasswordDeserializer extends JsonDeserializer<CharBuffer> {

    @Override
    public CharBuffer deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        char[] source = p.getTextCharacters();
        int offset = p.getTextOffset();
        int length = p.getTextLength();

        char[] passwordArray = new char[length];
        System.arraycopy(source, offset, passwordArray, 0, length);

        return CharBuffer.wrap(passwordArray);
    }
}
