package report;

import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/*Report ini digunakan untuk menghitung jumlah node.*/

public class ConvergenceTimeReport extends Report implements UpdateListener {

    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";

    public static final int DEFAULT_WAKTU = 900;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Double>> countingGossip = new HashMap<DTNHost, ArrayList<Double>>();
    private int updateCounter = 0; 
    
    public ConvergenceTimeReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(NODE_PERWAKTU)) {
            interval = settings.getInt(NODE_PERWAKTU);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_WAKTU;
        }
    }

    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }

        if (simTime - lastRecord >= interval) {
            //lastRecord = SimClock.getTime();
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    private void printLine(List<DTNHost> hosts) {

        for (DTNHost h : hosts) {
            
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) 
                continue;
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof NilaiInisiator)) 
                continue;
            NilaiInisiator n = (NilaiInisiator)de;
            
            ArrayList<Double> NodeList = new ArrayList<Double>();
            double temp = n.getNilai_val();
            if (countingGossip.containsKey(h)) {
                NodeList = countingGossip.get(h);
                NodeList.add(temp);
                countingGossip.put(h, NodeList);
            } else {
                countingGossip.put(h, NodeList);
            }
        }
    }

    @Override
    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Double>> entry : countingGossip.entrySet()) {
            String printHost = entry.getKey().getAddress() + "\t";
            for (Double NodeList : entry.getValue()) {
                printHost = printHost + "\t" + NodeList;
            }
            write(printHost);
        }
        super.done();
    }
}

