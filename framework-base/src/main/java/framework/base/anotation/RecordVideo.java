package framework.base.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 此注解是为了在测试过程中录制视频，以方便失败后查找失败原因；
 * 需要 提前安装CamStudio2 and camstudio_cl.exe
 * @author James Guo
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RecordVideo {
	
}

