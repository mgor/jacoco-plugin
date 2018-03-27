package hudson.plugins.jacoco.report;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.jacoco.core.analysis.IClassCoverage;

import com.google.common.io.Files;

import hudson.plugins.jacoco.JacocoBuildAction;

/**
 * @author Kohsuke Kawaguchi
 */
public final class ClassReport extends AggregatedReport<PackageReport,ClassReport,MethodReport> {

    private String sourceFilePath;
    private IClassCoverage classCov;

    @Override
	public void setName(String name) {
		super.setName(name.replaceAll("/", "."));
		//logger.log(Level.INFO, "ClassReport");
	}
	@Override
	public void add(MethodReport child) {
    	String newChildName = child.getName();
    	child.setName(newChildName);
        getChildren().put(child.getName(), child);
    }

    public void setSrcFileInfo(IClassCoverage classCov, String sourceFilePath) {
   		this.sourceFilePath = sourceFilePath;
   		this.classCov = classCov;
   	}

    /**
     * Read the source Java file for this class.
     * @return the source Java file for this class.
     */
    public File getSourceFilePath() {
        return new File(sourceFilePath);
    }

    public void printHighlightedSrcFile(Writer output) {
        new SourceAnnotator(getSourceFilePath()).printHighlightedSrcFile(classCov,output);
   	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":"
				+ " instruction=" + instruction
				+ " branch=" + branch
				+ " complexity=" + complexity
				+ " line=" + line
				+ " method=" + method;
	}

	public void generateHtmlReport(final File reportDirectory) throws IOException {
        final StringBuilder buffer = new StringBuilder();

        final String title = String.format("Class: %s", this.getName());
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

        buffer.append("<h2>Coverage</h2>\n");
        final StringWriter sw = new StringWriter();
        this.printHighlightedSrcFile(sw);
        buffer.append(sw.toString());

        buffer.append(JacocoBuildAction.getHtmlFooter());

        Files.write(buffer.toString().getBytes(), new File(reportDirectory, String.format("%s.html", this.getName())));
	}
}
