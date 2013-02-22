/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.lang.reflect.Method;
import org.junit.Test;

/**
 *
 * @author mre
 */
public class WarStarterTest {

    @Test
    public void shouldWorkWithSingleJar() throws Exception {
        
        File f = new File("D:\\labs\\blog\\target\\blog-0.0.1-SNAPSHOT.war");
        
        WarStarter bacl = new WarStarter(f, this.getClass().getClassLoader());
        
        Class<?> clazz = bacl.loadClass("Main");
        System.out.println(clazz);
        Class[] argTypes = new Class[]{String[].class};
        Method main = clazz.getDeclaredMethod("main", argTypes);
        String[] mainArgs = new String[]{};
        //Arrays.copyOfRange(args, 1, args.length);
        main.invoke(null, (Object) mainArgs);
        
        
    }
}
