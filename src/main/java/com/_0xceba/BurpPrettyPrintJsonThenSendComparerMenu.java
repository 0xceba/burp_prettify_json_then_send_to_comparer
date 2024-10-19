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
 * Duplicates the send to Comparer flow with the added step
 * of pretty printing the JSON.
 */
public class BurpPrettyPrintJsonThenSendComparerMenu implements ContextMenuItemsProvider{
    private final Logging logging;
    private final MontoyaApi montoyaApi;

    /**
     * Constructor
     *
     * @param montoyaApi Grants access to the Montoya API
     * @param logging Logging interface from Montoya API
     */
    public BurpPrettyPrintJsonThenSendComparerMenu(MontoyaApi montoyaApi, Logging logging) {
        this.logging = logging;
        this.montoyaApi = montoyaApi;
    }

    /**
     * Overriden method of the ContextMenuItemsProvider interface.
     * Invoked when user requests a request/response context menu.
     * Adds a context menu entry with an action listener.
     * Returns an array of components, but it's truncated to one context
     * menu item because the item label matches extension name.
     */
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        // Check if the method was called from a message editor. This is
        // required because other classes that can invoke context menus (e.g. HttpRequestResponse)
        // don't have methods to grab the user's selected text
        if (event.messageEditorRequestResponse().isPresent()) {
            JMenuItem customItem = new JMenuItem("Pretty print JSON then send to Comparer");
            customItem.addActionListener(e ->
            {
                menuAction(event);
            });
            return List.of(customItem);
        } else {
            return null;
        }
    }

    /**
     * Action listener event triggered when context menu item is executed.
     */
    private void menuAction(ContextMenuEvent event) {
        // SelectionContext object used when text is selected by the user
        SelectionContext selectionContext = event.messageEditorRequestResponse()
                .get()
                .selectionContext();
        // HttpRequestResponse object used when there's no text selected
        HttpRequestResponse requestResponse = event.messageEditorRequestResponse()
                .get()
                .requestResponse();
        String messageText;

        // True if user has selected text
        if (event.messageEditorRequestResponse()
                .get()
                .selectionOffsets()
                .isPresent()) {
            // Start index used to extract text content
            int startIndex = event.messageEditorRequestResponse()
                    .get()
                    .selectionOffsets()
                    .get()
                    .startIndexInclusive();
            // End index used to extract text content
            int endIndex = event.messageEditorRequestResponse()
                    .get()
                    .selectionOffsets()
                    .get()
                    .endIndexExclusive();

            // Extract the selected text from the request at offsets
            if (selectionContext == SelectionContext.REQUEST) {
                messageText = requestResponse.request()
                        .toString()
                        .substring(startIndex, endIndex);
            // Else extract the selected text from the response at offsets
            } else {
                messageText = requestResponse.response()
                        .toString()
                        .substring(startIndex, endIndex);
            }
        // Else there's no selected text so send message body to Comparer
        } else {
            if (selectionContext == SelectionContext.REQUEST) {
                messageText = requestResponse.request()
                        .bodyToString();
            } else {
                messageText = requestResponse.response()
                        .bodyToString();
            }
        }

        // Call prettyPrintJson to pretty print the text being sent to Comparer
        String prettyPrintedJson = prettyPrintJson(messageText);

        // Send print printed JSON to Comparer if not null
        if (prettyPrintedJson != null)
            montoyaApi.comparer()
                    .sendToComparer(ByteArray.byteArray(prettyPrintedJson));
    }

    /**
     * Pretty prints the JSON using Gson library.
     */
    private String prettyPrintJson(String messageText) {
        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            JsonElement json = gson.fromJson(messageText, JsonElement.class);
            return(gson.toJson(json));
        // Will catch errors for invalid JSON
        } catch(Exception e) {
            logging.raiseErrorEvent(e.getMessage());
        }
        return null;
    }
}
