package at.searles.commons.color;

public class Commons {

	/**
     * Always positive remainder.
	 * @param a
     * @param b
     * @return
     */
	public static int mod(int a, int b) {
		int r = a % b;

		if(r < 0) {
			r += b;
		}

		return r;
	}

	public static double clamp(double d, double min, double max) {
		return Math.min(max, Math.max(min, d));
	}

	public static float clamp(float d, float min, float max) {
		return Math.min(max, Math.max(min, d));
	}

	public static int clamp(int d, int min, int max) {
		return Math.min(max, Math.max(min, d));
	}

	/**
	 * short for a <= x && x <= b || b <= x && x <= a
	 * @param x
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean inRange(float x, float a, float b) {
		if(b < a) {
			float t = b;
			b = a;
			a = t;
		}

		return a <= x && x <= b;
	}

	/**
	 * returns the ratio of closeness of x to a and b.
	 * @param x
	 * @param a
	 * @param b
	 * @return If 0, then x==a, if 1, then x == b, otherwise it is in between
	 */
	public static float rangeFraction(float x, float a, float b) {
		if(b - a == 0) {
			return 0.f;
		}

		return (x - a) / (b - a);
	}


	/**
	 * Returns the distance of x0/y0 to the line [x1,y1] - [x2,y2]
	 */
	public static float distance(float x0, float y0, float x2, float y2, float x1, float y1) {
		// see https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
		float denom = (y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1;
		float nom = (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));

		return denom / nom;
	}

	/**
	 * Insersection of the line 0-1 and 2-3.
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param ret
	 * @return
	 */
	public static float[] intersect(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, float[] ret) {
		// find intersection of x2/y2 - x/y and x0/y0 - x1/y1
		float d1 = (x2 * y3 - y2 * x3);
		float d2 = (x0 * y1 - y0 * x1);
		float denomX = d1 * (x0 - x1) - (x2 - x3) * d2;
		float denomY = d1 * (y0 - y1) - (y2 - y3) * d2;
		float nom = (x2 - x3) * (y0 - y1) - (y2 - y3) * (x0 - x1);

		if(ret == null) ret = new float[2];

		ret[0] = denomX / nom;
		ret[1] = denomY / nom;

		return ret;
	}

	/**
	 * Finds the point closest to x, y inside the triangle 0-1-2.
	 * @param x
	 * @param y
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param ret
	 * @return
	 */
	public static float[] norm(float x, float y, float x0, float y0, float x1, float y1, float x2, float y2, float[] ret) {
		// if no return float, then create one
		if(ret == null) ret = new float[2];

		// check whether points are in correct order
		// i.e., clockwise.

		float orient = (y0 - y1) * (x2 - x0) - (x0 - x1) * (y2 - y0);

		if(orient < 0) {
			System.out.println("swapping 1 and 2");
			// not clockwise, thus switch 1 and 2.
			float t = x1;
			x1 = x2;
			x2 = t;

			t = y1;
			y1 = y2;
			y2 = t;
		}

		// get distance to all sides (ideally two should be enough)
		float d01 = distance(x, y, x0, y0, x1, y1);
		float d12 = distance(x, y, x1, y1, x2, y2);
		float d20 = distance(x, y, x2, y2, x0, y0);

		System.out.println("distances: " + d01 + ", " + d12 + ", " + d20);

		// and now find point with the distances.

		// Step 1: Find corner regions (points that are closest to a corner)
		// For these ones, there are exactly two distances < 0
		if(d01 <= 0f && d12 <= 0f) {
			System.out.println("closest to 1");
			ret[0] = x1; ret[1] = y1;
		} else if(d12 <= 0f && d20 <= 0f) {
			System.out.println("closest to 2");
			ret[0] = x2; ret[1] = y2;
		} else if(d20 <= 0f && d01 <= 0f) {
			System.out.println("closest to 0");
			ret[0] = x0; ret[1] = y0;
		} else {
			// next the regions that map to a point on a line
			if(d01 <= 0f) {
				System.out.println("on 0-1");
				// find intersection of x2/y2 - x/y and x0/y0 - x1/y1
				intersect(x2, y2, x, y, x0, y0, x1, y1, ret);
			} else if(d12 <= 0f) {
				System.out.println("on 1-2");
				// find intersection of x0/y0 - x/y and x1/y1 - x2/y2
				intersect(x0, y0, x, y, x2, y2, x1, y1, ret);
			} else if(d20 <= 0f) {
				System.out.println("on 2-0");
				// find intersection of x1/y1 - x/y and x2/y2 - x0/y0
				intersect(x1, y1, x, y, x0, y0, x2, y2, ret);
			} else {
				System.out.println("inside");
				// it is inside.
				ret[0] = x; ret[1] = y;
			}
		}

		return ret;
	}
}
