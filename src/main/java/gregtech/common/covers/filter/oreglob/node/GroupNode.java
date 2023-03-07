package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

public class GroupNode extends OreGlobNode{

	final OreGlobNode node;

	public GroupNode(OreGlobNode node){
		this.node = node;
	}

	@Override
	protected void visitInternal(NodeVisitor visitor){
		visitor.group(node, isInverted());
	}

	@Override
	public boolean isPropertyEqualTo(@NotNull OreGlobNode node){
		if(!(node instanceof GroupNode)) return false;
		return this.node.isStructurallyEqualTo(((GroupNode)node).node);
	}

	@Override
	protected MatchDescription getIndividualNodeMatchDescription(){
		return node.getMatchDescription();
	}
}
