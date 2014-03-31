package cmpe283.project1;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VHost {

	private HostSystem host;
	private List<VM> vms;

	public VHost(HostSystem host) throws Exception {
		this.host = host;
		setVMs();
	}

	public boolean recover() throws Exception {
		if (SnapShotMgr.revert2LastSnapshot(this)) {
			System.out.println("Host is recovered from snapshot. ");			
			return reconnect();
		}
		return false;
	}

	public void createSnapshot() throws Exception {
		if (vms == null)
			return;

		SnapShotMgr.createSanpshot(this);
		for (int i = 0; i < vms.size(); i++)
			SnapShotMgr.createSanpshot(vms.get(i));
		
	}

	public boolean failover(VHost newhost) throws Exception {
		System.out.println("Failing over to " + newhost.getIP() + "......");
		for (VM vm : vms) {
			// migrate vms to new host
			if (!vm.migrate(newhost)) return false;
			// or we can clone vms to new host
//			if (!vm.clone(newhost)) return false;
		}

		System.out.println("All Vms on host " + getIP()
				+ " are successfully migrated to " + newhost.getIP());		
		// remove vhost from vCenter
		remove();
		return true;
	}

	// reconnect host
		public boolean reconnect() throws Exception {
			int n = 0;
			while (n < Setting.NumConnectHost) {
				n++;
				Task task = host.reconnectHost_Task(null);
				System.out.println("Reconnecting host " + host.getName() + "......");
				Thread.sleep(1000);
				
				if (task.waitForTask() == Task.SUCCESS) {
					System.out.println("Reconnect host success");
					for (VM vm : vms) {
						vm.powerOn();
						while(vm.getIP() == null);
					}
					return true;
				} else {
					System.out.println(task.getTaskInfo().getError()
							.getLocalizedMessage());
				}
			}
			return false;
		}
		
	// disconnect host
	public void disconnect() throws Exception {
		Task task = host.disconnectHost();
		System.out.println("Disconnecting host " + host.getName() + "......");

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Disconnect host success");
		} else {
			System.out.println(task.getTaskInfo().getError()
					.getLocalizedMessage());
		}
	}

	// remove host
	public void remove() throws Exception {
		System.out.println("Removing host " + host.getName() + "......");
		
		ComputeResource cr = (ComputeResource) host.getParent();
		Task task = cr.destroy_Task();

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Removing host success");
		} else {
			System.out.println(task.getTaskInfo().getError()
					.getLocalizedMessage());
		}
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

	public HostSystem getHost() {
		return host;
	}

	public void setVMs() throws Exception {
		vms = new ArrayList<VM>();
		
		ManagedEntity[] mes = new InventoryNavigator(host)
				.searchManagedEntities("VirtualMachine");
		if (mes == null) return;	
		
		for (int i = 0; i < mes.length; i++) {
			vms.add(new VM((VirtualMachine) mes[i]));
			// vms.get(i).powerOn();
		}
	}

	public List<VM> getVMs() {
		return vms;
	}

	public String getIP() {
		return host.getConfig().getNetwork().getVnic()[0].getSpec().getIp()
				.getIpAddress();
	}

	public void print() throws Exception {
		System.out.println();
		System.out.println("vHost: " + getIP());
		PerfMgr.printPerf(host);
		System.out.println("=========VMs=========");

		if (vms == null)
			return;
		for (int i = 0; i < vms.size(); i++)
			vms.get(i).print();
		;
		System.out.println("=================================");
	}
}
