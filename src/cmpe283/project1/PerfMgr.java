package cmpe283.project1;

import java.util.HashMap;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;

public class PerfMgr {
	private static PerformanceManager perfMgr;
	private static HashMap<Integer, PerfCounterInfo> countersInfoMap;
	private static HashMap<String, Integer> countersMap;
	private static PerfMetricId[] pmis;

	public final static void setUp(ServiceInstance vCenter) throws Exception {
		perfMgr = vCenter.getPerformanceManager();
		PerfCounterInfo[] pcis = perfMgr.getPerfCounter();

		// create map between counter ID and PerfCounterInfo, counter name and ID
		countersInfoMap = new HashMap<Integer, PerfCounterInfo>();
		countersMap = new HashMap<String, Integer>();
		for (int i = 0; i < pcis.length; i++) {
			countersInfoMap.put(pcis[i].getKey(), pcis[i]);
			countersMap.put(pcis[i].getGroupInfo().getKey() + "."
							+ pcis[i].getNameInfo().getKey() + "."
							+ pcis[i].getRollupType(), pcis[i].getKey());
		}

		pmis = createPerfMetricId(Setting.PerfCounters);
		System.out.println("Performance manager is set up.");
	}

	public static void printPerf(ManagedEntity me) throws Exception {
		PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(me);
		int refreshRate = pps.getRefreshRate().intValue();
		
		// only return the latest one sample
		PerfQuerySpec qSpec = createPerfQuerySpec(me, 1, refreshRate);

		PerfEntityMetricBase[] pValues = perfMgr
				.queryPerf(new PerfQuerySpec[] { qSpec });
		if (pValues != null) {
			displayValues(pValues);
		}
	}

	private static PerfMetricId[] createPerfMetricId(String[] counters) {
		PerfMetricId[] metricIds = new PerfMetricId[counters.length];
		for (int i = 0; i < counters.length; i++) {
			PerfMetricId metricId = new PerfMetricId();
			metricId.setCounterId(countersMap.get(counters[i]));
			metricId.setInstance("*");
			metricIds[i] = metricId;
		}
		return metricIds;
	}

	private static PerfQuerySpec createPerfQuerySpec(ManagedEntity me,
			int maxSample, int interval) {

		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(me.getMOR());
		// set the maximum of metrics to be return
		qSpec.setMaxSample(new Integer(maxSample));
		qSpec.setMetricId(pmis);
		qSpec.setFormat("csv");
		qSpec.setIntervalId(new Integer(interval));

		return qSpec;
	}

	private static void displayValues(PerfEntityMetricBase[] values) {
		for (int i = 0; i < values.length; ++i) {
			printPerfMetricCSV((PerfEntityMetricCSV) values[i]);
		}
	}

	private static void printPerfMetricCSV(PerfEntityMetricCSV pem) {
		System.out.println("Performance: " + pem.getSampleInfoCSV());
		PerfMetricSeriesCSV[] csvs = pem.getValue();

		HashMap<Integer, PerfMetricSeriesCSV> stats = new HashMap<Integer, PerfMetricSeriesCSV>();

		for (int i = 0; i < csvs.length; i++) {
			stats.put(csvs[i].getId().getCounterId(), csvs[i]);
		}

		System.out.println("ID    Performance Counter                   Unit                Value");
		System.out.println("---------------------------------------------------------------------");
		for (String counter : Setting.PerfCounters) {
			Integer counterId = countersMap.get(counter);
			PerfCounterInfo pci = countersInfoMap.get(counterId);
			String value = null;
			if(stats.containsKey(counterId)) value = stats.get(counterId).getValue();
			System.out.println(String.format("%-6s%-38s%-20s%s", pci.getKey(), 
					pci.getGroupInfo().getKey()+"."+pci.getNameInfo().getKey()+"."+pci.getRollupType(),
					pci.getUnitInfo().getKey(), value));
		}
		System.out.println("---------------------------------------------------------------------");
	}
}
