package org.transpeg;


public class SymbolTable {
	Namespace          namespace = null;
	PegObject          scope     = null;
	UniMap<Functor>  symbolTable = null;

	public SymbolTable(Namespace namespace) {
		this.namespace = namespace;
	}

	@Override public String toString() {
		return "NS[" + this.scope + "]";
	}

	public final Namespace getNamespace() {
		return this.namespace;
	}

	public final SymbolTable getParentTable() {
		if(this.scope != null) {
			PegObject Node = this.scope.parent;
			while(Node != null) {
				if(Node.gamma != null) {
					return Node.gamma;
				}
				Node = Node.parent;
			}
		}
		return null;
	}

	public final void setSymbol(String key, Functor f) {
		if(this.symbolTable == null) {
			this.symbolTable = new UniMap<Functor>(null);
		}
		//		System.out.println("set key: " + key + "");
		this.symbolTable.put(key, f);
	}

	public final Functor getSymbol(String key) {
		SymbolTable table = this;
		while(table != null) {
			if(table.symbolTable != null) {
				Functor f = table.symbolTable.GetValue(key, null);
				if(f != null) {
					return f;
				}
			}
			table = table.getParentTable();
		}
		//		System.out.println("unknown key: " + key);
		return null;
	}

	public Functor getFunctor(PegObject node) {
		String key = node.name + ":" + node.size();
		Functor f = this.getSymbol(key);
		if(f != null) {
			return f;
		}
		key = node.name + "*";
		return this.getSymbol(key);
	}

	public void addFunctor(Functor f) {
		String key = f.key();
		Functor parent = this.getSymbol(key);
		f.nextChoice = parent;
		this.setSymbol(key, f);
		System.out.println("Functor: " + f.name + ": " + f.funcType);
	}

	public void setType(String name, MetaType type) {
		Functor parent = this.getSymbol(name);
		if(parent != null) {
			System.out.println("duplicated name:" + type);
		}
		this.setSymbol(name, new TypeFunctor(name, type));
	}

	public MetaType getType(String name, MetaType defaultType) {
		Functor f = this.getSymbol(name);
		if(f instanceof TypeFunctor) {
			return f.getReturnType(defaultType);
		}
		System.out.println("undefined type: " + name);
		return defaultType;
	}

	public FuncType getFuncType(String returnTypeName) {
		MetaType r = this.getType(returnTypeName, MetaType.UntypedType);
		return MetaType._LookupFuncType2(r);
	}
	
	public void load(String fileName, MetaEngine driver) {
		BunSource source = Main.loadSource(fileName);
		PegParserContext context =  this.namespace.parser.newContext(source);
		while(context.hasNode()) {
			PegObject node = context.parsePegNode(new PegObject(BunSymbol.TopLevelFunctor), "TopLevel", false/*hasNextChoice*/);
			node.gamma = this;
			Functor f = this.getFunctor(node);
			if(f != null) {
				if(f.match(node)) {
					f.build(node, driver);
				}
			}
		}
	}



}

