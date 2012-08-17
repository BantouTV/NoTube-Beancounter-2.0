package io.beancounter.commons.camel.tests;

import org.apache.camel.main.Main;

public final class MainApp {

    public static void main(String[] args) throws Exception {
        String host = "localhost:22133/test-queue";
        String message = "test message";
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            message = args[1];
        }
        System.out.println("\n\n\n\n");
        System.out.println("===============================================");
        System.out.println("Sending message: " + message + " to host: " + host);
        System.out.println("Press ctrl+c to stop");
        System.out.println("===============================================");
        System.out.println("\n\n\n\n");

        Main main = new Main();
//        main.enableHangupSupport();
        KestrelRoute route = new KestrelRoute();
        route.setHost(host);
        route.setMessage(message);
        main.addRouteBuilder(route);
        main.run();
    }

}
