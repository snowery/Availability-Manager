package cmpe283.project1;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;

public class VCenter {
	private ServiceInstance vCenter;
	private List<VHost> vHosts;
	private Alarm powerOffAlarm;

	public VCenter(ServiceInstance vCenter) throws Exception {
		this.vCenter = vCenter;
		setHosts();
		if (vHosts.size() == 0 && addAvailableHost() == null)
			throw new NullPointerException("No host available. ");
		
		PerfMgr.setUp(vCenter);
		powerOffAlarm = AlarmMgr.createPowerAlarm(vCenter);
	}
	
	public void printStat() throws Exception {
		while (true) {
			for (VHost host : vHosts)
				host.print();
			System.out.println(String.format("Sleeping %d seconds until next print...", Setting.PrintInterval));
			System.out.println();
			Thread.sleep(Setting.PrintInterval * 1000);
		}
	}

	public void heartbeat() throws Exception {
		while (true) {
			System.out.println();
			for (int i = 0; i < vHosts.size(); i++) {
				
				if (!checkAvailability(vHosts.get(i))) {
					// solution 1: if vhost is unavailable, recover from snapshot
					if (!vHosts.get(i).recover()) {
						// solution 2: if vhost is unavailable, add new host and failover
						VHost newhost = addAvailableHost();
						if (newhost == null)  break; // no available host
						if (vHosts.get(i).failover(newhost)) 
							// remove unavailable host from list
							vHosts.remove(i);
					}
					// restart check each vhost in the list
					i = -1;
				}
				
			} 
			System.out.println(String.format("Sleeping %d seconds until next heartbeat...", Setting.HeartbeatInterval));
			System.out.println();
			Thread.sleep(Setting.HeartbeatInterval * 1000);
		}
	}

	public void createSnapshot() throws Exception {
		while(true) {
			System.out.println();
			for (VHost host : vHosts) {
				host.createSnapshot();
			}
			System.out.println(String.format("Sleeping %d seconds until next snapshot...", Setting.BackupInterval));
			System.out.println();
			Thread.sleep(Setting.BackupInterval * 1000);
		}
	}

	public Alarm getPowerOffAlarm() {
		return powerOffAlarm;
	}
	
	private void setHosts() throws Exception {
		this.vHosts = new ArrayList<VHost>();
		Folder vCenterFolder = vCenter.getRootFolder();
		ManagedEntity[] vHosts = new InventoryNavigator(vCenterFolder)
				.searchManagedEntities("HostSystem");
		if (vHosts.length != 0) {
			for (int i = 0; i < vHosts.length; i++) {
				this.vHosts.add(new VHost((HostSystem) vHosts[i]));
			}
			System.out.println("All connected hosts retrieved.");
		} else {
			System.out.println("No host connected.");
		}
	}

	private VHost addAvailableHost() throws Exception {
		String newHost = findAvailableHost();
		if (newHost == null) {
			System.out.println("No host available.");
			return null;
		}
		return addHost(newHost);
	}
	
	private VHost addHost(String hostName) throws Exception {
		if (vHosts != null) {
			for (VHost host: vHosts) {
				if (hostName.equals(host.getIP())) return host;
			}
		}
		HostConnectSpec newHost = new HostConnectSpec();
		newHost.setHostName(hostName);
		newHost.setUserName("root");
		newHost.setPassword(Setting.Password);
		newHost.setSslThumbprint(Setting.Vhosts.get(hostName));

		Datacenter dc = (Datacenter) vCenter.getRootFolder().getChildEntity()[0];

		// add host
		Task task = dc.getHostFolder().addStandaloneHost_Task(newHost,
				new ComputeResourceConfigSpec(), true);
		System.out.println("Adding host " + hostName + "......");
		if (task.waitForTask() == Task.SUCCESS) {
			HostSystem host = (HostSystem) new InventoryNavigator(
					vCenter.getRootFolder()).searchManagedEntity("HostSystem",
					hostName);
			vHosts.add(new VHost(host));
			System.out.println("Add host success");
			return new VHost(host);
		} else {
			System.out.println(task.getTaskInfo().getError().getLocalizedMessage());
			return null;
		}
	}

	private String findAvailableHost() throws Exception {
		for (String ip : Setting.Vhosts.keySet()) {
			if (Ping.pingIP(ip)) {
				System.out.println(ip + " is available.");
				return ip;
			}
		}
		return null;
	}

	private boolean checkAvailability(VHost vhost) throws Exception {
		vhost.setVMs();
		List<VM> vms = vhost.getVMs();
		if (vms == null) return true;
		
		for (int i = 0; i < vms.size(); i++) {
			VM vm = vms.get(i);
			
			if (vm.checkPowerOffAlarm(powerOffAlarm)) {
				System.out.println(vm.getVM().getName() + " is powered off.");
				continue;
			}
			
			if (vm.ping()) {
				System.out.println(vm.getVM().getName() + " is available.");
				continue;
			}

			System.out.println(vm.getVM().getName() + " is unreachable.");

			// vm failure, vhost ping success, recover vm to last state
			if (vhost.ping()) {
				System.out.println(vhost.getIP() + ": host is available.");
				// recover vm by snapshot
				SnapShotMgr.revert2LastSnapshot(vm);
				vm.powerOn();
				while(vm.getIP() == null);
				i--;
			} else {
				System.out.println(vhost.getIP() + ": host ping failure.");
				return false;
			}
		}
		return true;
	}
}
