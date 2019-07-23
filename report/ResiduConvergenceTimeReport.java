package report;

import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/*Report ini digunakan untuk mengetahui jumlah node 
yang tidak memiliki hasil counting yang akurat.*/

public class ResiduConvergenceTimeReport extends Report implements UpdateListener {

    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";

    public static final int DEFAULT_WAKTU = 900;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Double>> countingGossip = new HashMap<DTNHost, ArrayList<Double>>();

    public ResiduConvergenceTimeReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(NODE_PERWAKTU)) {
            interval = settings.getInt(NODE_PERWAKTU);
        } else {
            interval = DEFAULT_WAKTU;
        }
    }

    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }

    private void printLine(List<DTNHost> hosts) {
        Settings s = new Settings();
        int numbernode1 = s.getInt("Group1.nrofHosts");
        int numbernode2 = s.getInt("Group2.nrofHosts");
        int nrofNode = numbernode1+numbernode2;
          double residu = 0;
          double rata = 0;
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter))
                continue;
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            NilaiInisiator n = (NilaiInisiator) de;

            int temp = (int)n.getNilai_val();
            if(temp < nrofNode){
                residu++;
            }else if(temp > nrofNode){
                residu++;
            }
            }
        double TotalResidu = residu;
        String output = format((int)SimClock.getTime()) +" \t "+ format(TotalResidu);
        write(output);
        }
}
