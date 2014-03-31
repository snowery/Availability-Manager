package cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.mo.ServiceInstance;

public class AvailabilityMgr {

	public static void main(String[] args) throws Exception {
/*		ServiceInstance si = new ServiceInstance(new URL(Setting.SuperVcenterUrl),
				Setting.UserName, Setting.Password, true);
		
		ResourcePool rp = (ResourcePool) new InventoryNavigator(si.getRootFolder())
		.searchManagedEntity("ResourcePool", "Team03_vHOSTS");
		
		VirtualMachine[] vms = rp.getVMs();
		for (VirtualMachine vm : vms) {
			System.out.println(vm.getName() + " : " + vm.getSummary().getGuest().getIpAddress());
		}
		si.getServerConnection().logout();
*/
				
		ServiceInstance si = new ServiceInstance(new URL(Setting.VcenterUrl),
				Setting.UserName, Setting.Password, true);
		VCenter vCenter = new VCenter(si);
		
		// create snapshot periodically for both vms and vhosts
		ThreadMgr backup = new ThreadMgr(vCenter, "backup");
		backup.start();				
		
		// heartbeat periodically
		ThreadMgr heartbeat = new ThreadMgr(vCenter, "heartbeat");
		heartbeat.start();
	
		// print vHosts and vms performance
		ThreadMgr printstat = new ThreadMgr(vCenter, "printstat");
		printstat.start();
	
//		si.getServerConnection().logout();
//		System.out.println("-Server Disconncted-");
	}
}
