package cmpe283.project1;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostVirtualNic;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class AvailabilityManager {
	public static ServiceInstance vCenter;	
	public static Alarm powerOffAlarm;
	private List<VHost> vHosts;
	
	public AvailabilityManager (String url, String user, String pwd) throws Exception {
		vCenter = new ServiceInstance(new URL(url), user, pwd, true);
		Folder vCenterFolder = vCenter.getRootFolder();
		ManagedEntity[] vHosts = new InventoryNavigator(vCenterFolder).searchManagedEntities("HostSystem");
		if(vHosts.length != 0) {
			this.vHosts = new ArrayList<VHost>(vHosts.length);
			
			for (int i = 0; i < vHosts.length; i++) {
				this.vHosts.add(new VHost((HostSystem) vHosts[i]));
			}
		}
		
		PerfMgr.setUp();
//		powerOffAlarm = AlarmMgr.createPowerAlarm(vCenter);
		
	}
	
	public void printStat() throws Exception {
		for (VHost host : vHosts) 
			host.print();
	}
	
	public void ping() throws Exception {
		for (VHost host : vHosts)  
			host.ping();
	}
	
	public void createSnapshot() throws Exception {
		for (VHost host : vHosts) {
			host.createSnapshot();
		}
	}
	public static void main(String[] args) throws Exception {
		AvailabilityManager manager = new AvailabilityManager("https://130.65.132.150/sdk", "administrator", "12!@qwQW");
		
		// print vHosts and vms performance
//		manager.printStat();
		
//		manager.createSnapshot();
//		manager.ping();		
		
//		powerOffAlarm.removeAlarm();
		System.out.println("PowerOff State Alarm removed!");
		vCenter.getServerConnection().logout();
		System.out.println("-Server Disconncted-");
	}
}
