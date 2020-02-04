package application.swingUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import application.PatchSelector;
import application.swingUI.dialogs.ConfirmationDialog;
import application.swingUI.dialogs.TextInputDialog;
import syn.Patch;
import syn.operator.Operator;
import utils.LocalFactory;

public class UIPatchSelector extends PatchSelector {
	private JPanel mainContainer;
	private JList<Patch> listPatches;

	public UIPatchSelector() {
		mainContainer = new JPanel();
		mainContainer.setBorder(BorderFactory.createTitledBorder("Patches"));
		mainContainer.setLayout(new BorderLayout());

		listPatches = new JList<>(new DefaultListModel<>());
		listPatches.setFocusable(false);
		listPatches.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane listScroller = new JScrollPane(listPatches);
		listScroller.setPreferredSize(new Dimension(listPatches.getWidth(), 100));
		mainContainer.add(listScroller, BorderLayout.PAGE_START);

		JPanel panelButtons = new JPanel();
		mainContainer.add(panelButtons, BorderLayout.CENTER);

		panelButtons.setLayout(new GridLayout(0, 2));

		LocalFactory<JButton> btn = (args) -> {
			String caption = (String) args[0];
			JButton newButton = new JButton(caption);
			newButton.setFocusable(false);
			panelButtons.add(newButton);
			return newButton;
		};

		btn.create("Rename").addActionListener((event) -> {
			Patch patch = getSelectedPatch();
			if (patch == null)
				return;

			String newName = getNewPatchName(patch.name);
			if (newName == null)
				return;

			patch.name = newName;
			refreshAndSelectPatch(patch);
		});

		btn.create("Create").addActionListener((event) -> {
			Patch patch = addNewPatch();
			refreshAndSelectPatch(patch);
		});

		btn.create("Delete").addActionListener((event) -> {
			if (!ConfirmationDialog.confirmed("Patch '" + getSelectedPatch().name + "' will be deleted. Continue?"))
				return;

			patches.remove(getSelectedPatch());
			refreshList();
		});

		btn.create("Clone").addActionListener((event) -> {
			Patch sourcePatch = getSelectedPatch();
			Patch patch = addNewPatch();
			if (patch == null)
				return;

			patch.copyFrom(sourcePatch);
			refreshAndSelectPatch(patch);
		});

		btn.create("Reset").addActionListener((event) -> {
			if (!ConfirmationDialog.confirmed("All existing patches will be deleted and replaced with default ones. Continue?"))
				return;

			patches = getDefaultPatches();
			refreshList();
		});

		listPatches.addListSelectionListener((event) -> {
			doOnPatchSelected(getSelectedPatch());
		});
	}

	private Patch getSelectedPatch() {
		return listPatches.getSelectedValue();
	}

	private void selectPatch(Patch patch) {
		listPatches.setSelectedValue(patch, true);
	}

	private void refreshAndSelectPatch(Patch patch) {
		if (patch == null)
			return;

		refreshList();
		selectPatch(patch);
	}

	private Patch addNewPatch() {
		String newPatchName = getNewPatchName();
		if (newPatchName == null)
			return null;

		Patch patch = new Patch();
		patch.algorithm.addOperator(new Operator());
		patch.name = newPatchName;
		patches.add(patch);

		sort();

		return patch;
	}

	private String getNewPatchName() {
		String initialName = "Patch_" + UUID.randomUUID().toString().substring(0, 8);
		return getNewPatchName(initialName);
	}

	private String getNewPatchName(String initialName) {
		TextInputDialog input = new TextInputDialog();
		input.initialValue = initialName;
		input.prompt = "Name for a new patch." + System.lineSeparator() +
			"Only latin letters, numbers and '_' symbol are allowed.";

		input.onInputValidation = (patchName) -> {
			return Pattern.matches("\\w*", patchName) && ( patchNameIsUnique(patchName) || canOverwrite(patchName) );
		};

		return input.getInput();
	}

	private boolean patchNameIsUnique(String newPatchName) {
		for (Patch patch : patches)
			if (patch.name.equalsIgnoreCase(newPatchName))
				return false;

		return true;
	}

	private boolean canOverwrite(String patchName) {
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
			null,
			"Patch with this name already exists. Overwrite it?",
			patchName,
			JOptionPane.OK_CANCEL_OPTION
		);
	}

	public JComponent getMainContainer() {
		return mainContainer;
	}

	@Override
	public void load() {
		super.load();
		refreshList();
	}

	private DefaultListModel<Patch> getListModel() {
		return (DefaultListModel<Patch>) listPatches.getModel();
	}

	public void refreshList() {
		getListModel().removeAllElements();
		for (Patch patch : patches)
			getListModel().addElement(patch);

		selectPatch(patches.get(0));
	}
}
