package application.swingUI;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import utils.Utils;

public class JTreeExt extends JTree {
	private static final long serialVersionUID = -159061715263110645L;
	private DefaultTreeModel model;
	private DefaultMutableTreeNode rootNode;
	public Utils.Callback onTreeChanged;

	public JTreeExt(String rootNodeName) {
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		rootNode = new DefaultMutableTreeNode(rootNodeName);
		model = new DefaultTreeModel(rootNode);
		setModel(model);

		model.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent arg0) {
				onTreeChanged();
			}
			@Override
			public void treeNodesInserted(TreeModelEvent arg0) {
				onTreeChanged();
			}
			@Override
			public void treeNodesRemoved(TreeModelEvent arg0) {
				onTreeChanged();
			}
			@Override
			public void treeStructureChanged(TreeModelEvent arg0) {
				onTreeChanged();
			}
			public void onTreeChanged() {
				if (onTreeChanged != null)
					onTreeChanged.execute();
			}
		});
	}

	public DefaultMutableTreeNode addToCurrentNode(Object userObject) {
		DefaultMutableTreeNode node = getSelectedNode();
		if (node == null)
			node = rootNode;

		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(userObject);

		model.insertNodeInto(childNode, node, node.getChildCount());

		TreePath newPath = new TreePath(childNode.getPath());
		scrollPathToVisible(newPath);

		return childNode;
	}

	public void removeCurrentNode() {
		DefaultMutableTreeNode node, parentNode, newNodeToBeSelected;

		node = getSelectedNode();
		if (node == null || node.isRoot() )
			return;

		parentNode = (DefaultMutableTreeNode) node.getParent();

		newNodeToBeSelected = node.getPreviousSibling();
		if (newNodeToBeSelected == null)
			newNodeToBeSelected = node.getNextSibling();
		if (newNodeToBeSelected == null)
			newNodeToBeSelected = parentNode;

		model.removeNodeFromParent(node);

		setSelectionPath(new TreePath(newNodeToBeSelected.getPath()));
	}

	public DefaultMutableTreeNode getSelectedNode() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		else
			return (DefaultMutableTreeNode) path.getLastPathComponent();
	}

	public Object getSelectedNodeObject() {
		DefaultMutableTreeNode node = getSelectedNode();
		if (node == null)
			return null;
		else
			return node.getUserObject();
	}
}
