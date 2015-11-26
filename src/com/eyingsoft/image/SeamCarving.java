package com.eyingsoft.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * This class implements the code for the SeamCarving algorithm.
 * 
 * @author MPGI2 Tutoren
 * @version 1.337
 */
public class SeamCarving {

	/**
	 * The picture is stored in a matrix of Pixels.
	 */
	private Pixel[][] pixels;

	/**
	 * The constructor of an SC Object which initializes the pixel matrix and
	 * copies the values of the image into the matrix.
	 * 
	 * @param image
	 *            the filename/path of the image
	 */
	public SeamCarving(Bitmap image) {
		pixels = new Pixel[image.getHeight()][image.getWidth()];

		for (int y = 0; y < image.getHeight(); ++y) {
			for (int x = 0; x < image.getWidth(); ++x) {
				this.pixels[y][x] = new Pixel(image.getPixel(x, y));
			}
		}

		update();
	}

	/**
	 * Finds and removes a seam.
	 * 
	 * @return removed seam
	 */
	public int[] findAndRemoveSeam() {
		int[] seam = findSeam();
		removeSeam(seam);
		return seam;
	}

	/**
	 * Finds a seam in the picture. The return value is an array which has the
	 * same length as the picture's height, indicating which pixel of which row
	 * has to be removed. So if the pixel to be removed in row y is (x, y), then
	 * x has to be defined in array[y].
	 * 
	 * @return seam with minimal energy
	 */
	private int[] findSeam() {
		int x = pixels.length - 1;
		int y = pixels[0].length - 1;
		int[] seam = new int[pixels.length];
		int k = 0;
		float acEnergy = Integer.MAX_VALUE;

		for (int i = 0; i <= y; i++) {
			if (pixels[x][i].getAccumulatedEnergy() < acEnergy) {
				acEnergy = pixels[x][i].getAccumulatedEnergy();
				k = i;
			}
		}
		seam[seam.length - 1] = k;
		// catch
		for (int i = x - 1; i >= 0; i--) { // zeile
			acEnergy = Integer.MAX_VALUE;
			for (int j = k - 1; j <= k + 1; j++) { // 3 pixel ueber dem
													// vorherigen
				if (isEntryCorrect(i, j)) {
					if (pixels[i][j].getAccumulatedEnergy() < acEnergy) {
						acEnergy = pixels[i][j].getAccumulatedEnergy();
						seam[i] = j;
						k = j;
					}
				}
			}
		}
		
		return seam;
	}

	/**
	 * Removes a given seam. A new matrix of pixels is assigned.
	 * 
	 * @param seam
	 *            seam to be removed
	 */
	private void removeSeam(int[] seam) {
		int k = 0;

		for (int i = 0; i < pixels.length; i++) {
			k = seam[i];
			while (k < pixels[0].length - 2) {
				pixels[i][k] = pixels[i][k + 1];
				k++;
			}
		}
		
		Pixel[][] pixels2 = new Pixel[pixels.length][pixels[0].length - 1];

		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length-1; j++) {
				pixels2[i][j] = pixels[i][j];
			}
		}
		pixels = pixels2;
		update();
	}

	/**
	 * Recalculates energies and accumulated energies of all pixels.
	 */
	private void update() {
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				pixels[i][j].setEnergy(calculateEnergy(i, j));
				pixels[i][j].setAccumulatedEnergy(calculateAccumulatedEnergy(i,
						j));
			}
		}
	}

	/**
	 * Calculates energy of pixel (x, y).
	 * 
	 * @param x
	 *            x coordinate of pixel
	 * @param y
	 *            y coordinate of pixel
	 * @return energy of pixel
	 */
	private float calculateEnergy(int x, int y) {
		int i;
		int j;
		int k = 0;
		float energy = 0;
		float intensity = pixels[x][y].getIntensity();
		for (i = x - 1; i <= x + 1; i++) {
			for (j = y - 1; j <= y + 1; j++) {
           			if (isEntryCorrect(x, y, i, j)) {
					energy += Math.abs(pixels[i][j].getIntensity() - intensity);
					k++;
				}
			}
		}
		return energy / k;

	}


	public boolean isEntryCorrect(int x, int y, int i, int j) {
		if (i >= 0 & i < pixels.length & j >= 0 & j < pixels[0].length
				& (i == x | j == y) & (i != x | j != y))
			return true;
		else
			return false;
	}

	public boolean isEntryCorrect(int i, int j) {
		if (i >= 0 & j >= 0 & i < pixels.length & j < pixels[0].length)
			return true;
		else
			return false;
	}

	/**
	 * Calculates accumulated energy of pixel (x, y).
	 * 
	 * @note Make sure the energy values are already calculated before calling
	 *       this method.
	 * 
	 * @param x
	 *            x coordinate of pixel
	 * @param y
	 *            y coordinate of pixel
	 * @return accumulated energy of pixel
	 */
	private float calculateAccumulatedEnergy(int x, int y) {
		float energy = pixels[x][y].getEnergy();

		if (x == 0)
			return energy;

		x--;
		float acEnergy = Integer.MAX_VALUE;

		for (int i = y - 1; i <= y + 1; i++) {
			if (isEntryCorrect(x, i))
				if (pixels[x][i].getAccumulatedEnergy() < acEnergy)
					acEnergy = pixels[x][i].getAccumulatedEnergy();
		}

		float newAc = energy + acEnergy;
		return newAc;
	}

	/**
	 * Returns the resulting image of the algorithm where the seam is removed.
	 * 
	 * @return resulting image
	 */
	public Bitmap getImage() {
		
		//Bitmap image = new Bitmap(this.pixels[0].length,
			//	this.pixels.length, BufferedImage.TYPE_INT_RGB);
		Bitmap image = Bitmap.createBitmap(this.pixels[0].length,
				this.pixels.length, Config.ARGB_8888);

		for (int y = 0; y < image.getHeight(); ++y)
			for (int x = 0; x < image.getWidth(); ++x)
				image.setPixel(x, y, this.pixels[y][x].getRGB());

		return image;
	}

}
