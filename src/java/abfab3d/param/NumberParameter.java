/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param;

// External Imports

/**
 * A Number parameter 
 *
 * @author Alan Hudson
 */
public abstract class NumberParameter extends BaseParameter implements Cloneable {
    public NumberParameter(String name, String desc) {
        super(name,desc);
    }

    public NumberParameter(String name) {
        super(name, name);
    }

    public NumberParameter clone() {
        return (NumberParameter) super.clone();
    }
}
