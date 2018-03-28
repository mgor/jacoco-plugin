package hudson.plugins.jacoco.report;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.io.Files;

import hudson.plugins.jacoco.JacocoBuildAction;

/**
 * @author Kohsuke Kawaguchi
 * @author David Carver
 */
public final class PackageReport extends AggregatedReport<CoverageReport,PackageReport,ClassReport> {

    /**
     * Give the default no-name package a non-empty name.
     */
    @Override
    public String getName() {
        String n = super.getName();
        return n.length() == 0 ? "(default)" : n;
    }

    @Override
    public void setName(String name) {
        super.setName(name.replaceAll("/", "."));
    }

    @Override
    public void add(ClassReport child) {
    	String newChildName = child.getName().replaceAll(this.getName() + ".", "");
    	child.setName(newChildName);
        this.getChildren().put(child.getName(), child);
        //logger.log(Level.INFO, "PackageReport");
    }

    //private static final Logger logger = Logger.getLogger(CoverageObject.class.getName());

    public void generateHtmlReport(final File reportDirectory) throws IOException {
        final StringBuilder buffer = new StringBuilder();

        final String title = String.format("Package: %s", this.getName());
        buffer.append(JacocoBuildAction.getHtmlHeader(title));

        final GraphImpl trend = this.getGraph(500, 200);
        buffer.append("<h1>").append(title).append("<h1>\n");
        buffer.append("<img src=\"data:image/png;base64, ").append(trend.getGraphAsBase64()).append("\"/>\n");

        buffer.append("<h2>Coverage Summary</h2>\n");
        buffer.append("<table border=\"1px\" class=\"html-report\">\n");
        buffer.append(JacocoBuildAction.getCaptionLine());
        buffer.append("<tr>\n");
        buffer.append("<td>").append(this.getName()).append("</td>\n");
        buffer.append(this.printFourCoverageColumns());
        buffer.append("</tr>\n");
        buffer.append("</table>\n");

        buffer.append("<h2>Coverage Breakdown by Source File</h2>\n");

        final Map<String, ClassReport> classes = this.getChildren();

        buffer.append("<table border=\"1px\" class=\"html-report\">\n");
        buffer.append(JacocoBuildAction.getCaptionLine());
        classes.forEach((name, report) -> {
            buffer.append("<tr>\n");
            buffer.append("<td class=\"nowrap");
            if (report.isFailed()) {
                buffer.append(" red");
            }
            buffer.append("\">");
            //buffer.append("<a href=\"").append(name).append(".html\">");
            buffer.append(name);
            //buffer.append("</a>");
            buffer.append("</td>\n");
            buffer.append(report.printFourCoverageColumns());
            buffer.append("</tr>\n");
        });

        buffer.append(JacocoBuildAction.getHtmlFooter());

        Files.write(buffer.toString().getBytes(StandardCharsets.UTF_8), new File(reportDirectory, String.format("%s.html", this.getName())));
    }

}
