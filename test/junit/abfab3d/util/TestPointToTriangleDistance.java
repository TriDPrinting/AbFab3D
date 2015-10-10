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

package abfab3d.util;

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;


// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.io.output.STLWriter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;


/**
 * Tests the functionality of ParametricSurfaceMaker
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestPointToTriangleDistance extends TestCase {

    static final double MM = 0.001;

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPointToTriangleDistance.class);
    }


    public void testTriangleInCube(){
        Vector3d 
            v000 = new Vector3d(0,0,0),
            v100 = new Vector3d(1,0,0),
            v010 = new Vector3d(0,1,0),
            v001 = new Vector3d(0,0,1),
            v101 = new Vector3d(1,0,1),
            v011 = new Vector3d(0,1,1),
            v110 = new Vector3d(1,1,0),
            v111 = new Vector3d(1,1,1);
        double s = 0.5;
        Vector3d tri[] = new Vector3d[]{new Vector3d(s,s,0), new Vector3d(0,s,s), new Vector3d(s,0,s)};
        //double eps = 1.e-8;
        double EPS = 1.e-15;
        //Vector3d tri[] = new Vector3d[]{new Vector3d(s,s,0), new Vector3d(0,s,s), new Vector3d(0.5*s,s+eps,0.5*s)};
        double delta = 1./3 - PointToTriangleDistance.getSquared(v000, tri);        
        printf("delta000: %10.3e\n", delta);
        assertTrue(fmt("region 0: delta: %10.5e",delta), abs(delta) < EPS);

        delta = 4./3 - PointToTriangleDistance.getSquared(v111, tri);
        assertTrue(fmt("region 0: delta: %10.5e",delta), abs(delta) < EPS);
        printf("delta111: %10.3e\n", delta);

        delta = 3./8 - PointToTriangleDistance.getSquared(v100, tri);
        printf("delta100: %10.3e\n", delta);
        assertTrue(fmt("region 3: delta: %8.5e",delta), abs(delta) < EPS);

        delta = 3./8 - PointToTriangleDistance.getSquared(v010, tri);
        printf("delta010: %10.3e\n", delta);
        assertTrue(fmt("region 5: delta: %8.5e",delta), abs(delta) < EPS);

        delta = 3./8 - PointToTriangleDistance.getSquared(v001, tri);
        printf("delta001: %10.3e\n", delta);
        assertTrue(fmt("region 1: delta: %8.5e",delta), abs(delta) < EPS);

        delta = 1./2-PointToTriangleDistance.getSquared(v110, tri);
        printf("delta110: %10.3e\n", delta);
        assertTrue(fmt("region 4: delta: %8.5e",delta), abs(delta) < EPS);

        delta = 1./2-PointToTriangleDistance.getSquared(v011, tri);
        printf("delta011: %10.3e\n", delta);
        assertTrue(fmt("region 6: delta: %8.5e",delta), abs(delta) < EPS);

        delta = 1./2-PointToTriangleDistance.getSquared(v101, tri);
        printf("delta101: %10.3e\n", delta);
        assertTrue(fmt("region 2: delta: %8.5e",delta), abs(delta) < EPS);
        
    }


    public void testNegativeDistance(){
        //v:  9.84385141970472100e-03 1.70822026594486320e-03 0.00000000000000000e+00
        //v0: 9.95184726672196900e-03 9.80171403295605800e-04 0.00000000000000000e+00
        //v1: 9.87883124233685100e-03 1.47140661366339200e-03 -4.93615095693959100e-04
        //v2: 9.80785280403230500e-03 1.95090322016128260e-03 0.00000000000000000e+00
        // these params return negative distance as a result of round off errors 
        Vector3d v  = new Vector3d(9.843851419704721e-03,  1.7082202659448632e-03, 0.0);
        Vector3d v0 = new Vector3d(9.951847266721969e-03, 9.8017140329560580e-04, 0.0);
        Vector3d v1 = new Vector3d(9.878831242336851e-03, 1.4714066136633920e-03, -4.936150956939591e-04);
        Vector3d v2 = new Vector3d(9.807852804032305e-03, 1.9509032201612826e-03, 0.0);
        double d = PointToTriangleDistance.getSquared(v, v0, v1, v2);
        assertTrue("positive distance squared", d >= 0.);
        
        printf("distance: %21.17e\n", d);
    }

    public void makeTest1(){

        
        double pointInTriangle[] = new double[3];
        double 
            v0x = -1,
            v0y = -1,
            v0z = 0,
            v1x = 1,
            v1y = -1,
            v1z = 0,
            v2x = 0,
            v2y = 2,
            v2z = 0;
        double 
            pntx = 0.1,
            pnty = 0,
            pntz = 1;


        for(int i = 0; i < 20; i++){
            pntx = 0.5;
            pnty = 4-i*0.5;

            double dist2 = PointToTriangleDistance.getSquared(pntx,pnty, pntz, 
                                                              v0x, v0y, v0z, 
                                                              v1x, v1y, v1z, 
                                                         v2x, v2y, v2z, 
                                                              pointInTriangle);
            printf("pnt:(%7.2f,%7.2f,%7.2f) dist2: %7.2f, pointInTriangle: (%7.2f,%7.2f,%7.2f)\n", pntx, pnty, pntz, dist2, pointInTriangle[0],pointInTriangle[1],pointInTriangle[2]);
        }

    }
    public void makeTest2(){

        
        double pointInTriangle[] = new double[3];
        double 
            v0x = -0.1,
            v0y = -0.1,
            v0z = 0,
            v1x = 0.1,
            v1y = -0.1,
            v1z = 0,
            v2x = 0,
            v2y = 0.2,
            v2z = 0;
        double 
            pntx = 0.1,
            pnty = 0,
            pntz = 1;

        int N = 1000000;
        Random rnd = new Random(121);

        long t0 = time();
        double d2max = 0;

        for(int i = 0; i < N; i++){

            pntx = (2*rnd.nextDouble()-1);
            pnty = (2*rnd.nextDouble()-1);
            pntz = (2*rnd.nextDouble()-1);
            
            double dist2 = PointToTriangleDistance.getSquared(pntx,pnty, pntz, 
                                                              v0x, v0y, v0z, 
                                                              v1x, v1y, v1z, 
                                                              v2x, v2y, v2z, 
                                                              pointInTriangle);
            double dx = pointInTriangle[0]-pntx;
            double dy = pointInTriangle[1]-pnty;
            double dz = pointInTriangle[2]-pntz;
            
            double d2 = abs(dx*dx + dy*dy + dz*dz - dist2);
            if(d2 > d2max) d2max = d2;
        }
        printf("speed dist(point,tri) calc / ms: %d\n", N/(time() - t0));
        printf("d2max: %7.1e\n", d2max);
        
    }

    public static void main(String arg[]){
        TestPointToTriangleDistance runner = new TestPointToTriangleDistance();
        //runner.makeTest1();
        runner.makeTest2();
        runner.makeTest2();
        runner.makeTest2();
        runner.makeTest2();
        runner.makeTest2();
    }

    
}
