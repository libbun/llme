package org.llme;


public class PegObject {
	SourceToken    source;
	String      name;
	PegObject   AST[] = null;
	PegObject   parent;
	SymbolTable gamma = null;
	Functor     matched = null;
	MetaType       typed   = null;

	PegObject(String functor) {
		this.name = functor;
	}

	public boolean is(String functor) {
		return this.name.startsWith(functor);
	}

	final boolean isErrorNode() {
		return this.name.equals(BunSymbol.PerrorFunctor);
	}

	final void setSource(Peg createdPeg, BunSource source, int startIndex, int endIndex) {
		this.source = source.newToken(createdPeg, startIndex, endIndex);
	}

	final void setMessage(Peg createdPeg, BunSource source, int startIndex, String message) {
		this.source = source.newToken(createdPeg, startIndex, startIndex, message);
	}

	final String getText() {
		if(this.source != null) {
			return this.source.getText();
		}
		return "NULL";
	}

	// AST[]

	public final int size() {
		if(this.AST == null) {
			return 0;
		}
		return this.AST.length;
	}

	public final PegObject get(int index) {
		return this.AST[index];
	}

	public final PegObject get(int index, PegObject defaultValue) {
		if(index < this.size()) {
			return this.AST[index];
		}
		return defaultValue;
	}

	public final void set(int index, PegObject node) {
		if(!(index < this.size())){
			this.expandAstToSize(index+1);
		}
		this.AST[index] = node;
	}

	//	public final void swap(int i, int j) {
	//		PegObject node = this.AST[i];
	//		this.AST[i] = this.AST[j];
	//		this.AST[j] = node;
	//	}

	public final void resizeAst(int size) {
		if(this.AST == null && size > 0) {
			this.AST = Main._NewPegObjectArray(size);
		}
		else if(this.AST.length != size) {
			PegObject[] newast = Main._NewPegObjectArray(size);
			if(size > this.AST.length) {
				Main._ArrayCopy(this.AST, 0, newast, 0, this.AST.length);
			}
			else {
				Main._ArrayCopy(this.AST, 0, newast, 0, size);
			}
			this.AST = newast;
		}
	}

	public final void expandAstToSize(int newSize) {
		if(newSize > this.size()) {
			this.resizeAst(newSize);
		}
	}

	void append(PegObject childNode) {
		int size = this.size();
		this.expandAstToSize(size+1);
		this.AST[size] = childNode;
		childNode.parent = this;
	}


	//	public final BType getTypeAt(int Index) {
	//		if(Index < this.AST.length) {
	//			return this.AST[Index].typed.GetRealType();
	//		}
	//		return BType.VoidType;  // to retrieve RecvType
	//	}
	//
	//	public final void setTypeAt(int Index, BType Type) {
	//		if(this.AST[Index] != null) {
	//			this.AST[Index].typed = Type;
	//		}
	//	}

	public final String getTextAt(int index, String defaultValue) {
		if(index < this.size()) {
			return this.AST[index].getText();
		}
		return defaultValue;
	}

	@Override
	public String toString() {
		SourceBuilder sb = new SourceBuilder(null, null);
		this.stringfy(sb);
		return sb.toString();
	}

	private Peg peg() {
		if(this.source != null) {
			return this.source.createdPeg;
		}
		return null;
	}

	final void stringfy(SourceBuilder sb) {
		if(this.AST == null) {
			sb.AppendNewLine(this.name+ ": ", this.getText(), "   ## by " + this.peg());
		}
		else {
			sb.AppendNewLine(this.name);
			sb.OpenIndent(" {            ## by " + this.peg());
			for(int i = 0; i < this.size(); i++) {
				this.AST[i].stringfy(sb);
			}
			sb.CloseIndent("}");
		}
	}

	final SymbolTable getSymbolTable() {
		PegObject node = this;
		while(node.gamma == null) {
			node = node.parent;
		}
		return node.gamma;
	}

	public MetaType getType(MetaType defaultType) {
		if(this.matched != null) {
			return this.matched.getReturnType(defaultType);
		}
		return defaultType;
	}

}
