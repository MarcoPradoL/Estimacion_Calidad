package xm.soft;

public class ServiceInfo {
	private String mName;
	private int mPerformance;
	private double mAvailability;
	private String mExtraInfo;
	private int mStatus;


	public String getName(){
		return mName;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public int getPerformance(){
		return mPerformance;
	}
	
	public void setPerformance(int value){
		mPerformance = value;
	}
	
	public double getAvailability(){
		return mAvailability;
	}
	
	public void setAvailability(double value){
		mAvailability = value;
	}
	
	public String getExtraInfo(){
		return mExtraInfo;
	}
	
	public void setExtraInfo(String info){
		mExtraInfo = info;
	}
	/*
	 * 0 indica que está en operación, 1 que falla y 2 que está no disponible
	 */
	public int getStatus(){
		return mStatus;
	}
	public void setStatus(int status){
		mStatus = status;
	}
	public String toString(){
		return "{Name: " + mName + ", Status: " + mStatus + ", Performance: " + mPerformance + "ms, Availability: " + mAvailability + "%, Extra: " + mExtraInfo + "}";   
	}
}
