package xm.soft;

import java.util.ArrayList;

public class SyncStack <E> {
	private ArrayList<E> mData;
	public SyncStack(){
		mData = new ArrayList<E>();
	}
	public synchronized void push(E data){
		mData.add(data);
		this.notify();
	}
	public synchronized int size(){
		return mData.size();
	}
	public synchronized E pop(){
		while (mData.isEmpty()){
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mData.remove(mData.size() - 1);
	}
}
