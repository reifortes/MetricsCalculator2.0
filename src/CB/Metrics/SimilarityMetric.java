package CB.Metrics;

import java.io.IOException;

public interface SimilarityMetric extends Metric 
{
	public double similarity(int idA, int idB) throws IOException;
}
