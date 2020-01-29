package application.swingUI;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class JTreeExt extends JTree {
	private static final long serialVersionUID = -159061715263110645L;

	private TreeWillExpandListener collapsingPreventor;
	private int[] selectionMap;

	public JTreeExt(String rootNodeName) {
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		collapsingPreventor = new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				throw new ExpandVetoException(event, "");
			}
		};
	}

	@Override
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}

	public DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) getModel().getRoot();
	}

	public DefaultMutableTreeNode getSelectedNode() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		else
			return (DefaultMutableTreeNode) path.getLastPathComponent();
	}

	public void selectNode(DefaultMutableTreeNode node) {
		if (node == null) {
			getSelectionModel().clearSelection();
			return;
		}

		TreePath path = new TreePath(node.getPath());
		getSelectionModel().setSelectionPath(path);
	}

	public void setCollapsable(boolean enabled) {
		if (enabled)
			removeTreeWillExpandListener(collapsingPreventor);
		else
			addTreeWillExpandListener(collapsingPreventor);
	}

	@SuppressWarnings("rawtypes")
	public void fullExpand() {
		Enumeration nodes = getRoot().breadthFirstEnumeration();
		while (nodes.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
			expandPath(new TreePath(node.getPath()));
		}
	}

	public void saveSelectionMap() {
		DefaultMutableTreeNode node = getSelectedNode();
		if (node == null) {
			selectionMap = new int[0];
			return;
		}

		int mapLevel = node.getLevel();
		int[] result = new int[mapLevel + 1];

		while (node != null) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

			int childIndex = parent == null ? 0 : parent.getIndex(node);
			result[mapLevel--] = childIndex;
			node = parent;
		}

		selectionMap = result;
	}

	public void loadSelectionMap() {
		DefaultMutableTreeNode node = getRoot();
		if (node == null)
			return;

		for (int i = 1; i < selectionMap.length; i++) {
			int childIndex = selectionMap[i];

			if (childIndex >= 0 && childIndex < node.getChildCount())
				node = (DefaultMutableTreeNode) node.getChildAt(childIndex);
			else
				break;
		}

		selectNode(node);
	}
}
