package report;

import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/*Report ini digunakan untuk menghitung rata-rata waktu node 
sehingga mendapatkan pengetahuan atau informasi yang sama di jaringan.*/

public class AverageConvergenceTimeReport extends Report implements UpdateListener {

    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";
    public static final int DEFAULT_WAKTU = 900;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    public AverageConvergenceTimeReport() {
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
        double rata = 0;
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter))
                continue;
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            NilaiInisiator n = (NilaiInisiator) de;

            int temp = (int)n.getNilai_val();
            rata += temp;
            }
        double AV_Rata = rata/hosts.size();
        String output = format((int)SimClock.getTime()) +" \t "+ format(AV_Rata);
        write(output);
        }
}
