package iomap.backend;

import example.movies.util.Util;

import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.setPort;

/**
 * @author Michael Hunger @since 22.10.13
 */
public class IomapServer {

    public static void main(String[] args) {
        setPort(Util.getWebPort());
        externalStaticFileLocation("src/main/webapp");
        // initializes the service
        final IomapService service = new IomapService(Util.getNeo4jUrl());
        // executes spark.get requests and serializes the results into json
        new IomapRoutes(service).init();
    }
}
