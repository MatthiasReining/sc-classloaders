
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WarStarter extends ClassLoader {

    private static final Logger LOG = Logger.getLogger(WarStarter.class.getName());
    private Map<String, byte[]> resourceStore;

    public static void main(String... args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        final String key = "WarStarter-Main-Class";

        String mainClass = System.getProperty(key);
        if (mainClass != null)
            System.out.println("Found SystemProperty 'WarStarter-Main-Class'");
        else {
            try {
                Manifest mf = new Manifest(WarStarter.class.getResourceAsStream("META-INF/MANIFEST.MF"));
                mainClass = mf.getMainAttributes().getValue(key);
                if (mainClass == null)
                    throw new IOException("There's no valid '" + key + "' key!");
                System.out.println("Found MANIFEST.MF");
            } catch (IOException ex) {
                System.err.println("There is no 'WarStarter-Main-Class' Parameter. Neither as system property (java -jar xy.war -DWarStarter-Main-Class=Main) nor as element (WarStarter-Main-Class) inside META-INF/MANIFEST.MF");
                System.exit(-1);
            }
        }

        
        String warFilePath = WarStarter.class.getResource("WEB-INF").getFile();
        warFilePath = warFilePath.substring(0, warFilePath.lastIndexOf("!"));
        warFilePath = warFilePath.substring("file:".length());
        System.out.println("war file path: " + warFilePath);
                
        File warFile = new File(warFilePath);        
        WarStarter ws = new WarStarter(warFile, WarStarter.class.getClassLoader());
        
        System.out.println("Start " + mainClass );
        Class<?> clazz = ws.loadClass(mainClass);
        
        Class[] argTypes = new Class[]{String[].class};
        Method main = clazz.getDeclaredMethod("main", argTypes);
        main.invoke(null, (Object) args);
    }

    public WarStarter(File warFile, ClassLoader parent) throws IOException {
        super(parent);

        InputStream war = new FileInputStream(warFile);
        init(war);
    }

    public WarStarter(InputStream warFile, ClassLoader parent) throws IOException {
        super(parent);

        init(warFile);
    }

    private void init(InputStream warFile) throws IOException {
        long startTime = System.currentTimeMillis();
        resourceStore = new HashMap<>();

        ZipInputStream zis = new ZipInputStream(warFile);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.isDirectory()) continue;

            String resourceName = ze.getName();

            if (resourceStore.containsKey(resourceName)) continue;

            byte[] b = getByteArrayFromZip(zis);
            if (resourceName.startsWith("WEB-INF/classes/") && resourceName.endsWith(".class"))
                //write always into resoureStore (classes over jars)
                //cut off 'WEB-INF/classes
                resourceStore.put(resourceName.substring("WEB-INF/classes/".length()), b);
            else if (resourceName.startsWith("WEB-INF/lib") && resourceName.endsWith(".jar")) {
                InputStream is = new ByteArrayInputStream(b);
                initJarFile(is);
            } else
                resourceStore.put(resourceName, b);
        }

        LOG.log(Level.INFO, "WARClassLoader was initalized in {0}ms.", (System.currentTimeMillis() - startTime));

    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        LOG.log(Level.FINEST, "WARClassLoader stream-request for '{0}'.", name);
        if (resourceStore.get(name) == null)
            return super.getResourceAsStream(name);
        byte[] b = resourceStore.get(name);
        return new ByteArrayInputStream(b);
    }

    @Override
    protected Class<? extends Object> findClass(String name) throws ClassNotFoundException {
        LOG.log(Level.FINEST, "WARClassLoader class request for '{0}'.", name);

        String resourceName = name.replace('.', '/') + ".class";

        byte[] b = resourceStore.get(resourceName);
        if (b == null) throw new ClassNotFoundException(name);


        Class<? extends Object> clazz = defineClass(name, b, 0, b.length);
        return clazz;
    }

    private byte[] getByteArrayFromZip(ZipInputStream jis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        for (int len = 0; len != -1;) {
            len = jis.read(b);
            if (len != -1) baos.write(b, 0, len);
        }
        baos.flush();
        baos.close();
        //FileOutputStream fos = new FileOutputStream("D:\\testjar\\" + javaName.substring(javaName.lastIndexOf(".")+1) + ".class");
        //fos.write(baos.toByteArray());
        //fos.flush();
        //fos.close();
        return baos.toByteArray();
    }

    private void initJarFile(InputStream is) throws IOException {
        JarInputStream jis = new JarInputStream(is);

        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null) {
            if (!je.isDirectory()) {
                String resourceName = je.getName();

                if (resourceStore.containsKey(resourceName)) continue;
                byte[] b = getByteArrayFromZip(jis);
                resourceStore.put(resourceName, b);
            }
        }
    }
}