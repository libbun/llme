package org.transpeg;

public final class SourceBuilder extends UniStringBuilder {
	SourceBuilder parent;

	public SourceBuilder(Object Template, SourceBuilder Parent) {
		super();
		this.parent = Parent;
	}

	public final SourceBuilder pop() {
		this.AppendLineFeed();
		return this.parent;
	}

	public final void AppendCode(String Source) {
		this.LastChar = '\0';
		int StartIndex = 0;
		int i = 0;
		while(i < Source.length()) {
			char ch = Main._GetChar(Source, i);
			if(ch == '\n') {
				if(StartIndex < i) {
					this.slist.add(Source.substring(StartIndex, i));
				}
				this.AppendNewLine();
				StartIndex = i + 1;
			}
			if(ch == '\t') {
				if(StartIndex < i) {
					this.slist.add(Source.substring(StartIndex, i));
				}
				this.append(this.Tabular);
				StartIndex = i + 1;
			}
			i = i + 1;
		}
		if(StartIndex < i) {
			this.slist.add(Source.substring(StartIndex, i));
		}
	}

	public final int GetPosition() {
		return this.slist.size();
	}

	public final String CopyString(int BeginIndex, int EndIndex) {
		return Main._SourceBuilderToString(this, BeginIndex, EndIndex);
	}

}
