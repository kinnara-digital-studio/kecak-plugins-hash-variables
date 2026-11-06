package com.kinnarastudio.kecakplugins.hashvariables;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.ResourceBundle;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

public class ConvertImageBase64 extends DefaultHashVariablePlugin {
    private final static String LABEL = "Convert Image to Base64";

    @Override
    public String getPrefix() {
        return "convertImageBase64";
    }

    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<>();
        syntax.add(getPrefix() + ".IMAGE_URL");
        syntax.add(getPrefix() + ".data.IMAGE_URL");
        syntax.add(getPrefix() + ".base64.IMAGE_URL");
        syntax.add(getPrefix() + ".raw.IMAGE_URL");
        return syntax;
    }

    @Override
    public String processHashVariable(String keys) {
        if (keys == null || keys.trim().isEmpty()) {
            return "";
        }

        String format = "base64";
        String imageUrl = keys.trim();

        if (imageUrl.startsWith("data.")) {
            format = "data";
            imageUrl = imageUrl.substring(5).trim();
        } else if (imageUrl.startsWith("base64.")) {
            format = "base64";
            imageUrl = imageUrl.substring(7).trim();
        } else if (imageUrl.startsWith("raw.")) {
            format = "raw";
            imageUrl = imageUrl.substring(4).trim();
        }

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LogUtil.warn(getClassName(), "Failed to fetch image from URL [" + imageUrl + "], HTTP response code: " + responseCode);
                return "";
            }

            String contentType = connection.getContentType();
            if (contentType != null && contentType.contains(";")) {
                contentType = contentType.split(";")[0].trim();
            }
            if (contentType == null || contentType.isEmpty() || !contentType.startsWith("image/")) {
                contentType = "image/jpeg";
            }

            byte[] imageBytes;
            try (InputStream in = connection.getInputStream()) {
                imageBytes = in.readAllBytes();
            }

            String base64Data = Base64.getEncoder().encodeToString(imageBytes);

            if ("data".equalsIgnoreCase(format)) {
                return "data:" + contentType + ";base64," + base64Data;
            } else if ("raw".equalsIgnoreCase(format)) {
                return base64Data;
            } else {
                return "base64://" + base64Data;
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error converting image to Base64 from URL [" + imageUrl + "]: " + e.getMessage());
            return "";
        }
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        return resourceBundle.getString("buildNumber");
    }
    
}
