package cmpe283.project1;

import java.rmi.RemoteException;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;


public class VHost {

	private HostSystem host;
	private VM[] vms;
	
	public VHost(HostSystem host) throws Exception {
		this.host = host;

		ManagedEntity[] mes = new InventoryNavigator(host).searchManagedEntities("VirtualMachine");
		if(mes==null || mes.length ==0) {
			return;
		}
		
		vms = new VM[mes.length];
		
		for (int i = 0; i < mes.length; i++) {
			vms[i] = new VM((VirtualMachine) mes[i]);
			//vms[i].powerOn();
		}
	}
	
	// 0: success 1: VM failure 2: Vhost failure
	public void ping() throws Exception {
		for (int i = 0; i < vms.length; i++) {
			if (vms[i].ping()) {
				System.out.println(vms[i].getIP() + ": Vm ping success.");
				
			}
			else {
				System.out.println(vms[i].getIP() + ": Vm ping failure.");
				if (host.getOverallStatus() == ManagedEntityStatus.green)
					System.out.println(getIP() + ": Vhost ping success.");
				else
					System.out.println(getIP() + ": Vhost ping failure.");
			}
		}
		
	}
	
	public String getIP() {
		return host.getConfig().getNetwork().getVnic()[0].spec.ip.ipAddress;
	}
	
	public void pirntVMs() throws Exception {
		System.out.println("vHost: " + getIP());
		PerfMgr.printPerf(host);
		System.out.println("=========VMs=========");
		
		if (vms == null) return;
		for (int i = 0; i < vms.length; i++) 
			vms[i].print();
		System.out.println("=================================");
	}
	
}
