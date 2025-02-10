package com._0xceba;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse.SelectionContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.awt.Component;
import java.util.List;
import javax.swing.JMenuItem;

/**
 * Overrides provideMenuItems to add an entry to the context menu.
 * Entry action duplicates the send to Comparer action with the added step
 * of pretty printing the JSON.
 */
public class BurpPrettifyJsonThenSendComparerMenu implements ContextMenuItemsProvider{
    private final Logging burpLogging;
    private final MontoyaApi montoyaApi;

    /**
     * Constructs a new context menu provider.
     *
     * @param montoyaApi    Grants access to the Montoya API.
     * @param burpLogging   Logging interface from Montoya API.
     */
    public BurpPrettifyJsonThenSendComparerMenu(MontoyaApi montoyaApi, Logging burpLogging) {
        this.burpLogging = burpLogging;
        this.montoyaApi = montoyaApi;
    }

    /**
     * Provides the context menu item that can be invoked within the message editor.
     *
     * @param contextMenuEvent  The event triggering the context menu action.
     * @return A context menu item list.
     */
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent contextMenuEvent) {
        // Handle events that come from the message editor
        if (contextMenuEvent.messageEditorRequestResponse().isPresent()) {
            // Create a JMenuItem with a label that matches the extension name to add a single menu item
            JMenuItem contextMenuItem = new JMenuItem("Prettify JSON then send to Comparer");
            // Add an action listener to handle user interaction
            contextMenuItem.addActionListener(e ->
            {
                menuAction(contextMenuEvent);
            });
            // Return the provider list of the single context menu item
            return List.of(contextMenuItem);
        } else {
            return null;
        }
    }

    /**
     * Action listener event triggered when context menu item is executed.
     *
     * @param contextMenuEvent  The event triggering the context menu action.
     */
    private void menuAction(ContextMenuEvent contextMenuEvent) {
        // Retrieve the selection context (i.e., whether the user selected text in the request or response)
        SelectionContext selectionContext = contextMenuEvent.messageEditorRequestResponse()
                .get()
                .selectionContext();
        // Retrieve the full HttpRequestResponse object, which is used if no text is selected
        HttpRequestResponse requestResponse = contextMenuEvent.messageEditorRequestResponse()
                .get()
                .requestResponse();
        // Variable to store the extracted text
        String messageText;

        // Check if the user has selected text within the message editor
        if (contextMenuEvent.messageEditorRequestResponse()
                .get()
                .selectionOffsets()
                .isPresent()) {
            // Extract the start index of the selected text
            int startIndex = contextMenuEvent.messageEditorRequestResponse()
                    .get()
                    .selectionOffsets()
                    .get()
                    .startIndexInclusive();
            // Extract the end index of the selected text
            int endIndex = contextMenuEvent.messageEditorRequestResponse()
                    .get()
                    .selectionOffsets()
                    .get()
                    .endIndexExclusive();

            // Extract the selected text based on the selection context (request or response)
            if (selectionContext == SelectionContext.REQUEST) {
                messageText = requestResponse.request()
                        .toString()
                        .substring(startIndex, endIndex);
            } else { // Selection is from the response
                messageText = requestResponse.response()
                        .toString()
                        .substring(startIndex, endIndex);
            }
        } else { // If no text is selected, extract the full request or response body
            if (selectionContext == SelectionContext.REQUEST) {
                messageText = requestResponse.request()
                        .bodyToString();
            } else {
                messageText = requestResponse.response()
                        .bodyToString();
            }
        }

        // Pretty-print the extracted JSON text
        String prettyPrintedJson = prettyPrintJson(messageText);

        // If pretty-printing was successful (not null), send the formatted JSON to Comparer
        if (prettyPrintedJson != null)
            montoyaApi.comparer()
                    .sendToComparer(ByteArray.byteArray(prettyPrintedJson));
    }

    /**
     * Formats a given JSON string with proper indentation for readability.
     *
     * @param messageText   The raw JSON string to be formatted.
     * @return  A pretty-printed JSON string if parsing is successful; null otherwise.
     */
    private String prettyPrintJson(String messageText) {
        try {
            // Create a Gson instance with pretty-printing enabled
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            // Parse the input string as a JSON element
            JsonElement json = gson.fromJson(messageText, JsonElement.class);

            // Convert the parsed JSON element back to a formatted JSON string
            return(gson.toJson(json));
        } catch(Exception e) { // Catch any errors related to invalid JSON parsing
            burpLogging.raiseErrorEvent(e.getMessage());
        }
        // Return null if the input was not valid JSON
        return null;
    }
}
