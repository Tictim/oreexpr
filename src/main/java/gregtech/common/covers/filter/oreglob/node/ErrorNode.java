package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

public class ErrorNode extends OreGlobNode{

	ErrorNode(){
	}

	@Override
	protected void visitInternal(NodeVisitor visitor){
		visitor.error();
	}

	@Override
	public boolean isStructurallyEqualTo(@NotNull OreGlobNode node){ // removed inverted flag check
		return this==node||node instanceof ErrorNode&&isStructurallyEqualTo(this.getNext(), node.getNext());
	}

	@Override
	public boolean isPropertyEqualTo(@NotNull OreGlobNode node){
		return node instanceof ErrorNode;
	}

	@Override
	protected MatchDescription getIndividualNodeMatchDescription(){
		return MatchDescription.IMPOSSIBLE;
	}
}
