package org.transpeg;

public class UniStringBuilder {
	public final UniArray<String> SourceList = new UniArray<String>(new String[128]);
	protected int IndentLevel = 0;
	protected String CurrentIndentString = "";
	protected char LastChar   = '\n';
	protected String LineFeed = "\n";
	protected String Tabular  = "   ";

	public UniStringBuilder() {
	}

	public final boolean IsEmpty(String Text) {
		return (Text == null || Text.length() == 0);
	}

	public final void Append(String Source) {
		if(!this.IsEmpty(Source)) {
			this.SourceList.add(Source);
			this.LastChar = Main._GetChar(Source, Source.length()-1);
		}
	}

	public final void Append(String Text, String Text2) {
		this.SourceList.add(Text);
		this.SourceList.add(Text2);
	}

	public final void Append(String Text, String Text2, String Text3) {
		this.SourceList.add(Text);
		this.SourceList.add(Text2);
		this.SourceList.add(Text3);
	}

	public final void AppendInt(int Value) {
		this.SourceList.add("" + Value);
	}

	public final void AppendQuotedText(String Text) {
		this.SourceList.add(UniCharset._QuoteString(Text));
	}

	public final void AppendLineFeed() {
		if(this.LastChar != '\n') {
			this.SourceList.add(this.LineFeed);
		}
	}

	public final void OpenIndent() {
		this.IndentLevel = this.IndentLevel + 1;
		this.CurrentIndentString = null;
	}

	public final void OpenIndent(String Text) {
		if(Text != null && Text.length() > 0) {
			this.Append(Text);
		}
		this.OpenIndent();
	}

	public final void CloseIndent() {
		this.IndentLevel = this.IndentLevel - 1;
		this.CurrentIndentString = null;
		assert(this.IndentLevel >= 0);
	}

	public final void CloseIndent(String Text) {
		this.CloseIndent();
		if(Text != null && Text.length() > 0) {
			this.AppendNewLine(Text);
		}
	}

	public final int SetIndentLevel(int IndentLevel) {
		int Level = this.IndentLevel;
		this.IndentLevel = IndentLevel;
		this.CurrentIndentString = null;
		return Level;
	}

	private final void AppendIndentString() {
		if (this.CurrentIndentString == null) {
			this.CurrentIndentString = this.joinStrings(this.Tabular, this.IndentLevel);
		}
		this.SourceList.add(this.CurrentIndentString);
	}

	public final String joinStrings(String Unit, int Times) {
		String s = "";
		int i = 0;
		while(i < Times) {
			s = s + Unit;
			i = i + 1;
		}
		return s;
	}

	public final void AppendNewLine() {
		this.AppendLineFeed();
		this.AppendIndentString();
	}

	public final void AppendNewLine(String Text) {
		this.AppendNewLine();
		this.Append(Text);
	}

	public final void AppendNewLine(String Text, String Text2) {
		this.AppendNewLine();
		this.Append(Text);
		this.Append(Text2);
	}

	public final void AppendNewLine(String Text, String Text2, String Text3) {
		this.AppendNewLine();
		this.Append(Text);
		this.Append(Text2);
		this.Append(Text3);
	}

	public final boolean EndsWith(char s) {
		return this.LastChar == s;
	}

	public final void AppendWhiteSpace() {
		if(this.LastChar == ' ' || this.LastChar == '\t' || this.LastChar == '\n') {
			return;
		}
		this.SourceList.add(" ");
	}

	public final void AppendWhiteSpace(String Text) {
		this.AppendWhiteSpace();
		this.Append(Text);
	}

	public final void AppendWhiteSpace(String Text, String Text2) {
		this.AppendWhiteSpace();
		this.Append(Text);
		this.Append(Text2);
	}

	public final void AppendWhiteSpace(String Text, String Text2, String Text3) {
		this.AppendWhiteSpace();
		this.Append(Text);
		this.Append(Text2);
		this.Append(Text3);
	}

	public final void Clear() {
		this.SourceList.clear(0);
	}

	@Override public final String toString() {
		return Main._SourceBuilderToString(this);
	}
	////
	////	@Deprecated public final void AppendLine(String Text) {
	////		this.Append(Text);
	////		this.AppendLineFeed();
	//	}

}
