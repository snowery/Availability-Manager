package cmpe283.project1;

import com.vmware.vim25.AlarmState;
import com.vmware.vim25.VirtualMachineQuickStats;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VM {	
	public static boolean ping(VirtualMachine vm) throws Exception {
		if (!Ping.pingIP(vm.getGuest().getIpAddress()) && !checkPowerOffAlarm(vm)) {
			System.out.println(vm.getName() + " is unreachable.");
			return false;
		}
		System.out.println(vm.getName() + " is available. ");
		return true;
	}

	private static boolean checkPowerOffAlarm(VirtualMachine vm) {
		AlarmState[] as = vm.getTriggeredAlarmState();
		if (as == null)
			return false;
		for (AlarmState state : as) {
			// if the vm has a poweroff alarm, return true;
			if (AvailabilityManager.powerOffAlarm.getMOR().getVal()
					.equals(state.getAlarm().getVal())) {
				System.out.println(vm.getName() + " is powered off. ");
				return true;
			}
		}
		return false;
	}

	public static void powerOn(VirtualMachine vm) throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println(vm.getName() + " is powering on...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is running now.");
	}

	public static void powerOff(VirtualMachine vm) throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println(vm.getName() + " is powering off...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is shut down.");
	}

	public static void print(VirtualMachine vm) throws Exception {
		System.out.println("\nName: " + vm.getName());
		System.out.println("Guest OS: "
				+ vm.getSummary().getConfig().guestFullName);
		System.out.println("VM Version: " + vm.getConfig().version);
		System.out.println("CPU: " + vm.getConfig().getHardware().numCPU
				+ " vCPU");
		System.out.println("Memory: " + vm.getConfig().getHardware().memoryMB
				+ " MB");
		System.out.println("IP Addresses: " + vm.getGuest().getIpAddress());
		System.out.println("State: " + vm.getGuest().guestState);

		if (!vm.getGuest().guestState.equals("notRunning")) {
			PerfMgr.printPerf(vm); // print real time performance
			System.out.println("Data from VirtualMachineQuickStats: ");
			VirtualMachineQuickStats qs = vm.getSummary().getQuickStats();
			System.out.println(String.format("%-25s%s", "OverallCpuUsage: ",
					qs.getOverallCpuUsage() + " MHz"));
			System.out.println(String.format("%-25s%s", "GuestMemoryUsage: ",
					qs.getGuestMemoryUsage() + " MB"));
			System.out.println(String.format("%-25s%s",
					"ConsumedOverheadMemory: ", qs.getConsumedOverheadMemory()
							+ " MB"));
			System.out.println(String.format("%-25s%s", "FtLatencyStatus: ",
					qs.getFtLatencyStatus()));
			System.out.println(String.format("%-25s%s",
					"GuestHeartbeatStatus: ", qs.getGuestHeartbeatStatus()));
		}
	}
}
