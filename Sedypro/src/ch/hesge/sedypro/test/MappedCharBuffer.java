package ch.hesge.sedypro.test;

import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class MappedCharBuffer {

	private MappedByteBuffer buffer = null;
	private CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder();

	/**
	 * TODO documentation
	 * 
	 * @param buffer
	 */
	private MappedCharBuffer(MappedByteBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	public int length() {
		return buffer.limit();
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	public char charAt(int index) {
		return (char) buffer.get(index);
	}

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	public CharSequence subSequence(int start, int end) {
		try {
			int limit = buffer.limit();
			int position = buffer.position();
			buffer.limit(end);
			buffer.position(start);
			CharBuffer result = decoder.decode(buffer);
			buffer.limit(limit);
			buffer.position(position);
			return result;
		}
		catch (CharacterCodingException cce) {
			// FIXME
			throw new RuntimeException(cce);
		}
	}
}
