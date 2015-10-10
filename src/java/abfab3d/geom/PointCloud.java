/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
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
import javax.vecmath.Tuple3d;

import java.util.Vector;

import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.PointSet;

import static abfab3d.util.Units.MM;

/**
   creates a set of small objects (octahedra) to represent unstructured cloud of 3D points 

   @author Vladimir Bulatov
 */
public class PointCloud implements TriangleProducer, PointSet  {
    
    
    // points are represented via 3 coordinates 
    double coord[] = null;

    int m_size=0; // points count 
    int m_arrayCapacity = 0;

    // size of geometrical shape to represent each point 
    double pointSize = 0.05*MM;
    
    /**
       makes empty point cloud.
       Pount can be added using add() method
     */
    public PointCloud(){
        this(10);
    }

    public PointCloud(int initialCapacity){

        // avoid wrong behavior 
        if(initialCapacity < 1) initialCapacity = 1;
            
        m_arrayCapacity = initialCapacity;
        coord = new double[3*m_arrayCapacity];
        
    }

    /**
       accept coordinates as flat array of double
     */
    public PointCloud(double coord[]){
        this.coord = coord;
        m_arrayCapacity = coord.length/3;
        m_size = m_arrayCapacity;

    }
    
    /**
       accept coordinates as vector of Vector3d
     */
    public PointCloud(Vector<Vector3d> points){
        
        this.coord = getCoord(points);
        m_size = coord.length/3;
        m_arrayCapacity = m_size;
    }


    /**
     * Clear all point and triangle data.
     */
    public void clear() {
        m_size = 0;
        m_arrayCapacity = 10;
        coord = new double[3*m_arrayCapacity];
    }

    /**
       
     */
    public void setPointSize(double size){
        pointSize = size;
    }

    
    public final void addPoint(double x, double y, double z){
        if(m_size >= m_arrayCapacity)
            reallocArray();

        int start = m_size*3;
        coord[start] = x;
        coord[start+1]= y;
        coord[start+2]= z;
        m_size++;
    }

    public boolean getTriangles(TriangleCollector collector){ 
       
        int count = m_size;
        Octa shape = new Octa(pointSize/2);
        
        for(int i = 0; i < count; i++){
            int start  = i*3;
            double 
                x = coord[start],
                y = coord[start+1],
                z = coord[start+2];
            shape.makeShape(collector, x,y,z);
        }
        return true;
        
    }


    /**
       interface PointSet 
     */
    public int size(){

        return m_size;

    }

    public void getPoint(int index, Tuple3d point){

        int start = index*3;
        point.x = coord[start];
        point.y = coord[start+1];
        point.z = coord[start+2];

    }
    
    private void reallocArray(){

        int ncapacity = 2*m_arrayCapacity;
        double ncoord[] = new double[ncapacity*3];
        System.arraycopy(coord, 0, ncoord,0,coord.length);
        m_arrayCapacity = ncapacity;
        coord = ncoord;

    }


    private static double [] getCoord(Vector<Vector3d> points){

        int count = points.size();
        double coord[] = new double[count*3];
        for(int i = 0; i < count; i++){
            Vector3d pnt = points.get(i);
            int i3 = i*3;
            coord[i3] = pnt.x;
            coord[i3+1] = pnt.y;
            coord[i3+2] = pnt.z;
        }
        return coord;
    }

    static class Octa {
        
        double r;
        Vector3d 
            vx = new Vector3d(), 
            v_x  = new Vector3d(), 
            vy = new Vector3d(), 
            v_y = new Vector3d(), 
            vz = new Vector3d(), 
            v_z = new Vector3d();  
        
        Octa (double r){
            this.r = r;
        }
        // vertices to make 
        
        /**
           make octahedron of given radius and center 
        */
        protected void makeShape(TriangleCollector tc, double x, double y, double z){
            
            
            vx.set (x+r,y,z);
            v_x.set(x-r,y,z);
            vy.set (x,y+r,z);
            v_y.set(x,y-r,z);
            vz.set (x,y,z+r);
            v_z.set(x,y,z-r);
            
            tc.addTri(vz, vx, vy);
            tc.addTri(vz ,vy,v_x);
            tc.addTri(vz,v_x,v_y);
            tc.addTri(vz,v_y, vx);
            
            tc.addTri(v_z, vy, vx);
            tc.addTri(v_z,v_x, vy);
            tc.addTri(v_z,v_y,v_x);
            tc.addTri(v_z, vx,v_y);
        }
    }    
}
