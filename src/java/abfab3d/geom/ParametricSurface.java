/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.geom;

import javax.vecmath.Vector3d;

import abfab3d.util.Vec; 

import abfab3d.util.TriangleCollector;

import static java.lang.Math.sqrt;


/**
 *
 * @author Vladimir Bulatov
 */
public interface ParametricSurface { 

    public double[] getDomainBounds();
    public int[] getGridSize();
    public Vector3d getPoint(Vector3d v, Vector3d out);

}

