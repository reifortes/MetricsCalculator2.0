package Measure;


import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.sun.management.*;

public class ProcessCPUTimer extends CPUTimer {

	@SuppressWarnings("restriction")
	private OperatingSystemMXBean osbean;
	
	private long _startProcessTime;
	protected long processResult;
	
	@SuppressWarnings("restriction")
	public ProcessCPUTimer(String name)
	{
		super(name);
		osbean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();	
	}
	
	public void start ()
    {
        _startProcessTime = getProcessTimeInMillis();
        super.start();
    } 

    public long stop ()
    {
        processResult = (getProcessTimeInMillis() - _startProcessTime);
        _startProcessTime = 0l;
        return super.stop();
    }
    
    public void print()
    {
    	System.out.println(name+":\n"
    						+ "    process CPU time: "+processResult+" ms\n"
    						+ "    thread CPU time: "+CPUresult+" ms\n"
    					    + "    elapsed time: "+elapsedResult+" ms");
    }
	
    @SuppressWarnings("restriction")
	private long getProcessTimeInMillis ()
    {
        return TimeUnit.MILLISECONDS.convert(osbean.getProcessCpuTime(), TimeUnit.NANOSECONDS);
    }
}
