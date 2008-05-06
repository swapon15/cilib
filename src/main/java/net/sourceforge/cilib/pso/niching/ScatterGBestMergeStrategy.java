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
package net.sourceforge.cilib.pso.niching;

import java.util.List;
import java.util.ListIterator;

import net.sourceforge.cilib.algorithm.population.PopulationBasedAlgorithm;
import net.sourceforge.cilib.controlparameter.ConstantControlParameter;
import net.sourceforge.cilib.controlparameter.ControlParameter;
import net.sourceforge.cilib.entity.Particle;
import net.sourceforge.cilib.entity.visitor.RadiusVisitor;
import net.sourceforge.cilib.pso.PSO;
import net.sourceforge.cilib.pso.particle.StandardParticle;
import net.sourceforge.cilib.pso.velocityupdatestrategies.StandardVelocityUpdate;
import net.sourceforge.cilib.type.types.Real;
import net.sourceforge.cilib.type.types.container.Vector;
import net.sourceforge.cilib.util.DistanceMeasure;
import net.sourceforge.cilib.util.EuclideanDistanceMeasure;

/**
 * 
 * @author Edrich van Loggerenberg
 * 
 * @param <E> The type which should extend {@linkplain PopulationBasedAlgorithm}.
 */
public class ScatterGBestMergeStrategy<E extends PopulationBasedAlgorithm> implements MergeStrategy<E>
{
    private ControlParameter threshold;

    /**
     * Create an instance of the {@linkplain ScatterGBestMergeStrategy}. The default threshold
     * is defined to have the value of 0.001.
     */
    public ScatterGBestMergeStrategy() {
    	this.threshold = new ConstantControlParameter(0.001);
    }

    /**
     * Test if the given value is smaller than a predetermined epsilon.
     * @param value The value to test.
     * @return <code>true</code> if the given value os smaller, <code>false</code> otherwise.
     */
    private boolean testNearZero(double value) {
    	if (value < 0.0001)
    		return true;

    	return false;
    }

    /**
     * Normalise the given particles??? TODO: This method's documentation needs to be expanded.
     * @param mainSwarm The {@linkplain PSO} of the main swarm.
     * @param gBest1 The first global best particle.
     * @param gBest2 The second global best particle.
     * @return The normalised value.
     */
    public double normalise(PSO mainSwarm, Particle gBest1, Particle gBest2) {
		Real range = (Real) ((Vector) mainSwarm.getOptimisationProblem().getDomain().getBuiltRepresenation().getClone()).get(0);
	
		Vector gBestPos1 = (Vector) gBest1.getPosition();
		Vector gBestPos2 = (Vector) gBest2.getPosition();
	
		Vector v1 = new Vector();
		Vector v2 = new Vector();
	
		for (int i = 0; i < gBest1.getPosition().getDimension(); i++) {
		    v1.append(new Real((gBestPos1.getReal(i) - range.getLowerBound()) / (range.getUpperBound() - range.getLowerBound())));
		    v2.append(new Real((gBestPos2.getReal(i) - range.getLowerBound()) / (range.getUpperBound() - range.getLowerBound())));
		}
	
		double sum = 0.0;
		for (int i = 0; i < gBest1.getPosition().getDimension(); i++)
		    sum += (v1.getReal(i) - v2.getReal(i)) * (v1.getReal(i) - v2.getReal(i));
		
		sum = sum / gBest1.getPosition().getDimension();
		sum = Math.sqrt(sum);
	
		return sum;
    }

    /**
     * Get the threshold value.
     * @return The threshold value.
     */
    public ControlParameter getThreshold() {
    	return threshold;
    }

    /**
     * Set the value of the threshold.
     * @param threshold The threshold value to set.
     */
    public void setThreshold(ControlParameter threshold) {
    	this.threshold = threshold;
    }

    /**
     * {@inheritDoc}
     */
    public void merge(PSO mainSwarm, List<PopulationBasedAlgorithm> subSwarms) {
		for (ListIterator<PopulationBasedAlgorithm> i = subSwarms.listIterator(); i.hasNext();) {
		    PSO subSwarm1 = (PSO) i.next();
	
		    if (subSwarm1 != null) {
		    	Particle gBestParticle1 = subSwarm1.getTopology().getBestEntity();
	
		    	for (ListIterator<PopulationBasedAlgorithm> j = subSwarms.listIterator(); j.hasNext();) {
		    		PSO subSwarm2 = (PSO) j.next();
	
		    		if (subSwarm2 != null) {
		    			if (subSwarm1 != subSwarm2) {
		    				Particle gBestParticle2 = subSwarm2.getTopology().getBestEntity();
	
		    				if (testNearZero(getRadius(subSwarm1)) && testNearZero(getRadius(subSwarm2))) {
		    					if (normalise(mainSwarm, gBestParticle1, gBestParticle2) < threshold.getParameter()) {
		    						if (gBestParticle1.getFitness().getValue() < gBestParticle2.getFitness().getValue()) {
		    							scatterSwarm(mainSwarm, subSwarm2);
		    							j.remove();
		    							i = subSwarms.listIterator();
		    						}
		    						else {
		    							scatterSwarm(mainSwarm, subSwarm1);
		    							i.remove();
		    							j = subSwarms.listIterator();
		    						}
		    						break;
		    					}
		    				}
		    				else {
		    					DistanceMeasure distanceMeasure = new EuclideanDistanceMeasure();
	
		    					double distance = distanceMeasure.distance(getAverageVector(subSwarm1), getAverageVector(subSwarm2));
	
		    					if ((distance < (getRadius(subSwarm1) + getRadius(subSwarm2))) && (distance < 0.001)) {
		    						if (gBestParticle1.getFitness().getValue() < gBestParticle2.getFitness().getValue()) {
		    							scatterSwarm(mainSwarm, subSwarm2);
		    							j.remove();
		    							i = subSwarms.listIterator();
		    						}
		    						else {
		    							scatterSwarm(mainSwarm, subSwarm1);
		    							i.remove();
		    							j = subSwarms.listIterator();
		    						}
		    						break;
		    					}
		    				}
		    			}
		    		}
		    	}
		    }
		}
    }

    /**
     * Get the average vector of the given subswarm.
     * @param subSwarm The subswarm to average
     * @return The average position vector of the swarm.
     */
    private Vector getAverageVector(PSO subSwarm) {
    	AveragePositionVisitor averagePositionVisitor = new AveragePositionVisitor();
    	subSwarm.accept(averagePositionVisitor);
    	return averagePositionVisitor.getAveragePosition();
	}
    
    /**
     * Get the radius of the givem swarm.
     * @param subSwarm The swarm to determine the radius of.
     * @return The radius size of the subswarm.
     */
    private double getRadius(PSO subSwarm) {
    	RadiusVisitor radiusVisitor = new RadiusVisitor();
    	subSwarm.accept(radiusVisitor);
    	return radiusVisitor.getResult();
	}

    /**
     * Scatter the swarm.
     * @param mainSwarm The main swarm.
     * @param subSwarm The sub swarm.
     */
	private void scatterSwarm(PSO mainSwarm, PSO subSwarm) {
		//System.out.println("Scatter: " + subSwarm.getTopology().size());
	
		ListIterator<Particle> subSwarmIterator = subSwarm.getTopology().listIterator();
		while (subSwarmIterator.hasNext()) {
		    ConstantControlParameter socialAcceleration = new ConstantControlParameter(0.0);
		    
		    //ControlParameterUpdateStrategy inertia = new AccelerationUpdateStrategy();
		    //inertia.setParameter(0.0);
	
		    StandardParticle subSwarmParticle = (StandardParticle) subSwarmIterator.next();
		    
		    ((StandardVelocityUpdate) subSwarmParticle.getVelocityUpdateStrategy()).setSocialAcceleration(socialAcceleration);
		    //((StandardVelocityUpdate) subSwarmParticle.getVelocityUpdateStrategy()).setInertiaWeight(inertia);
	
		    // Reinitialise the subswarm particle.
		    subSwarmParticle.initialise(mainSwarm.getOptimisationProblem());
		    mainSwarm.getTopology().add(subSwarmParticle);
		    
		    subSwarmIterator.remove();
		}
    }
}
