package at.searles.commons.math;

public class Scale {

	public static Scale createScaled(double sc) {
		return new Scale(sc, 0, 0, sc, 0, 0);
	}

	/*
	 * If this was a matrix, it would be
	 * m[0] = xx, m[1] = yx, m[2] = cx
	 * m[3] = xy, m[4] = yy, m[5] = cy
	 */
	public final double xx, xy, yx, yy, cx, cy;

	public Scale(double xx, double xy, double yx, double yy, double cx, double cy) {
		this.xx = xx;
		this.xy = xy;
		this.yx = yx;
		this.yy = yy;
		this.cx = cx;
		this.cy = cy;
	}

	public boolean equals(Object o) {
		if(o instanceof Scale) {
			Scale sc = (Scale) o;
			return sc.xx == xx && sc.xy == xy && sc.yx == yx && sc.yy == yy && sc.cx == cx && sc.cy == cy;
		} else {
			return false;
		}
	}

	public static Scale fromMatrix(double...m) {
		return new Scale(m[0], m[3], m[1], m[4], m[2], m[5]);
	}

	public static Scale fromMatrix(float...m) {
		return new Scale(m[0], m[3], m[1], m[4], m[2], m[5]);
	}

	public double[] scalePoint(double x, double y, double[] dst) {
		dst[0] = xx * x + yx * y + cx;
        dst[1] = xy * x + yy * y + cy;

        return dst;
	}

	public double[] invScalePoint(double x, double y, double[] dst) {
		x -= cx;
		y -= cy;

		double det = xx * yy - xy * yx;
		double detX = x * yy - y * yx;
		double detY = xx * y - xy * x;

        dst[0] = detX / det;
        dst[1] = detY / det;

        return dst;
	}


	public Scale createZoom(double px, double py, double factor) {
		double ncx = xx * px + yx * py + cx;
		double ncy = xy * px + yy * py + cy;

		return new Scale(xx * factor, xy * factor, yx * factor, yy * factor, ncx, ncy);
	}

	/**
	 * applies the matrix m relative to this scale.
	 * m must contain 6 values.
	 * @param that matrix to be post-contatenated with this scale
	 * @return
	 */
	public Scale createRelative(Scale that) {
		double m0 = xx * that.xx + yx * that.xy;
		double m1 = xx * that.yx + yx * that.yy;
		double m2 = xx * that.cx + yx * that.cy + cx;
		double m3 = xy * that.xx + yy * that.xy;
		double m4 = xy * that.yx + yy * that.yy;
		double m5 = xy * that.cx + yy * that.cy + cy;

		return new Scale(m0, m3, m1, m4, m2, m5);
	}

	/**
     * Creates the matrix/scale with [[xx,xy],[yx,yy]] st
     * an odd number of items have a negative sign and |xx|=|yy| and |xy|=|yx|.
     */
	public Scale createOrthogonal() {

        double a = Math.max(Math.abs(xx), Math.abs(yy));
        double b = Math.max(Math.abs(xy), Math.abs(yx));

        double minD = 0.;
        int index = -1;

        for(int i : new int[]{1, 2, 4, 7, 8, 11, 13, 14}) {
            // instead of a complex if, let's investigate these few cases.
            double oxx = ((i & 1) == 0) ? a : -a;
            double oxy = ((i & 2) == 0) ? b : -b;
            double oyx = ((i & 4) == 0) ? b : -b;
            double oyy = ((i & 8) == 0) ? a : -a;

            double d = Math.abs(xx-oxx) + Math.abs(xy-oxy) + Math.abs(yx-oyx) + Math.abs(yy-oyy);

            if(index == -1 || d < minD) {
                index = i;
                minD = d;
            }
        }

        double oxx = ((index & 1) == 0) ? a : -a;
        double oxy = ((index & 2) == 0) ? b : -b;
        double oyx = ((index & 4) == 0) ? b : -b;
        double oyy = ((index & 8) == 0) ? a : -a;

        return new Scale(oxx, oxy, oyx, oyy, cx, cy);
	}

	public String toString() {
		return xx + ":" + xy + " " + yx + ":" + yy + " " + cx + ":" + cy;
	}


}
