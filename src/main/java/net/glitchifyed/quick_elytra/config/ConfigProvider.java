package net.glitchifyed.quick_elytra.config;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ConfigProvider implements SimpleConfig.DefaultConfig {

    private String configContents = "";

    public List<Pair> getConfigsList() {
        return configsList;
    }

    private final List<Pair> configsList = new ArrayList<>();

    public void addKeyValuePair(Pair<String, ?> keyValuePair, String comment) {
        configsList.add(keyValuePair);
        configContents += keyValuePair.getFirst() + "=" + keyValuePair.getSecond() + " #"
                + comment + "\n";// + " | default: " + keyValuePair.getSecond() + "\n";
    }

    public void resetConfig() {
        configContents = "";
        configsList.clear();
    }

    @Override
    public String get(String namespace) {
        return configContents;
    }
}