package gps.acquisition;

public class Acquisition {
	private final int nSamples;
	private final int samplingFrequency = 400000;
	private final int frequencyStep = 1000;
	private final int minFrequency = -5000;
	private final int maxFrequency = 5000;
	private final int nFrequencies = 11;
	private final int[] frequencies = {-5000, -4000, -3000, -2000, -1000, 0, 1000, 2000, 3000, 4000, 5000};
	private final float gamma = 0.015f;
	
	private float[][] samples;
	private int samplesIndex = 0;
	private float[][] codes;
	private int codesIndex = 0;
	private int dopplerShift;
	private int codeShift;
	
	public Acquisition(int nrOfSamples){
		nSamples = nrOfSamples;
		samples = new float[nSamples][2];
		codes = new float[nSamples][2];
	}
	
	public void enterSample(float real, float imag){
		if (samplesIndex < nSamples) {
			samples[samplesIndex][0] = real;
			samples[samplesIndex][1] = imag;
			samplesIndex++;
			}		
	}
	
	public void enterCode(float real, float imag){
		if (codesIndex < nSamples) {
			codes[codesIndex][0] = real;
			codes[codesIndex][1] = real;
			codesIndex++;
		}
	}
	
	public boolean startAcquisition(){
		float[][][] X = new float[nFrequencies][nSamples][2];
		//TODO Wahrscheinlich Potenzial zum optimieren hier -> array sinnvoll durchlaufen/weniger oft den Winkel berechnen/Winkel zwischenspeichern?
		//TODO Trigonometrische Funktionen?
		for (int d = 0; d < nFrequencies; ++d) {
			for (int n = 0; n < nSamples; ++n) {
				float angle = (-2 * (float)Math.PI * frequencies[d] * n) / samplingFrequency;
				X[d][n] = rotVector2d(samples[n], angle);
			}
		}
		
		float[][][] R = new float[nFrequencies][nSamples][2];
		
		return false;
	}
	
	public int getDopplerverschiebung(){
		return dopplerShift;
	}
	
	public int getCodeVerschiebung(){
		return codeShift;
	}
	
	/**
	 * Rotates a 2d vector by a given angle
	 * @param vector A 2d float vector.
	 * @param angle The angle in rad.
	 * @return The rotated vector.
	 */
	public static float[] rotVector2d(float[] vector, float angle) {
		float cosAngle = (float)Math.cos(angle);
		float sinAngle = (float)Math.sin(angle);
		float[] result = new float[2];
		result[0] = cosAngle * vector[0] - sinAngle * vector[1]; //TODO vector[0] und [1] zwischenspeichern fuer weniger arrayzugriffe?
		result[1] = sinAngle * vector[0] + cosAngle * vector[1];
		return result;
	}
}