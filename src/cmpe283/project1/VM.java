package cmpe283.project1;

import com.vmware.vim25.AlarmState;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineQuickStats;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VM {
	private VirtualMachine vm;
	private boolean powerOff;
	
	public VM(VirtualMachine vm) {
		this.vm = vm;		
		powerOff = (vm.getRuntime().getPowerState() == VirtualMachinePowerState.poweredOff);
	}
	
	public void setPowerOff() {
		powerOff = !powerOff;
	}
	
	public boolean ping() throws Exception {
		if (vm.getGuestHeartbeatStatus() != ManagedEntityStatus.green) {
			System.out.println("Alarms on VM: " + vm.getName());
			AlarmState[] as = Alarms.alarmMgr.getAlarmState(vm);
			for(AlarmState state : as) {
				System.out.println("state.getEntity().getVal(): " + state.getEntity().getVal());
				System.out.println("state.getEntity().getType(): " + state.getEntity().getType());
				System.out.println("state.getAlarm().getVal(): " + state.getAlarm().getVal());
				System.out.println("state.getAlarm().getType(): " + state.getAlarm().getType());
				System.out.println("state.getOverallStatus(): " + state.getOverallStatus());
				//return state.getOverallStatus() == ManagedEntityStatus.red;
			}
		}
		return true;
	}
	
	public void powerOn() throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println(vm.getName() + " is powering on...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is running now.");
	}
	
	public void powerOff() throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println(vm.getName() + " is powering off...");
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(vm.getName() + " is shut down.");
	}
	
	public String getIP() {
		return vm.getGuest().getIpAddress();
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
		System.out.println("IP Addresses: "
				+ vm.getGuest().getIpAddress());
		System.out.println("State: " + vm.getGuest().guestState);
		
		
		if(!vm.getGuest().guestState.equals("notRunning")) {
			PerfMgr.printPerf(vm); // print real time performance
			System.out.println("Data from VirtualMachineQuickStats: ");
			VirtualMachineQuickStats qs = vm.getSummary().getQuickStats();
			System.out.println(String.format("%-25s%s", "OverallCpuUsage: ", qs.getOverallCpuUsage() + " MHz"));
			System.out.println(String.format("%-25s%s", "GuestMemoryUsage: ", qs.getGuestMemoryUsage() + " MB"));
			System.out.println(String.format("%-25s%s", "ConsumedOverheadMemory: ", qs.getConsumedOverheadMemory() + " MB"));
			System.out.println(String.format("%-25s%s", "FtLatencyStatus: ", qs.getFtLatencyStatus()));
			System.out.println(String.format("%-25s%s", "GuestHeartbeatStatus: ", qs.getGuestHeartbeatStatus()));			
		}	
	}
}
