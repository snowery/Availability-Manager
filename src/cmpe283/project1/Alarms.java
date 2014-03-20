package cmpe283.project1;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.GroupAlarmAction;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class Alarms {
	public static AlarmManager alarmMgr;
	//create alarm on the datacenter, which can be applied to all vms
	public static void createPowerAlarm(ServiceInstance vc) throws Exception{

		ManagedEntity[] dcs = vc.getRootFolder().getChildEntity();
		
		alarmMgr = vc.getAlarmManager();
		AlarmSpec spec = new AlarmSpec();
		StateAlarmExpression expression = createStateAlarmExpression();
//		AlarmAction powerChangeAction = createAlarmTriggerAction(createActionOnPowerChange());
		
	    spec.setAction(null);
	    spec.setExpression(expression);
	    spec.setName("VmPowerStateAlarm");
	    spec.setDescription("Monitor VM state and alarm red if VM powers off");
	    spec.setEnabled(true);    
	    
	    AlarmSetting as = new AlarmSetting();
	    as.setReportingFrequency(0); //the alarm is allowed to trigger as often as possible
	    as.setToleranceRange(0);
	    
	    spec.setSetting(as);
	    
	    for(int i = 0; i < dcs.length; i++)
	    	alarmMgr.createAlarm(dcs[i], spec);
	}

	//set alarm to red when the vm powered off, otherwise, green
	private static StateAlarmExpression createStateAlarmExpression() {
		StateAlarmExpression expression = new StateAlarmExpression();
		expression.setType("VirtualMachine");
		expression.setStatePath("runtime.powerState");
		expression.setOperator(StateAlarmOperator.isEqual);
		expression.setRed("poweredOff");
		return expression;
	}
/*
	private static MethodAction createActionOnPowerChange() {
		MethodAction action = new MethodAction();
		action.setName("PowerOnVM_Task"); // how to invoke self defined method???
		MethodActionArgument argument = new MethodActionArgument();
		argument.setValue(null);
		action.setArgument(new MethodActionArgument[] { argument });
		return action;
	}

	private static AlarmTriggeringAction createAlarmTriggerAction(Action action) {
		AlarmTriggeringAction alarmAction = new AlarmTriggeringAction();
		alarmAction.setYellow2red(true);
		alarmAction.setYellow2green(true);
		alarmAction.setAction(action);
		return alarmAction;
	}
*/
}
