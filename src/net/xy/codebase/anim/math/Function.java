package net.xy.codebase.anim.math;

import net.xy.codebase.anim.IActor;
import net.xy.codebase.anim.IAnimationContext;
import net.xy.codebase.anim.IAxis;
import net.xy.codebase.anim.IFunction;

/**
 * function collection for animation purpose
 *
 * @author Xyan
 *
 */
public class Function {
	/**
	 * simple linear function
	 */
	public static final IFunction LINEAR = new IFunction() {
		@Override
		public double getVal(final IActor a, final IAnimationContext ac, final IAxis x, final IAxis y) {
			final double value = x.getVal(a, ac);
			final double p = Math.min((value - x.getMin(a, ac)) / (x.getMax(a, ac) - x.getMin(a, ac)), 1);
			return y.getMin(a, ac) * (1 - p) + y.getMax(a, ac) * p;
		}

		@Override
		public String toString() {
			return "LINEAR";
		};
	};

	/**
	 * reverse liner function
	 */
	public static final IFunction REVERSE = new IFunction() {
		@Override
		public double getVal(final IActor a, final IAnimationContext ac, final IAxis x, final IAxis y) {
			final double value = x.getVal(a, ac);
			final double p = 1 - Math.min((value - x.getMin(a, ac)) / (x.getMax(a, ac) - x.getMin(a, ac)), 1);
			return y.getMin(a, ac) * (1 - p) + y.getMax(a, ac) * p;
		}

		@Override
		public String toString() {
			return "REVERSE";
		};
	};

	/**
	 * dummy which alway returns Y axis current value
	 */
	public static final IFunction NONE = new IFunction() {
		@Override
		public double getVal(final IActor a, final IAnimationContext ac, final IAxis x, final IAxis y) {
			return y.getVal(a, ac);
		}

		@Override
		public String toString() {
			return "NONE";
		};
	};

	/**
	 * function for linear color animation in ARGB format
	 */
	public static final IFunction LINEAR_ARGB = new IFunction() {
		@Override
		public double getVal(final IActor a, final IAnimationContext ac, final IAxis x, final IAxis y) {
			final double value = x.getVal(a, ac);
			final double p = Math.min((value - x.getMin(a, ac)) / (x.getMax(a, ac) - x.getMin(a, ac)), 1);

			final short[] yminc = fromInt((int) y.getMin(a, ac));
			final short[] ymaxc = fromInt((int) y.getMax(a, ac));
			final short[] resc = new short[4];
			for (int i = 0; i < yminc.length; i++)
				resc[i] = (short) (yminc[i] * (1 - p) + ymaxc[i] * p);

			return (resc[3] & 0xFF) << 24 | //
			(resc[0] & 0xFF) << 16 | //
			(resc[1] & 0xFF) << 8 | //
			(resc[2] & 0xFF) << 0;
		}

		private short[] fromInt(final int rgb) {
			return new short[] { //
					(short) (rgb >> 16 & 0xFF), //
					(short) (rgb >> 8 & 0xFF), //
					(short) (rgb >> 0 & 0xFF), //
					(short) (rgb >> 24 & 0xff) //
			};
		}

		@Override
		public String toString() {
			return "LINEAR_ARGB";
		};
	};

	/**
	 * linear zic zac curved shaking funktion
	 */
	public static final IFunction SHAKING = new IFunction() {
		@Override
		public double getVal(final IActor a, final IAnimationContext ac, final IAxis x, final IAxis y) {
			final double value = x.getVal(a, ac);
			final double fac = Math.abs(1d - value % 2d);

			final double span = y.getMax(a, ac) - y.getMin(a, ac);
			return y.getMin(a, ac) + span * fac;
		}

		@Override
		public String toString() {
			return "SHAKING";
		};
	};
}
