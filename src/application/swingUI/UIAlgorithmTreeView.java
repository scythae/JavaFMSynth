package application.swingUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import syn.operator.Algorithm;
import syn.operator.Operator;
import utils.LocalFactory;
import utils.Utils;

public class UIAlgorithmTreeView extends UIAlgorithm {
	private JTreeExt tree;
	private JPanel mainContainer;
	private DefaultMutableTreeNode selectedNode;
	private Algorithm algorithm;

	public UIAlgorithmTreeView() {
		mainContainer = new JPanel();
		mainContainer.setLayout(new BorderLayout());
		mainContainer.setBorder(BorderFactory.createTitledBorder("Operators"));

		tree = new JTreeExt("Synth");
		tree.setFocusable(false);
		tree.setCollapsable(false);

		JScrollPane treeScroller = new JScrollPane(tree);
		mainContainer.add(treeScroller, BorderLayout.CENTER);

		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridLayout(1, 2));

		LocalFactory<JButton> btnFactory = (args) -> {
			String caption = (String) args[0];
			JButton btn = new JButton(caption);
			btn.setFocusable(false);
			panelButtons.add(btn);
			return btn;
		};

		JButton btnRemoveOperator = btnFactory.create("Remove");
		JButton btnAddOperator = btnFactory.create("Add");

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

		setAlgorithm(new Algorithm());
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

		if (tree.getRoot().getChildCount() > 0)
			tree.selectNode((DefaultMutableTreeNode) tree.getRoot().getFirstChild());
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
