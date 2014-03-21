package cmpe283.project1;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class SnapShotMgr {

	public static void createSanpshot(VirtualMachine vm) throws Exception {
		String snapshotname = vm.getName() + "-SnapShot";
		String desc = "A snapshot of " + vm.getName();

		Task task = vm.createSnapshot_Task(snapshotname, desc, false, false);
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(snapshotname + " was created.");
	}

	public static void revertSnapshot(VirtualMachine vm, String snapshotname) throws Exception {
		VirtualMachineSnapshot vmsnap = getSnapshotInTree(vm, snapshotname);
		if (vmsnap != null) {
			Task task = vmsnap.revertToSnapshot_Task(null);
			if (task.waitForTask() == Task.SUCCESS) {
				System.out.println(vm.getName() + " reverted to snapshot:"
						+ snapshotname);
			}
		}
	}

	public static void removeSnapshot(VirtualMachine vm, String snapshotname) throws Exception {
		VirtualMachineSnapshot vmsnap = getSnapshotInTree(vm, snapshotname);
		if (vmsnap != null) {
			Task task = vmsnap.removeSnapshot_Task(true);
			if (task.waitForTask() == Task.SUCCESS) {
				System.out.println("Removed snapshot:" + snapshotname);
			}
		}
	}

	public static void removeAllSnapshot(VirtualMachine vm) throws Exception {
		Task task = vm.removeAllSnapshots_Task();
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Removed all snapshots for " + vm.getName());
		}
	}

	private static VirtualMachineSnapshot getSnapshotInTree(VirtualMachine vm,
			String snapName) {
		if (vm == null || snapName == null) {
			return null;
		}

		VirtualMachineSnapshotTree[] snapTree = vm.getSnapshot()
				.getRootSnapshotList();
		if (snapTree != null) {
			ManagedObjectReference mor = findSnapshotInTree(snapTree, snapName);
			if (mor != null) {
				return new VirtualMachineSnapshot(vm.getServerConnection(), mor);
			}
		}
		return null;
	}

	private static ManagedObjectReference findSnapshotInTree(
			VirtualMachineSnapshotTree[] snapTree, String snapName) {
		for (int i = 0; i < snapTree.length; i++) {
			VirtualMachineSnapshotTree node = snapTree[i];
			if (snapName.equals(node.getName())) {
				return node.getSnapshot();
			} else {
				VirtualMachineSnapshotTree[] childTree = node
						.getChildSnapshotList();
				if (childTree != null) {
					ManagedObjectReference mor = findSnapshotInTree(childTree,
							snapName);
					if (mor != null) {
						return mor;
					}
				}
			}
		}
		return null;
	}
}
