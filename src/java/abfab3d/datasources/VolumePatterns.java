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

package abfab3d.datasources;


import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.Vec;


import javax.vecmath.Vector3d;

import static java.lang.Math.floor;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sinh;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;


public class VolumePatterns {

    static final boolean DEBUG = false;
    static int debugCount = 100;
    
    public static class Balls  extends TransformableDataSource{
        

        double period;
        double RR, R2;

        public Balls(double period, double radius){

            this.period = period;
            
            RR = radius*radius;
            
            R2 = radius*2;

        }

        public int getDataValue(Vec pnt, Vec data){
            super.transform(pnt);

            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];
            double vs = pnt.getScaledVoxelSize();
            
            x -= period*floor(x/period);
            y -= period*floor(y/period);
            z -= period*floor(z/period);
            
            x -= period/2;
            y -= period/2;
            z -= period/2;
            
            double dist = ((x*x + y*y + z*z) - RR)/R2;
            data.v[0] = step10(dist, 0,vs);

            if(DEBUG && debugCount-- > 0)
                printf("(%10.5f %10.5f %10.5f) -> %10.5f\n", x,y,z,data.v[0]);

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
        
    }

    public static class CubicGrid  extends TransformableDataSource{
        


        double period;
        double RR, R2;

        public CubicGrid(double period, double radius){

            this.period = period;
            
            RR = radius*radius;
            
            R2 = radius*2;

        }

        public int getDataValue(Vec pnt, Vec data){
            
            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];
            double vs = pnt.getScaledVoxelSize();
            
            x -= period*floor(x/period);
            y -= period*floor(y/period);
            z -= period*floor(z/period);
            
            x -= period/2;
            y -= period/2;
            z -= period/2;
            
            double dxy = step10(((x*x + y*y) - RR)/R2, 0, vs);
            double dyz = step10(((y*y + z*z) - RR)/R2, 0, vs);
            double dzx = step10(((z*z + x*x) - RR)/R2, 0, vs);

            double d = 0;
            if( dxy > d) d = dxy;
            if( dyz > d) d = dyz;
            if( dzx > d) d = dzx;

            data.v[0] = d;

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
        
    } // class CubicGrid


    /**
       approximation to Gyroid 
    */
    public static class Gyroid  extends TransformableDataSource{
        

        // member variables should be initialized 
        private double m_thickness;
        private double m_level;
        private double m_centerX,m_centerY,m_centerZ;
        private double m_factor = 0;

        DoubleParameter mp_period = new DoubleParameter("period", "Period of surface", 10*MM);
        DoubleParameter mp_thickness = new DoubleParameter("thickness", "thicknenss of surface", 0.2*MM);
        DoubleParameter mp_level = new DoubleParameter("level", "isosurface level", 0);
        Vector3dParameter mp_center = new Vector3dParameter("center", "center of gyroid", new Vector3d(0,0,0));
        
        Parameter m_aparam[] = new Parameter[]{
            mp_period,
            mp_thickness,
            mp_level,
            mp_center,
        };    
        

        public Gyroid(){
            super.addParams(m_aparam);
        }

        public Gyroid(double period, double thickness){
            super.addParams(m_aparam);
            setPeriod(period);
            setThickness(thickness);
        }

        public void setPeriod(double value){

            mp_period.setValue(value);
        }

        public void setThickness(double value){

            mp_thickness.setValue(value);

        }

        public void setCenter(Vector3d value) {
            mp_center.setValue(value);
        }

        public void setLevel(double value) {
            mp_level.setValue(value);
        }

        public int initialize(){

            super.initialize();

            Vector3d center = (Vector3d)mp_center.getValue();

            m_centerX = center.x;
            m_centerY = center.y;
            m_centerZ = center.z;
            double period = (Double)mp_period.getValue();
            
            m_factor = 2*PI/period;
            m_level = (Double)mp_level.getValue();
            m_thickness = mp_thickness.getValue();

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){
            
            super.transform(pnt);
            double x = pnt.v[0] - m_centerX;
            double y = pnt.v[1] - m_centerY;
            double z = pnt.v[2] - m_centerZ;
            
            x *= m_factor;
            y *= m_factor;
            z *= m_factor;
            double vs = pnt.getScaledVoxelSize();
            
            double d = abs(( sin(x)*cos(y) + sin(y)*cos(z) + sin(z) * cos(x) - m_level)/m_factor) - (m_thickness);
            
            data.v[0] = step10(d, 0, vs);

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
        
    } // Gyroid 

    /**
       http://en.wikipedia.org/wiki/Lidinoid
    */
    public static class Lidinoid extends TransformableDataSource{

        private double m_thickness;
        private double m_level;
        private double m_factor = 0;

        DoubleParameter mp_period = new DoubleParameter("period", "Period of surface", 10*MM);
        DoubleParameter mp_thickness = new DoubleParameter("thickness", "thicknenss of surface", 0.2*MM);
        DoubleParameter mp_level = new DoubleParameter("level", "isosurface level", 0);

        Parameter m_aparam[] = new Parameter[]{
            mp_period,
            mp_thickness,
            mp_level,
        };    

        public Lidinoid(){
            super.addParams(m_aparam);
        }
        public Lidinoid(double period, double thickness){

            super.addParams(m_aparam);

            mp_period.setValue(period);
            mp_thickness.setValue(thickness);

        }

        public int initialize(){

            super.initialize();

            double period = (Double)mp_period.getValue();
            
            m_factor = 2*PI/period;
            m_level = (Double)mp_level.getValue();
            m_thickness = mp_thickness.getValue();

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);

            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= m_factor;
            y *= m_factor;
            z *= m_factor;

            double vs = pnt.getScaledVoxelSize();
            double 
                s2x = sin(2*x),
                s2y = sin(2*y),
                s2z = sin(2*z),
                c2x = cos(2*x),
                c2y = cos(2*y),
                c2z = cos(2*z),
                sx = sin(x),
                sy = sin(y),
                sz = sin(z),
                cx = cos(x),
                cy = cos(y),
                cz = cos(z);



            double d = abs(((s2x * cy * sz + s2y * cz * sx + s2z * cx * sy) -
                            (c2x * c2y + c2y * c2z + c2z * c2x) 
                            - m_level)/(4*m_factor))  - m_thickness;
            
            data.v[0] = step10(d, 0, (vs));

            super.getMaterialDataValue(pnt, data);
            return RESULT_OK;
        }

    } // Lidinoid

    /**
     * Schwarz Primitive as defined here: http://en.wikipedia.org/wiki/Schwarz_minimal_surface#Schwarz_P_.28.22Primitive.22.29
     *
     * @author Alan Hudson
     */
    public static class SchwarzP extends TransformableDataSource{


        private double m_thickness;
        private double m_level;
        private double m_factor = 0;

        DoubleParameter mp_period = new DoubleParameter("period", "Period of surface", 10*MM);
        DoubleParameter mp_thickness = new DoubleParameter("thickness", "thicknenss of surface", 0.2*MM);
        DoubleParameter mp_level = new DoubleParameter("level", "isosurface level", 0);

        Parameter m_aparam[] = new Parameter[]{
            mp_period,
            mp_thickness,
            mp_level,
        };    
        
        public SchwarzP(){
            super.addParams(m_aparam);
        }

        public SchwarzP(double period, double thickness){

            super.addParams(m_aparam);

            mp_period.setValue(period);
            mp_thickness.setValue(thickness);
        }

        public int initialize(){

            super.initialize();

            double period = (Double)mp_period.getValue();
            
            m_factor = 2*PI/period;
            m_level = (Double)mp_level.getValue();
            m_thickness = mp_thickness.getValue();

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= m_factor;
            y *= m_factor;
            z *= m_factor;

            double vs = pnt.getScaledVoxelSize();
            double d = abs(cos(x) + cos(y) + cos(z)-m_level)/m_factor - m_thickness;

            data.v[0] = step10(d, 0, (vs));

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

    /**
     * Schwarz Diamond as defined here: http://en.wikipedia.org/wiki/Schwarz_minimal_surface#Schwarz_P_.28.22Primitive.22.29
     *
     * @author Alan Hudson
     */
    public static class SchwarzD extends TransformableDataSource {
        
        double m_thickness;
        double m_level;
        double m_factor = 0;

        DoubleParameter mp_period = new DoubleParameter("period", "Period of surface", 10*MM);
        DoubleParameter mp_thickness = new DoubleParameter("thickness", "thicknenss of surface", 0.2*MM);
        DoubleParameter mp_level = new DoubleParameter("level", "isosurface level", 0);

        Parameter m_aparam[] = new Parameter[]{
            mp_period,
            mp_thickness,
            mp_level,
        };    

        public SchwarzD(){
            super.addParams(m_aparam);
        }

        public SchwarzD(double period, double thickness){
            super.addParams(m_aparam);

            mp_period.setValue(period);
            mp_thickness.setValue(thickness);
        }

        public int initialize(){

            super.initialize();

            double period = (Double)mp_period.getValue();
            
            m_factor = 2*PI/period;
            m_level = mp_level.getValue();
            m_thickness = mp_thickness.getValue();

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= m_factor;
            y *= m_factor;
            z *= m_factor;

            double vs = pnt.getScaledVoxelSize();
            double d = abs(sin(x) * sin(y) * sin(z) + sin(x) * cos(y) * cos(z) + cos(x) * sin(x) * cos(z) + cos(x) * cos(y) * sin(z) - m_level)/m_factor - m_thickness;

            data.v[0] = step10(d, 0, (vs));
            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

    /**
     * Scherk Second Surface as defined here: http://en.wikipedia.org/wiki/Scherk_surface
     *
     * @author Alan Hudson
     */
    public static class ScherkSecond extends TransformableDataSource {

        double m_thickness;
        double m_level;
        double m_factor = 0;

        DoubleParameter mp_period = new DoubleParameter("period", "Period of surface", 10*MM);
        DoubleParameter mp_thickness = new DoubleParameter("thickness", "thicknenss of surface", 0.2*MM);
        DoubleParameter mp_level = new DoubleParameter("level", "isosurface level", 0);

        Parameter m_aparam[] = new Parameter[]{
            mp_period,
            mp_thickness,
            mp_level,
        };    

        public ScherkSecond(){
            super.addParams(m_aparam);
        }

        public ScherkSecond(double period, double thickness){
            super.addParams(m_aparam);

            mp_period.setValue(period);
            mp_thickness.setValue(thickness);
        }

        public int initialize(){

            super.initialize();

            double period = (Double)mp_period.getValue();
            
            m_factor = 2*PI/period;
            m_level = mp_level.getValue();
            m_thickness = mp_thickness.getValue();

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= m_factor;
            y *= m_factor;
            z *= m_factor;

            double d = abs(sin(z) - sinh(x)*sinh(y) - m_level)/m_factor - m_thickness;

            data.v[0] = step10(d, 0, pnt.getScaledVoxelSize());

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

    /**
     * Enneper Surface as describe here:  http://en.wikipedia.org/wiki/Enneper_surface
     *
     * @author Alan Hudson
     */
    public static class Enneper extends TransformableDataSource{

        double m_level;
        double m_factor;
        double m_thickness;

        DoubleParameter mp_size = new DoubleParameter("size", "size of surface", 10*MM);
        DoubleParameter mp_thickness = new DoubleParameter("thickness", "thicknenss of surface", 0.2*MM);
        DoubleParameter mp_level = new DoubleParameter("level", "isosurface level", 0);

        Parameter m_aparam[] = new Parameter[]{
            mp_size,
            mp_thickness,
            mp_level,
        };    


        public Enneper(){
            super.addParams(m_aparam);            
        }
        
        public Enneper(double size, double thickness){
            super.addParams(m_aparam);

            mp_size.setValue(size);
            mp_thickness.setValue(thickness);
            
        }

        public int initialize(){

            super.initialize();

            double size = (Double)mp_size.getValue();
            
            m_factor = 1/size;
            m_level = mp_level.getValue();
            m_thickness = mp_thickness.getValue();

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= m_factor;
            y *= m_factor;
            z *= m_factor;
            
            double z2 = z*z;
            double z3 = z2*z;
            double z4 = z2*z2;
            double z5 = z2*z3;
            double z6 = z3*z3;
            double x2 = x*x;
            double x4 = x2*x2;
            double x6 = x4*x2;
            double y2 = y*y;
            double y4 = y2*y2;
            double y6 = y4*y2;

            double d = abs( (64*z4 - 128*z2 + 64)*z5 - 702 * x2*y2*z3 - 18 * x2*y2*z + 144*(y2 - x2)*z6 
                            +162*(y4 - x4)*z2 + 27*(y6 - x6) + 9*(x4 + y4)*z + 48*(x2 + y2)*z3 
                            -432*(x2 + y2)*z5 + 81*(x4*y2- x2*y4) + 240*(y2-x2)*z4 -135 *(x4 + y4)*z3 
                            - m_level)/1000 - m_thickness;
            double vs = pnt.getScaledVoxelSize();

            data.v[0] = step10(d, 0, (vs));

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

}
