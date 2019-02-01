package Measure;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CPUTimer 
{
    private long _startCPUTime = 0l;
    private long _startElapsedTime = 0l;
    
    private ThreadMXBean bean;
    protected String name = "Timer";
    
    protected long CPUresult;
    protected long elapsedResult;
    
    public CPUTimer()
    {
    	bean = ManagementFactory.getThreadMXBean();
    	if (!bean.isCurrentThreadCpuTimeSupported()) System.err.println("Warning: Thread CPU timing not supported");
    }
    
    public CPUTimer(String nm)
    {
    	this();
    	name = nm;
    }
    
    public void start ()
    {
        _startCPUTime = getCpuTimeInMillis();
        _startElapsedTime = getCurrentTimeInMillis();
    } 

    public long stop ()
    {
        CPUresult = (getCpuTimeInMillis() - _startCPUTime);
        elapsedResult = (getCurrentTimeInMillis() - _startElapsedTime);
        _startCPUTime = 0l;
        return CPUresult;
    }

    public boolean isRunning ()
    {
        return _startCPUTime != 0l;
    }
    
    public long status()
    {
    	if(isRunning())
    	{
    	CPUresult = (getCpuTimeInMillis() - _startCPUTime);
        return CPUresult;
    	}
    	return 0l;
    }
    
    public void print()
    {
    	System.out.println(name+":\n"
    						+ "    CPU time: "+CPUresult+" ms\n"
    					    + "    elapsed time: "+elapsedResult+" ms");
    }

    
    
    /** thread CPU time in milliseconds. */
    private long getCpuTimeInMillis ()
    {
        return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime()/1000000: 0L;
    }
    
    private long getCurrentTimeInMillis()
    {
    	return System.nanoTime() / 1000000;
    }
}