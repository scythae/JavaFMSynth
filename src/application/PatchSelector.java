package application;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import syn.Patch;
import syn.operator.AlgorithmBank;
import utils.Paths;
import utils.Utils;

public class PatchSelector {
	public interface OnPatchSelected {
		void execute(Patch patch);
	}

	public OnPatchSelected onPatchSelected;
	protected List<Patch> patches;

	protected void doOnPatchSelected(Patch patch) {
		if (onPatchSelected != null && patch != null)
			onPatchSelected.execute(patch);
	}

	public void load () {
		patches = loadSavedPatches();
		if (patches.isEmpty())
			patches = getDefaultPatches();

		doOnPatchSelected(patches.get(0));
	}

	public List<Patch> loadSavedPatches() {
		List<Patch> result = new LinkedList<> ();

		for (File file : getPatchFiles()) {
			Patch patch = (Patch) Utils.loadObjectFromFile(file.getPath());
			if (patch != null) {
				patch.name = Utils.removeFileExtension(file.getName());
				result.add(patch);
			}
		}

		return result;
	}

	private File[] getPatchFiles() {
		File f = new File(Paths.patchesStorage);

		FileFilter filter = (File pathname) -> {
			return pathname.isFile() && pathname.getName().endsWith(Paths.patchExtendion);
		};

		File[] patchFiles = f.listFiles(filter);
		if (patchFiles == null)
			patchFiles = new File[0];

		return patchFiles;
	}

	public List<Patch> getDefaultPatches() {
		List<Patch> result = new LinkedList<> ();
		Patch patch;

		patch = new Patch();
		patch.algorithm = Utils.clone(AlgorithmBank.TubularBell);
		patch.name = "_1_TubularBell";
		result.add(patch);

		patch = new Patch();
		patch.algorithm = Utils.clone(AlgorithmBank.GhostPad);
		patch.name = "_2_GhostPad";
		result.add(patch);

		patch = new Patch();
		patch.algorithm = Utils.clone(AlgorithmBank.KickDrum);
		patch.name = "_3_KickDrum";
		patch.gain = 2.5;
		result.add(patch);

		return result;
	}

	public void save() {
		HashSet<String> patchNames = new HashSet<>();

		for (Patch patch : patches) {
			Utils.saveObjectToFile(patch, Paths.patchesStorage + patch.name + Paths.patchExtendion);
			patchNames.add(patch.name);
		}

		for (File file : getPatchFiles()) {
			String patchName = Utils.removeFileExtension(file.getName());
			if (!patchNames.contains(patchName))
				file.delete();
		}
	}

	public void sort() {
		patches.sort((Patch p1, Patch p2) -> {
			return p1.name.compareToIgnoreCase(p2.name);
		});

	}
}
