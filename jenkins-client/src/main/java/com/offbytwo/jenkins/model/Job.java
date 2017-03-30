/*
 * Copyright (c) 2013 Cosmin Stejerean, Karl Heinz Marbaise, and contributors.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.model;

import static org.apache.commons.lang.StringUtils.join;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class Job extends BaseModel {

    private String name;
    private String url;

    public Job() {
    }

    public Job(String name, String url) {
        this();
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public JobWithDetails details() throws IOException {
        return client.get(url, JobWithDetails.class);
    }

    /**
     * Get a file from workspace.
     * 
     * @param fileName The name of the file to download from workspace. You can
     *            also access files which are in sub folders of the workspace.
     * @return The string which contains the content of the file.
     * @throws IOException in case of an error.
     */
    public String getFileFromWorkspace(String fileName) throws IOException {
        InputStream is = client.getFile(URI.create(url + "/ws/" + fileName));
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    /**
     * Trigger a build without parameters
     * 
     * @return {@link QueueReference} for further analysis of the queued build.
     * @throws IOException in case of an error.
     */
    public QueueReference build() throws IOException {
        ExtractHeader location = client.post(url + "build", null, ExtractHeader.class, false);
        return new QueueReference(location.getLocation());

    }

    /**
     * Trigger a build with crumbFlag.
     * 
     * @param crumbFlag true or false.
     * @return {@link QueueReference} for further analysis of the queued build.
     * @throws IOException in case of an error.
     */
    public QueueReference build(boolean crumbFlag) throws IOException {
        ExtractHeader location = client.post(url + "build", null, ExtractHeader.class, crumbFlag);
        return new QueueReference(location.getLocation());
    }

    /**
     * Trigger a parameterized build
     *
     * @param params the job parameters
     * @return {@link QueueReference} for further analysis of the queued build.
     * @throws IOException in case of an error.
     */
    public QueueReference build(Map<String, String> params) throws IOException {
        String qs = join(Collections2.transform(params.entrySet(), new MapEntryToQueryStringPair()), "&");
        /**
        There are send twice post request and will build twice
     */
       // client.post(url + "buildWithParameters?" + qs);
        ExtractHeader location = client.post(url + "buildWithParameters?" + qs, null, ExtractHeader.class, false);
        return new QueueReference(location.getLocation());
    }

    /**
     * Trigger a parameterized build
     *
     * @param params the job parameters
     * @param crumbFlag determines whether crumb flag is used
     * @return {@link QueueReference} for further analysis of the queued build.
     * @throws IOException in case of an error.
     */
    public QueueReference build(Map<String, String> params, boolean crumbFlag) throws IOException {
        String qs = join(Collections2.transform(params.entrySet(), new MapEntryToQueryStringPair()), "&");
        ExtractHeader location = client.post(url + "buildWithParameters?" + qs, null, ExtractHeader.class, crumbFlag);
        return new QueueReference(location.getLocation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Job job = (Job) o;

        if (name != null ? !name.equals(job.name) : job.name != null)
            return false;
        if (url != null ? !url.equals(job.url) : job.url != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    private static class MapEntryToQueryStringPair implements Function<Map.Entry<String, String>, String> {
        @Override
        public String apply(Map.Entry<String, String> entry) {
            Escaper escaper = UrlEscapers.urlFormParameterEscaper();
            return escaper.escape(entry.getKey()) + "=" + escaper.escape(entry.getValue());
        }
    }
}
