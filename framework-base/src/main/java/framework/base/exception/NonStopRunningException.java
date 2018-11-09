package framework.base.exception;

/**
 * 当方法抛出这个异常时，表示虽然有错，但程序仍可继续运行；
 * 
 * 一般用在软断言中；
 * @author James Guo
 *
 */
public class NonStopRunningException extends Exception {

	private static final long serialVersionUID = 1L;

	public NonStopRunningException(){
		
	}
	
	public NonStopRunningException(String msg){
		super(msg);
	}
	
	public NonStopRunningException(String msg, Throwable t){
		super(msg, t);
	}
	
	public NonStopRunningException(Throwable t){
		super(t);
	}
}
