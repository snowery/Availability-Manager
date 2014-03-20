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
	private List<VHost> vHosts;
	private ManagedEntity[] vms;
	
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
		
		
		//Alarms.createPowerAlarm(vCenter);
		
	}
	
	public void printStat() throws Exception {
		for (VHost host : vHosts) 
			host.pirntVMs();
	}
	
	public void ping() throws Exception {
		for (VHost host : vHosts)  
			host.ping();
	}
	
	public static void main(String[] args) throws Exception {
		AvailabilityManager manager = new AvailabilityManager("https://130.65.132.150/sdk", "administrator", "12!@qwQW");
		PerfMgr.setUp();
		manager.printStat();
		//manager.ping();
	/*	
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		if(mes==null || mes.length ==0)
		{
			return;
		}
		for (int i = 0; i < mes.length; i++)
		{
			VirtualMachine vm = (VirtualMachine) mes[i]; 
			
			VirtualMachineConfigInfo vminfo = vm.getConfig();
			VirtualMachineCapability vmc = vm.getCapability();
			
			vm.getResourcePool();
			System.out.println("Hello " + vm.getName());
			System.out.println("GuestOS: " + vminfo.getGuestFullName());
			System.out.println("Multiple snapshot supported: " + vmc.isMultipleSnapshotsSupported());
		}
		*/
		vCenter.getServerConnection().logout();
		System.out.println("end");
	}
}
