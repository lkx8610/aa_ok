package framework.base.exception;
/**
 * 当方法抛出这个自定义的异常时，表示要停止当前的程序运行；
 * 一般用在硬断言中；
 * @author James Guo
 *
 */
public class StopRunningException extends Exception {

	private static final long serialVersionUID = 1L;

	public StopRunningException(){
		
	}
	
	public StopRunningException(String msg){
		super(msg);
	}
	
	public StopRunningException(String msg, Exception e){
		super(msg, e);
	}
	
	public StopRunningException(Exception e){
		super(e);
	}
	
	public StopRunningException(Throwable t){
		super(t);
	}
}
