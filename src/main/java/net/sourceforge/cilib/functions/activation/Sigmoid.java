/*
 * Copyright (C) 2003 - 2008
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science
 * University of Pretoria
 * South Africa
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.cilib.functions.activation;

import net.sourceforge.cilib.controlparameter.ConstantControlParameter;
import net.sourceforge.cilib.controlparameter.ControlParameter;
import net.sourceforge.cilib.type.types.Real;
import net.sourceforge.cilib.type.types.Type;
import net.sourceforge.cilib.type.types.container.Vector;
import net.sourceforge.cilib.util.VectorUtils;

/**
 *
 */
public class Sigmoid extends ActivationFunction {
	private static final long serialVersionUID = 8291966233976579855L;
	private ControlParameter steepness;
	private ControlParameter offset;
	
	public Sigmoid() {
		setDomain("R(0.0, 1.0)");
		this.steepness = new ConstantControlParameter(1.0);
		this.offset = new ConstantControlParameter(0.0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sigmoid getClone() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double evaluate(Type x) {
		Vector vector = (Vector) x;
		
		if (vector.getDimension() != 1)
			throw new UnsupportedOperationException("Cannot determine the actvation of more than a single value");
		
		if (steepness.getParameter() < 0.0)
			throw new UnsupportedOperationException("Steepness value for sigmoid function must be >= 0");
		
		return (1.0 / (1.0+Math.pow(Math.E, -1.0*steepness.getParameter()*(vector.getReal(0)-offset.getParameter()))));
	}
	
	public Double evaluate(Number number) {
		Vector vector = new Vector();
		vector.add(new Real(number.doubleValue()));
		return evaluate(vector);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getMaximum() {
		return new Double(1.0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getMinimum() {
		return new Double(0.0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector getGradient(Vector x) {
		double point = x.getReal(0);
		double valueAtPoint = evaluate(point);
		double result = valueAtPoint * (1 - valueAtPoint);
		
		return VectorUtils.create(result);
	}

	public ControlParameter getSteepness() {
		return steepness;
	}

	public void setSteepness(ControlParameter steepness) {
		this.steepness = steepness;
	}

	public ControlParameter getOffset() {
		return offset;
	}

	public void setOffset(ControlParameter offset) {
		this.offset = offset;
	}

}
