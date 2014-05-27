package org.transpeg;

public class UniCharset {
	public final static UniCharset WhiteSpace = new UniCharset(" \t");
	public final static UniCharset WhiteSpaceNewLine = new UniCharset(" \t\n");
	public final static UniCharset SemiColon = new UniCharset(" \t\n;");
	public final static UniCharset Letter = new UniCharset("A-Za-z_");
	public final static UniCharset NameSymbol = new UniCharset("A-Za-z0-9_");
	public final static UniCharset NotWhiteSpaceNewLine = new UniCharset("!-:<-~");
	public final static UniCharset NodeLabel = new UniCharset("A-Za-z0-9_.");

	String    charSet;
	boolean[] asciiSet;
	UniMap<String> utfBitMap = null;

	public UniCharset(String charSet) {
		this.charSet = charSet;
		this.asciiSet = new boolean[128];
		this.parse(charSet);
	}

	public final boolean match(char ch) {
		if(ch < 128) {
			return this.asciiSet[ch];
		}
		if(this.utfBitMap != null) {
			return this.utfBitMap.hasKey(Main._CharToString(ch));
		}
		return false;
	}

	private void set(char ch) {
		if(ch < 128) {
			//System.out.println("charSet='"+this.charSet+"' : ch = '" + ch + "'");
			this.asciiSet[ch] = true;
		}
		else {
			if(this.utfBitMap == null) {
				this.utfBitMap = new UniMap<String>();
			}
			String key = Main._CharToString(ch);
			this.utfBitMap.put(key, key);
		}
	}

	private void parse(String text) {
		CharacterReader r = new CharacterReader(text);
		char ch = r.readChar();
		while(ch != 0) {
			char next = r.readChar();
			if(next == '-') {
				this.set2(ch, r.readChar());
				ch = r.readChar();
			}
			else {
				this.set(ch);
				ch = next;
			}
		}
	}

	private void set2(int ch, int ch2) {
		for(;ch <= ch2; ch++) {
			this.set((char)ch);
		}
	}

	public static final String _QuoteString(char OpenChar, String Text, char CloseChar) {
		char SlashChar = '\\';
		StringBuilder sb = new StringBuilder();
		sb.append(OpenChar);
		int i = 0;
		for(; i < Text.length(); i = i + 1) {
			char ch = Main._GetChar(Text, i);
			if(ch == '\n') {
				sb.append(SlashChar);
				sb.append("n");
			}
			else if(ch == '\t') {
				sb.append(SlashChar);
				sb.append("t");
			}
			else if(ch == CloseChar) {
				sb.append(SlashChar);
				sb.append(ch);
			}
			else if(ch == '\\') {
				sb.append(SlashChar);
				sb.append(SlashChar);
			}
			else {
				sb.append(ch);
			}
		}
		sb.append(CloseChar);
		return sb.toString();
	}

	public static final String _QuoteString(String OpenQuote, String Text, String CloseQuote) {
		StringBuilder sb = new StringBuilder();
		sb.append(OpenQuote);
		int i = 0;
		for(; i < Text.length(); i = i + 1) {
			char ch = Main._GetChar(Text, i);
			if(ch == '\n') {
				sb.append("\\n");
			}
			else if(ch == '\t') {
				sb.append("\\t");
			}
			else if(ch == '"') {
				sb.append("\\\"");
			}
			else if(ch == '\'') {
				sb.append("\\'");
			}
			else if(ch == '\\') {
				sb.append("\\\\");
			}
			else {
				sb.append(ch);
			}
		}
		sb.append(CloseQuote);
		return sb.toString();
	}

	public static final String _UnquoteString(String text) {
		if(text.indexOf("\\") == -1) {
			return text;
		}
		CharacterReader r = new CharacterReader(text);
		StringBuilder sb = new StringBuilder();
		while(r.hasChar()) {
			char ch = r.readChar();
			if(ch == '0') {
				break;
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	public static final String _QuoteString(String Text) {
		return UniCharset._QuoteString("\"", Text, "\"");
	}

	public final static double _ParseFloat(String Text) {
		try {
			return Double.parseDouble(Text);
		}
		catch(NumberFormatException e) {
			//ZLogger.VerboseException(e);
		}
		return 0.0;
	}

	public final static long _ParseInt(String Text) {
		try {
			return Long.parseLong(Text);
		}
		catch(NumberFormatException e) {
			//ZLogger.VerboseException(e);
		}
		return 0L;
	}

}

class CharacterReader {
	String text;
	int pos;
	public CharacterReader(String text) {
		this.text = text;
		this.pos = 0;
	}

	public boolean hasChar() {
		return (pos < this.text.length());
	}

	private char read(int pos) {
		if(pos < this.text.length()) {
			return Main._GetChar(this.text, pos);
		}
		return 0;
	}

	char readChar() {
		if(this.pos < this.text.length()) {
			char ch = this.read(this.pos);
			if(ch == '\\') {
				char ch1 = this.read(this.pos+1);
				if(ch1 == 'u' || ch1 == 'U') {
					ch = this.readUtf(this.read(this.pos+2), this.read(this.pos+3), this.read(this.pos+4), this.read(this.pos+5));
					this.pos = this.pos + 5;
				}
				else {
					ch = this.readEsc(ch1);
					this.pos = this.pos + 1;
				}
			}
			this.pos = this.pos + 1;
			return ch;
		}
		return '\0';
	}

	private char readEsc(char ch1) {
		switch (ch1) {
		case 'a':  return '\007'; /* bel */
		case 'b':  return '\b';  /* bs */
		case 'e':  return '\033'; /* esc */
		case 'f':  return '\f';   /* ff */
		case 'n':  return '\n';   /* nl */
		case 'r':  return '\r';   /* cr */
		case 't':  return '\t';   /* ht */
		case 'v':  return '\013'; /* vt */
		}
		return ch1;
	}

	private int hex(int c) {
		if('0' <= c && c <= '9') {
			return c - '0';
		}
		if('a' <= c && c <= 'f') {
			return c - 'a' + 10;
		}
		if('A' <= c && c <= 'F') {
			return c - 'A' + 10;
		}
		return 0;
	}

	private char readUtf(char ch1, char ch2, char ch3, char ch4) {
		int c = this.hex(ch1);
		c = (c * 16) + this.hex(ch2);
		c = (c * 16) + this.hex(ch3);
		c = (c * 16) + this.hex(ch4);
		return (char)c;
	}
}

