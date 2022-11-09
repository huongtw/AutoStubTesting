package auto_testcase_generation.testdatagen.fastcompilation.randomgeneration;

import com.dse.boundary.PrimitiveBound;
import com.dse.config.IFunctionConfigBound;
import com.dse.environment.Environment;
import com.dse.util.VariableTypeUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Generate value random
 * 
 * @author ducanhnguyen
 *
 */
public class BasicTypeRandom {
//	static final int SMALL_LOWER_BOUND = 0;
//	static final int SMALL_UPPER_BOUND = 4;

	public static long random(long lower, long upper){
		if (lower == upper)
			return lower;
		else if (lower < upper)
			return new RandomDataGenerator().nextLong(lower, upper);
		else
			return -1;
	}
	/**
	 * @param minBound
	 * @param maxBound
	 * @param type     "int", "long long int", "unsigned int"
	 * @return
	 */
	public static long generateInt(String minBound, String maxBound, String type){
		type = VariableTypeUtils.removeRedundantKeyword(type);
		type = VariableTypeUtils.deleteReferenceOperator(type);
		final long DEFAULT_VALUE = 0l;
		if (!minBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) && !minBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)
				&& !maxBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) && !maxBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
			long lower = Long.parseLong(minBound);
			long upper = Long.parseLong(maxBound);

			IFunctionConfigBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
			if (bound instanceof PrimitiveBound) {
				if (lower < (long)((PrimitiveBound) bound).getLowerAsDouble())
					lower = (long)((PrimitiveBound) bound).getLowerAsDouble();
				if (upper > (long)((PrimitiveBound) bound).getUpperAsDouble())
					upper = (long)((PrimitiveBound) bound).getUpperAsDouble();

				if (lower <= upper)
					return BasicTypeRandom.random(lower, upper);
			}

		} else if (minBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) && maxBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
			IFunctionConfigBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
			if (bound != null && bound instanceof PrimitiveBound) {
				return BasicTypeRandom.random(
						Long.parseLong(((PrimitiveBound) bound).getLower()),
						Long.parseLong(((PrimitiveBound) bound).getUpper()));
			}
		} else if (minBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) && maxBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE)) {
			IFunctionConfigBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
			if (bound != null && bound instanceof PrimitiveBound) {
				return Long.parseLong(((PrimitiveBound) bound).getLower());
			}
		} else if (minBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE) && maxBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
			IFunctionConfigBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
			if (bound != null && bound instanceof PrimitiveBound) {
				return Long.parseLong(((PrimitiveBound) bound).getUpper());
			}
		}
		return DEFAULT_VALUE;
	}

	public static long generateInt(long minBound, long maxBound) {
		return generateInt(minBound + "", maxBound + "");
	}

	public static long generateInt(String minBound, String maxBound) {
		final long DEFAULT_VALUE = 0l;
		if (!minBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) && !minBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)
				&& !maxBound.equals(IFunctionConfigBound.MIN_VARIABLE_TYPE) && !maxBound.equals(IFunctionConfigBound.MAX_VARIABLE_TYPE)) {
//			return BasicTypeRandom.random(Long.parseLong(minBound), Long.parseLong(maxBound));
			BigDecimal bd_min = new BigDecimal(minBound);
			BigDecimal bd_max = new BigDecimal(maxBound);
			return BasicTypeRandom.random(bd_min.longValue(), bd_max.longValue());
		}
		return DEFAULT_VALUE;
//		if (minBound <= 0l && maxBound >= 0l) {
//			return
//					//new Random().nextInt(maxBound + Math.abs(minBound) + 1) + minBound;
//		} else if (minBound >= 0) {
//			return new Random().nextInt(maxBound - minBound + 1) + minBound;
//		} else {
//			//(minBound <= 0 && maxBound <= 0)
//			return new Random().nextInt(Math.abs(maxBound) - Math.abs(minBound) + 1) + maxBound;
//		}
	}

	public static double generateFloat(String minBound, String maxBound) {
		Random rand = new Random();
		long randomNum = generateInt(minBound + "", maxBound+"");
		int decimal = rand.nextInt(100);
		String output = randomNum + "." + decimal;
		return Double.parseDouble(output);
	}

//	public static double generateSmallFloat(long minBound, long maxBound) {
//		Random rand = new Random();
//		int randomNum = generateInt(minBound, maxBound);
//		int decimal = rand.nextInt(100);
//		String output = randomNum + "." + decimal;
//		return Double.parseDouble(output);
//	}
//
//	public static int generateSmallInt(long minBound, long maxBound) {
//		if (maxBound <= SMALL_LOWER_BOUND) {
//			// nothing to do
//		} else if (minBound <= SMALL_LOWER_BOUND && SMALL_UPPER_BOUND <= maxBound) {
//			minBound = SMALL_LOWER_BOUND;
//			maxBound = SMALL_UPPER_BOUND;
//		} else if (minBound <= SMALL_LOWER_BOUND) {
//			minBound = SMALL_LOWER_BOUND;
//		} else if (SMALL_UPPER_BOUND <= maxBound) {
//			maxBound = SMALL_UPPER_BOUND;
//		}
//
//		if (minBound <= 0 && maxBound >= 0) {
//			return new Random().nextInt(maxBound - minBound + 1) - minBound;
//		} else if (minBound >= 0) {
//			return new Random().nextInt(maxBound - minBound + 1) + minBound;
//		} else {
//			return new Random().nextInt(-1 * minBound - (-1) * maxBound + 1) + minBound;
//		}
//	}

	public static void main(String[] args) {
		System.out.println(BasicTypeRandom.generateInt(-10, 100));
		System.out.println(BasicTypeRandom.generateInt(1, 100));
		System.out.println(BasicTypeRandom.generateInt(-12, -10));
	}
}
