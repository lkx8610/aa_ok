package framework.base.exception;
/** 
*@description TODO 
*@author James Guo
**/
public class TimeoutException extends Exception {
	private static final long serialVersionUID = 1L;
	public TimeoutException() {
		
	}
	public TimeoutException(String msg) {
		super(msg);
	}
}
