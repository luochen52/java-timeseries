/*
 * Copyright (c) 2017 Jacob Rachiele
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *
 * Jacob Rachiele
 */

package com.github.signaflo.math.polynomial.interpolation;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.github.signaflo.math.function.CubicFunction;
import com.github.signaflo.math.function.QuadraticFunction;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Jacob Rachiele
 */
public class NewtonPolynomialSpec {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void whenPointAndValuesLengthDifferThenException() {
    exception.expect(IllegalArgumentException.class);
    new NewtonPolynomial(new double[] {0.3}, new double[] {0.4, 1.1});
  }

  @Test
  public void whenPointAndValuesEmptyThenException() {
    exception.expect(IllegalArgumentException.class);
    new NewtonPolynomial(new double[] {}, new double[] {});
  }

  @Test
  public void whenToQuadraticOnLinearFunctionExceptionThrown() {
    exception.expect(IllegalStateException.class);
    NewtonPolynomial np = new NewtonPolynomial(new double[] {0.4, 1.1}, new double[] {0.8, 2.2});
    np.toQuadratic();
  }

  @Test
  public void whenToCubicOnQuadraticFunctionExceptionThrown() {
    exception.expect(IllegalStateException.class);
    NewtonPolynomial np = new NewtonPolynomial(new double[] {1, 3, 5}, new double[] {1, 9, 25});
    np.toCubic();
  }

  @Test
  public void whenOnePointThenCoefficientEqualsFunctionValue() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0}, new double[] {4.0});
    assertThat(np.getCoefficient(0), is(equalTo(4.0)));
  }

  @Test
  public void whenMultiplePointsThenFirstCoefficientEqualsFirstFunctionValue() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0, 3.0}, new double[] {4.0, 9.0});
    assertThat(np.getCoefficient(0), is(equalTo(4.0)));
  }

  @Test
  public void whenTwoPointsThenSecondCoefficientEqualsLinearSlopeValue() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0, 3.0}, new double[] {4.0, 9.0});
    assertThat(np.getCoefficient(1), is(equalTo(5.0)));
  }

  @Test
  public void whenThreePointsThenThirdCoefficientEqualsThirdDividedDifference() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0, 4.0, 7.0}, new double[] {4.0, 16.0, 49.0});
    assertThat(np.getCoefficient(2), is(equalTo(1.0)));
  }

  @Test
  public void whenThreePointPolyEvalutatedThenResultCorrect() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0, 4.0, 7.0}, new double[] {4.0, 16.0, 49.0});
    assertThat(np.evaluateAt(3.0), is(equalTo(9.0)));
  }

  @Test
  public void whenConvertedThenQuadraticFunctionCorrect() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0, 4.0, 7.0}, new double[] {4.0, 16.0, 49.0});
    QuadraticFunction function = np.toQuadratic();
    double[] expected = new double[] {1.0, 0.0, 0.0};
    assertThat(function.coefficientsDbl(), is(equalTo(expected)));
  }

  @Test
  public void whenConvertedThenCubicFunctionCorrect() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {4.0, 2.0, 5.0, 7.0}, new double[]
        {64.0, 8.0, 125.0, 343.0});
    CubicFunction function = np.toCubic();
    double[] expected = new double[] {1.0, 0.0, 0.0, 0.0};
    assertThat(function.coefficientsDbl(), is(equalTo(expected)));
  }


  @Test
  public void whenNewtonPolynomialBuiltThenDividedDifferencesCorrect() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {1.0, 2.0, 3.0, 4.0}, new double[]
        {1.0, 8.0, 27.0, 64.0});
    //CubicFunction function = np.toCubic();
    double expected = 1.0;
    assertThat(np.getCoefficient(0), is(equalTo(expected)));
    expected = 7.0;
    assertThat(np.getCoefficient(1), is(equalTo(expected)));
    expected = 6.0;
    assertThat(np.getCoefficient(2), is(equalTo(expected)));
    expected = 1.0;
    assertThat(np.getCoefficient(3), is(equalTo(expected)));
  }

  @Test
  public void testEqualsAndHashCode() {
    NewtonPolynomial np = new NewtonPolynomial(new double[] {2.0, 4.0, 7.0}, new double[] {4.0, 16.0, 49.0});
    NewtonPolynomial np2 = new NewtonPolynomial(new double[] {1.0, 2.0, 3.0, 4.0}, new double[]
        {1.0, 8.0, 27.0, 64.0});
    NewtonPolynomial npAgain = new NewtonPolynomial(new double[] {2.0, 4.0, 7.0}, new double[] {4.0, 16.0, 49.0});
    assertThat(np, is(np));
    MatcherAssert.assertThat(np, is(not(np2)));
    MatcherAssert.assertThat(np2, is(not(np)));
    assertThat(np.hashCode(), is(not(np2.hashCode())));
    assertThat(np.hashCode(), is(npAgain.hashCode()));
    assertThat(np.equals(null), is(false));

  }
}
