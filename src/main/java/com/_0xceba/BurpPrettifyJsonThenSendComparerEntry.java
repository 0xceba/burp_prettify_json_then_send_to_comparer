package com._0xceba;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.MontoyaApi;

/**
 * This class serves as the entry point for the Burp Prettify JSON Then Send to Comparer extension.
 * It implements the BurpExtension interface to integrate with Burp.
 */
public class BurpPrettifyJsonThenSendComparerEntry implements BurpExtension {

    /**
     * Initializes the Burp Prettify JSON Then Send to Comparer extension.
     * This method is called by Burp when the extension is loaded.
     *
     * @param montoyaApi    Provides access to the Montoya API operations.
     */
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        // Set the extension name in Burp
        montoyaApi.extension().setName("Prettify JSON then send to Comparer");

        // Initialize logging mechanism
        Logging burpLogging = montoyaApi.logging();

        // Log initialization output
        montoyaApi.logging().logToOutput("Prettify JSON then send to Comparer v" +
                getClass().getPackage().getImplementationVersion() +
                " loaded successfully.");

        // Register an unload handler that is called when the extension is unloaded or Burp is exited
        montoyaApi.extension().registerUnloadingHandler(() -> {
            montoyaApi.logging().logToOutput("Prettify JSON then send to Comparer unloaded successfully.");
        });

        // Register a context menu provider to add the extension action to the context menu
        montoyaApi.userInterface().registerContextMenuItemsProvider(new BurpPrettifyJsonThenSendComparerMenu(montoyaApi,burpLogging));
    }
}