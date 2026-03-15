package com.example;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class PrimaryController implements Initializable {

    @FXML private TextField addressBar;
    @FXML private ChoiceBox<String> searchDropdown;
    @FXML private TabPane tabPane;
    @FXML private VBox githubPanel;
    @FXML private WebView githubWebView;

    // Speicher für den aktuellen Web-Style (für neue Tabs)
    private String currentWebStyle = null; 
    private boolean isGitHubLoaded = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Suchmaschinen-Optionen füllen
        searchDropdown.getItems().addAll("Google", "Bing", "DuckDuckGo", "Yahoo");
        searchDropdown.setValue("Google");

        // Den ersten Tab beim Start öffnen
        createNewTab();
    }

    // --- TAB-MANAGEMENT ---

    @FXML
    private void createNewTab() {
        WebView newBrowser = new WebView();
        WebEngine engine = newBrowser.getEngine();

        // Falls ein Theme aktiv ist, wende es sofort auf den neuen Tab an
        if (currentWebStyle != null) {
            URL res = getClass().getResource(currentWebStyle);
            if (res != null) {
                engine.setUserStyleSheetLocation(res.toExternalForm());
            }
        }
        
        Tab tab = new Tab("Neuer Tab");
        tab.setContent(newBrowser);

        // Titel des Tabs an Webseite anpassen
        engine.titleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) tab.setText(newVal);
        });

        // Adresszeile updaten, wenn man im Tab surft
        engine.locationProperty().addListener((obs, oldVal, newVal) -> {
            if (tab.isSelected()) addressBar.setText(newVal);
        });

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        engine.load("https://www.google.com");
    }

    private WebEngine getCurrentEngine() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof WebView) {
            return ((WebView) selectedTab.getContent()).getEngine();
        }
        return null;
    }

    // --- NAVIGATION ---

    @FXML
    private void loadAction() {
        WebEngine engine = getCurrentEngine();
        if (engine == null) return;

        String input = addressBar.getText().trim();
        if (input.isEmpty()) return;

        if (input.contains(".") && !input.contains(" ")) {
            String url = input.startsWith("http") ? input : "https://" + input;
            engine.load(url);
        } else {
            String selectedEngine = searchDropdown.getValue();
            String query = input.replace(" ", "+");
            
            String searchUrl = switch (selectedEngine) {
                case "Bing" -> "https://www.bing.com/search?q=";
                case "DuckDuckGo" -> "https://duckduckgo.com/?q=";
                case "Yahoo" -> "https://search.yahoo.com/search?p=";
                default -> "https://www.google.com/search?q="; 
            };
            engine.load(searchUrl + query);
        }
    }

    @FXML private void goBack() {
        WebEngine engine = getCurrentEngine();
        if (engine != null && engine.getHistory().getCurrentIndex() > 0) engine.getHistory().go(-1);
    }

    @FXML private void goForward() {
        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            int idx = engine.getHistory().getCurrentIndex();
            if (idx < engine.getHistory().getEntries().size() - 1) engine.getHistory().go(1);
        }
    }

    @FXML private void reload() {
        WebEngine engine = getCurrentEngine();
        if (engine != null) engine.reload();
    }

    @FXML private void exitApp() { Platform.exit(); }

    // --- THEMES & WEB STYLES ---

    @FXML private void loadRetro() { 
        setTheme("retro.css"); 
        applyWebStyle("/com/example/retro-web.css");
    }

    @FXML private void loadDark() { 
        setTheme("dark.css"); 
        applyWebStyle("/com/example/dark-web.css");
    }

    @FXML private void loadModern() { 
        setTheme("modern.css"); 
        applyWebStyle("/com/example/modern-web.css");
    }

    private void setTheme(String themeFile) {
        var scene = tabPane.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            URL res = getClass().getResource(themeFile);
            if (res != null) {
                scene.getStylesheets().add(res.toExternalForm());
            }
        }
    }

    private void applyWebStyle(String resourcePath) {
        // Merkt sich den Style für neue Tabs
        this.currentWebStyle = resourcePath;
        
        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            URL res = getClass().getResource(resourcePath);
            if (res != null) {
                engine.setUserStyleSheetLocation(res.toExternalForm());
            } else {
                System.err.println("Web-CSS nicht gefunden: " + resourcePath);
            }
        }
    }

    // --- SPECIAL FEATURES ---

    @FXML
    private void toggleGitHub() {
        if (githubPanel.isVisible()) {
            githubPanel.setVisible(false);
            githubPanel.setManaged(false);
        } else {
            githubPanel.setVisible(true);
            githubPanel.setManaged(true);
            if (!isGitHubLoaded) {
                githubWebView.getEngine().load("https://github.com");
                isGitHubLoaded = true;
            }
        }
    }

    @FXML
    private void openDevTools() {
        WebEngine engine = getCurrentEngine();
        if (engine != null) {
            String script = 
                "var consoleDiv = document.getElementById('java-console');" +
                "if(!consoleDiv) {" +
                "   consoleDiv = document.createElement('div');" +
                "   consoleDiv.id = 'java-console';" +
                "   consoleDiv.style.cssText = 'position:fixed;bottom:0;left:0;width:100%;height:150px;background:#222;color:#0f0;font-family:monospace;overflow-y:scroll;z-index:9999;padding:10px;border-top:2px solid red;';" +
                "   document.body.appendChild(consoleDiv);" +
                "   console.log = function(msg) { consoleDiv.innerHTML += '> ' + msg + '<br/>'; };" +
                "   window.onerror = function(msg, url, line) { consoleDiv.innerHTML += '<span style=\"color:red\">Fehler: ' + msg + ' (Zeile ' + line + ')</span><br/>'; };" +
                "   console.log('Eigene Konsole aktiviert...');" +
                "} else {" +
                "   consoleDiv.style.display = (consoleDiv.style.display === 'none' ? 'block' : 'none');" +
                "}";
            engine.executeScript(script);
        }
    }
}