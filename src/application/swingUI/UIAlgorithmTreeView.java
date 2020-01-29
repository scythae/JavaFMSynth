package application.swingUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import syn.operator.Algorithm;
import syn.operator.Operator;
import utils.Utils;

public class UIAlgorithmTreeView extends UIAlgorithm {
	private JTreeExt tree;
	private JPanel mainContainer;
	private DefaultMutableTreeNode selectedNode;
	private Algorithm algorithm;

	public UIAlgorithmTreeView() {
		mainContainer = new JPanel();
		mainContainer.setLayout(new BorderLayout());

		tree = new JTreeExt("Synth");
		tree.setFocusable(false);
		tree.setCollapsable(false);
		mainContainer.add(tree, BorderLayout.CENTER);

		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridLayout(1, 2));

		JButton btnAddOperator = new JButton("Add");
		panelButtons.add(btnAddOperator);

		JButton btnRemoveOperator = new JButton("Remove");
		panelButtons.add(btnRemoveOperator);

		panelButtons.setSize(panelButtons.getWidth(), btnAddOperator.getHeight());

		mainContainer.add(panelButtons, BorderLayout.PAGE_END);

		btnAddOperator.addActionListener((actionEvent) -> {
			if (onAlgorithmOperatorChange == null || selectedNode == null)
				return;

			Algorithm algorithm = getNodeAlgorithm(selectedNode);
			Operator operator = new Operator();

			onAlgorithmOperatorChange.execute(algorithm, operator, true);
			refresh();
		});

		btnRemoveOperator.addActionListener((actionEvent) -> {
			if (onAlgorithmOperatorChange == null || selectedNode == null)
				return;

			Algorithm algorithm = getNodeAlgorithm(selectedNode.getParent());
			Operator operator = Utils.cast(getNodeAlgorithm(selectedNode), Operator.class);

			if (selectedNode.getNextSibling() == null) {
				DefaultMutableTreeNode nodeToBeSelected = selectedNode.getPreviousSibling();
				if (nodeToBeSelected == null)
					nodeToBeSelected = (DefaultMutableTreeNode) selectedNode.getParent();

				tree.selectNode(nodeToBeSelected);
			}

			onAlgorithmOperatorChange.execute(algorithm, operator, false);
			refresh();
		});

		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				selectedNode = tree.getSelectedNode();

				Operator operator = Utils.cast(getNodeAlgorithm(selectedNode), Operator.class);
				if (onOperatorSelected != null)
					onOperatorSelected.execute(operator);
			}
		});
	}

	private Algorithm getNodeAlgorithm(TreeNode node) {
		if (!(node instanceof DefaultMutableTreeNode))
			return null;

		Object nodeObject = ((DefaultMutableTreeNode) node).getUserObject();
		if (nodeObject instanceof Algorithm)
			return (Algorithm) nodeObject;
		else
			return null;
	}

	@Override
	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
		refresh();
	}

	@Override
	public void refresh() {
		tree.saveSelectionMap();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		fillNode(root, algorithm);

		tree.getModel().setRoot(root);
		tree.fullExpand();

		tree.loadSelectionMap();
	}

	private void fillNode(DefaultMutableTreeNode node, Algorithm algorithm) {
		node.setUserObject(algorithm);

		for (Operator op : algorithm.getOperators()) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode();
			node.add(child);
			fillNode(child, op);
		}
	}

	@Override
	public JComponent getMainComponent() {
		return mainContainer;
	}
}
