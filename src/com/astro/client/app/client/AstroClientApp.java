package com.astro.client.app.client;

import java.util.Date;

import com.astro.client.app.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.jjs.ast.js.JsonArray;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.dom.client.Text;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class AstroClientApp implements EntryPoint {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
	    + "attempting to contact the server. Please check your network " + "connection and try again.";

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
	final Button sendButton = new Button("Generate Panchangam");
	final TextBox nameField = new TextBox();
	nameField.setText("GWT User");
	final Label errorLabel = new Label();

	final Label locationLabel = new Label("Location");
	final Label dateLabel = new Label("Date ");

	DatePicker datePicker = new DatePicker();
	final Label text = new Label();
	datePicker.setYearAndMonthDropdownVisible(true);
	datePicker.setYearArrowsVisible(true);
	// Set the value in the text box when the user selects a date
	datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
	    public void onValueChange(ValueChangeEvent<Date> event) {
		Date date = event.getValue();
		String dateString = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(date);
		text.setText(dateString);
	    }
	});

	// Set the default value
	datePicker.setValue(new Date(), true);

	ListBox listBox1 = new ListBox();
	listBox1.addItem("Sunnyvale");
	listBox1.addItem("Seattle");
	listBox1.addItem("Trivandrum");
	listBox1.setVisibleItemCount(1);
	// We can add style names to widgets
	sendButton.addStyleName("sendButton");

	// Add the nameField and sendButton to the RootPanel
	// Use RootPanel.get() to get the entire body element
	RootPanel.get("locationTextContainer").add(locationLabel);
	RootPanel.get("locationSelectorContainer").add(listBox1);

	RootPanel.get("dateTextContainer").add(dateLabel);
	RootPanel.get("dateSelectorContainer").add(datePicker);

	RootPanel.get("labelButtonContainer").add(new Label(" "));
	RootPanel.get("generateButtonContainer").add(sendButton);

	// RootPanel.get("errorLabelContainer").add(errorLabel);

	// Focus the cursor on the name field when the app loads
	nameField.setFocus(true);
	nameField.selectAll();

	// Create the popup dialog box
	final DialogBox dialogBox = new DialogBox();
	dialogBox.setText("Today's Panchangam");
	dialogBox.setAnimationEnabled(true);
	final Button closeButton = new Button("Close");
	// We can set the id of a widget by accessing its Element
	closeButton.getElement().setId("closeButton");
	final Label textToServerLabel = new Label();
	final HTML serverResponseLabel = new HTML();
	VerticalPanel dialogVPanel = new VerticalPanel();
	dialogVPanel.addStyleName("dialogVPanel");
	dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
	dialogVPanel.add(textToServerLabel);
	dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
	dialogVPanel.add(serverResponseLabel);
	dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
	dialogVPanel.add(closeButton);
	dialogBox.setWidget(dialogVPanel);

	// Add a handler to close the DialogBox
	closeButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		dialogBox.hide();
		sendButton.setEnabled(true);
		sendButton.setFocus(true);
	    }
	});

	// Create a handler for the sendButton and nameField
	class MyHandler implements ClickHandler, KeyUpHandler {
	    /**
	     * Fired when the user clicks on the sendButton.
	     */
	    public void onClick(ClickEvent event) {
		sendNameToServer();
	    }

	    /**
	     * Fired when the user types in the nameField.
	     */
	    public void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		    sendNameToServer();
		}
	    }

	    /**
	     * Send the name from the nameField to the server and wait for a response.
	     */
	    private void sendNameToServer() {
		// First, we validate the input.
		errorLabel.setText("");
		String textToServer = nameField.getText();
		if (!FieldVerifier.isValidName(textToServer)) {
		    errorLabel.setText("Please enter at least four characters");
		    return;
		}

		// Then, we send the input to the server.
		sendButton.setEnabled(false);
		textToServerLabel.setText(textToServer);
		serverResponseLabel.setText("");
		greetingService.greetServer(textToServer, new AsyncCallback<String>() {
		    public void onFailure(Throwable caught) {
			// Show the RPC error message to the user
			dialogBox.setText("Remote Procedure Call - Failure");
			serverResponseLabel.addStyleName("serverResponseLabelError");
			serverResponseLabel.setHTML(SERVER_ERROR);
			dialogBox.center();
			closeButton.setFocus(true);
		    }

		    public void onSuccess(String result) {
			dialogBox.setText("Today's Panchangam Value");
			serverResponseLabel.removeStyleName("serverResponseLabelError");
			String resultVal = result;
			JSONValue resVal = JSONParser.parseLenient(resultVal);
			JSONArray array = resVal.isArray();
			String output = "";
			for (int i = 0; i < 2; i++) {

			}
			serverResponseLabel.setHTML(resVal.toString());
			dialogBox.center();
			closeButton.setFocus(true);
		    }
		});
	    }
	}

	// Add a handler to send the name to the server
	MyHandler handler = new MyHandler();
	sendButton.addClickHandler(handler);
	nameField.addKeyUpHandler(handler);
    }
}
