package cmpe283.project1;

import com.vmware.vim25.AlarmState;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineQuickStats;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VM {

	private VirtualMachine vm;

	public VM(VirtualMachine vm) {
		this.vm = vm;
	}

	public void powerOn() throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println(vm.getName() + " is powering on...");
		
		if (task.waitForTask() == Task.SUCCESS)
			Thread.sleep(5000);
			System.out.println(vm.getName() + " is running now.");
	}

	public void powerOff() throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println(vm.getName() + " is powering off...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is shut down.");
	}

	public boolean clone(VHost newhost) throws Exception {
		VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
		VirtualMachineRelocateSpec locationSpec = new VirtualMachineRelocateSpec();
		locationSpec.setHost(newhost.getHost().getMOR());

		cloneSpec.setLocation(locationSpec);
		cloneSpec.setPowerOn(true);
		cloneSpec.setTemplate(false);

		Task task = vm.cloneVM_Task((Folder) vm.getParent(), vm.getName()
				+ "-Clone", cloneSpec);
		System.out.println("Launching the VM clone task. " + "Please wait ...");

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("VM got cloned successfully.");
			powerOn();
			return true;
		} else {
			System.out.println("Failure -: VM cannot be cloned");
			return false;
		}
	}

	public boolean migrate(VHost newhost) throws Exception {
		HostSystem newHost = newhost.getHost();
		ComputeResource cr = (ComputeResource) newHost.getParent();

		System.out.println("Start migration......");
		// migrate no matter the vm's power state is on or off
		Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
				VirtualMachineMovePriority.highPriority, null);

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(vm.getName() + " is migrated to host "
					+ newHost.getName());
			// if the vm is powered off, power it on
			if (vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOn)
				powerOn();
			return true;
		} else {
			System.out.println(vm.getName() + " migration failed!");
			TaskInfo info = task.getTaskInfo();
			System.out.println(info.getError().getFault());
		}
		return false;
	}

	public boolean checkPowerOffAlarm(Alarm alarm) {
		AlarmState[] as = vm.getTriggeredAlarmState();
		if (as == null)
			return false;
		for (AlarmState state : as) {
			// if the vm has a poweroff alarm, return true;
			if (alarm.getMOR().getVal().equals(state.getAlarm().getVal()))
				return true;
		}
		return false;
	}

	public boolean ping() throws Exception {
		int time = 0;
		
		while (!Ping.pingIP(getIP())) {						
			time++;
			if (time >= Setting.NumPing)
				return false;
			else 
				Thread.sleep(Setting.PingInterval * 1000);
		}
		return true;
	}

	public VirtualMachine getVM() {
		return vm;
	}

	public String getIP() {
		return vm.getGuest().getIpAddress();
	}
	
	public String getName() {
		return vm.getName();
	}
	
	public void print() throws Exception {
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
