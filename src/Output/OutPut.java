package Output;

public interface OutPut {
	
	public void setValue(Long itemID, Long userID, Double metricValue);
	public void setValue(Long itemID, Double metricValue);
	public void setValue(Double metricValue);
	public void finish();
	public void flush();
	public void setValueForce(Long itemID, Long userID, Double metricValue);
	

}
