package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

public class EthereumClientConfig extends ODocumentWrapper {

    public static final String CLASS_NAME = "EthereumClientConfig";

    public static final String OPROPERTY_NAME        = "name";
    public static final String OPROPERTY_HOST        = "host";
    public static final String OPROPERTY_PORT        = "port";
    public static final String OPROPERTY_WORK_FOLDER = "workFolder";
    public static final String OPROPERTY_TIMEOUT     = "timeout";

    public static class Builder {

        private ODocument doc;

        public Builder() {
            doc = new ODocument();
        }

        public EthereumClientConfig build() {
            return new EthereumClientConfig(doc);
        }

        public Builder setName(String name) {
            doc.field(OPROPERTY_NAME, name);
            return this;
        }

        public Builder setHost(String host) {
            doc.field(OPROPERTY_HOST, host);
            return this;
        }

        public Builder setPort(int port) {
            doc.field(OPROPERTY_PORT, port);
            return this;
        }

        public Builder setWorkFolder(String folder) {
            doc.field(OPROPERTY_WORK_FOLDER, folder);
            return this;
        }

        public Builder setTimeout(int timeout) {
            doc.field(OPROPERTY_TIMEOUT, timeout);
            return this;
        }
    }

    public EthereumClientConfig(ODocument doc) {
        super(doc);
    }

    public String getName() {
        return document.field(OPROPERTY_NAME);
    }

    public String getHost() {
        return document.field(OPROPERTY_HOST);
    }

    public int getPort() {
        return document.field(OPROPERTY_PORT);
    }

    public String getWorkFolder() {
        String folder = document.field(OPROPERTY_WORK_FOLDER);
        if (!folder.endsWith("/")) folder += "/";
        return folder;
    }

    public int getTimeout() {
        return document.field(OPROPERTY_TIMEOUT);
    }

    @Override
    public <RET extends ODocumentWrapper> RET save() {
        throw new IllegalStateException("Can't save config model!");
    }

    @Override
    public <RET extends ODocumentWrapper> RET save(String iClusterName) {
        throw new IllegalStateException("Can;t save config model!");
    }

}
