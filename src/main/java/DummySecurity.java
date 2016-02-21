import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;

public class DummySecurity {

    public String keystoreFile() {
        return keystoreFile==null?null:keystoreFile.getAbsolutePath();
    }

    public String keystorePassword() {
        return keystorePassword;
    }

    public String truststoreFile() {
        return truststoreFile==null?null:truststoreFile.getAbsolutePath();
    }

    public String truststorePassword() {
        return truststorePassword;
    }

    private File keystoreFile;
    public final String keystorePassword ="keytorePass";
    public final File truststoreFile =null;
    public final String truststorePassword =null;
    private boolean isValid=false;

    public static DummySecurity instance() {
        if(dummySecurity.isValid) {
            return dummySecurity;
        }
        return null;
    }
    private final static DummySecurity dummySecurity=new DummySecurity();

    private DummySecurity() {
        try {
            keystoreFile=new File("../keystore.jks");
            if(!keystoreFile.exists()) {
                keystoreFile = File.createTempFile("dummykeystore", ".tmp");
            }
            try (FileOutputStream fos = new FileOutputStream(keystoreFile);) {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                char[] password = keystorePassword.toCharArray();
                ks.load(null, password);
                ks.store(fos, password);
                isValid = true;
                System.out.println("Keystore: " + keystoreFile.getAbsolutePath());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
