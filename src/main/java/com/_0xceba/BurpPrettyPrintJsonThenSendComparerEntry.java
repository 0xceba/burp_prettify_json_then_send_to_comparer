package com._0xceba;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.MontoyaApi;

/**
 * Class entry point which implements the Montoya API BurpExtension
 */
public class BurpPrettyPrintJsonThenSendComparerEntry implements BurpExtension {

    /**
     * Main function.
     *
     * @param montoyaApi Grants access to the Montoya API operations.
     */
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("Pretty print JSON then send to Comparer");

        // Initialize logging and persistence instances
        Logging logging = montoyaApi.logging();

        // Log load output
        montoyaApi.logging().logToOutput("Pretty print JSON then send to Comparer v" +
                getClass().getPackage().getImplementationVersion() +
                " loaded successfully.");

        // Register the unload handler
        montoyaApi.extension().registerUnloadingHandler(() -> {
            montoyaApi.logging().logToOutput("Pretty print JSON then send to Comparer unloaded successfully.");
        });

        // Register
        montoyaApi.userInterface().registerContextMenuItemsProvider(new BurpPrettyPrintJsonThenSendComparerMenu(montoyaApi,logging));
    }
}