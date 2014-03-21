package cmpe283.project1;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;


public class VHost {

	private HostSystem host;
	private List<VirtualMachine> vms;
	
	public VHost(HostSystem host) throws Exception {
		this.host = host;

		ManagedEntity[] mes = new InventoryNavigator(host).searchManagedEntities("VirtualMachine");
		if(mes==null || mes.length ==0) {
			return;
		}
		
		vms = new ArrayList<VirtualMachine>();
		
		for (int i = 0; i < mes.length; i++) {
			vms.add((VirtualMachine) mes[i]);
			//vms.get(i).powerOn();
		}
	}

	public void ping() throws Exception {
		for (int i = 0; i < vms.size(); i++) {
			if (!VM.ping(vms.get(i))){
				int n = 0; 
				while (n < Setting.NumBootHost) {
					if (Ping.pingIP(getIP())) {
						System.out.println(getIP() + ": Vhost ping success.");
						//TODO recover VM from snapshot
						break;
					}
					else {
						System.out.println(getIP() + ": Vhost ping failure.");
						//TODO try to restart vhost
						n++;
					}
				}
				//System.out.println("Vhost is unreachable.");
				//break;
			}
		}
		//TODO add another available host
		//TODO migrate Vms
		//TODO remove this host
	}
	
	
	
	public void createSnapshot() throws Exception {
		if (vms == null) return;
		for (int i = 0; i < vms.size(); i++) 
			SnapShotMgr.createSanpshot(vms.get(i));
	}
	
	public String getIP() {
		return host.getConfig().getNetwork().getVnic()[0].getSpec().getIp().getIpAddress();
	}
	
	public void print() throws Exception {
		System.out.println();
		System.out.println("vHost: " + getIP());
		PerfMgr.printPerf(host);
		System.out.println("=========VMs=========");
		
		if (vms == null) return;
		for (int i = 0; i < vms.size(); i++) 
			VM.print(vms.get(i));
		System.out.println("=================================");
	}
	
}
