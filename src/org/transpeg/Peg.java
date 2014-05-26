package org.transpeg;

public abstract class Peg {
	public final static boolean _BackTrack = true;

	String    name     = null;
	int       priority = 0;
	boolean   debug    = false;

	BunSource source = null;
	int       sourcePosition = 0;

	Peg(String leftLabel) {
		this.name = leftLabel;
	}

	protected abstract void stringfy(UniStringBuilder sb, boolean debugMode);
	protected abstract PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice);
	public abstract void accept(PegVisitor visitor);

	@Override public String toString() {
		UniStringBuilder sb = new UniStringBuilder();
		this.stringfy(sb, false);
		return sb.toString();
	}

	public PegSequence append(Peg e) {
		if(e instanceof PegSequence) {
			((PegSequence)e).list.add(0, this);
			return ((PegSequence)e);
		}
		if(this instanceof PegSequence) {
			((PegSequence)this).list.add(e);
			return ((PegSequence)this);
		}
		else {
			PegSequence seq = new PegSequence(this);
			seq.append(e);
			return seq;
		}
	}

	protected PegObject debugMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		if(this.debug) {
			PegObject node2 = this.lazyMatch(inNode, source, false);
			String msg = "matched";
			if(node2.isErrorNode()) {
				msg = "failed";
			}
			String line = source.formatErrorMessage(msg, this.toString());
			System.out.println(line + "\n\tnode #" + inNode + "# => #" + node2 + "#");
			return node2;
		}
		return this.lazyMatch(inNode, source, hasNextChoice);
	}

	public final String toPrintableString(String name) {
		UniStringBuilder sb = new UniStringBuilder();
		sb.Append(name);
		sb.Append(" <- ");
		this.joinPrintableString(sb, this);
		return sb.toString();
	}

	private void joinPrintableString(UniStringBuilder sb, Peg e) {
		if(e instanceof PegChoice) {
			((PegChoice)e).firstExpr.stringfy(sb, true);
			sb.Append("\n\t/ ");
			this.joinPrintableString(sb, ((PegChoice)e).secondExpr);
		}
		else {
			e.stringfy(sb, true);
		}
	}

	//	Peg removeLeftRecursion(PegParser p) {
	//		return this;
	//	}
	//
	//	final boolean checkAll(PegParser p, String leftName, int order) {
	//		Peg e = this;
	//		boolean checkResult = true;
	//		while(e != null) {
	//			if(!e.check(p, leftName, order)) {
	//				checkResult = false;
	//			}
	//			order = order + 1;
	//			e = e.nextExpr;
	//		}
	//		return checkResult;
	//	}
	//
	//	boolean check(PegParser p, String leftName, int order) {
	//		return true; //
	//	}

	void setSource(BunSource source, int sourcePosition) {
		this.source = source;
		this.sourcePosition = sourcePosition;
	}

	protected void dump(String msg) {
		if(this.source != null) {
			System.out.println(this.source.formatErrorLineMarker("*", this.sourcePosition, msg));
		}
		else {
			System.out.println("unknown source: " + msg);
		}
	}

	protected void warning(String msg) {
		if(Main.pegDebugger) {
			Main._PrintLine("PEG warning: " + msg);
		}
	}

}

abstract class PegAtom extends Peg {
	String symbol;
	public PegAtom (String leftLabel, String symbol) {
		super(leftLabel);
		this.symbol = symbol;
	}
}

class PegString extends PegAtom {
	public PegString(String leftLabel, String symbol) {
		super(leftLabel, symbol);
	}

	@Override
	protected void stringfy(UniStringBuilder sb, boolean debugMode) {
		char Quote = '\'';
		if(this.symbol.indexOf("'") != -1) {
			Quote = '"';
		}
		sb.Append(UniCharset._QuoteString(Quote, this.symbol, Quote));
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegString(this);
	}

	@Override
	public PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		if(source.match(this.symbol)) {
			return inNode;
		}
		return source.newExpectedErrorNode(this, this, hasNextChoice);
	}

}

class PegAny extends PegAtom {
	public PegAny(String leftLabel) {
		super(leftLabel, ".");
	}

	@Override
	protected void stringfy(UniStringBuilder sb, boolean debugMode) {
		sb.Append(".");
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegAny(this);
	}

	@Override
	public PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		if(source.hasChar()) {
			source.consume(1);
			return inNode;
		}
		return source.newExpectedErrorNode(this, this, hasNextChoice);
	}

}

class PegCharacter extends PegAtom {
	UniCharset charset;
	public PegCharacter(String leftLabel, String token) {
		super(leftLabel, token);
		this.charset = new UniCharset(token);
	}

	@Override
	protected void stringfy(UniStringBuilder sb, boolean debugMode) {
		sb.Append("[" + this.symbol, "]");
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegCharacter(this);
	}

	@Override
	public PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		char ch = source.getChar();
		if(!this.charset.match(ch)) {
			return source.newExpectedErrorNode(this, this, hasNextChoice);
		}
		source.consume(1);
		return inNode;
	}

}

class PegLabel extends PegAtom {
	public PegLabel(String leftLabel, String token) {
		super(leftLabel, token);
	}

	@Override
	protected void stringfy(UniStringBuilder sb, boolean debugMode) {
		sb.Append(this.symbol);
	}

	@Override protected PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		PegObject left = source.parsePegNode(parentNode, this.symbol, hasNextChoice);
		if(left.isErrorNode()) {
			return left;
		}
		return source.parseRightPegNode(left, this.symbol);
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegLabel(this);
	}

	//	@Override boolean check(PegParser p, String leftName, int order) {
	//		if(!p.hasPattern(this.symbol)) {
	//			LibBunSystem._Exit(1, "undefined label: " + this.symbol);
	//		}
	//		return true;
	//	}
	//
	//	@Override Peg removeLeftRecursion(PegParser p) {
	//		if(this.nextExpr != null) {
	//			CommonArray<String> list = p.pegMap.keys();
	//			for(int i = 0; i < list.size(); i++) {
	//				String key = list.ArrayValues[i];
	//				Peg e = p.pegMap.GetValue(key, null);
	//				if(this.lookupFirstLabel(e, this.symbol)) {
	//					LibBunSystem._Exit(1, "find indirect left recursion " + this.name + " <- " + this.symbol + "...");
	//					return null;
	//				}
	//			}
	//		}
	//		return this;
	//	}
	//
	//	private boolean lookupFirstLabel(Peg e, String symbol) {
	//		if(e instanceof PegChoice) {
	//			if(!this.lookupFirstLabel(((PegChoice) e).firstExpr, symbol)) {
	//				return this.lookupFirstLabel(((PegChoice) e).secondExpr, symbol);
	//			}
	//		}
	//		if(e instanceof PegLabel) {
	//			if(symbol.equals(((PegLabel) e).symbol)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

}

abstract class PegSuffixed extends Peg {
	Peg innerExpr;
	public PegSuffixed(String leftLabel, Peg e) {
		super(leftLabel);
		this.innerExpr = e;
	}
	@Override
	protected final void stringfy(UniStringBuilder sb, boolean debugMode) {
		if(this.innerExpr instanceof PegAtom) {
			this.innerExpr.stringfy(sb, debugMode);
		}
		else {
			sb.Append("(");
			this.innerExpr.stringfy(sb, debugMode);
			sb.Append(")");
		}
		sb.Append(this.getOperator());
	}
	protected abstract String getOperator();

}

class PegOptionalExpr extends PegSuffixed {
	public PegOptionalExpr(String leftLabel, Peg e) {
		super(leftLabel, e);
	}

	@Override
	protected String getOperator() {
		return "?";
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegOptional(this);
	}

	@Override protected PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		PegObject node = parentNode;
		int stackPosition = source.getStackPosition(this);
		node = this.innerExpr.debugMatch(node, source, true);
		if(node.isErrorNode()) {
			source.popBack(stackPosition, Peg._BackTrack);
			node = parentNode;
		}
		return node;
	}
}

class PegOneMoreExpr extends PegSuffixed {
	public PegOneMoreExpr(String leftLabel, Peg e) {
		super(leftLabel, e);
	}

	@Override
	protected String getOperator() {
		return "+";
	}

	@Override
	public PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		int startPosition = source.getPosition();
		PegObject prevNode = parentNode;
		int count = 0;
		while(source.hasChar()) {
			boolean aChoice = true;
			if(count < 1) {
				aChoice = hasNextChoice;
			}
			PegObject node = this.innerExpr.debugMatch(prevNode, source, aChoice);
			if(node.isErrorNode()) {
				break;
			}
			if(node != prevNode) {
				this.warning("ignored result of " + this.innerExpr);
			}
			prevNode = node;
			if(!(startPosition < source.getPosition())) {
				this.warning("avoid infinite loop " + this);
				break;
			}
			count = count + 1;
		}
		if(count < 1) {
			return source.newExpectedErrorNode(this, this.innerExpr, hasNextChoice);
		}
		//System.out.println("prevNode: " + prevNode + "s,e=" + prevNode.sourcePosition + ", " + prevNode.endIndex);
		return prevNode;
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitOneMore(this);
	}
}

class PegZeroMoreExpr extends PegSuffixed {
	public PegZeroMoreExpr(String leftLabel, Peg e) {
		super(leftLabel, e);
	}

	@Override
	protected String getOperator() {
		return "*";
	}

	@Override
	public PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		int startPosition = source.getPosition();
		PegObject prevNode = parentNode;
		int count = 0;
		while(source.hasChar()) {
			PegObject node = this.innerExpr.debugMatch(prevNode, source, true);
			if(node.isErrorNode()) {
				break;
			}
			if(node != prevNode) {
				this.warning("ignored result of " + this.innerExpr);
			}
			prevNode = node;
			if(!(startPosition < source.getPosition())) {
				this.warning("avoid infinite loop " + this);
				break;
			}
			count = count + 1;
		}
		return prevNode;
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitZeroMore(this);
	}
}

abstract class PegPredicate extends Peg {
	Peg innerExpr;
	public PegPredicate(String leftLabel, Peg e) {
		super(leftLabel);
		this.innerExpr = e;
	}
	@Override
	protected final void stringfy(UniStringBuilder sb, boolean debugMode) {
		sb.Append(this.getOperator());
		if(this.innerExpr instanceof PegAtom) {
			this.innerExpr.stringfy(sb, debugMode);
		}
		else {
			sb.Append("(");
			this.innerExpr.stringfy(sb, debugMode);
			sb.Append(")");
		}
	}
	protected abstract String getOperator();
}

class PegAndPredicate extends PegPredicate {
	PegAndPredicate(String leftLabel, Peg e) {
		super(leftLabel, e);
	}

	@Override
	protected String getOperator() {
		return "&";
	}

	@Override
	protected PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		PegObject node = parentNode;
		int stackPosition = source.getStackPosition(this);
		node = this.innerExpr.debugMatch(node, source, true);
		source.popBack(stackPosition, Peg._BackTrack);
		return node;
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegAnd(this);
	}

}

class PegNotPredicate extends PegPredicate {
	PegNotPredicate(String leftLabel, Peg e) {
		super(leftLabel, e);
	}

	@Override
	protected String getOperator() {
		return "!";
	}

	@Override
	protected PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		PegObject node = parentNode;
		int stackPosition = source.getStackPosition(this);
		node = this.innerExpr.debugMatch(node, source, hasNextChoice);
		source.popBack(stackPosition, Peg._BackTrack);
		if(node.isErrorNode()) {
			return parentNode;
		}
		return source.newUnexpectedErrorNode(this, this.innerExpr, hasNextChoice);
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitPegNot(this);
	}

}

class PegSequence extends Peg {
	UniArray<Peg> list;

	PegSequence(Peg first) {
		super(first.name);
		this.list = new UniArray<Peg>(new Peg[2]);
		this.list.add(first);
	}

	@Override protected void stringfy(UniStringBuilder sb, boolean debugMode) {
		for(int i = 0; i < this.list.size(); i++) {
			if(i > 0) {
				sb.Append(" ");
			}
			Peg e = this.list.ArrayValues[i];
			if(e instanceof PegChoice || e instanceof PegSequence) {
				sb.Append("(");
				e.stringfy(sb, debugMode);
				sb.Append(")");
			}
			else {
				e.stringfy(sb, debugMode);
			}
		}
	}

	@Override
	protected PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		for(int i = 0; i < this.list.size(); i++) {
			Peg e  = this.list.ArrayValues[i];
			inNode = e.debugMatch(inNode, source, hasNextChoice);
			if(inNode.isErrorNode()) {
				return inNode;
			}
		}
		return inNode;
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitSequence(this);
	}

}

class PegChoice extends Peg {
	Peg firstExpr;
	Peg secondExpr;
	PegChoice(String leftLabel, Peg e, Peg e2) {
		super(leftLabel);
		this.firstExpr = e;
		this.secondExpr = e2;
	}

	@Override
	protected void stringfy(UniStringBuilder sb, boolean debugMode) {
		this.firstExpr.stringfy(sb, debugMode);
		sb.Append(" / ");
		this.secondExpr.stringfy(sb, debugMode);
	}

	@Override
	protected PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		Peg e = this;
		int stackPosition = source.getStackPosition(this);
		while(e instanceof PegChoice) {
			PegObject node = parentNode;
			node = ((PegChoice) e).firstExpr.debugMatch(node, source, true);
			if(!node.isErrorNode()) {
				return node;
			}
			source.popBack(stackPosition, Peg._BackTrack);
			e = ((PegChoice) e).secondExpr;
		}
		return e.debugMatch(parentNode, source, hasNextChoice);
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitChoice(this);
	}

	//	@Override public String firstChars(CommonMap<Peg> m) {
	//		return this.firstExpr.firstChars(m) + this.secondExpr.firstChars(m);
	//	}
	//
	//	@Override Peg removeLeftRecursion(PegParser p) {
	//		this.firstExpr = this.firstExpr.removeLeftRecursion(p);
	//		this.secondExpr = this.secondExpr.removeLeftRecursion(p);
	//		if(this.firstExpr == null) {
	//			if(this.secondExpr == null) {
	//				return null;
	//			}
	//			return this.secondExpr;
	//		}
	//		if(this.secondExpr == null) {
	//			return this.firstExpr;
	//		}
	//		return this;
	//	}
	//
	//	@Override boolean check(PegParser p, String leftName, int order) {
	//		boolean checkResult = true;
	//		if(!this.firstExpr.checkAll(p, leftName, order)) {
	//			checkResult = false;
	//		}
	//		if(!this.secondExpr.checkAll(p, leftName, order)) {
	//			checkResult = false;
	//		}
	//		return checkResult;
	//	}

}

class PegSetter extends PegPredicate {
	boolean allowError = false;
	public PegSetter(String leftLabel, Peg e, boolean allowError) {
		super(leftLabel, e);
		this.innerExpr = e;
		this.allowError = allowError;
	}
	@Override
	protected String getOperator() {
		if(this.allowError) {
			return "$$";
		}
		else {
			return "$";
		}
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitSetter(this);
	}

	@Override
	public PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
		if(this.allowError) {
			int pos = source.getPosition();
			PegObject node = this.innerExpr.debugMatch(parentNode, source, false);
			source.push(this, parentNode, -1, node);
			if(node.isErrorNode()) {
				source.showPosition("syntax error: " + node, pos);
				int indent = source.source.getIndentSize(pos);
				source.skipIndent(indent);
			}
			return parentNode;
		}
		PegObject node = this.innerExpr.debugMatch(parentNode, source, hasNextChoice);
		if(node.isErrorNode()) {
			return node;
		}
		if(parentNode == node) {
			//			this.warning("node was not created: nothing to set " + node + " ## created by " + this.innerExpr);
			return parentNode;
		}
		source.push(this, parentNode, -1, node);
		return parentNode;
	}
}

class PegObjectName extends Peg {
	String nodeName = null;

	public PegObjectName(String leftLabel, String nodeName) {
		super(leftLabel);
		this.nodeName = nodeName;
	}

	@Override
	protected final void stringfy(UniStringBuilder sb, boolean debugMode) {
		if(debugMode) {
			sb.Append("#");
			sb.Append(this.nodeName);
		}
	}

	@Override
	public PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		inNode.name = this.nodeName;
		return inNode;
	}

	@Override
	public void accept(PegVisitor visitor) {
		//visitor.visitNewObject(this);
	}
}

class PegNewObject extends Peg {
	Peg innerExpr;
	boolean leftJoin = false;
	String nodeName = "";

	public PegNewObject(String leftLabel, boolean leftJoin, Peg e) {
		super(leftLabel);
		this.innerExpr = e;
		this.leftJoin = leftJoin;
	}

	@Override
	protected final void stringfy(UniStringBuilder sb, boolean debugMode) {
		if(debugMode) {
			sb.Append("{");
			if(this.leftJoin) {
				sb.Append("$ ");
			}
		}
		this.innerExpr.stringfy(sb, debugMode);
		if(debugMode) {
			sb.Append("}");
		}
	}

	@Override
	public PegObject lazyMatch(PegObject inNode, PegParserContext source, boolean hasNextChoice) {
		// prefetch first node..
		int pos = source.getPosition();
		int stack = source.getStackPosition(this);
		PegObject newnode = source.newPegObject(this.nodeName);
		if(this.leftJoin) {
			source.push(this, newnode, 0, inNode);
		}
		PegObject node = this.innerExpr.debugMatch(newnode, source, false);
		if(node.isErrorNode()) {
			//System.out.println("disposing... object pos=" + pos + ", by " + this + "error="+node);
			//source.popBack(stack, Peg._BackTrack);
			return node;
		}
		int top = source.getStackPosition(this);
		for(int i = stack; i < top; i++) {
			Log log = source.logStack.ArrayValues[i];
			if(log.type == 'p' && log.parentNode == newnode) {
				newnode.append((PegObject)log.childNode);
			}
		}
		if(newnode.name == null || newnode.name.length() == 0) {
			newnode.name = source.source.substring(pos, source.getPosition());
		}
		newnode.setSource(this, source.source, pos, source.getPosition());
		return newnode;
	}

	@Override
	public void accept(PegVisitor visitor) {
		visitor.visitNewObject(this);

	}

	//	private boolean containSetter(Peg e, boolean checkSequence) {
	//		if(e instanceof PegSetter) {
	//			return true;
	//		}
	//		if(e instanceof PegPredicate) {
	//			return this.containSetter(((PegPredicate) e).innerExpr, true);
	//		}
	//		if(e instanceof PegChoice) {
	//			if(this.containSetter(((PegChoice) e).firstExpr, true)) {
	//				return true;
	//			}
	//			if(this.containSetter(((PegChoice) e).secondExpr, true)) {
	//				return true;
	//			}
	//		}
	//		if(checkSequence && e.nextExpr != null) {
	//			return this.containSetter(e.nextExpr, true);
	//		}
	//		return false;
	//	}

}

//class PegSemanticAction extends Peg {
//	String name;
//	SemanticFunction f;
//	PegSemanticAction(String leftLabel, BunSource source, int sourcePosition, String name, SemanticFunction f) {
//		super(leftLabel, source);
//		this.name = name;
//		this.f = f;
//	}
//	@Override protected String stringfy() {
//		return "    :" + this.name;
//	}
//	@Override protected PegObject lazyMatch(PegObject parentNode, PegParserContext source, boolean hasNextChoice) {
//		parentNode.setSemanticAction(this.f);
//		return parentNode;
//	}
//	@Override boolean check(PegParser p, String leftName, int order) {
//		return true;
//	}
//}
