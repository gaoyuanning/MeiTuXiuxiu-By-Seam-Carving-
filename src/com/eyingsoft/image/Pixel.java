package com.eyingsoft.image;

import android.graphics.Color;

/**
 * This class represents a Pixel of the Picture. It stores not only the information
 * to draw the pixel but also information about its energy, etc.
 * 
 * @author MPGI2 Tutoren
 * @version 1.337
 */
public class Pixel {
	
	/**
	 * Represents the color of the pixel.
	 */
	private final int rgb;

	/**
	 * Stores the intensity as an float by adding the rgb-colors with appropriate
	 *  	coefficients and dividing the result by 3.
	 */
	private final float intensity;

	/**
	 * Sum of the differences between this pixels' intensity and the 
	 * 		intensities of its 4 neighbors divided by 4.
	 */
	private float energy;

	/**
	 * Represents the minimal energy of a path to this pixel.
	 */
	private float accumulatedEnergy;

	/**
	 * The constructor of a pixel. It also sets the intensity.
	 * @param rgb the color-value of it
	 */
	public Pixel(int rgb) {
		this.rgb = rgb;

		Color color = new Color();
		float red = color.red(rgb);
		float green = color.green(rgb);
		float blue = color.blue(rgb);
		//Because the colors contribute differently to the intensity of a pixel
		//as interpreted by the human eye, the following coefficients are used
		this.intensity = (float)(0.299*red + 0.587*green + 0.114*blue) / 3.0f;
	}

	//Getter and Setter Methods for the variables
	
	public void setEnergy(float energy) {
		this.energy = energy;
	}

	public float getEnergy() {
		return this.energy;
	}

	public void setAccumulatedEnergy(float accumulatedEnergy) {
		this.accumulatedEnergy = accumulatedEnergy;
	}

	public float getAccumulatedEnergy() {
		return this.accumulatedEnergy;
	}

	public float getIntensity() {
		return this.intensity;
	}

	public int getRGB() {
		return this.rgb;
	}

}


