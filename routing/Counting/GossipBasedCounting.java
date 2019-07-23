package routing.Counting;

import core.*;
import java.util.*;
import report.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/*Class ini digunakan untuk melakukan perhitungan jumlah total node
dengan menggunakan algoritma Gossip-Based Aggregation.
Pada method connectionUp digunakan untuk menentukan inisiator,
"inisiator" akan dipanggil dari setting(INISIATOR_SETTING).
Method doExchangeForNewConnection digunakan untuk algoritma 
Gossip-Based Aggregation. Method nilai() digunakan untuk copy pesan
pada Spray and Wait.*/

public class GossipBasedCounting implements RoutingDecisionEngine, NilaiInisiator {

    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "GossipBasedCounting";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";
    public static final String INISIATOR_SETTING = "inisiator";

    protected double inisial_val = 0;
    protected int Inisiator;
    protected double rata;

    public boolean isBinary;

    public GossipBasedCounting (Settings s) {
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            this.isBinary = false;
        }
        if (s.contains(INISIATOR_SETTING)) {
            Inisiator = s.getInt(INISIATOR_SETTING);
        }
    }

    public GossipBasedCounting (GossipBasedCounting  r) {
        this.Inisiator = r.Inisiator;
        this.isBinary = r.isBinary;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        if (thisHost.getAddress() == Inisiator && this.inisial_val == 0) {
            this.inisial_val = 1;
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) { 
        DTNHost thisHost = con.getOtherNode(peer);
        GossipBasedCounting  pr = getOtherSnWDecisionEngine(peer);
        double n;
        if (this.inisial_val + pr.inisial_val != 0) {
            this.inisial_val = (this.inisial_val + pr.inisial_val) / 2;
            n = pr.inisial_val = this.inisial_val;
            System.out.println("" + n);
            rata = (int) Math.floor(1 / n);
            System.out.println(rata);
        }
    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, nilai());
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            nrofCopies = 1;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (nrofCopies > 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return false;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new GossipBasedCounting (this);
    }

    private GossipBasedCounting  getOtherSnWDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";
        return (GossipBasedCounting ) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public double getNilai_val() {
        return this.rata;
    }
    
    private int nilai(){
        return (int)getNilai_val()/2;
    }
}

