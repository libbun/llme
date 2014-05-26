package org.llme;

public class PegParserContext extends SourceContext {
	public  PegParser    parser;

	final UniArray<Log> logStack = new UniArray<Log>(new Log[128]);
	private int stackTop = 0;

	private final UniMap<PegObject> memoMap = new UniMap<PegObject>(null);
	private final UniMap<Memo> memoMap2 = new UniMap<Memo>(null);
	int memoHit = 0;
	int memoMiss = 0;
	int memoSize = 0;
	int objectCount = 0;
	int errorCount = 0;


	public PegParserContext(PegParser parser, BunSource source, int startIndex, int endIndex) {
		super(source, startIndex, endIndex);
		this.parser = parser;
	}

	@Override
	public SourceContext subContext(int startIndex, int endIndex) {
		return new PegParserContext(this.parser, this.source, startIndex, endIndex);
	}


	@Override
	public void skipIndent(int indentSize) {
		int pos = this.sourcePosition;
		//		this.showPosition("skip characters until indent="+indentSize + ", pos=" + pos, pos);
		for(;pos < this.endPosition; pos = pos + 1) {
			char ch = this.charAt(pos);
			if(ch == '\n' && pos > this.sourcePosition) {
				int posIndent = this.source.getIndentSize(pos+1);
				if(posIndent <= indentSize) {
					this.sourcePosition = pos + 1;
					//					System.out.println("skip characters until indent="+indentSize + ", pos=" + this.sourcePosition);
					return ;
				}
			}
		}
		//		System.out.println("skip characters until indent="+indentSize + ", pos = endPosition");
		this.sourcePosition = this.endPosition;
	}

	//	@Override
	//	public final int skipWhiteSpace(boolean IncludeNewLine) {
	//		if(IncludeNewLine) {
	//			while(this.hasChar()) {
	//				char ch = this.charAt(this.sourcePosition);
	//				if(ch != ' ' && ch != '\t' && ch != '\n') {
	//					break;
	//				}
	//				this.consume(1);
	//			}
	//		}
	//		else {
	//			while(this.hasChar()) {
	//				char ch = this.charAt(this.sourcePosition);
	//				if(ch != ' ' && ch != '\t') {
	//					break;
	//				}
	//				this.consume(1);
	//			}
	//		}
	//		return this.sourcePosition;
	//	}

	public boolean hasNode() {
		this.matchZeroMore(UniCharset.WhiteSpaceNewLine);
		return this.sourcePosition < this.endPosition;
	}

	public PegObject parseNode(String key) {
		PegObject po = this.parsePegNode(new PegObject(BunSymbol.TopLevelFunctor), key, false);
		//return po.eval(this.source, parentNode);
		return po;
	}

	public String getFirstChar() {
		return String.valueOf(this.getChar());
	}

	public boolean isLeftRecursion(String PatternName) {
		Peg e = this.parser.getRightPattern(PatternName, this.getFirstChar());
		return e != null;
	}

	public final PegObject parsePegNode(PegObject parentNode, String pattern, boolean hasNextChoice) {
		int pos = this.getPosition();
		String key = pattern + ":" + pos;
		Memo m = this.memoMap2.GetValue(key, null);
		if(m != null) {
			this.memoHit = this.memoHit + 1;
			this.sourcePosition = m.nextPosition;
			if(m.result == null) {
				return parentNode;
			}
			return m.result;
		}
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			PegObject node = e.debugMatch(parentNode, this, hasNextChoice);
			m = new Memo();
			m.nextPosition = this.getPosition();
			if(node != parentNode) {
				m.result = node;
			}
			this.memoMiss = this.memoMiss + 1;
			this.memoMap2.put(key, m);
			return node;
		}
		Main._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return this.defaultFailureNode;
	}

	public final PegObject parsePegNode2(PegObject parentNode, String pattern, boolean hasNextChoice) {
		int pos = this.getPosition();
		String key = pattern + ":" + pos;
		PegObject node = this.memoMap.GetValue(key, null);
		if(node != null) {
			this.memoHit = this.memoHit + 1;
			return node;
		}
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			node = e.debugMatch(parentNode, this, hasNextChoice);
			if(node.isErrorNode() && hasNextChoice) {
				this.memoMiss = this.memoMiss + 1;
				this.memoMap.put(key, node);
				return node;
			}
			if(node != parentNode && node.isErrorNode()) {
				this.memoMiss = this.memoMiss + 1;
				this.memoMap.put(key, node);
				return node;
			}
			return node;
		}
		Main._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return this.defaultFailureNode;
	}

	public final PegObject parsePegNodeNon(PegObject parentNode, String pattern, boolean hasNextChoice) {
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			return e.debugMatch(parentNode, this, hasNextChoice);
		}
		Main._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return this.defaultFailureNode;
	}

	public final PegObject parseRightPegNode(PegObject left, String symbol) {
		String key = this.parser.nameRightJoinName(symbol);
		Peg e = this.parser.getPattern(key, this.getFirstChar());
		while(e != null) {
			Peg sec = e;
			if(e instanceof PegChoice) {
				sec = ((PegChoice) e).firstExpr;
				e = ((PegChoice) e).secondExpr;
			}
			else {
				e = null;
			}
			PegObject right = sec.debugMatch(left, this, true);
			if(!right.isErrorNode()) {
				left = right;
			}
		}
		return left;
	}

	final int getStackPosition(Peg trace) {
		this.pushImpl(trace, null, '\0', null, 0, null);
		return this.stackTop;
	}

	private void pushImpl(Peg trace, String msg, char type, Object parentNode, int index, Object childNode) {
		Log log = null;
		if(this.stackTop < this.logStack.size()) {
			if(this.logStack.ArrayValues[this.stackTop] == null) {
				this.logStack.ArrayValues[this.stackTop] = new Log();
			}
			log = this.logStack.ArrayValues[this.stackTop];
		}
		else {
			log = new Log();
			this.logStack.add(log);
		}
		log.trace = trace;
		log.sourcePosition = this.sourcePosition;
		log.msg = msg;
		log.type = type;
		log.parentNode = parentNode;
		log.index = index;
		log.childNode = childNode;
		this.stackTop = this.stackTop + 1;
	}

	void pushLog(Peg trace, String msg) {
		this.pushImpl(trace, msg, 'm', null, 0, null);
	}

	void popBack(int stackPostion, boolean backtrack) {
		this.stackTop = stackPostion-1;
		Log log = this.logStack.ArrayValues[stackPostion-1];
		if(backtrack) {
			this.rollback(log.sourcePosition);
		}
	}

	public void push(Peg trace, PegObject parentNode, int index, PegObject node) {
		this.pushImpl(trace, "", 'p', parentNode, index, node);
	}

	public PegObject newPegObject(String fanctor) {
		PegObject node = new PegObject(fanctor);
		this.objectCount = this.objectCount + 1;
		return node;
	}

	private final PegObject defaultFailureNode = new PegObject(BunSymbol.PerrorFunctor);

	public PegObject newErrorNode(Peg created, String msg, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		else {
			PegObject node = new PegObject(BunSymbol.PerrorFunctor);
			PegObject msgnode = new PegObject(BunSymbol.StringFunctor);
			msgnode.setMessage(created, this.source, this.sourcePosition, msg);
			node.append(msgnode);
			node.source = msgnode.source;
			this.errorCount = this.errorCount + 1;
			return node;
		}
	}

	public PegObject newExpectedErrorNode(Peg created, Peg e, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		return this.newErrorNode(created, "expected " + e.toString(), false);
	}

	public PegObject newUnexpectedErrorNode(Peg created, Peg e, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		return this.newErrorNode(created, "unexpected " + e.toString(), false);
	}

//	public PegObject newFunctionErrorNode(Peg created, SemanticFunction f, boolean hasNextChoice) {
//		if(hasNextChoice) {
//			return this.defaultFailureNode;
//		}
//		return this.newErrorNode(created, "function  " + f + " was failed", false);
//	}





}

class Log {
	int sourcePosition;
	Peg trace;
	String msg;
	char type;
	Object parentNode;
	int index;
	Object childNode;

	@Override public String toString() {
		return "" + this.sourcePosition + " " + this.msg;
	}
}

class Memo {
	PegObject result;
	int nextPosition;
}
