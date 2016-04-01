package sdis.multicast;

// Performs and handles operations related to the multicast groups
public class MultiCastDriver{
    private MultiCastDriverMC mc = new MultiCastDriverMC();
    private MultiCastDriverMDB mdb = new MultiCastDriverMDB();
    private MultiCastDriverMDR mdr = new MultiCastDriverMDR();
    
    public MultiCastDriver(){};
    
    public void start(){
        mc.start();
        mdb.start();
        mdr.start();
    }
}
