package cmpe283.project1;

public class ThreadMgr implements Runnable{

	private VCenter vCenter;
	private String threadName;
	private Thread t;
	
	public ThreadMgr(VCenter vCenter, String name) {
		this.vCenter = vCenter;
		threadName = name;
	}
	
	@Override
	public void run() {
		switch (threadName) {
		case "heartbeat":
			try {
				vCenter.heartbeat();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "backup":
			try {
				vCenter.createSnapshot();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "printstat":
			try {
				vCenter.printStat();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		default:
			System.out.println("Wrong thread name! Choose from heartbeat, backup or printstat");
			break;
		}		
	}
	
	public void start ()
	   {
	      System.out.println("\n===== Starting " +  threadName + " =====");
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }
}
